package com.prj.musicft.presentation.navigation

sealed class Screen(val route: String) {
    object Splash : Screen("splash")
    object Home : Screen("home")
    object Library : Screen("library")
    object Search : Screen("search")
    object Settings : Screen("settings")
    // Player might be a sheet, but could be a screen for full view
    object Player : Screen("player")
    object Playlists : Screen("playlists")
    object CollectionList : Screen("collection_list/{type}?playlistId={playlistId}") {
        fun createRoute(type: String, playlistId: Long? = null): String {
            return if (playlistId != null) {
                "collection_list/$type?playlistId=$playlistId"
            } else {
                "collection_list/$type"
            }
        }
    }
    object PermissionRequest : Screen("permission_request")
}
