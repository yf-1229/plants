package com.plants.ui

import androidx.lifecycle.createSavedStateHandle
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.plants.MainActivity

object AppViewModelProvider {
    val Factory = viewModelFactory {
            initializer {
                PlantsViewModel(
                    savedStateHandle = this.createSavedStateHandle(),
                    plantsRepository = MainActivity().container.plantsRepository
                )
            }
    }
}