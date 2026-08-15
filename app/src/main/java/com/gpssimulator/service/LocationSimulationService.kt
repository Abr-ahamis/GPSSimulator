package com.gpssimulator.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import android.os.Looper
import android.util.Log
import androidx.core.app.NotificationCompat
import com.gpssimulator.GPSimulatorApp
import com.gpssimulator.R
import com.gpssimulator.data.model.LocationPoint
import com.gpssimulator.data.model.Route
import com.gpssimulator.location.RouteEngine
import com.gpssimulator.utils.NotificationHelper
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlin.math.*

class LocationSimulationService : Service() {
    
    private val serviceScope = CoroutineScope(Dispatchers.Main + SupervisorJob())
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    
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
    private var mockModeEnabled = false
    private var mockModeEnabling = false
    private var highAccuracyUpdatesActive = false
    private var injectedPointCount = 0
    
    override fun onCreate() {
        super.onCreate()
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        setupLocationCallback()
        Log.i(TAG, "Service created")
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
                    ?: Log.e(TAG, "Start command rejected: route was missing")
            }
            "pause_simulation" -> pauseSimulation()
            "resume_simulation" -> resumeSimulation()
            ACTION_STOP_SIMULATION -> stopSimulation()
        }
        // Never recreate a simulation after Android has stopped the service.
        return START_NOT_STICKY
    }
    
    private fun setupLocationCallback() {
        locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                Log.d(TAG, "High-accuracy location update received")
            }
        }
    }
    
    private fun startSimulation(route: Route) {
        if (_isSimulating.value) return
        
        currentRoute = route
        routeEngine = RouteEngine(route)
        startTime = System.currentTimeMillis()
        injectedPointCount = 0
        
        _isSimulating.value = true
        _isPaused.value = false
        SimulationState.setSimulating(true)
        SimulationState.setPaused(false)
        SimulationState.setProgress(0f)
        updateNotification("Simulating ${route.name}")
        Log.i(TAG, "Simulation started: routeId=${route.id}, points=${route.getAllPoints().size}")

        enableHighAccuracyUpdates()
        
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
        Log.i(TAG, "Simulation paused")
    }

    private fun resumeSimulation() {
        _isPaused.value = false
        SimulationState.setPaused(false)
        updateNotification("Simulation Resumed")
        Log.i(TAG, "Simulation resumed")
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
        val mockLocation = locationPoint.toLocation()

        if (mockModeEnabled) {
            injectMockLocation(mockLocation)
            return
        }

        if (mockModeEnabling) {
            Log.d(TAG, "Waiting for fused mock mode to become active")
            return
        }

        mockModeEnabling = true
        fusedLocationClient.setMockMode(true)
            .addOnSuccessListener {
                mockModeEnabling = false
                mockModeEnabled = true
                Log.i(TAG, "Fused mock mode enabled")
                injectMockLocation(mockLocation)
            }
            .addOnFailureListener { error ->
                mockModeEnabling = false
                Log.e(TAG, "Fused mock mode was rejected. Select this app in Developer options > Mock location app.", error)
                stopSimulation()
            }
    }

    private fun injectMockLocation(mockLocation: android.location.Location) {
        fusedLocationClient.setMockLocation(mockLocation)
            .addOnSuccessListener {
            injectedPointCount++
            if (injectedPointCount == 1 || injectedPointCount % LOCATION_LOG_INTERVAL == 0) {
                Log.i(TAG, "Mock location injected: point=$injectedPointCount, accuracy=${mockLocation.accuracy}m")
            }
            }
            .addOnFailureListener { error ->
                Log.e(TAG, "Mock location injection failed", error)
                stopSimulation()
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
        Log.i(TAG, "Simulation completed: injectedPoints=$injectedPointCount")
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
        Log.i(TAG, "Simulation stopped: injectedPoints=$injectedPointCount")
        stopSelf()
    }

    private fun enableHighAccuracyUpdates() {
        if (highAccuracyUpdatesActive) return

        try {
            val request = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 2_000L)
                .setMinUpdateIntervalMillis(1_000L)
                .setWaitForAccurateLocation(true)
                .build()
            fusedLocationClient.requestLocationUpdates(request, locationCallback, Looper.getMainLooper())
            highAccuracyUpdatesActive = true
            Log.i(TAG, "High-accuracy location updates requested")
        } catch (e: SecurityException) {
            Log.w(TAG, "Location permission unavailable for high-accuracy updates", e)
        }
    }

    private fun cleanupMockProvider() {
        if (highAccuracyUpdatesActive) {
            fusedLocationClient.removeLocationUpdates(locationCallback)
            highAccuracyUpdatesActive = false
            Log.i(TAG, "High-accuracy location updates stopped")
        }

        try {
            if (mockModeEnabled) {
                fusedLocationClient.setMockMode(false)
                    .addOnSuccessListener { Log.i(TAG, "Fused mock mode disabled; normal phone location restored") }
                    .addOnFailureListener { error -> Log.e(TAG, "Unable to disable fused mock mode", error) }
                mockModeEnabled = false
            }
        } catch (e: Exception) {
            Log.e(TAG, "Unable to disable fused mock mode", e)
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
        Log.i(TAG, "Service destroyed; cleaning up location state")
        super.onDestroy()
        simulationJob?.cancel()
        serviceScope.cancel()
        cleanupMockProvider()
    }

    override fun onTaskRemoved(rootIntent: Intent?) {
        Log.i(TAG, "Task removed; cleaning up location state")
        stopSimulation()
        super.onTaskRemoved(rootIntent)
    }
    
    companion object {
        private const val TAG = "LocationSimulation"
        private const val LOCATION_LOG_INTERVAL = 10
        const val NOTIFICATION_ID = 1
        const val ACTION_START_SIMULATION = "start_simulation"
        const val ACTION_STOP_SIMULATION = "stop_simulation"
        const val NOTIFICATION_CHANNEL_ID = "gps_simulator_channel"
    }
}
