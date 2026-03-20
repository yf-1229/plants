package com.plants.ui

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import com.plants.data.PlantsRepository

class PlantsViewModel(
    private val savedStateHandle: SavedStateHandle,
    private val plantsRepository: PlantsRepository
) : ViewModel() {
    private val plantIdKey = "plantId"
    fun updateId(id: Int) {
        savedStateHandle[plantIdKey] = id
    }
}

