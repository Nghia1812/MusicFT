package com.prj.musicft.presentation.navigation

sealed class Screen(val route: String) {
    object Splash : Screen("splash")
    object Home : Screen("home")
    object Library : Screen("library")
    object Search : Screen("search")
    object Settings : Screen("settings")
    // Player might be a sheet, but could be a screen for full view
    object Player : Screen("player")
    object CollectionList : Screen("collection_list/{type}") {
        fun createRoute(type: String) = "collection_list/$type"
    }
}
