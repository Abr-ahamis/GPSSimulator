package com.gpssimulator.data.model

import android.location.Location

data class LocationPoint(
    val latitude: Double,
    val longitude: Double,
    val timestamp: Long = System.currentTimeMillis(),
    val speed: Float = 0f,
    val bearing: Float = 0f
) {
    fun toLocation(): Location {
        return Location("mock").apply {
            latitude = this@LocationPoint.latitude
            longitude = this@LocationPoint.longitude
            time = this@LocationPoint.timestamp
            speed = this@LocationPoint.speed
            bearing = this@LocationPoint.bearing
            accuracy = 5f // High accuracy for mock locations
        }
    }
    
    companion object {
        fun fromLocation(location: Location): LocationPoint {
            return LocationPoint(
                latitude = location.latitude,
                longitude = location.longitude,
                timestamp = location.time,
                speed = location.speed,
                bearing = location.bearing
            )
        }
    }
}
