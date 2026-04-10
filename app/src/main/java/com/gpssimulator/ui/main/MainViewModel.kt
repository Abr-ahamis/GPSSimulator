package com.gpssimulator.ui.main

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.gpssimulator.GPSimulatorApp
import com.gpssimulator.data.model.LocationPoint
import com.gpssimulator.data.model.PaceConfig
import com.gpssimulator.data.model.Route
import com.gpssimulator.service.RouteGenerator
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

enum class RouteType {
    RANDOM, PINS, DRAW
}

class MainViewModel(application: Application) : AndroidViewModel(application) {
    
    private val routeGenerator = RouteGenerator()
    private val app = application as GPSimulatorApp
    
    private val _isSimulating = MutableStateFlow(false)
    val isSimulating: StateFlow<Boolean> = _isSimulating

    private val _isPaused = MutableStateFlow(false)
    val isPaused: StateFlow<Boolean> = _isPaused
    
    private val _currentLocation = MutableStateFlow<LocationPoint?>(null)
    val currentLocation: StateFlow<LocationPoint?> = _currentLocation
    
    private val _progress = MutableStateFlow(0f)
    val progress: StateFlow<Float> = _progress
    
    private val _distance = MutableStateFlow(5000.0) // Default 5km
    val distance: StateFlow<Double> = _distance
    
    private val _paceConfig = MutableStateFlow(PaceConfig(330)) // Default 05:30
    val paceConfig: StateFlow<PaceConfig> = _paceConfig
    
    private val _routeType = MutableStateFlow(RouteType.RANDOM)
    val routeType: StateFlow<RouteType> = _routeType

    // For Pin and Draw modes
    private val _customPoints = MutableStateFlow<List<LocationPoint>>(emptyList())
    val customPoints: StateFlow<List<LocationPoint>> = _customPoints

    fun setCurrentLocation(location: LocationPoint) {
        _currentLocation.value = location
    }
    
    fun setDistance(distance: Double) {
        _distance.value = distance
    }
    
    fun setPaceConfig(paceSeconds: Int) {
        _paceConfig.value = PaceConfig(paceSeconds)
    }
    
    fun setRouteType(type: RouteType) {
        _routeType.value = type
    }

    fun addCustomPoint(point: LocationPoint) {
        _customPoints.value = _customPoints.value + point
    }

    fun clearCustomPoints() {
        _customPoints.value = emptyList()
    }
    
    fun startSimulation() {
        _isSimulating.value = true
        _isPaused.value = false
        _progress.value = 0f
    }
    
    fun stopSimulation() {
        _isSimulating.value = false
        _isPaused.value = false
        _progress.value = 0f
    }

    fun pauseSimulation() {
        _isPaused.value = true
    }

    fun resumeSimulation() {
        _isPaused.value = false
    }
    
    suspend fun generateRoute(startPoint: LocationPoint): Route? {
        return try {
            val route = when (_routeType.value) {
                RouteType.RANDOM -> {
                    routeGenerator.generateRandomRoute(
                        startPoint = startPoint,
                        totalDistance = _distance.value,
                        paceConfig = _paceConfig.value
                    )
                }
                RouteType.PINS -> {
                    if (_customPoints.value.isEmpty()) return null
                    routeGenerator.generateCustomRouteFromPins(
                        pins = listOf(startPoint) + _customPoints.value,
                        paceConfig = _paceConfig.value
                    )
                }
                RouteType.DRAW -> {
                    if (_customPoints.value.isEmpty()) return null
                    routeGenerator.generateCustomDrawnRoute(
                        points = listOf(startPoint) + _customPoints.value,
                        paceConfig = _paceConfig.value
                    )
                }
            } ?: return null
            
            // Save route to database
            val routeId = app.routeRepository.insertRoute(route)
            
            route.copy(id = routeId)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}
