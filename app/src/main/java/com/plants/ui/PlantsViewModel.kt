package com.plants.ui

import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.plants.data.Plant
import com.plants.data.PlantsRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

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

    var codeUiState by mutableStateOf(CodeUiState())
        private set


    suspend fun savePlant() {
        if (validateInput()) {
            plantsRepository.insertPlant(codeUiState.codeDetails.toPlant())
        }
    }

    private fun validateInput(uiState: CodeDetails = codeUiState.codeDetails): Boolean {
        return with(uiState) {
            title.isNotBlank()
        }
    }

    suspend fun updateCode() {
        if (validateInput(codeUiState.codeDetails)) {
            plantsRepository.updatePlant(codeUiState.codeDetails.toPlant())
        }
    }

    fun updateUiState(codeDetails: CodeDetails) {
        codeUiState = CodeUiState(
            codeDetails = codeDetails,
            isEntryValid = validateInput(codeDetails)
        )
    }

    fun deletePlant(id: Int) {
        viewModelScope.launch {
            try {
                // 現在のノードを取得
                val currentPlant = plantsRepository.getPlantStream(id).first()

                if (currentPlant != null) {
                    val updatedPlant = currentPlant.copy(
                        // isDeleted = true
                    )
                    // 更新を保存
                    plantsRepository.updatePlant(updatedPlant)

                } else {
                    Log.e("HomeViewModel", "Failed to retrieve currentPlant: Plant is null")
                }


            } catch (e: Exception) {
                Log.e("HomeViewModel", "Failed to update reactions", e)
            }
        }
    }

}


private const val TIMEOUT_MILLIS = 5_000L

/**
 * Ui State for HomeScreen
 */
data class HomeUiState(
    val plantList: List<Plant> = listOf()
)

data class CodeUiState(
    val codeDetails: CodeDetails = CodeDetails(),
    val isEntryValid: Boolean = false,
)

data class CodeDetails(
    val id: Int = 0,
    val title: String = "",
    val description: String = "",
)

fun CodeDetails.toPlant(): Plant {
    return Plant(
        id = id,
        name = title,
        description = description,
    )
}

fun Plant.toCodeUiState(): CodeDetails {
    return CodeDetails(
        id = id,
        title = name,
        description = description,
    )
}

fun Plant.toCodeDetails(): CodeDetails {
    return CodeDetails(
        id = id,
        title = name,
        description = description,
    )
}