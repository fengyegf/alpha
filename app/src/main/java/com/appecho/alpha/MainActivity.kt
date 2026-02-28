package com.appecho.alpha

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.runtime.getValue
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.appecho.alpha.ui.theme.AlphaTheme
import com.appecho.alpha.ui.theme.search.SearchScreen
import com.appecho.alpha.ui.theme.category.CategoryScreen
import com.appecho.alpha.ui.theme.home.HomeScreen
import com.appecho.alpha.ui.theme.home.UpdateChecker
import com.appecho.alpha.ui.theme.manage.ManageScreenRoute
import com.appecho.alpha.ui.theme.navigation.bottomNavItems

class MainActivity : ComponentActivity() {
    @SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        //解决小米手机全面屏显示白条问题
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
        window.addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION)
//        window.navigationBarColor = android.graphics.Color.TRANSPARENT
        setContent {
            AlphaTheme {
                UpdateChecker(updateUrl = "https://raw.githubusercontent.com/fengyegf/fengyegf.github.io/refs/heads/main/src/pages/update.json")
                val navController = rememberNavController()
                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentRoute = navBackStackEntry?.destination?.route
                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    bottomBar = {
                        NavigationBar {
                            bottomNavItems.forEach { screen ->
                                NavigationBarItem(
                                    selected = currentRoute == screen.route,
                                    onClick = {
                                        navController.navigate(screen.route){
                                            popUpTo(navController.graph.startDestinationId){
                                                saveState = true
                                            }
                                            launchSingleTop =true
                                            restoreState =true
                                        }
                                    },
                                    label = { Text(screen.title) },
                                    icon = {
                                        Icon(
                                            painter = painterResource(id = screen.icon),
                                            contentDescription = screen.title
                                        )
                                    }
                                )
                            }
                        }
                    }
                ) { innerPadding -> NavHost(
                    navController = navController,
                    startDestination = "home", // 设置默认启动页的路由
                    modifier = Modifier.padding(innerPadding) // 确保内容不会被导航栏遮挡
                ) {
                    composable("home"){
                        HomeScreen(
                            onSearchClick = { navController.navigate("search") },
                            onNavigateToManage = { navController.navigate("manage") {
                                popUpTo(navController.graph.startDestinationId) { saveState = true }
                                launchSingleTop = true
                                restoreState = true
                            } }
                        )
                    }
                    composable("category"){ CategoryScreen() }
                    composable("manage"){ ManageScreenRoute() }
                    composable("search"){
                        SearchScreen(onBack = {navController.popBackStack()})
                    }
                }}
            }
        }
    }
}
