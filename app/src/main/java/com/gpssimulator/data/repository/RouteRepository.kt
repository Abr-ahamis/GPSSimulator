package com.gpssimulator.data.repository

import com.gpssimulator.data.database.RouteDao
import com.gpssimulator.data.database.RouteEntity
import com.gpssimulator.data.model.Route
import kotlinx.coroutines.flow.Flow
import java.util.Date

class RouteRepository(private val routeDao: RouteDao) {
    
    val allRoutes: Flow<List<RouteEntity>> = routeDao.getAllRoutes()
    
    suspend fun insertRoute(route: Route): Long {
        val entity = RouteEntity.fromRoute(route)
        return routeDao.insertRoute(entity)
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
