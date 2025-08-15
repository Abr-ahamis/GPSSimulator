package com.gpssimulator.ui.main

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.gpssimulator.GPSimulatorApp
import com.gpssimulator.data.model.LocationPoint
import com.gpssimulator.data.model.MovementType
import com.gpssimulator.data.model.Route
import com.gpssimulator.service.RouteGenerator
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class MainViewModel(application: Application) : AndroidViewModel(application) {
    
    private val routeGenerator = RouteGenerator()
    private val app = application as GPSimulatorApp
    
    private val _isSimulating = MutableStateFlow(false)
    val isSimulating: StateFlow<Boolean> = _isSimulating
    
    private val _currentLocation = MutableStateFlow<LocationPoint?>(null)
    val currentLocation: StateFlow<LocationPoint?> = _currentLocation
    
    private val _progress = MutableStateFlow(0f)
    val progress: StateFlow<Float> = _progress
    
    private val _distance = MutableStateFlow(5000.0) // Default 5km
    val distance: StateFlow<Double> = _distance
    
    private val _movementType = MutableStateFlow(MovementType.WALKING)
    val movementType: StateFlow<MovementType> = _movementType
    
    private val _isCircularRoute = MutableStateFlow(true)
    val isCircularRoute: StateFlow<Boolean> = _isCircularRoute
    
    fun setCurrentLocation(location: LocationPoint) {
        _currentLocation.value = location
    }
    
    fun setDistance(distance: Double) {
        _distance.value = distance
    }
    
    fun setMovementType(movementType: MovementType) {
        _movementType.value = movementType
    }
    
    fun setCircularRoute(isCircular: Boolean) {
        _isCircularRoute.value = isCircular
    }
    
    fun startSimulation() {
        _isSimulating.value = true
        _progress.value = 0f
    }
    
    fun stopSimulation() {
        _isSimulating.value = false
        _progress.value = 0f
    }
    
    suspend fun generateRoute(startPoint: LocationPoint): Route? {
        return try {
            val route = if (_isCircularRoute.value) {
                routeGenerator.generateCircularRoute(
                    centerPoint = startPoint,
                    totalDistance = _distance.value,
                    movementType = _movementType.value
                )
            } else {
                routeGenerator.generateRandomRoute(
                    startPoint = startPoint,
                    totalDistance = _distance.value,
                    movementType = _movementType.value
                )
            }
            
            // Save route to database
            val routeId = app.routeRepository.insertRoute(
                name = route.name,
                startPoint = startPoint,
                totalDistance = route.totalDistance,
                estimatedDuration = route.estimatedDuration,
                movementType = route.movementType
            )
            
            route.copy(id = routeId)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}
