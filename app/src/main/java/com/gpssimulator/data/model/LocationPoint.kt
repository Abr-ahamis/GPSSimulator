package com.gpssimulator.data.model

import android.location.Location
import android.location.LocationManager
import android.os.Build
import android.os.SystemClock
import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class LocationPoint(
    var latitude: Double,
    var longitude: Double,
    val timestamp: Long = System.currentTimeMillis(),
    val speed: Float = 0f,
    val bearing: Float = 0f
) : Parcelable {
    fun toLocation(): Location {
        return Location(LocationManager.GPS_PROVIDER).apply {
            latitude = this@LocationPoint.latitude
            longitude = this@LocationPoint.longitude
            time = System.currentTimeMillis()
            speed = this@LocationPoint.speed
            bearing = this@LocationPoint.bearing
            accuracy = 5f // High accuracy for mock locations
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
                elapsedRealtimeNanos = SystemClock.elapsedRealtimeNanos()
            }
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
