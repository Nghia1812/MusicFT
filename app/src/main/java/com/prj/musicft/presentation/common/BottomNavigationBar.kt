package com.prj.musicft.presentation.common

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.PlayArrow
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.currentBackStackEntryAsState
import com.prj.musicft.presentation.navigation.Screen
import com.prj.musicft.presentation.theme.CyberpunkTeal
import com.prj.musicft.presentation.theme.SurfaceSlate

sealed class BottomNavItem(
        val route: String,
        val title: String,
        val selectedIcon: ImageVector,
        val unselectedIcon: ImageVector
) {
    object Home :
            BottomNavItem(
                    route = Screen.Home.route,
                    title = "Home",
                    selectedIcon = Icons.Filled.Home,
                    unselectedIcon = Icons.Outlined.Home
            )
    object Library :
        BottomNavItem(
            route = Screen.Library.route,
            title = "Library",
            selectedIcon = Icons.Filled.PlayArrow, // Placeholder, usually a custom icon
            // Using LibraryMusic as closest material equivalent to the 'folder/music' icon
            // in image
            unselectedIcon = Icons.Outlined.PlayArrow //TODO: Later add image vector manually
        )

    object Search :
        BottomNavItem(
            route = Screen.Search.route,
            title = "Search",
            selectedIcon = Icons.Filled.Search,
            unselectedIcon = Icons.Outlined.Search
        )

    object Settings :
            BottomNavItem(
                    route = Screen.Settings.route,
                    title = "Settings",
                    selectedIcon = Icons.Filled.PlayArrow,
                    unselectedIcon = Icons.Outlined.Settings
            )
}

@Composable
fun AppBottomNavigation(navController: NavController, modifier: Modifier = Modifier) {
    val items = listOf(
        BottomNavItem.Home,
        BottomNavItem.Search,
        BottomNavItem.Library,
        BottomNavItem.Settings
    )

    // Customizing NavigationBar to match "Cyberpunk" look
    // Dark background, no elevation (flat), lighter icons
    NavigationBar(
            modifier = modifier,
            containerColor = SurfaceSlate,
            contentColor = Color.White,
            tonalElevation = 0.dp
    ) {
        val navBackStackEntry by navController.currentBackStackEntryAsState()
        val currentDestination = navBackStackEntry?.destination

        items.forEach { item ->
            val selected = currentDestination?.hierarchy?.any { it.route == item.route } == true

            NavigationBarItem(
                    icon = {
                        Icon(
                                imageVector =
                                        if (selected) item.selectedIcon else item.unselectedIcon,
                                contentDescription = item.title
                        )
                    },
                    label = {
                        Text(text = item.title, style = MaterialTheme.typography.labelSmall)
                    },
                    selected = selected,
                    onClick = {
                        navController.navigate(item.route) {
                            // Pop up to the start destination of the graph to
                            // avoid building up a large stack of destinations
                            popUpTo(navController.graph.findStartDestination().id) {
                                saveState = true
                            }
                            // Avoid multiple copies of the same destination when
                            // reselecting the same item
                            launchSingleTop = true
                            // Restore state when reselecting a previously selected item
                            restoreState = true
                        }
                    },
                    colors =
                            NavigationBarItemDefaults.colors(
                                    selectedIconColor = CyberpunkTeal,
                                    selectedTextColor = CyberpunkTeal,
                                    indicatorColor =
                                            SurfaceSlate, // Make indicator invisible (blend with
                                    // background) or slight tint
                                    unselectedIconColor = Color.Gray,
                                    unselectedTextColor = Color.Gray
                            )
            )
        }
    }
}
