package com.otago.open

/**
 * Data class for fetched folders / PDFs from the COSC website
 *
 * @param itemFile When this particular file (indicated vis [itemUrl] should be saved
 * @param itemUrl The URL of the item (folder / PDF) to ferch
 * @param coscName The name of the item on the COSC website
 * @param type The type of the item (folder or PDF etc)
 */
data class FetchResult(val itemFile: String, val itemUrl: String, val coscName: String, val type: FileNavigatorType)