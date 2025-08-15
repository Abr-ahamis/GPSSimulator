package com.gpssimulator.data.repository

import com.gpssimulator.data.database.RouteDao
import com.gpssimulator.data.database.RouteEntity
import com.gpssimulator.data.model.LocationPoint
import com.gpssimulator.data.model.MovementType
import kotlinx.coroutines.flow.Flow
import java.util.Date

class RouteRepository(private val routeDao: RouteDao) {
    
    val allRoutes: Flow<List<RouteEntity>> = routeDao.getAllRoutes()
    
    suspend fun insertRoute(
        name: String,
        startPoint: LocationPoint,
        totalDistance: Double,
        estimatedDuration: Long,
        movementType: MovementType
    ): Long {
        val route = RouteEntity(
            name = name,
            startLatitude = startPoint.latitude,
            startLongitude = startPoint.longitude,
            totalDistance = totalDistance,
            estimatedDuration = estimatedDuration,
            movementType = movementType
        )
        return routeDao.insertRoute(route)
    }
    
    suspend fun updateRouteCompletion(routeId: Long, actualDuration: Long) {
        val route = routeDao.getRouteById(routeId)
        route?.let {
            val updatedRoute = it.copy(
                isCompleted = true,
                actualDuration = actualDuration,
                completedAt = Date()
            )
            routeDao.updateRoute(updatedRoute)
        }
    }
    
    suspend fun deleteRoute(route: RouteEntity) {
        routeDao.deleteRoute(route)
    }
    
    suspend fun deleteCompletedRoutes() {
        routeDao.deleteCompletedRoutes()
    }
}
