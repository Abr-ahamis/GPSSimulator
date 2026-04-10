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
import androidx.core.app.NotificationCompat
import com.gpssimulator.GPSimulatorApp
import com.gpssimulator.R
import com.gpssimulator.data.model.LocationPoint
import com.gpssimulator.data.model.Route
import com.gpssimulator.location.RouteEngine
import com.gpssimulator.utils.NotificationHelper
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlin.math.*

class LocationSimulationService : Service() {
    
    private val serviceScope = CoroutineScope(Dispatchers.Main + SupervisorJob())
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var locationManager: LocationManager
    
    private var simulationJob: Job? = null
    private var currentRoute: Route? = null
    private var routeEngine: RouteEngine? = null
    private var startTime = 0L
    
    private val _isSimulating = MutableStateFlow(false)
    val isSimulating: StateFlow<Boolean> = _isSimulating

    private val _isPaused = MutableStateFlow(false)
    val isPaused: StateFlow<Boolean> = _isPaused
    
    private val _currentLocation = MutableStateFlow<LocationPoint?>(null)
    val currentLocation: StateFlow<LocationPoint?> = _currentLocation
    
    private val _progress = MutableStateFlow(0f)
    val progress: StateFlow<Float> = _progress
    
    private lateinit var locationCallback: LocationCallback
    private var mockProviderRegistered = false
    
    override fun onCreate() {
        super.onCreate()
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
        
        setupLocationCallback()
        startForeground(NOTIFICATION_ID, createNotification())
    }
    
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_START_SIMULATION -> {
                val route = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    intent.getParcelableExtra("route", Route::class.java)
                } else {
                    @Suppress("DEPRECATION")
                    intent.getParcelableExtra("route")
                }
                route?.let { startSimulation(it) }
            }
            "pause_simulation" -> pauseSimulation()
            "resume_simulation" -> resumeSimulation()
            ACTION_STOP_SIMULATION -> stopSimulation()
        }
        return START_STICKY
    }
    
    private fun setupLocationCallback() {
        locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                // Tracking if needed
            }
        }
    }
    
    private fun startSimulation(route: Route) {
        if (_isSimulating.value) return
        
        currentRoute = route
        routeEngine = RouteEngine(route)
        startTime = System.currentTimeMillis()
        
        _isSimulating.value = true
        _isPaused.value = false
        SimulationState.setSimulating(true)
        SimulationState.setPaused(false)
        SimulationState.setProgress(0f)
        updateNotification("Simulating ${route.name}")
        
        launchSimulationLoop()
    }
    
    private fun launchSimulationLoop() {
        simulationJob?.cancel()
        simulationJob = serviceScope.launch {
            simulateRoute()
        }
    }

    private fun pauseSimulation() {
        _isPaused.value = true
        SimulationState.setPaused(true)
        simulationJob?.cancel()
        updateNotification("Simulation Paused")
    }

    private fun resumeSimulation() {
        _isPaused.value = false
        SimulationState.setPaused(false)
        updateNotification("Simulation Resumed")
        launchSimulationLoop()
    }
    
    private suspend fun simulateRoute() {
        val engine = routeEngine ?: return
        
        while (engine.hasNext() && _isSimulating.value && !_isPaused.value) {
            val step = engine.getNextStep()
            if (step.delayBeforeMs > 0) {
                delay(step.delayBeforeMs)
            }
            if (!_isSimulating.value || _isPaused.value) {
                break
            }
            val currentPoint = step.point
            
            setMockLocation(currentPoint)
            _currentLocation.value = currentPoint
            SimulationState.setCurrentLocation(currentPoint)
            
            val progress = engine.getProgress()
            _progress.value = progress
            SimulationState.setProgress(progress)
        }
        
        if (!engine.hasNext()) {
            completeSimulation()
        }
    }
    
    private fun setMockLocation(locationPoint: LocationPoint) {
        try {
            val mockLocation = locationPoint.toLocation()
            fusedLocationClient.setMockMode(true)
            fusedLocationClient.setMockLocation(mockLocation)

            if (!mockProviderRegistered) {
                try {
                    locationManager.addTestProvider(
                        LocationManager.GPS_PROVIDER,
                        false, false, false, false,
                        true, true, true, 0, 5
                    )
                    mockProviderRegistered = true
                } catch (_: Exception) {
                    // Some devices reject replacing the built-in GPS provider.
                }
            }

            if (mockProviderRegistered) {
                locationManager.setTestProviderEnabled(LocationManager.GPS_PROVIDER, true)
                locationManager.setTestProviderLocation(LocationManager.GPS_PROVIDER, mockLocation)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
    
    private fun completeSimulation() {
        _isSimulating.value = false
        _progress.value = 100f
        SimulationState.setSimulating(false)
        SimulationState.setPaused(false)
        SimulationState.setProgress(100f)
        
        val duration = System.currentTimeMillis() - startTime
        
        currentRoute?.let { route ->
            serviceScope.launch {
                val app = application as GPSimulatorApp
                app.routeRepository.updateRouteCompletion(route.id, duration)
            }
        }
        
        updateNotification("Simulation completed")
        cleanupMockProvider()
        
        serviceScope.launch {
            delay(5000)
            stopSelf()
        }
    }
    
    private fun stopSimulation() {
        _isSimulating.value = false
        _isPaused.value = false
        SimulationState.reset()
        simulationJob?.cancel()
        
        cleanupMockProvider()
        
        updateNotification("Simulation stopped")
        stopSelf()
    }

    private fun cleanupMockProvider() {
        try {
            fusedLocationClient.setMockMode(false)
        } catch (e: Exception) {
            e.printStackTrace()
        }

        try {
            if (mockProviderRegistered) {
                locationManager.setTestProviderEnabled(LocationManager.GPS_PROVIDER, false)
                locationManager.removeTestProvider(LocationManager.GPS_PROVIDER)
                mockProviderRegistered = false
            }
        } catch (e: IllegalArgumentException) {
            mockProviderRegistered = false
        } catch (e: Exception) {
            e.printStackTrace()
        }
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
        cleanupMockProvider()
    }
    
    companion object {
        const val NOTIFICATION_ID = 1
        const val ACTION_START_SIMULATION = "start_simulation"
        const val ACTION_STOP_SIMULATION = "stop_simulation"
        const val NOTIFICATION_CHANNEL_ID = "gps_simulator_channel"
    }
}
