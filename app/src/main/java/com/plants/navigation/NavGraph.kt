package com.plants.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable

import androidx.navigation.compose.rememberNavController

@Composable
fun PlantNavHost(startDestinations: String = Destinations.Home.route) {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = startDestinations, modifier = Modifier) {
        composable(Destinations.Home.route) {
            HomeScreen(navController = navController)
        }
        composable(Destinations.Code.route) {
            CodeScreen(navController = navController)
        }
        composable(Destinations.Settings.route) {
            SettingsScreen(navController = navController)
        }
        composable(Destinations.Loading.route) {
            LoadingScreen(navController = navController)
        }
    }
}


