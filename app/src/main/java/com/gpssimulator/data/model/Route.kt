package com.gpssimulator.data.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import java.util.Date

@Parcelize
data class Route(
    val id: Long = 0,
    val name: String,
    val points: List<LocationPoint>,
    val totalDistance: Double,
    val estimatedDuration: Long,
    val paceConfig: PaceConfig,
    val createdAt: Date = Date(),
    val isCompleted: Boolean = false,
    val actualDuration: Long = 0
) : Parcelable {
    fun getAllPoints(): List<LocationPoint> {
        return points
    }
}
