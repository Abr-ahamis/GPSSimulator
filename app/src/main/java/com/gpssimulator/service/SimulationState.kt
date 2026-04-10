package com.gpssimulator.service

import com.gpssimulator.data.model.LocationPoint
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

object SimulationState {
    private val _isSimulating = MutableStateFlow(false)
    val isSimulating: StateFlow<Boolean> = _isSimulating.asStateFlow()

    private val _isPaused = MutableStateFlow(false)
    val isPaused: StateFlow<Boolean> = _isPaused.asStateFlow()

    private val _currentLocation = MutableStateFlow<LocationPoint?>(null)
    val currentLocation: StateFlow<LocationPoint?> = _currentLocation.asStateFlow()

    private val _progress = MutableStateFlow(0f)
    val progress: StateFlow<Float> = _progress.asStateFlow()

    fun setSimulating(value: Boolean) {
        _isSimulating.value = value
    }

    fun setPaused(value: Boolean) {
        _isPaused.value = value
    }

    fun setCurrentLocation(value: LocationPoint?) {
        _currentLocation.value = value
    }

    fun setProgress(value: Float) {
        _progress.value = value
    }

    fun reset() {
        _isSimulating.value = false
        _isPaused.value = false
        _currentLocation.value = null
        _progress.value = 0f
    }
}
