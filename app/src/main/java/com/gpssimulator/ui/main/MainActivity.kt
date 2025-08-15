package com.gpssimulator.ui.main

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.os.IBinder
import android.provider.Settings
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.gpssimulator.GPSimulatorApp
import com.gpssimulator.R
import com.gpssimulator.data.model.LocationPoint
import com.gpssimulator.data.model.MovementType
import com.gpssimulator.databinding.ActivityMainBinding
import com.gpssimulator.service.LocationSimulationService
import com.gpssimulator.ui.history.HistoryActivity
import com.gpssimulator.ui.settings.SettingsActivity
import com.gpssimulator.utils.LocationUtils
import com.gpssimulator.utils.NotificationHelper
import com.gpssimulator.utils.PermissionHelper
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import org.osmdroid.api.IMapController
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.Polyline
import androidx.lifecycle.lifecycleScope
import android.Manifest

class MainActivity : AppCompatActivity() {
    
    private lateinit var binding: ActivityMainBinding
    private val viewModel: MainViewModel by viewModels()
    private lateinit var locationUtils: LocationUtils
    
    private lateinit var mapView: MapView
    private lateinit var mapController: IMapController
    private var currentLocationMarker: Marker? = null
    private var routePolyline: Polyline? = null
    
    private var locationSimulationService: LocationSimulationService? = null
    private var isServiceBound = false
    
