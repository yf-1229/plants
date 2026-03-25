package com.plants.data

import kotlinx.coroutines.flow.Flow

class OfflinePlantsRepository(private val plantDao: PlantDao) : PlantsRepository {
    override fun getAllPlantsStream(): Flow<List<Plant>> = plantDao.getAll()

    override suspend fun getPlantStream(id: Int): Flow<Plant?> = plantDao.get(id)

    override suspend fun insertPlant(item: Plant) = plantDao.insert(item)

    override suspend fun updatePlant(item: Plant) = plantDao.update(item)

    override suspend fun deletePlant(item: Plant) = plantDao.delete(item)
}
