package com.plants.ui

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.plants.data.Plant
import com.plants.data.PlantsRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

class PlantsViewModel(
    private val savedStateHandle: SavedStateHandle,
    private val plantsRepository: PlantsRepository
) : ViewModel() {
    private val plantIdKey = "plantId"
    fun updateId(id: Int) {
        savedStateHandle[plantIdKey] = id
    }

    val homeUiState: StateFlow<HomeUiState> =
        plantsRepository.getAllPlantsStream().map { HomeUiState(it) }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(TIMEOUT_MILLIS),
                initialValue = HomeUiState()
            )
}

private const val TIMEOUT_MILLIS = 5_000L

/**
 * Ui State for HomeScreen
 */
data class HomeUiState(
    val plantList: List<Plant> = listOf()
)

data class CodeUiState(
    val plant: Plant? = null,
    val title: String = ""
)
