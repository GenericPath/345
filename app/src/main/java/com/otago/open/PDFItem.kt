/*
Copyright (C) 2020 Burnie Lorimer, Damian Soo, Garth Wales, Louis Whitburn

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
 * @param imageResource the index of imageResource to use from project.
 * @param pathName the path of where the item is located.
 * @param pathType the type of item at the path.
 */
data class PDFItem(val imageResource: Int, val pathName: String, val pathType: FileNavigatorType)
