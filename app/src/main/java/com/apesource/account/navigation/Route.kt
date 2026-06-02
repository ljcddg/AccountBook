package com.apesource.account.navigation

sealed class Route(val route: String) {
    object Home : Route("home")
    object Statistics : Route("statistics")
    object Assets : Route("assets")
    object Budget : Route("budget")
    object Settings : Route("settings")
    object Profile : Route("profile")
    object ImportExport : Route("import_export")
    object Calendar : Route("calendar")
}
