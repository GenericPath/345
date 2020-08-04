/*
Copyright (C) 2020 Damian Soo, Garth Wales, Louis Whitburn

This program is free software; you can redistribute it and/or
modify it under the terms of the GNU General Public License
as published by the Free Software Foundation; either version 2
of the License, or (at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with this program; if not, write to the Free Software
Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
*/

package com.otago.open

/**
 * Data class for the PDF items in the [PDFListFragment]
 *
 * @param imageResource The index of imageResource to use from project
 * @param pathType The type of item at the path (folder, PDF, etc)
 * @param paperFolder The base directory for the paper (e.g. /data/android_stuff_here/COSC***)
 * @param paperUrl The base URL for the paper (e.g. https://cs.otago.ac.nz/COSC***)
 * @param pathSubName The rest of the folder name or URL for a resource, e.g. lectures.php/Lecture01.pdf
 * @param prettyPath The pretty name for the path (e.g. lectures.php -> Lectures)
 */
data class PDFItem(val imageResource: Int, val pathType: FileNavigatorType, val paperFolder: String, val paperUrl: String, val pathSubName: String, val prettyPath: String)
