package com.gpssimulator.service

import com.gpssimulator.data.model.LocationPoint
import com.gpssimulator.data.model.PaceConfig
import com.gpssimulator.data.model.Route
import com.gpssimulator.data.network.OSRMService
import com.gpssimulator.utils.PolylineDecoder
import kotlin.math.*
import kotlin.random.Random

class RouteGenerator {
    
    private val osrmService = OSRMService.create()

    suspend fun generateRandomRoute(
        startPoint: LocationPoint,
        totalDistance: Double,
        paceConfig: PaceConfig
    ): Route? {
        // Pick a random destination roughly totalDistance/2 away to make a return trip
        val targetDistance = totalDistance / 2.0
        val randomBearing = Random.nextDouble() * 360.0
        val destinationPoint = calculateDestinationPoint(startPoint, targetDistance, randomBearing)

        // Query OSRM for route from start -> destination -> start
        val coordinates = "${startPoint.longitude},${startPoint.latitude};${destinationPoint.longitude},${destinationPoint.latitude};${startPoint.longitude},${startPoint.latitude}"
        
        return fetchRouteFromOSRM(coordinates, "Random Route - ${totalDistance.toInt()}m", paceConfig)
    }

    suspend fun generateCustomRouteFromPins(
        pins: List<LocationPoint>,
        paceConfig: PaceConfig
    ): Route? {
        if (pins.size < 2) return null
        
        val coordinates = pins.joinToString(";") { "${it.longitude},${it.latitude}" }
        return fetchRouteFromOSRM(coordinates, "Custom Route", paceConfig)
    }

    suspend fun generateCustomDrawnRoute(
        points: List<LocationPoint>,
        paceConfig: PaceConfig
    ): Route {
        val totalDistance = calculateTotalDistance(points)
        val estimatedDuration = (totalDistance / (1000.0 / paceConfig.baseSeconds) * 1000).toLong()
        
        return Route(
            name = "Drawn Route",
            points = points,
            totalDistance = totalDistance,
            estimatedDuration = estimatedDuration,
            paceConfig = paceConfig
        )
    }

    private suspend fun fetchRouteFromOSRM(coordinates: String, name: String, paceConfig: PaceConfig): Route? {
        return try {
            val response = osrmService.getRoute(coordinates)
            if (response.code == "Ok" && response.routes.isNotEmpty()) {
                val osrmRoute = response.routes.first()
                val decodedPoints = PolylineDecoder.decode(osrmRoute.geometry)
                
                Route(
                    name = name,
                    points = decodedPoints,
                    totalDistance = osrmRoute.distance,
                    estimatedDuration = ((osrmRoute.distance / 1000.0) * paceConfig.baseSeconds * 1000).toLong(),
                    paceConfig = paceConfig
                )
            } else null
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
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

    private fun calculateTotalDistance(points: List<LocationPoint>): Double {
        if (points.size < 2) return 0.0
        var total = 0.0
        for (i in 0 until points.size - 1) {
            total += calculateDistance(points[i], points[i + 1])
        }
        return total
    }

    private fun calculateDistance(point1: LocationPoint, point2: LocationPoint): Double {
        val earthRadius = 6371000.0
        val lat1 = Math.toRadians(point1.latitude)
        val lon1 = Math.toRadians(point1.longitude)
        val lat2 = Math.toRadians(point2.latitude)
        val lon2 = Math.toRadians(point2.longitude)
        val dLat = lat2 - lat1
        val dLon = lon2 - lon1
        val a = sin(dLat / 2) * sin(dLat / 2) +
                cos(lat1) * cos(lat2) * sin(dLon / 2) * sin(dLon / 2)
        val c = 2 * atan2(sqrt(a), sqrt(1 - a))
        return earthRadius * c
    }
}
