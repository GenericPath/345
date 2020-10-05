package com.otago.open

/**
 * Potential reasons a folder may be empty to be presented in a toast
 *
 * @param message The human readable message for the toast (if present)
 */
enum class RefreshNotifyType(val message: String) {
    NONE(""), //No message
    CACHE_NAV_NONE("No content downloaded yet - try refreshing"),
    NAV_NONE("No content yet"),
    REFRESH_NONE("No content yet"),
    NAV_PAPERS("No papers selected - select some from the home menu")
}