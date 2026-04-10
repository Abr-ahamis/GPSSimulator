package com.gpssimulator.location

import com.gpssimulator.data.model.LocationPoint
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.pow
import kotlin.math.sin
import kotlin.math.sqrt
import kotlin.random.Random

object WalkingPathEnhancer {
    private const val SidewalkOffsetMeters = 1.4
    private const val MaxLateralDriftMeters = 0.45

    fun enhance(points: List<LocationPoint>): List<LocationPoint> {
        if (points.size < 2) return points

        val totalDistance = points.zipWithNext { start, end -> distanceMeters(start, end) }.sum()
        if (totalDistance <= 0.0) return points

        val isOutAndBack = distanceMeters(points.first(), points.last()) < 30.0
        val initialSide = if (Random.nextBoolean()) 1.0 else -1.0

        val enhanced = mutableListOf<LocationPoint>()
        enhanced += applyOffset(points.first(), bearingDegrees(points.first(), points[1]), initialSide * SidewalkOffsetMeters)

        var cumulativeDistance = 0.0
        var lateralDriftMeters = 0.0
        var distanceUntilDriftChange = Random.nextDouble(1.0, 3.0)

        points.zipWithNext().forEach { (start, end) ->
            val segmentDistance = distanceMeters(start, end)
            if (segmentDistance <= 0.0) return@forEach

            val heading = bearingDegrees(start, end)
            var travelledOnSegment = 0.0

            while (travelledOnSegment < segmentDistance) {
                val stepDistance = minOf(Random.nextDouble(1.0, 3.0), segmentDistance - travelledOnSegment)
                travelledOnSegment += stepDistance
                cumulativeDistance += stepDistance

                distanceUntilDriftChange -= stepDistance
                if (distanceUntilDriftChange <= 0.0) {
                    lateralDriftMeters = (lateralDriftMeters + Random.nextDouble(-0.20, 0.20))
                        .coerceIn(-MaxLateralDriftMeters, MaxLateralDriftMeters)
                    distanceUntilDriftChange = Random.nextDouble(1.0, 3.0)
                }

                val interpolation = (travelledOnSegment / segmentDistance).coerceIn(0.0, 1.0)
                val centerPoint = interpolate(start, end, interpolation)
                val sideMultiplier = if (isOutAndBack && cumulativeDistance > totalDistance / 2.0) {
                    -initialSide
                } else {
                    initialSide
                }
                val offsetMeters = sideMultiplier * SidewalkOffsetMeters + lateralDriftMeters
                enhanced += applyOffset(centerPoint, heading, offsetMeters)
            }
        }

        return enhanced
    }

    private fun interpolate(start: LocationPoint, end: LocationPoint, fraction: Double): LocationPoint {
        return LocationPoint(
            latitude = start.latitude + (end.latitude - start.latitude) * fraction,
            longitude = start.longitude + (end.longitude - start.longitude) * fraction
        )
    }

    private fun applyOffset(point: LocationPoint, headingDegrees: Float, offsetMeters: Double): LocationPoint {
        val perpendicularBearing = headingDegrees + 90.0
        val distanceRatio = offsetMeters / 6371000.0
        val bearingRadians = Math.toRadians(perpendicularBearing.toDouble())
        val latitudeRadians = Math.toRadians(point.latitude)
        val longitudeRadians = Math.toRadians(point.longitude)

        val offsetLatitude = asinSafe(
            sin(latitudeRadians) * cos(distanceRatio) +
                cos(latitudeRadians) * sin(distanceRatio) * cos(bearingRadians)
        )
        val offsetLongitude = longitudeRadians + atan2(
            sin(bearingRadians) * sin(distanceRatio) * cos(latitudeRadians),
            cos(distanceRatio) - sin(latitudeRadians) * sin(offsetLatitude)
        )

        return point.copy(
            latitude = Math.toDegrees(offsetLatitude),
            longitude = Math.toDegrees(offsetLongitude)
        )
    }

    private fun distanceMeters(start: LocationPoint, end: LocationPoint): Double {
        val earthRadius = 6371000.0
        val dLat = Math.toRadians(end.latitude - start.latitude)
        val dLon = Math.toRadians(end.longitude - start.longitude)
        val lat1 = Math.toRadians(start.latitude)
        val lat2 = Math.toRadians(end.latitude)

        val a = sin(dLat / 2).pow(2) +
            cos(lat1) * cos(lat2) * sin(dLon / 2).pow(2)
        val c = 2 * atan2(sqrt(a), sqrt(1 - a))
        return earthRadius * c
    }

    private fun bearingDegrees(start: LocationPoint, end: LocationPoint): Float {
        val lat1 = Math.toRadians(start.latitude)
        val lat2 = Math.toRadians(end.latitude)
        val dLon = Math.toRadians(end.longitude - start.longitude)

        val y = sin(dLon) * cos(lat2)
        val x = cos(lat1) * sin(lat2) - sin(lat1) * cos(lat2) * cos(dLon)
        return ((Math.toDegrees(atan2(y, x)) + 360.0) % 360.0).toFloat()
    }

    private fun asinSafe(value: Double): Double {
        return kotlin.math.asin(value.coerceIn(-1.0, 1.0))
    }
}
