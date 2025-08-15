package com.gpssimulator.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.location.Location
import android.location.LocationManager
import android.os.Build
import android.os.IBinder
import android.os.Looper
import androidx.core.app.NotificationCompat
import com.gpssimulator.GPSimulatorApp
import com.gpssimulator.R
import com.gpssimulator.data.model.LocationPoint
import com.gpssimulator.data.model.Route
import com.gpssimulator.utils.NotificationHelper
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import java.util.concurrent.TimeUnit
import kotlin.math.*

class LocationSimulationService : Service() {
    
    private val serviceScope = CoroutineScope(Dispatchers.Main + SupervisorJob())
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var locationManager: LocationManager
    private lateinit var routeGenerator: RouteGenerator
    
    private var simulationJob: Job? = null
    private var currentRoute: Route? = null
    private var currentPointIndex = 0
    private var allRoutePoints = listOf<LocationPoint>()
    private var startTime = 0L
    
    private val _isSimulating = MutableStateFlow(false)
    val isSimulating: StateFlow<Boolean> = _isSimulating
    
    private val _currentLocation = MutableStateFlow<LocationPoint?>(null)
    val currentLocation: StateFlow<LocationPoint?> = _currentLocation
    
    private val _progress = MutableStateFlow(0f)
    val progress: StateFlow<Float> = _progress
    
    private lateinit var locationCallback: LocationCallback
    
    override fun onCreate() {
        super.onCreate()
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
        routeGenerator = RouteGenerator()
        
        setupLocationCallback()
        startForeground(NOTIFICATION_ID, createNotification())
    }
    
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_START_SIMULATION -> {
                val route = intent.getParcelableExtra<Route>("route")
                route?.let { startSimulation(it) }
            }
            ACTION_STOP_SIMULATION -> {
                stopSimulation()
            }
        }
        return START_STICKY
    }
    
    private fun setupLocationCallback() {
        locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                locationResult.lastLocation?.let { location ->
                    // We might want to track real location for reference
                }
            }
        }
    }
    
    private fun startSimulation(route: Route) {
        if (_isSimulating.value) return
        
        currentRoute = route
        allRoutePoints = route.getAllPoints()
        currentPointIndex = 0
        startTime = System.currentTimeMillis()
        
        _isSimulating.value = true
        updateNotification("Simulating ${route.name}")
        
        simulationJob = serviceScope.launch {
            simulateRoute()
        }
    }
    
    private suspend fun simulateRoute() {
        val route = currentRoute ?: return
        
        while (currentPointIndex < allRoutePoints.size && _isSimulating.value) {
            val currentPoint = allRoutePoints[currentPointIndex]
            
            // Set mock location
            setMockLocation(currentPoint)
            _currentLocation.value = currentPoint
            
            // Update progress
            val progress = (currentPointIndex.toFloat() / allRoutePoints.size.toFloat()) * 100
            _progress.value = progress
            
            // Calculate delay based on speed and distance to next point
            val delay = calculateDelayToNextPoint(currentPointIndex)
            
            // Add some randomness to make it more natural
            val randomDelay = (delay * (0.8 + Math.random() * 0.4)).toLong()
            
            delay(randomDelay)
            currentPointIndex++
        }
        
        // Route completed
        completeSimulation()
    }
    
    private fun calculateDelayToNextPoint(pointIndex: Int): Long {
        if (pointIndex >= allRoutePoints.size - 1) return 1000L
        
        val currentPoint = allRoutePoints[pointIndex]
        val nextPoint = allRoutePoints[pointIndex + 1]
        
        val distance = calculateDistance(currentPoint, nextPoint)
        val speed = currentPoint.speed.takeIf { it > 0 } ?: currentRoute?.movementType?.baseSpeed ?: 1.4f
        
        return (distance / speed * 1000).toLong()
    }
    
    private fun setMockLocation(locationPoint: LocationPoint) {
        try {
            val mockLocation = locationPoint.toLocation()
            
            // Add test provider if not exists
            if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                locationManager.addTestProvider(
                    LocationManager.GPS_PROVIDER,
                    false,
                    false,
                    false,
                    false,
                    true,
                    true,
                    true,
                    0,
                    0
                )
            }
            
            locationManager.setTestProviderEnabled(LocationManager.GPS_PROVIDER, true)
            locationManager.setTestProviderLocation(LocationManager.GPS_PROVIDER, mockLocation)
            
        } catch (e: SecurityException) {
            // Handle security exception
            e.printStackTrace()
        } catch (e: Exception) {
            // Handle other exceptions
            e.printStackTrace()
        }
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
    
    private fun completeSimulation() {
        _isSimulating.value = false
        _progress.value = 100f
        
        val duration = System.currentTimeMillis() - startTime
        
        // Save route completion to database
        currentRoute?.let { route ->
            serviceScope.launch {
                val app = application as GPSimulatorApp
                app.routeRepository.updateRouteCompletion(route.id, duration)
            }
        }
        
        updateNotification("Simulation completed")
        
        // Remove test provider
        try {
            locationManager.setTestProviderEnabled(LocationManager.GPS_PROVIDER, false)
            locationManager.removeTestProvider(LocationManager.GPS_PROVIDER)
        } catch (e: Exception) {
            e.printStackTrace()
        }
        
        // Stop service after a delay
        serviceScope.launch {
            delay(5000) // Show completion for 5 seconds
            stopSelf()
        }
    }
    
    private fun stopSimulation() {
        _isSimulating.value = false
        simulationJob?.cancel()
        
        // Remove test provider
        try {
            locationManager.setTestProviderEnabled(LocationManager.GPS_PROVIDER, false)
            locationManager.removeTestProvider(LocationManager.GPS_PROVIDER)
        } catch (e: Exception) {
            e.printStackTrace()
        }
        
        updateNotification("Simulation stopped")
        stopSelf()
    }
    
    private fun createNotification(): Notification {
        return NotificationHelper.createSimulationNotification(
            context = this,
            title = "GPS Simulator",
            content = "Ready to start simulation",
            isRunning = false
        )
    }
    
    private fun updateNotification(content: String) {
        val notification = NotificationHelper.createSimulationNotification(
            context = this,
            title = "GPS Simulator",
            content = content,
            isRunning = _isSimulating.value,
            progress = _progress.value.toInt()
        )
        
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(NOTIFICATION_ID, notification)
    }
    
    override fun onBind(intent: Intent?): IBinder? {
        return null
    }
    
    override fun onDestroy() {
        super.onDestroy()
        simulationJob?.cancel()
        serviceScope.cancel()
        
        // Clean up test provider
        try {
            locationManager.setTestProviderEnabled(LocationManager.GPS_PROVIDER, false)
            locationManager.removeTestProvider(LocationManager.GPS_PROVIDER)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
    
    companion object {
        const val NOTIFICATION_ID = 1
        const val ACTION_START_SIMULATION = "start_simulation"
        const val ACTION_STOP_SIMULATION = "stop_simulation"
        const val NOTIFICATION_CHANNEL_ID = "gps_simulator_channel"
    }
}
