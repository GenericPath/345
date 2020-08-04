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
 * Data Class for the Course items in the [FetchFragment]
 *
 * @param imageResource The image from the project to use.
 * @param courseName The name of the course.
 * @param courseUrl The (probable) url to the course webpage.
 * @param courseCode The paper code for the given course
 */
data class CourseItem(val imageResource: Int, val courseName: String, val courseUrl: String, val courseCode: String)