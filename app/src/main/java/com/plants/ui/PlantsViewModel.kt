package com.plants.ui

import android.content.Context
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.plants.data.Block
import com.plants.data.BlockType
import com.plants.data.Plant
import com.plants.data.PlantsRepository
import com.plants.ml.CodeAnalysisParams
import com.plants.ml.LiteRTCodeAnalyzer
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
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

    // -------------------------------------------------------------------------
    // Code-editor block state
    // -------------------------------------------------------------------------

    /** The sequence of blocks currently placed in the code editor. */
    val placedBlocks: SnapshotStateList<Block> = mutableStateListOf(
        Block(type = BlockType.MOVE)
    )

    /** Appends a new block of [blockType] to the placed sequence. */
    fun addBlock(blockType: BlockType) {
        placedBlocks.add(Block(type = blockType))
    }

    /** Removes the last placed block (minimum one block is always kept). */
    fun removeLastBlock() {
        if (placedBlocks.size > 1) placedBlocks.removeLastOrNull()
    }

    // -------------------------------------------------------------------------
    // LiteRT analysis
    // -------------------------------------------------------------------------

    private val _analysisParams = MutableStateFlow<CodeAnalysisParams?>(null)

    /** Latest analysis result; null until [analyzeBlocks] has been called. */
    val analysisParams: StateFlow<CodeAnalysisParams?> = _analysisParams.asStateFlow()

    /**
     * Runs 行動性/冗長性 analysis on the current [placedBlocks] sequence using
     * [LiteRTCodeAnalyzer] (with automatic rule-based fallback) and updates
     * [analysisParams].
     *
     * Runs on [Dispatchers.Default] so as not to block the UI thread.
     */
    fun analyzeBlocks(context: Context) {
        val snapshot = placedBlocks.toList()
        viewModelScope.launch(Dispatchers.Default) {
            val analyzer = LiteRTCodeAnalyzer(context.applicationContext)
            try {
                _analysisParams.value = analyzer.analyze(snapshot)
            } catch (e: Exception) {
                Log.e("PlantsViewModel", "analyzeBlocks failed", e)
            } finally {
                analyzer.close()
            }
        }
    }

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
    val parameter: Int = 0,
)

fun CodeDetails.toPlant(): Plant {
    return Plant(
        id = id,
        name = title,
        description = description,
        parameter = parameter,
    )
}

fun Plant.toCodeUiState(): CodeDetails {
    return CodeDetails(
        id = id,
        title = name,
        description = description,
        parameter = parameter,
    )
}

fun Plant.toCodeDetails(): CodeDetails {
    return CodeDetails(
        id = id,
        title = name,
        description = description,
        parameter = parameter,
    )
}