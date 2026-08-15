package com.gpssimulator.ui.main

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.content.pm.PackageManager
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Build
import android.os.Bundle
import android.os.IBinder
import android.provider.Settings
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.gpssimulator.GPSimulatorApp
import com.gpssimulator.R
import com.gpssimulator.data.model.LocationPoint
import com.gpssimulator.databinding.ActivityMainBinding
import com.gpssimulator.service.LocationSimulationService
import com.gpssimulator.service.SimulationState
import com.gpssimulator.ui.history.HistoryActivity
import com.gpssimulator.ui.settings.SettingsActivity
import com.gpssimulator.utils.LocationUtils
import com.gpssimulator.utils.NotificationHelper
import com.gpssimulator.utils.PermissionHelper
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import org.osmdroid.api.IMapController
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.XYTileSource
import org.osmdroid.util.BoundingBox
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.Polyline
import androidx.lifecycle.lifecycleScope
import android.Manifest
import java.util.Locale

class MainActivity : AppCompatActivity(), SensorEventListener {
    private enum class DashboardMode {
        SETUP,
        READY,
        ACTIVE,
        PAUSED
    }

    private enum class SetupStep {
        DISTANCE,
        PACE,
        ROUTE,
        SUMMARY
    }
    
    private lateinit var binding: ActivityMainBinding
    private val viewModel: MainViewModel by viewModels()
    private lateinit var locationUtils: LocationUtils
    private lateinit var sensorManager: SensorManager
    private var rotationSensor: Sensor? = null
    private var smoothedHeadingDegrees: Double? = null
    
    private lateinit var mapView: MapView
    private lateinit var mapController: IMapController
    private var currentLocationMarker: Marker? = null
    private var routePolyline: Polyline? = null
    private var setupStep = SetupStep.DISTANCE
    private var isGeneratingRoute = false
    private var hasConfirmedDistance = false
    private var hasConfirmedPace = false
    private var hasConfirmedRoute = false
    
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
        Configuration.getInstance().load(
            applicationContext,
            getSharedPreferences("osmdroid", Context.MODE_PRIVATE)
        )
        Configuration.getInstance().userAgentValue = "$packageName (GPSimulator Android app)"
        
