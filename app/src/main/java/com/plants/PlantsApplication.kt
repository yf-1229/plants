package com.plants

import android.app.Application
import com.plants.data.AppContainer
import com.plants.data.AppDataContainer

class PlantsApplication : Application() {
    lateinit var container: AppContainer
        private set

    override fun onCreate() {
        super.onCreate()
        container = AppDataContainer(this)
    }
}