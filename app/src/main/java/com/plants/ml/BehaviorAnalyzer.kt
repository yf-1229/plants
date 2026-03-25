package com.plants.ml

import com.example.generateblock.model.BehaviorMetrics
import com.example.generateblock.model.Block
import com.example.generateblock.model.BlockType

/**
 * Analyzes behavioral properties of each block in a sequence.
 *
 * Activity scores reflect how "energetic" a block type is:
 *   - Motion/Audio blocks (MOVE, TURN, SOUND)  → high   (0.85–1.0)
 *   - Appearance/Speech blocks (SAY)            → medium-high (0.65)
 *   - Control blocks (REPEAT, IF)               → medium (0.45–0.55)
 *   - Variable blocks (SET_VAR, CHANGE_VAR)     → low-medium (0.35–0.4)
 *   - Timing/Terminal blocks (WAIT, STOP)        → low  (0.1–0.2)
 *
 * Redundancy score for position i = (count of same type in positions 0..i-1) / i
 */
object BehaviorAnalyzer {

    private val activityScores: Map<BlockType, Float> = mapOf(
        BlockType.MOVE to 1.0f,
        BlockType.TURN to 0.85f,
        BlockType.SOUND to 0.9f,
        BlockType.SAY to 0.65f,
        BlockType.REPEAT to 0.55f,
        BlockType.IF to 0.45f,
        BlockType.SET_VAR to 0.4f,
        BlockType.CHANGE_VAR to 0.35f,
        BlockType.WAIT to 0.2f,
        BlockType.STOP to 0.1f
    )

    /**
     * Returns a [BehaviorMetrics] list parallel to [sequence].
     */
    fun analyze(sequence: List<Block>): List<BehaviorMetrics> {
        return sequence.mapIndexed { index, block ->
            val activityScore = activityScores[block.type] ?: 0.5f

            val priorSameTypeCount = sequence
                .subList(0, index)
                .count { it.type == block.type }
            val redundancyScore = if (index == 0) 0f else priorSameTypeCount.toFloat() / index

            BehaviorMetrics(
                activityScore = activityScore,
                redundancyScore = redundancyScore
            )
        }
    }
}