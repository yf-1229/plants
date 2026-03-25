package com.plants.ml

/**
 * Per-block analysis scores produced by [BehaviorAnalyzer] or [LiteRTCodeAnalyzer].
 *
 * @property activityScore  行動性スコア (0.0–1.0): how energetic this block type is.
 * @property redundancyScore 冗長性スコア (0.0–1.0): how often the same type appeared before.
 */
data class BehaviorMetrics(
    val activityScore: Float,
    val redundancyScore: Float,
)
