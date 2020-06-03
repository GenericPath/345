package com.otago.open

/** Data Class for the PDF items in the [PDFListFragment]
 * @param imageResource the index of imageResource to use from project.
 * @param pathName the path of where the item is located.
 * @param pathType the type of item at the path.
 */
data class PDFItem(val imageResource: Int, val pathName: String, val pathType: String)