        locationUtils = LocationUtils(this)
        sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
        rotationSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR)
        NotificationHelper.createNotificationChannel(this)
        
        setupMap()
        setupUI()
        observeViewModel()
        updateSummaryPanel()
        updateDashboardMode(DashboardMode.SETUP)
        
        checkAndRequestLocationPermissions()
    }
    
    private fun setupMap() {
        mapView = binding.mapView
        // OpenStreetMap France provides an open-data, HTTPS raster tile service with no API key.
        // Use its three public hosts so a single unavailable host does not blank the map.
        mapView.setTileSource(
            XYTileSource(
                "OpenStreetMapFrance",
                0,
                20,
                256,
                ".png",
                arrayOf(
                    "https://a.tile.openstreetmap.fr/osmfr/",
                    "https://b.tile.openstreetmap.fr/osmfr/",
                    "https://c.tile.openstreetmap.fr/osmfr/"
                ),
                "© OpenStreetMap contributors, tiles by OpenStreetMap France"
            )
        )
        // Request tiles at the screen density so the map remains sharp on high-DPI phones.
        mapView.setTilesScaledToDpi(true)
        mapView.setMultiTouchControls(true)
        
        mapController = mapView.controller
        mapController.setZoom(15.0)
        
        // Setup MapEventsOverlay
        val mapEventsReceiver = object : org.osmdroid.events.MapEventsReceiver {
            override fun singleTapConfirmedHelper(p: GeoPoint?): Boolean {
                p?.let { handleMapTap(it) }
                return true
            }

            override fun longPressHelper(p: GeoPoint?): Boolean {
                return false
            }
        }
        val mapEventsOverlay = org.osmdroid.views.overlay.MapEventsOverlay(mapEventsReceiver)
        mapView.overlays.add(0, mapEventsOverlay)
        
        // Get current location and center map
        getCurrentLocationAndCenter()
    }

    private var customPointsMarkers: MutableList<Marker> = mutableListOf()
    private var customRoutePolyline: Polyline? = null

    private fun handleMapTap(geoPoint: GeoPoint) {
        val routeType = viewModel.routeType.value
        if (routeType == RouteType.PINS || routeType == RouteType.DRAW) {
            val locationPoint = LocationPoint(geoPoint.latitude, geoPoint.longitude)
            viewModel.addCustomPoint(locationPoint)
            
            val marker = Marker(mapView).apply {
                position = geoPoint
                setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                title = "Point ${viewModel.customPoints.value.size}"
                icon = ContextCompat.getDrawable(this@MainActivity, android.R.drawable.ic_menu_myplaces)
            }
            mapView.overlays.add(marker)
            customPointsMarkers.add(marker)
            
            drawCustomPointsLine()
            mapView.invalidate()
        }
    }
    
    private fun drawCustomPointsLine() {
        val points = viewModel.customPoints.value
        
        customRoutePolyline?.let { mapView.overlays.remove(it) }
        
        val geoPoints = mutableListOf<GeoPoint>()
        viewModel.currentLocation.value?.let { 
           geoPoints.add(GeoPoint(it.latitude, it.longitude))
        }
        geoPoints.addAll(points.map { GeoPoint(it.latitude, it.longitude) })
        
        if (geoPoints.size < 2) return
        
        customRoutePolyline = Polyline().apply {
            setPoints(geoPoints)
            outlinePaint.color = ContextCompat.getColor(this@MainActivity, android.R.color.holo_blue_dark)
            outlinePaint.strokeWidth = 6f
        }
        
        mapView.overlays.add(customRoutePolyline)
    }

    private fun clearCustomMapPoints() {
        viewModel.clearCustomPoints()
        customPointsMarkers.forEach { mapView.overlays.remove(it) }
        customPointsMarkers.clear()
        customRoutePolyline?.let { mapView.overlays.remove(it) }
        customRoutePolyline = null
        mapView.invalidate()
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
                hasConfirmedDistance = true
                updateSummaryPanel()
                updateDashboardMode(currentDashboardMode())
            }

            distanceNextButton.setOnClickListener {
                hasConfirmedDistance = true
                setupStep = SetupStep.PACE
                updateDashboardMode(currentDashboardMode())
            }
            
            // Pace selector
            paceSlider.addOnChangeListener { _, value, _ ->
                val paceSeconds = value.toInt()
                viewModel.setPaceConfig(paceSeconds)
                paceText.text = formatPace(paceSeconds)
                hasConfirmedPace = true
                updateSummaryPanel()
                updateDashboardMode(currentDashboardMode())
            }

            paceNextButton.setOnClickListener {
                hasConfirmedPace = true
                setupStep = SetupStep.ROUTE
                updateDashboardMode(currentDashboardMode())
            }
            
            // Route type selector
            routeTypeGroup.setOnCheckedChangeListener { _, checkedId ->
                val routeType = when (checkedId) {
                    R.id.routeRandom -> RouteType.RANDOM
                    R.id.routePins -> RouteType.PINS
                    R.id.routeDraw -> RouteType.DRAW
                    else -> RouteType.RANDOM
                }
                viewModel.setRouteType(routeType)
                clearCustomMapPoints() // Clear custom points when switching modes
                hasConfirmedRoute = true
                updateSummaryPanel()
                updateDashboardMode(currentDashboardMode())
            }

            routeNextButton.setOnClickListener {
                hasConfirmedRoute = true
                setupStep = SetupStep.SUMMARY
                updateDashboardMode(currentDashboardMode())
            }

            mapModeDoneButton.setOnClickListener {
                hasConfirmedRoute = true
                setupStep = SetupStep.SUMMARY
                updateDashboardMode(currentDashboardMode())
            }

            clearMapPointsButton.setOnClickListener {
                clearCustomMapPoints()
            }
            
            startButton.setOnClickListener { startSimulation() }
            
            pauseButton.setOnClickListener { pauseSimulation() }
            continueButton.setOnClickListener { resumeSimulation() }
            
            stopButton.setOnClickListener { stopSimulation() }
            pausedStopButton.setOnClickListener { stopSimulation() }
            
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

        binding.paceText.text = formatPace(viewModel.paceConfig.value.baseSeconds)
    }
    
    private fun observeViewModel() {
        lifecycleScope.launch {
            SimulationState.isSimulating.collectLatest { isSimulating ->
                updateUIForSimulationState(isSimulating, SimulationState.isPaused.value)
            }
        }
        
        lifecycleScope.launch {
            SimulationState.isPaused.collectLatest { isPaused ->
                updateUIForSimulationState(SimulationState.isSimulating.value, isPaused)
            }
        }
        
        lifecycleScope.launch {
            viewModel.currentLocation.collectLatest { location ->
                if (!SimulationState.isSimulating.value) {
                    location?.let { updateMapLocation(it) }
                }
            }
        }

        lifecycleScope.launch {
            SimulationState.currentLocation.collectLatest { location ->
                location?.let { updateMapLocation(it) }
            }
        }
        
        lifecycleScope.launch {
            SimulationState.progress.collectLatest { progress ->
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
    
    private fun updateUIForSimulationState(isSimulating: Boolean, isPaused: Boolean) {
        binding.progressBar.isIndeterminate = false
        if (!isSimulating) {
            binding.progressBar.progress = 0
            binding.progressText.text = "0%"
        }

        val mode = when {
            isSimulating && isPaused -> DashboardMode.PAUSED
            isSimulating -> DashboardMode.ACTIVE
            hasConfirmedPace -> currentDashboardMode()
            else -> DashboardMode.SETUP
        }
        updateDashboardMode(mode)
    }
    
    private fun startSimulation() {
        if (isGeneratingRoute) {
            Log.w(TAG, "Start ignored: route generation is already in progress")
            return
        }
        if (!locationUtils.hasLocationPermission()) {
            Log.w(TAG, "Start blocked: fine location permission is missing")
            Toast.makeText(this, "Location permission required", Toast.LENGTH_SHORT).show()
            return
        }
        
        if (!locationUtils.isLocationEnabled()) {
            Log.w(TAG, "Start blocked: phone location is disabled")
            Toast.makeText(this, "Please enable GPS", Toast.LENGTH_SHORT).show()
            return
        }
        
        val currentLocation = viewModel.currentLocation.value
        if (currentLocation == null) {
            Log.i(TAG, "Start deferred: requesting current location")
            Toast.makeText(this, "Getting current location...", Toast.LENGTH_SHORT).show()
            getCurrentLocationAndCenter()
            return
        }
        
        lifecycleScope.launch {
            try {
                isGeneratingRoute = true
                binding.startButton.isEnabled = false
                binding.startButton.text = "Preparing..."
                Log.i(TAG, "Generating ${viewModel.routeType.value} route")
                val route = viewModel.generateRoute(currentLocation)
                route?.let {
                    Log.i(TAG, "Route ready: distance=${it.totalDistance.toInt()}m, points=${it.getAllPoints().size}")
                    drawRouteOnMap(it)
                    startLocationSimulationService(it)
                } ?: run {
                    Log.w(TAG, "Route generation returned no route")
                    Toast.makeText(
                        this@MainActivity,
                        "Please add route points first",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            } catch (e: Exception) {
                Log.e(TAG, "Route generation failed", e)
                Toast.makeText(
                    this@MainActivity,
                    "Failed to generate route: ${e.message}",
                    Toast.LENGTH_LONG
                ).show()
            } finally {
                isGeneratingRoute = false
                binding.startButton.isEnabled = true
                binding.startButton.text = getString(R.string.start)
            }
        }
    }
    
    private fun stopSimulation() {
        Log.i(TAG, "Stop requested from the UI")
        val intent = Intent(this, LocationSimulationService::class.java).apply {
            action = LocationSimulationService.ACTION_STOP_SIMULATION
        }
        startService(intent)
        viewModel.stopSimulation()
        
        // Clear route from map
        routePolyline?.let { mapView.overlays.remove(it) }
        routePolyline = null
        clearCustomMapPoints()
        mapView.invalidate()
        setupStep = SetupStep.DISTANCE
        hasConfirmedDistance = false
        hasConfirmedPace = false
        hasConfirmedRoute = false
        updateDashboardMode(currentDashboardMode())
    }
    
    private fun pauseSimulation() {
        Log.i(TAG, "Pause requested from the UI")
        val intent = Intent(this, LocationSimulationService::class.java).apply {
            action = "pause_simulation"
        }
        startService(intent)
        viewModel.pauseSimulation()
    }
    
    private fun resumeSimulation() {
        Log.i(TAG, "Resume requested from the UI")
        val intent = Intent(this, LocationSimulationService::class.java).apply {
            action = "resume_simulation"
        }
        startService(intent)
        viewModel.resumeSimulation()
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
            val boundingBox = BoundingBox.fromGeoPoints(points)
            mapView.zoomToBoundingBox(boundingBox, false, 100)
        }
        
        mapView.invalidate()
    }
    
    private fun startLocationSimulationService(route: com.gpssimulator.data.model.Route) {
        Log.i(TAG, "Starting simulation service: routeId=${route.id}")
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
                    updateSummaryPanel()
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
        rotationSensor?.let {
            sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_UI)
        }
        bindToSimulationService()
    }
    
    override fun onPause() {
        super.onPause()
        mapView.onPause()
        sensorManager.unregisterListener(this)
        unbindFromSimulationService()
    }

    override fun onSensorChanged(event: SensorEvent?) {
        if (event?.sensor?.type != Sensor.TYPE_ROTATION_VECTOR) return

        val rotationMatrix = FloatArray(9)
        SensorManager.getRotationMatrixFromVector(rotationMatrix, event.values)
        val orientation = FloatArray(3)
        SensorManager.getOrientation(rotationMatrix, orientation)
        val rawHeading = (Math.toDegrees(orientation[0].toDouble()) + 360.0) % 360.0
        smoothedHeadingDegrees = smoothHeading(smoothedHeadingDegrees, rawHeading)
        viewModel.setHeadingDegrees(smoothedHeadingDegrees)
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) = Unit

    private fun smoothHeading(current: Double?, next: Double): Double {
        if (current == null) return next
        val delta = (((next - current) + 540.0) % 360.0) - 180.0
        return (current + delta * 0.2 + 360.0) % 360.0
    }

    private fun currentDashboardMode(): DashboardMode {
        return when {
            SimulationState.isSimulating.value && SimulationState.isPaused.value -> DashboardMode.PAUSED
            SimulationState.isSimulating.value -> DashboardMode.ACTIVE
            setupStep == SetupStep.SUMMARY && hasConfirmedDistance && hasConfirmedPace && hasConfirmedRoute -> DashboardMode.READY
            else -> DashboardMode.SETUP
        }
    }

    private fun updateDashboardMode(mode: DashboardMode) {
        binding.apply {
            val isSimulating = mode == DashboardMode.ACTIVE || mode == DashboardMode.PAUSED

            distanceGroup.isEnabled = !isSimulating
            distanceNextButton.isEnabled = !isSimulating
            paceSlider.isEnabled = !isSimulating
            paceNextButton.isEnabled = !isSimulating
            routeTypeGroup.isEnabled = !isSimulating
            routeNextButton.isEnabled = !isSimulating

            val showFullMapRouteMode = !isSimulating &&
                setupStep == SetupStep.ROUTE &&
                (viewModel.routeType.value == RouteType.PINS || viewModel.routeType.value == RouteType.DRAW)

            setSectionVisible(distanceSection, !isSimulating && setupStep == SetupStep.DISTANCE)
            setSectionVisible(paceSection, !isSimulating && setupStep == SetupStep.PACE)
            setSectionVisible(routeSection, !isSimulating && setupStep == SetupStep.ROUTE && !showFullMapRouteMode)
            setSectionVisible(summaryPanel, !isSimulating && setupStep == SetupStep.SUMMARY)
            setSectionVisible(mapModePanel, showFullMapRouteMode)

            setSectionVisible(startButton, mode == DashboardMode.READY)
            setSectionVisible(activeButtonRow, mode == DashboardMode.ACTIVE)
            setSectionVisible(pauseButtonLayout, mode == DashboardMode.PAUSED)
            setSectionVisible(progressContainer, isSimulating)
            setSectionVisible(controlPanel, isSimulating || !showFullMapRouteMode)

            if (showFullMapRouteMode) {
                mapModeTitle.text = if (viewModel.routeType.value == RouteType.PINS) "Pin Route" else "Draw Route"
                mapGuideText.text = if (viewModel.routeType.value == RouteType.PINS) {
                    "Tap the map to place pins, then press Review Setup."
                } else {
                    "Tap along the map to shape the route, then press Review Setup."
                }
            }

            panelTitle.text = when (mode) {
                DashboardMode.SETUP -> "Runner Control"
                DashboardMode.READY -> "Ready To Launch"
                DashboardMode.ACTIVE -> "Run In Progress"
                DashboardMode.PAUSED -> "Run Paused"
            }
            panelSubtitle.text = when (mode) {
                DashboardMode.SETUP -> "Choose distance, pace, then route type."
                DashboardMode.READY -> "Summary locked. Press start when ready."
                DashboardMode.ACTIVE -> "Pause or stop the active simulation."
                DashboardMode.PAUSED -> "Continue the run or stop it."
            }
        }
    }

    private fun updateSummaryPanel() {
        binding.summaryGoalValue.text = String.format(Locale.getDefault(), "%.1f KM", viewModel.distance.value / 1000.0)
        binding.summaryTargetValue.text = "${formatPace(viewModel.paceConfig.value.baseSeconds)} min/km"
        binding.summaryVarianceValue.text = getString(R.string.variance_dynamic)
        binding.summaryPathValue.text = when (viewModel.routeType.value) {
            RouteType.RANDOM -> "Random Generation"
            RouteType.PINS -> "Pinned Waypoints"
            RouteType.DRAW -> "Drawn Path"
        }
    }

    private fun formatPace(paceSeconds: Int): String {
        val m = paceSeconds / 60
        val s = paceSeconds % 60
        return String.format(Locale.getDefault(), "%02d:%02d", m, s)
    }

    private fun revealView(view: View, animate: Boolean = true) {
        if (view.visibility == View.VISIBLE) return
        view.visibility = View.VISIBLE
        if (animate) {
            view.alpha = 0f
            view.translationY = 24f
            view.animate()
                .alpha(1f)
                .translationY(0f)
                .setDuration(220L)
                .start()
        } else {
            view.alpha = 1f
            view.translationY = 0f
        }
    }

    private fun popIn(view: View) {
        view.scaleX = 0.92f
        view.scaleY = 0.92f
        view.alpha = 0f
        view.animate()
            .alpha(1f)
            .scaleX(1f)
            .scaleY(1f)
            .setDuration(180L)
            .start()
    }

    private fun setSectionVisible(view: View, visible: Boolean) {
        view.visibility = if (visible) View.VISIBLE else View.GONE
        if (visible) {
            view.alpha = 1f
            view.translationY = 0f
        }
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
        private const val TAG = "GPSimulatorUI"
        private const val REQUEST_LOCATION_PERMISSIONS = 100
        private const val REQUEST_BACKGROUND_LOCATION_PERMISSION = 101
    }
}
