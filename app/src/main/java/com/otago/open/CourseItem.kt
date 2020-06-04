package com.otago.open

/**
 * Data Class for the Course items in the [FetchFragment]
 * @param imageResource The image from the project to use.
 * @param courseName The name of the course.
 * @param courseUrl The (probable) url to the course webpage.
 * @param courseCode The paper code for the given course
 */
data class CourseItem(val imageResource: Int, val courseName: String, val courseUrl: String, val courseCode: String)