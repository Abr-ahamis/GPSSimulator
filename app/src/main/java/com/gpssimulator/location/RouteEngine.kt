package com.gpssimulator.location

import com.gpssimulator.data.model.LocationPoint
import com.gpssimulator.data.model.Route
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.max
import kotlin.math.pow
import kotlin.math.roundToLong
import kotlin.math.sin
import kotlin.math.sqrt

class RouteEngine(
    private val route: Route,
    private val isCircular: Boolean = false
) {
    data class SimulationStep(
        val point: LocationPoint,
        val delayBeforeMs: Long
    )

    private var index = 0
    private val points = route.getAllPoints()
    private val paceEngine = PaceEngine(route.paceConfig.baseSeconds)
    private var simulatedElapsedMs = 0L
    private var distanceTravelledMeters = 0.0

    fun hasNext(): Boolean {
        return isCircular || index < points.size
    }

    fun getNextStep(): SimulationStep {
        if (points.isEmpty()) throw IllegalStateException("Route is empty")

        if (index >= points.size) {
            if (isCircular) {
                index = 0
            } else {
                return SimulationStep(points.last(), 0L)
            }
        }

        val rawPoint = points[index]
        val previousPoint = when {
            points.isEmpty() -> null
            index == 0 && isCircular && points.size > 1 -> points.last()
            index > 0 -> points[index - 1]
            else -> null
        }

        val pace = paceEngine.randomPace()
        val speed = paceEngine.paceToSpeed(pace)
        val delayBeforeMs = previousPoint?.let { previous ->
            val segmentDistance = calculateDistanceMeters(previous, rawPoint)
            distanceTravelledMeters += segmentDistance
            ((segmentDistance / speed) * 1000.0).roundToLong().coerceIn(250L, 10_000L)
        } ?: 0L

        simulatedElapsedMs += delayBeforeMs
        index++

        return SimulationStep(
            point = rawPoint.copy(
            speed = speed,
            bearing = previousPoint?.let { calculateBearing(previousPoint, rawPoint) } ?: rawPoint.bearing,
            timestamp = System.currentTimeMillis() + simulatedElapsedMs
        ),
            delayBeforeMs = delayBeforeMs
        )
    }

    fun getProgress(): Float {
        if (points.size < 2 || route.totalDistance <= 0.0) return 0f
        return ((distanceTravelledMeters / route.totalDistance) * 100.0)
            .toFloat()
            .coerceIn(0f, 100f)
    }

    fun reset() {
        index = 0
        simulatedElapsedMs = 0L
        distanceTravelledMeters = 0.0
    }

    private fun calculateDistanceMeters(start: LocationPoint, end: LocationPoint): Double {
        val earthRadius = 6371000.0
        val latDistance = Math.toRadians(end.latitude - start.latitude)
        val lonDistance = Math.toRadians(end.longitude - start.longitude)
        val a = sin(latDistance / 2).pow(2) +
            cos(Math.toRadians(start.latitude)) *
            cos(Math.toRadians(end.latitude)) *
            sin(lonDistance / 2).pow(2)
        val c = 2 * atan2(sqrt(a), sqrt(max(0.0, 1 - a)))
        return earthRadius * c
    }

    private fun calculateBearing(start: LocationPoint, end: LocationPoint): Float {
        val lat1 = Math.toRadians(start.latitude)
        val lat2 = Math.toRadians(end.latitude)
        val dLon = Math.toRadians(end.longitude - start.longitude)

        val y = sin(dLon) * cos(lat2)
        val x = cos(lat1) * sin(lat2) - sin(lat1) * cos(lat2) * cos(dLon)
        val bearing = Math.toDegrees(atan2(y, x))
        return ((bearing + 360.0) % 360.0).toFloat()
    }
}
