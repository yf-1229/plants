package com.plants.ml

import com.plants.data.BlockType

/**
 * Aggregated parameters produced by analysing a block sequence.
 *
 * @property activityScore      行動性スコア — average activity of the sequence (0.0–1.0).
 * @property redundancyScore    冗長性スコア — average redundancy of the sequence (0.0–1.0).
 * @property diversityRatio     多様性 — ratio of unique block types to total blocks (0.0–1.0).
 * @property dominantBlockType  Most frequently used block type, or null for empty sequences.
 * @property blocksAnalyzed     Total number of blocks in the analysed sequence.
 * @property generatedParameter Derived integer parameter (0–100) combining activity,
 *                              diversity, and inverse redundancy.
 * @property perBlockMetrics    Per-block scores; empty when produced by the LiteRT path.
 * @property analyzedByLiteRT   True when inference was performed by the LiteRT runtime.
 */
data class CodeAnalysisParams(
    val activityScore: Float,
    val redundancyScore: Float,
    val diversityRatio: Float,
    val dominantBlockType: BlockType?,
    val blocksAnalyzed: Int,
    val generatedParameter: Int,
    val perBlockMetrics: List<BehaviorMetrics> = emptyList(),
    val analyzedByLiteRT: Boolean = false,
)
