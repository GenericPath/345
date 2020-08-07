package com.otago.open

import android.util.Log
import org.jsoup.HttpStatusException
import org.jsoup.Jsoup
import org.jsoup.nodes.Element
import org.jsoup.UnsupportedMimeTypeException
import org.jsoup.nodes.Document
import java.io.*
import java.net.MalformedURLException
import java.net.SocketTimeoutException
import java.net.URL
import java.nio.file.Files
import java.nio.file.StandardCopyOption

/**
 * Common operations for PDF files / course folders
 */
object PDFOperations {
    /**
     * Gets the resource icon from a [FileNavigatorType]
     *
     * @param navType The [FileNavigatorType] we want an icon for
     *
     * @return The icon's ID
     */
    fun getResourceItem(navType: FileNavigatorType): Int {
        return when (navType) {
            FileNavigatorType.FOLDER -> R.drawable.ic_folder
            FileNavigatorType.PDF -> R.drawable.ic_pdf
            FileNavigatorType.MARKS -> R.drawable.ic_thumb //TODO: New icon
        }
    }

    /**
     * Creates [PDFItem]s from [FetchResult]s by deciding on the pretty name and the icon to use.
     *
     * @param fetched The fetched / chosen PDFs / folders
     *
     * @return The processed list of [FetchResult]s as a list of [PDFItem]s
     */
    fun generatePdfItems(fetched: List<FetchResult>) : List<PDFItem> {
        val pdfs = ArrayList<PDFItem>(fetched.size)
        fetched.forEach {
            pdfs.add(PDFItem(getResourceItem(it.type), it.type, it.itemFile, it.itemUrl, it.coscName))
        }
        return pdfs
    }

    /**
     * Determines where a [href] on a page given by [onPageUrl] should point, if [onPageUrl] is
     * a file (i.e. no forward slash), otherwise when in the folder given by [onPageUrl] it determines
     * where the sub-resource would be located
     *
     * If [href] starts with a "/" we just return the [href] relative to the COSC website
     *
     * If [href] is a https link we just return it, if it starts with http we make it a https link
     *
     * This method does not have any special handling for any .. or repeated ././
     *
     * @param onPageUrl The page that the link is on, or the folder that the [href] is relative to
     * @param href The href
     *
     * @return Where [href] should point if it appears on a page [onPageUrl], or if it is a sub-resource of a folder [onPageUrl]
     */
    fun determinePath(onPageUrl: String, href: String): String {
        //If it's a full URL just return it
        if (href.startsWith("http")) {
            return if (href.startsWith("https")) {
                href
            } else {
                href.replace("http", "https")
            }
        }

        //If it starts with ./ then this is the same as starting without that
        val trimHref = if (href.startsWith("./")) {
            href.replaceFirst("./", "")
        } else {
            href
        }

        return when {
            //If we are relative to a folder, just concatenate the folder and sub-directory
            onPageUrl.endsWith("/") -> {
                "$onPageUrl$trimHref" //onPageUrl already has a trailing /
            }
            //If the href is absolute then just concatenate the COSC website and the href
            trimHref.startsWith('/') -> {
                return "https://cs.otago.ac.nz$trimHref"
            }
            //Otherwise just trim the filename from the onPageUrl (if there is no parent folder, just return "")
            //And add the href to it
            else -> {
                onPageUrl.substringBeforeLast("/", "") + "/$trimHref"
            }
        }
    }

    /**
     * Creates a meta file for the file indicated via [saveFile] based on the [FetchResult] [it]
     * This creates the file [saveFile].meta
     *
     * @param saveFile The file to create a meta file for
     * @param it The [FetchResult] to (partially) save in the meta file
     */
    fun createMetaFile(saveFile: String, it: FetchResult) {
        //Open a file to write
        val metaStream = BufferedWriter(PrintWriter(FileOutputStream("$saveFile.meta")))

        //Write the COSC name and URL
        metaStream.write(it.coscName)
        metaStream.newLine()
        metaStream.write(it.itemUrl)
        metaStream.newLine() //THE MOST IMPORTANT LINE IN THIS WHOLE APP!!!!!!!!!!!!!!!!
        //Each time you create a file that doesn't end with a newline Ken Thompson and Dennis Ritchie (RIP) shed a single tear

        //Close the file stream
        metaStream.close()
    }

    /**
     * Creates a meta file for the file indicated via [saveFolder] and [fileName]
     * based on the [FetchResult] [it]
     *
     * This creates the file [saveFolder]/[fileName].meta
     */
    fun createMetaFile(saveFolder: String, fileName: String, it: FetchResult) {
        createMetaFile("$saveFolder/$fileName", it)
    }

