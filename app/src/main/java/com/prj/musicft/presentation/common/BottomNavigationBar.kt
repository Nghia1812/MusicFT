package com.prj.musicft.presentation.common

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.currentBackStackEntryAsState
import com.prj.musicft.presentation.navigation.Screen
import androidx.compose.ui.res.stringResource
import com.prj.musicft.R

sealed class BottomNavItem(
    val route: String,
    val titleResId: Int
) {
    @Composable
    abstract fun getIcon(selected: Boolean): ImageVector

    object Home : BottomNavItem(
        route = Screen.Home.route,
        titleResId = R.string.home
    ) {
        @Composable
        override fun getIcon(selected: Boolean): ImageVector =
            if (selected) Icons.Filled.Home else Icons.Outlined.Home
    }

    object Library : BottomNavItem(
        route = Screen.Library.route,
        titleResId = R.string.library
    ) {
        @Composable
        override fun getIcon(selected: Boolean): ImageVector =
            ImageVector.vectorResource(R.drawable.ic_library)
    }

    object Search : BottomNavItem(
        route = Screen.Search.route,
        titleResId = R.string.search
    ) {
        @Composable
        override fun getIcon(selected: Boolean): ImageVector =
            if (selected) Icons.Filled.Search else Icons.Outlined.Search
    }

    object Settings : BottomNavItem(
        route = Screen.Settings.route,
        titleResId = R.string.settings
    ) {
        @Composable
        override fun getIcon(selected: Boolean): ImageVector =
            ImageVector.vectorResource(R.drawable.ic_settings)
    }
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
        containerColor = MaterialTheme.colorScheme.surface,
        contentColor = MaterialTheme.colorScheme.onSurface,
        tonalElevation = 0.dp
    ) {
        val navBackStackEntry by navController.currentBackStackEntryAsState()
        val currentDestination = navBackStackEntry?.destination

        items.forEach { item ->
            val selected = currentDestination?.hierarchy?.any { it.route == item.route } == true

            NavigationBarItem(
                icon = {
                    Icon(
                        imageVector = item.getIcon(selected),
                        contentDescription = stringResource(item.titleResId)
                    )
                },
                label = {
                    Text(text = stringResource(item.titleResId), style = MaterialTheme.typography.labelSmall)
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
                        selectedIconColor = MaterialTheme.colorScheme.primary,
                        selectedTextColor = MaterialTheme.colorScheme.primary,
                        indicatorColor =
                            MaterialTheme.colorScheme.surface, // Make indicator invisible (blend with
                        // background) or slight tint
                        unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                        unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
                    )
            )
        }
    }
}
