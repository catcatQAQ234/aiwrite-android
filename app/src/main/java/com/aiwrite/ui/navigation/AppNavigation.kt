package com.aiwrite.ui.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.LibraryBooks
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material.icons.outlined.Image
import androidx.compose.material.icons.outlined.LibraryBooks
import androidx.compose.material.icons.outlined.Public
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.aiwrite.ui.screens.creativehub.CreativeHubScreen
import com.aiwrite.ui.screens.imagegen.ImageGenScreen
import com.aiwrite.ui.screens.novel.NovelListScreen
import com.aiwrite.ui.screens.novel.detail.NovelDetailScreen
import com.aiwrite.ui.screens.novel.detail.PipelineScreen
import com.aiwrite.ui.screens.novel.editor.ChapterEditorScreen
import com.aiwrite.ui.screens.settings.SettingsScreen
import com.aiwrite.ui.screens.world.WorldScreen

sealed class Screen(
    val route: String,
    val title: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector
) {
    data object CreativeHub : Screen("creative_hub", "创作", Icons.Filled.AutoAwesome, Icons.Outlined.AutoAwesome)
    data object Novels : Screen("novels", "小说", Icons.Filled.LibraryBooks, Icons.Outlined.LibraryBooks)
    data object World : Screen("world", "世界", Icons.Filled.Public, Icons.Outlined.Public)
    data object ImageGen : Screen("image_gen", "图生", Icons.Filled.Image, Icons.Outlined.Image)
    data object Settings : Screen("settings", "设置", Icons.Filled.Settings, Icons.Outlined.Settings)
}

object Routes {
    const val NOVEL_DETAIL = "novel_detail/{novelId}"
    const val CHAPTER_EDITOR = "chapter_editor/{chapterId}"
    const val PIPELINE = "pipeline/{novelId}"

    fun novelDetail(novelId: String) = "novel_detail/$novelId"
    fun chapterEditor(chapterId: String) = "chapter_editor/$chapterId"
    fun pipeline(novelId: String) = "pipeline/$novelId"
}

val bottomNavItems = listOf(
    Screen.CreativeHub,
    Screen.Novels,
    Screen.World,
    Screen.ImageGen,
    Screen.Settings
)

private val bottomNavRoutes = bottomNavItems.map { it.route }.toSet()

@Composable
fun AiWriteNavHost() {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route
    val showBottomBar = currentRoute in bottomNavRoutes

    Scaffold(
        bottomBar = {
            if (showBottomBar) {
                NavigationBar {
                    val currentDestination = navBackStackEntry?.destination

                    bottomNavItems.forEach { screen ->
                        NavigationBarItem(
                            icon = {
                                Icon(
                                    imageVector = if (currentDestination?.hierarchy?.any { it.route == screen.route } == true) {
                                        screen.selectedIcon
                                    } else {
                                        screen.unselectedIcon
                                    },
                                    contentDescription = screen.title
                                )
                            },
                            label = { Text(screen.title) },
                            selected = currentDestination?.hierarchy?.any { it.route == screen.route } == true,
                            onClick = {
                                navController.navigate(screen.route) {
                                    popUpTo(navController.graph.findStartDestination().id) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            }
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Screen.CreativeHub.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(Screen.CreativeHub.route) { CreativeHubScreen() }
            composable(Screen.Novels.route) {
                NovelListScreen(
                    onNovelClick = { novelId ->
                        navController.navigate(Routes.novelDetail(novelId))
                    }
                )
            }
            composable(
                route = Routes.NOVEL_DETAIL,
                arguments = listOf(navArgument("novelId") { type = NavType.StringType })
            ) {
                NovelDetailScreen(
                    onBack = { navController.popBackStack() },
                    onChapterClick = { chapterId ->
                        navController.navigate(Routes.chapterEditor(chapterId))
                    },
                    onPipelineClick = { novelId ->
                        navController.navigate(Routes.pipeline(novelId))
                    }
                )
            }
            composable(
                route = Routes.CHAPTER_EDITOR,
                arguments = listOf(navArgument("chapterId") { type = NavType.StringType })
            ) {
                ChapterEditorScreen(
                    onBack = { navController.popBackStack() }
                )
            }
            composable(
                route = Routes.PIPELINE,
                arguments = listOf(navArgument("novelId") { type = NavType.StringType })
            ) {
                PipelineScreen(
                    onBack = { navController.popBackStack() }
                )
            }
            composable(Screen.World.route) { WorldScreen() }
            composable(Screen.ImageGen.route) { ImageGenScreen() }
            composable(Screen.Settings.route) { SettingsScreen() }
        }
    }
}
