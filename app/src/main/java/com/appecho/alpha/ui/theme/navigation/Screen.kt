package com.appecho.alpha.ui.theme.navigation

import androidx.annotation.DrawableRes
import com.appecho.alpha.R

sealed class Screen(
    val route: String,
    val title: String,
    @field:DrawableRes val icon: Int
){
    object Home : Screen("home","首页",R.drawable.deployed_code_24px)
    object Category: Screen("category","分类",R.drawable.view_cozy_24px)
    object Manage: Screen("manage","管理",R.drawable.deployed_code_update_24px)
}

val bottomNavItems = listOf(
    Screen.Home,
    Screen.Category,
    Screen.Manage
)