    /**
     * Loads a meta file for the resource [assFile] (associated file), i.e. the loaded meta file
     * will be [assFile].meta
     *
     * @param assFile The resource which we want to load a meta file for
     *
     * @return A [FetchResult] for [assFile]
     */
    fun loadMetaFile(assFile: String): FetchResult? {
        //Get the file name
        val metaName = "$assFile.meta"

        Log.d("Loading Meta File", metaName)

        //Load
        val reader = BufferedReader(FileReader(metaName))
        val coscName = reader.readLine()
        val url = reader.readLine()

        //Determine nav type
        val navType = when {
            assFile.endsWith(".pdf", true) -> {
                FileNavigatorType.PDF
            }
            assFile.endsWith("marks.php", true) -> {
                FileNavigatorType.MARKS
            }
            File(assFile).isDirectory -> {
                FileNavigatorType.FOLDER
            }
            else -> {
                null
            }
        }

        //Return our "pretend" fetch result
        return if (navType == null) {
            null
        }
        else {
            FetchResult(assFile, url, coscName, navType)
        }
    }

    /**
     * Downloads a file at [url] to the file [parentDir]/[fileName] using a HTTP GET
     *
     * During the download the file is saved at [parentDir]/[fileName].download before
     * being moved to [parentDir]/[fileName]
     *
     * @param url The file to download
     * @param parentDir Where to save the file
     * @param fileName The name for the downloaded file
     *
     * @return The name of the saved file ([parentDir]/[fileName]) if all succeeded, otherwise null
     */
    fun downloadFile(url: String, parentDir: String, fileName: String): String? {
        //Generate the URL for where the PDF is
        val resUrl = URL(url)

        //Make the folder to save the lecture PDF into
        File(parentDir).mkdirs()

        //Create a buffer for the HTTP requests
        val buf = ByteArray(1024)

        //Create the file to save the lecture PDF into
        val outFile  = File(parentDir, "$fileName.download")
        Log.d("PDF Saving", outFile.absolutePath)
        try {
            outFile.createNewFile()
        } catch (e: IOException) {
            //TODO: Something here
            e.printStackTrace()
            return null
        }

        try {
            val outStream = BufferedOutputStream(
                FileOutputStream(outFile)
            )

            Log.d("PDF Downloading", resUrl.toExternalForm())

            //Open a HTTP connection
            val conn = resUrl.openConnection()
            val inStream = conn.getInputStream()

            //Read the result and write it to file
            var byteRead = inStream.read(buf)
            while (byteRead != -1) {
                outStream.write(buf, 0, byteRead)
                byteRead = inStream.read(buf)
            }

            //Close the file streams
            inStream.close()
            outStream.close()

            //If we're happy with the final file then move it into its proper location
            Files.move(outFile.toPath(), File(parentDir, fileName).toPath(), StandardCopyOption.ATOMIC_MOVE, StandardCopyOption.REPLACE_EXISTING)

            Log.d("Successfully Saved", "$parentDir/$fileName")
            return "$parentDir/$fileName"
        } catch (e: FileNotFoundException) {
            //TODO: Something here
            e.printStackTrace()
            return null
        } catch (e: IOException) {
            //TODO: Something here
            e.printStackTrace()
            return null
        }
    }

    /**
     * Helper function for [fetchPdf].
     * Processes a link in a given COSC website "coursepagefullwidth" div
     *
     * @param parentFolder The folder where the item pointed to by the link would be saved
     * @param url The url where the link is found
     * @param it The html link element (a)
     *
     * @return A [FetchResult] corresponding to this link, if this link is relevant, otherwise null
     */
    private fun fetchProcessHref(parentFolder: String, url: String, it: Element): FetchResult? {
        val href = it.attr("href")
        val hrefName = href.substringAfterLast('/')
        Log.d("Found href (PDF)", href)
        if (href.endsWith(".pdf")) {
            Log.d("Fetched Link", href)
            val newUrl = determinePath(url, href)

            Log.d("Found PDF (URL)", newUrl)
            return FetchResult(
                "$parentFolder/$hrefName",
                newUrl,
                it.text(),
                FileNavigatorType.PDF
            )
        }

        return null
    }

    /**
     * Helper function for [fetchFolder].
     * Processes a link in a given COSC website "coursepagenavmenu" div
     *
     * @param parentFolder The folder where the item pointed to by the link would be saved
     * @param url The url where the link is found
     * @param it The html link element (a)
     *
     * @return A [FetchResult] corresponding to this link, if this link is relevant, otherwise null
     */
    private fun fetchProcessFolder(parentFolder: String, url: String, it: Element): FetchResult? {
        val href = it.attr("href")
        val hrefName = href.substringAfterLast('/')
        Log.d("Found href (folder)", "$url;$href")
        val newUrl = determinePath(url, href)

        //Don't fetch the home page (we're already there)
        //Make sure we are still on the COSC website (check for php at the end)
        if (newUrl.endsWith(".php") && !newUrl.endsWith("index.php")) {
            val name = it.text()

            //If it's a marks page adjust the navigator type accordingly
            val navType = if (newUrl.endsWith("marks.php")) {
                FileNavigatorType.MARKS
            } else {
                FileNavigatorType.FOLDER
            }

            Log.d("Detected Name", name)
            Log.d("Found Folder (URL)", newUrl)
            return FetchResult("$parentFolder/$hrefName", newUrl, name, navType)
        }

        return null
    }

