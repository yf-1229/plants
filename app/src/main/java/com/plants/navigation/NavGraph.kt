package com.plants.navigation

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.plants.ui.CodeScreen
import com.plants.ui.HomeScreen

@Composable
fun PlantNavHost() {
    val navController = rememberNavController()
    val startDestinations: String = Destinations.Home.route

    NavHost(
        navController = navController,
        startDestination = startDestinations,
        modifier = Modifier
    ) {
        composable(Destinations.Home.route) {
            HomeScreen(
                onStartClick = { navController.navigate(Destinations.Code.route) }
            )
        }
        composable(Destinations.Code.route) {
            CodeScreen()
        }
        composable(Destinations.Settings.route) {
            Text(text = "Settings Screen")
        }
        composable(Destinations.Loading.route) {
            Text(text = "Loading Screen")
        }
    }
}
