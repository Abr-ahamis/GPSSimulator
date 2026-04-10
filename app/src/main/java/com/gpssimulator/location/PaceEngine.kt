package com.gpssimulator.location

import kotlin.random.Random

class PaceEngine(private val baseSeconds: Int) {
    private var currentPaceSeconds = baseSeconds.toDouble()

    fun nextPaceSeconds(segmentDistanceMeters: Double): Double {
        val drift = Random.nextInt(-30, 31).toDouble()
        val adaptation = (segmentDistanceMeters / 10.0).coerceIn(0.15, 0.45)
        currentPaceSeconds += (baseSeconds + drift - currentPaceSeconds) * adaptation
        return currentPaceSeconds.coerceIn(baseSeconds - 30.0, baseSeconds + 30.0)
    }

    fun paceToSpeed(paceSeconds: Double): Float {
        // speed = meters / seconds (1000m per km)
        return (1000.0 / paceSeconds).toFloat()
    }
}