    /**
     * Helper function for [fetchLinks]
     * Finds PDFs in a COSC web page
     *
     * @param document The Jsoup document containing the parsed DOM
     * @param parentFolder The folder where the PDF pointed to by the link would be saved
     * @param url The url where the PDF is found
     *
     * @return An [ArrayList] of [FetchResult]s referring to PDF on the page at [url]
     */
    private fun fetchPdf(document: Document, parentFolder: String, url: String): ArrayList<FetchResult> {
        val links = ArrayList<FetchResult>()

        document.select("div#coursepagefullwidth a").forEach {
            val item = fetchProcessHref(parentFolder, url, it)

            if (item != null) {
                links.add(item)
            }
        }

        return links
    }

    /**
     * Helper function for [fetchLinks]
     * Finds subfolders (lectures, tutorials, etc)  in a COSC web page
     *
     * @param document The Jsoup document containing the parsed DOM
     * @param parentFolder The folder in which the folder pointed to by the link would be created
     * @param url The url where the link is found
     *
     * @return An [ArrayList] of [FetchResult]s referring to folder (link) on the page at [url]
     */
    private fun fetchFolder(document: Document, parentFolder: String, url: String): ArrayList<FetchResult> {
        val links = ArrayList<FetchResult>()

        document.select("div#coursepagenavmenu a").forEach {
            val item = fetchProcessFolder(parentFolder, url, it)

            if (item != null) {
                links.add(item)
            }
        }

        return links
    }

    /**
     * Retrieve links from a table containing href elements.
     *
     * @see PDFItem for more details on what the [parentFolder], and [url] are part of
     *
     * @param parentFolder The folder in which the downloaded content will be stored
     * @param url The URL at which we will look for folders and PDFs etc
     * @param doFolders Whether we want to include folders. Currently this should only be true if paperSubName is "" or "index.php" due to the COSC website navigation structure
     *
     * @return The list of [FetchResult]s for each PDF (or folder if [doFolders] is true) on the page
     */
    fun fetchLinks(parentFolder: String, url: String, doFolders: Boolean): ArrayList<FetchResult> {
        val links = ArrayList<FetchResult>()
        Log.d("Jsoup URL", url)

        try {
            val document = Jsoup.connect(url).get()

            //PDF links
            links.addAll(fetchPdf(document, parentFolder, url))

            if (doFolders) {
                //Nav links
                links.addAll(fetchFolder(document, parentFolder, url))
            }

        } catch (e: MalformedURLException) {
            //TODO: Do something here
            e.printStackTrace()
        } catch (e: HttpStatusException) {
            //TODO: Do something here
            e.printStackTrace()
        } catch (e: UnsupportedMimeTypeException) {
            //TODO: Do something here
            e.printStackTrace()
        } catch (e: SocketTimeoutException) {
            //TODO: Do something here
            e.printStackTrace()
        } catch (e: IOException) {
            //TODO: Do something here
            e.printStackTrace()
        }

        return links
    }

    /**
     * Downloads a PDF using the [downloadFile] logic.
     *
     * @param pdfFile The [FetchResult] corresponding to the PDF to download
     */
    fun downloadPDF(pdfFile: FetchResult) {
        //Common stuff good to know
        val parentDir = pdfFile.itemFile.substringBeforeLast('/', "")
        val fileName = pdfFile.itemFile.substringAfterLast('/', "")

        //If it's a folder then we just need to create it
        if (pdfFile.type == FileNavigatorType.FOLDER || pdfFile.type == FileNavigatorType.MARKS) {
            Log.d("Creating Folder ", pdfFile.itemFile)
            try {
                File(pdfFile.itemFile).mkdirs()

                createMetaFile(parentDir, fileName, pdfFile)

            } catch (e: FileNotFoundException) {
                //TODO: Something here
                e.printStackTrace()
                return
            } catch (e: IOException) {
                //TODO: Something here
                e.printStackTrace()
                return
            }
            return
        }

        //Sanitise input - in case of a blank href
        if (pdfFile.itemUrl.isBlank()) {
            return
        }

        val outFile = downloadFile(pdfFile.itemUrl, parentDir, fileName)

        if (outFile != null) {
            Log.d("Creating Download Meta-File", outFile)
            createMetaFile(outFile, pdfFile)
        }
    }
}