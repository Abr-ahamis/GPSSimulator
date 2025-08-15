package com.gpssimulator.data.database

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.gpssimulator.data.model.LocationPoint
import com.gpssimulator.data.model.MovementType
import java.util.Date

@Entity(tableName = "routes")
data class RouteEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val startLatitude: Double,
    val startLongitude: Double,
    val totalDistance: Double,
    val estimatedDuration: Long,
    val movementType: MovementType,
    val createdAt: Date = Date(),
    val isCompleted: Boolean = false,
    val actualDuration: Long = 0,
    val completedAt: Date? = null
) {
    fun toLocationPoint(): LocationPoint {
        return LocationPoint(
            latitude = startLatitude,
            longitude = startLongitude
        )
    }
}
