package com.gpssimulator.service

import com.gpssimulator.data.model.LocationPoint
import com.gpssimulator.data.model.MovementType
import com.gpssimulator.data.model.Route
import com.gpssimulator.data.model.RouteSegment
import kotlin.math.*
import kotlin.random.Random

class RouteGenerator {
    
    fun generateCircularRoute(
        centerPoint: LocationPoint,
        totalDistance: Double, // in meters
        movementType: MovementType,
        numSegments: Int = 8
    ): Route {
        val radius = totalDistance / (2 * PI) // Calculate radius for circular route
        val segments = mutableListOf<RouteSegment>()
        var currentPoint = centerPoint
        var accumulatedDistance = 0.0
        
        for (i in 0 until numSegments) {
            val angleStep = (2 * PI) / numSegments
            val currentAngle = i * angleStep
            
            // Calculate next point on circle
            val nextAngle = (i + 1) * angleStep
            val nextPoint = calculatePointOnCircle(centerPoint, radius, nextAngle)
            
            // Calculate distance between current and next point
            val segmentDistance = calculateDistance(currentPoint, nextPoint)
            accumulatedDistance += segmentDistance
            
            // Calculate segment duration with variation
            val baseSpeed = movementType.baseSpeed
            val speedVariation = movementType.speedVariation
            val randomSpeed = baseSpeed + (Random.nextDouble() - 0.5) * 2 * speedVariation
            val segmentDuration = (segmentDistance / randomSpeed * 1000).toLong()
            
            val segment = RouteSegment(
                startPoint = currentPoint,
                endPoint = nextPoint,
                distance = segmentDistance,
                duration = segmentDuration,
                movementType = movementType
            )
            
            segments.add(segment)
            currentPoint = nextPoint
        }
        
        // Add segment to return to center
        val returnDistance = calculateDistance(currentPoint, centerPoint)
        val returnDuration = (returnDistance / movementType.baseSpeed * 1000).toLong()
        val returnSegment = RouteSegment(
            startPoint = currentPoint,
            endPoint = centerPoint,
            distance = returnDistance,
            duration = returnDuration,
            movementType = movementType
        )
        segments.add(returnSegment)
        
        val totalDuration = segments.sumOf { it.duration }
        
        return Route(
            name = "Circular Route - ${totalDistance.toInt()}m",
            startPoint = centerPoint,
            segments = segments,
            totalDistance = totalDistance,
            estimatedDuration = totalDuration,
            movementType = movementType
        )
    }
    
    fun generateRandomRoute(
        startPoint: LocationPoint,
        totalDistance: Double,
        movementType: MovementType,
        maxSegmentLength: Double = 200.0
    ): Route {
        val segments = mutableListOf<RouteSegment>()
        var currentPoint = startPoint
        var remainingDistance = totalDistance
        var accumulatedDistance = 0.0
        
        while (remainingDistance > 0) {
            // Generate random direction and distance for this segment
            val segmentDistance = minOf(
                remainingDistance,
                maxSegmentLength * (0.5 + Random.nextDouble() * 0.5)
            )
            
            val bearing = Random.nextDouble() * 360.0
            val nextPoint = calculateDestinationPoint(currentPoint, segmentDistance, bearing)
            
            // Calculate segment duration with variation
            val baseSpeed = movementType.baseSpeed
            val speedVariation = movementType.speedVariation
            val randomSpeed = baseSpeed + (Random.nextDouble() - 0.5) * 2 * speedVariation
            val segmentDuration = (segmentDistance / randomSpeed * 1000).toLong()
            
            val segment = RouteSegment(
                startPoint = currentPoint,
                endPoint = nextPoint,
                distance = segmentDistance,
                duration = segmentDuration,
                movementType = movementType
            )
            
            segments.add(segment)
            currentPoint = nextPoint
            remainingDistance -= segmentDistance
            accumulatedDistance += segmentDistance
        }
        
        // Add segment to return to start
        val returnDistance = calculateDistance(currentPoint, startPoint)
        val returnDuration = (returnDistance / movementType.baseSpeed * 1000).toLong()
        val returnSegment = RouteSegment(
            startPoint = currentPoint,
            endPoint = startPoint,
            distance = returnDistance,
            duration = returnDuration,
            movementType = movementType
        )
        segments.add(returnSegment)
        
        val totalDuration = segments.sumOf { it.duration }
        
        return Route(
            name = "Random Route - ${totalDistance.toInt()}m",
            startPoint = startPoint,
            segments = segments,
            totalDistance = totalDistance,
            estimatedDuration = totalDuration,
            movementType = movementType
        )
    }
    
    private fun calculatePointOnCircle(
        center: LocationPoint,
        radius: Double,
        angle: Double
    ): LocationPoint {
        val earthRadius = 6371000.0 // Earth's radius in meters
        
        val lat1 = Math.toRadians(center.latitude)
        val lon1 = Math.toRadians(center.longitude)
        
        val angularDistance = radius / earthRadius
        
        val lat2 = Math.asin(
            sin(lat1) * cos(angularDistance) +
                    cos(lat1) * sin(angularDistance) * cos(angle)
        )
        
        val lon2 = lon1 + atan2(
            sin(angle) * sin(angularDistance) * cos(lat1),
            cos(angularDistance) - sin(lat1) * sin(lat2)
        )
        
        return LocationPoint(
            latitude = Math.toDegrees(lat2),
            longitude = Math.toDegrees(lon2)
        )
    }
    
    private fun calculateDestinationPoint(
        start: LocationPoint,
        distance: Double,
        bearing: Double
    ): LocationPoint {
        val earthRadius = 6371000.0 // Earth's radius in meters
        
        val lat1 = Math.toRadians(start.latitude)
        val lon1 = Math.toRadians(start.longitude)
        val bearingRad = Math.toRadians(bearing)
        
        val angularDistance = distance / earthRadius
        
        val lat2 = Math.asin(
            sin(lat1) * cos(angularDistance) +
                    cos(lat1) * sin(angularDistance) * cos(bearingRad)
        )
        
        val lon2 = lon1 + atan2(
            sin(bearingRad) * sin(angularDistance) * cos(lat1),
            cos(angularDistance) - sin(lat1) * sin(lat2)
        )
        
        return LocationPoint(
            latitude = Math.toDegrees(lat2),
            longitude = Math.toDegrees(lon2)
        )
    }
    
    private fun calculateDistance(point1: LocationPoint, point2: LocationPoint): Double {
        val earthRadius = 6371000.0 // Earth's radius in meters
        
        val lat1 = Math.toRadians(point1.latitude)
        val lon1 = Math.toRadians(point1.longitude)
        val lat2 = Math.toRadians(point2.latitude)
        val lon2 = Math.toRadians(point2.longitude)
        
        val dLat = lat2 - lat1
        val dLon = lon2 - lon1
        
        val a = sin(dLat / 2) * sin(dLat / 2) +
                cos(lat1) * cos(lat2) *
                sin(dLon / 2) * sin(dLon / 2)
        
        val c = 2 * atan2(sqrt(a), sqrt(1 - a))
        
        return earthRadius * c
    }
}