    private val serviceConnection = object : ServiceConnection {
        override fun onServiceConnected(className: ComponentName, service: IBinder) {
            // This is a started service, not a bound service
            locationSimulationService = null
            isServiceBound = false
        }
        
        override fun onServiceDisconnected(arg0: ComponentName) {
            locationSimulationService = null
            isServiceBound = false
        }
    }
    
    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val allGranted = permissions.values.all { it }
        if (allGranted) {
            checkAndRequestBackgroundLocationPermission()
        } else {
            Toast.makeText(this, "Location permissions are required", Toast.LENGTH_SHORT).show()
        }
    }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        // Configure OSMDroid
        Configuration.getInstance().load(applicationContext, 
            androidx.preference.PreferenceManager.getDefaultSharedPreferences(applicationContext))
        Configuration.getInstance().userAgentValue = packageName
        
        locationUtils = LocationUtils(this)
        NotificationHelper.createNotificationChannel(this)
        
        setupMap()
        setupUI()
        observeViewModel()
        
        checkAndRequestLocationPermissions()
    }
    
    private fun setupMap() {
        mapView = binding.mapView
        mapView.setTileSource(TileSourceFactory.MAPNIK)
        mapView.setMultiTouchControls(true)
        
        mapController = mapView.controller
        mapController.setZoom(15.0)
        
        // Get current location and center map
        getCurrentLocationAndCenter()
    }
    
    private fun setupUI() {
        binding.apply {
            // Distance selector
            distanceGroup.setOnCheckedChangeListener { _, checkedId ->
                when (checkedId) {
                    R.id.distance5km -> viewModel.setDistance(5000.0)
                    R.id.distance10km -> viewModel.setDistance(10000.0)
                    R.id.distanceCustom -> {
                        // Show custom distance dialog
                        showCustomDistanceDialog()
                    }
                }
            }
            
            // Movement type selector
            movementTypeGroup.setOnCheckedChangeListener { _, checkedId ->
                val movementType = when (checkedId) {
                    R.id.movementWalking -> MovementType.WALKING
                    R.id.movementRunning -> MovementType.RUNNING
                    R.id.movementCycling -> MovementType.CYCLING
                    else -> MovementType.WALKING
                }
                viewModel.setMovementType(movementType)
            }
            
            // Route type selector
            routeTypeGroup.setOnCheckedChangeListener { _, checkedId ->
                val isCircular = when (checkedId) {
                    R.id.routeCircular -> true
                    R.id.routeRandom -> false
                    else -> true
                }
                viewModel.setCircularRoute(isCircular)
            }
            
            // Start/Stop button
            startStopButton.setOnClickListener {
                if (viewModel.isSimulating.value) {
                    stopSimulation()
                } else {
                    startSimulation()
                }
            }
            
            // Navigation buttons
            historyButton.setOnClickListener {
                startActivity(Intent(this@MainActivity, HistoryActivity::class.java))
            }
            
            settingsButton.setOnClickListener {
                startActivity(Intent(this@MainActivity, SettingsActivity::class.java))
            }
            
            // Current location button
            currentLocationButton.setOnClickListener {
                getCurrentLocationAndCenter()
            }
        }
    }
    
    private fun observeViewModel() {
        lifecycleScope.launch {
            viewModel.isSimulating.collectLatest { isSimulating ->
                updateUIForSimulationState(isSimulating)
            }
        }
        
        lifecycleScope.launch {
            viewModel.currentLocation.collectLatest { location ->
                location?.let { updateMapLocation(it) }
            }
        }
        
        lifecycleScope.launch {
            viewModel.progress.collectLatest { progress ->
                binding.progressBar.progress = progress.toInt()
                binding.progressText.text = "${progress.toInt()}%"
            }
        }
    }
    
    private fun getCurrentLocationAndCenter() {
        lifecycleScope.launch {
            try {
                val location = locationUtils.getCurrentLocation()
                location?.let {
                    val geoPoint = GeoPoint(it.latitude, it.longitude)
                    mapController.setCenter(geoPoint)
                    viewModel.setCurrentLocation(LocationPoint.fromLocation(it))
                }
            } catch (e: Exception) {
                Toast.makeText(
                    this@MainActivity,
                    "Failed to get current location",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }
    
    private fun updateMapLocation(locationPoint: LocationPoint) {
        val geoPoint = GeoPoint(locationPoint.latitude, locationPoint.longitude)
        
        // Update or create marker
        currentLocationMarker?.let { mapView.overlays.remove(it) }
        currentLocationMarker = Marker(mapView).apply {
            position = geoPoint
            title = "Current Location"
            snippet = "Speed: ${locationPoint.speed} m/s"
            setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
        }
        mapView.overlays.add(currentLocationMarker)
        
        // Center map on current location
        mapController.setCenter(geoPoint)
        mapView.invalidate()
    }
    
    private fun updateUIForSimulationState(isSimulating: Boolean) {
        binding.apply {
            startStopButton.text = if (isSimulating) "Stop Simulation" else "Start Simulation"
            startStopButton.setBackgroundColor(
                ContextCompat.getColor(
                    this@MainActivity,
                    if (isSimulating) android.R.color.holo_red_dark else android.R.color.holo_green_dark
                )
            )
            
            // Disable controls during simulation
            distanceGroup.isEnabled = !isSimulating
            movementTypeGroup.isEnabled = !isSimulating
            routeTypeGroup.isEnabled = !isSimulating
            
            progressBar.isIndeterminate = isSimulating
            if (!isSimulating) {
                progressBar.progress = 0
                progressText.text = "0%"
            }
        }
    }
    
    private fun startSimulation() {
        if (!locationUtils.hasLocationPermission()) {
            Toast.makeText(this, "Location permission required", Toast.LENGTH_SHORT).show()
            return
        }
        
        if (!locationUtils.isLocationEnabled()) {
            Toast.makeText(this, "Please enable GPS", Toast.LENGTH_SHORT).show()
            return
        }
        
        // Check for mock location permission
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (!Settings.Secure.getString(contentResolver, Settings.Secure.ALLOW_MOCK_LOCATION).equals("1")) {
                Toast.makeText(this, "Please enable mock location in developer options", Toast.LENGTH_LONG).show()
                return
            }
        }
        
        val currentLocation = viewModel.currentLocation.value
        if (currentLocation == null) {
            Toast.makeText(this, "Getting current location...", Toast.LENGTH_SHORT).show()
            getCurrentLocationAndCenter()
            return
        }
        
        lifecycleScope.launch {
            try {
                val route = viewModel.generateRoute(currentLocation)
                route?.let {
                    drawRouteOnMap(it)
                    startLocationSimulationService(it)
                }
            } catch (e: Exception) {
                Toast.makeText(
                    this@MainActivity,
                    "Failed to generate route: ${e.message}",
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }
    
    private fun stopSimulation() {
        val intent = Intent(this, LocationSimulationService::class.java).apply {
            action = LocationSimulationService.ACTION_STOP_SIMULATION
        }
        startService(intent)
        viewModel.stopSimulation()
        
        // Clear route from map
        routePolyline?.let { mapView.overlays.remove(it) }
        routePolyline = null
        mapView.invalidate()
    }
    
    private fun drawRouteOnMap(route: com.gpssimulator.data.model.Route) {
        val points = route.getAllPoints().map { point ->
            GeoPoint(point.latitude, point.longitude)
        }
        
        routePolyline = Polyline().apply {
            setPoints(points)
            outlinePaint.color = ContextCompat.getColor(this@MainActivity, R.color.route_color)
            outlinePaint.strokeWidth = 5f
        }
        
        mapView.overlays.add(routePolyline)
        
        // Fit map to show entire route
        if (points.isNotEmpty()) {
            val boundingBox = org.osmdroid.boundingbox.BoundingBox.fromGeoPoints(points)
            mapView.zoomToBoundingBox(boundingBox, false, 100)
        }
        
        mapView.invalidate()
    }
    
    private fun startLocationSimulationService(route: com.gpssimulator.data.model.Route) {
        val intent = Intent(this, LocationSimulationService::class.java).apply {
            action = LocationSimulationService.ACTION_START_SIMULATION
            putExtra("route", route)
        }
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(intent)
        } else {
            startService(intent)
        }
        
        viewModel.startSimulation()
    }
    
    private fun checkAndRequestLocationPermissions() {
        if (!PermissionHelper.hasLocationPermissions(this)) {
            requestPermissionLauncher.launch(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                )
            )
        } else {
            checkAndRequestBackgroundLocationPermission()
        }
    }
    
    private fun checkAndRequestBackgroundLocationPermission() {
        if (!PermissionHelper.hasBackgroundLocationPermission(this)) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                PermissionHelper.requestBackgroundLocationPermission(this, REQUEST_BACKGROUND_LOCATION_PERMISSION)
            }
        }
    }
    
    private fun showCustomDistanceDialog() {
        // Create a dialog for custom distance input
        val dialogView = layoutInflater.inflate(R.layout.dialog_custom_distance, null)
        val editText = dialogView.findViewById<android.widget.EditText>(R.id.customDistanceEditText)
        
        androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle("Custom Distance")
            .setView(dialogView)
            .setPositiveButton("OK") { _, _ ->
                val distanceText = editText.text.toString()
                val distance = distanceText.toDoubleOrNull()
                if (distance != null && distance > 0) {
                    viewModel.setDistance(distance * 1000) // Convert km to meters
                    binding.distanceCustom.text = "${distanceText} km"
                } else {
                    Toast.makeText(this, "Invalid distance", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }
    
    override fun onResume() {
        super.onResume()
        mapView.onResume()
        bindToSimulationService()
    }
    
    override fun onPause() {
        super.onPause()
        mapView.onPause()
        unbindFromSimulationService()
    }
    
    private fun bindToSimulationService() {
        Intent(this, LocationSimulationService::class.java).also { intent ->
            bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE)
        }
    }
    
    private fun unbindFromSimulationService() {
        if (isServiceBound) {
            unbindService(serviceConnection)
            isServiceBound = false
        }
    }
    
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        
        when (requestCode) {
            REQUEST_LOCATION_PERMISSIONS -> {
                if (PermissionHelper.areAllPermissionsGranted(grantResults)) {
                    checkAndRequestBackgroundLocationPermission()
                } else {
                    Toast.makeText(this, "Location permissions required", Toast.LENGTH_SHORT).show()
                }
            }
            REQUEST_BACKGROUND_LOCATION_PERMISSION -> {
                if (!PermissionHelper.areAllPermissionsGranted(grantResults)) {
                    Toast.makeText(
                        this,
                        "Background location permission recommended for best experience",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
        }
    }
    
    companion object {
        private const val REQUEST_LOCATION_PERMISSIONS = 100
        private const val REQUEST_BACKGROUND_LOCATION_PERMISSION = 101
    }
}
