package com.plants.data

import android.content.Context

/**
 * App container for Dependency injection.
 */
interface AppContainer {
    val plantRepository: PlantsRepository
}

/**
 * [AppContainer] implementation that provides instance of [OfflineItemsRepository]
 */
class AppDataContainer(private val context: Context) : AppContainer {
    /**
     * Implementation for [ItemsRepository]
     */
    override val plantRepository: PlantsRepository by lazy {
        OfflinePlantsRepository(PlantsDatabase.getDatabase(context).plantDao())
    }
}