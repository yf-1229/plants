package com.plants.navigation

sealed class Destinations(val route: String) {
    object Home : Destinations("home")
    object Code: Destinations("code")
    object Settings : Destinations("settings")
    object Loading : Destinations("loading")
}