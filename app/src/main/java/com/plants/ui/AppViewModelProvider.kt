package com.plants.ui

import android.app.Application
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.createSavedStateHandle
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.plants.PlantsApplication
import com.plants.data.AppDataContainer

object AppViewModelProvider {
    val Factory = viewModelFactory {
        initializer {
            val app = checkNotNull(this[APPLICATION_KEY]) as Application
            val container = (app as? PlantsApplication)?.container ?: AppDataContainer(app)

            PlantsViewModel(
                savedStateHandle = createSavedStateHandle(),
                plantsRepository = container.plantsRepository
            )
        }
    }
}