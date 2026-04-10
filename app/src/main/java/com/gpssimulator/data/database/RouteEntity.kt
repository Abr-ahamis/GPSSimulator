package com.gpssimulator.data.database

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.gpssimulator.data.model.LocationPoint
import com.gpssimulator.data.model.Route
import com.gpssimulator.data.model.PaceConfig
import java.util.Date

@Entity(tableName = "routes")
data class RouteEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val points: List<LocationPoint>,
    val totalDistance: Double,
    val estimatedDuration: Long,
    val basePaceSeconds: Int,
    val createdAt: Date = Date(),
    val isCompleted: Boolean = false,
    val actualDuration: Long = 0,
    val completedAt: Date? = null
) {
    fun toRoute(): Route {
        return Route(
            id = id,
            name = name,
            points = points,
            totalDistance = totalDistance,
            estimatedDuration = estimatedDuration,
            paceConfig = PaceConfig(basePaceSeconds),
            createdAt = createdAt,
            isCompleted = isCompleted,
            actualDuration = actualDuration
        )
    }

    companion object {
        fun fromRoute(route: Route): RouteEntity {
            return RouteEntity(
                id = route.id,
                name = route.name,
                points = route.points,
                totalDistance = route.totalDistance,
                estimatedDuration = route.estimatedDuration,
                basePaceSeconds = route.paceConfig.baseSeconds,
                createdAt = route.createdAt,
                isCompleted = route.isCompleted,
                actualDuration = route.actualDuration
            )
        }
    }
}
