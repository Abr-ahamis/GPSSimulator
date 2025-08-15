package com.gpssimulator.ui.history

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.gpssimulator.GPSimulatorApp
import com.gpssimulator.data.database.RouteEntity
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class HistoryViewModel(application: Application) : AndroidViewModel(application) {
    
    private val app = application as GPSimulatorApp
    private val _routeHistory = MutableStateFlow<List<RouteEntity>>(emptyList())
    val routeHistory: StateFlow<List<RouteEntity>> = _routeHistory.asStateFlow()
    
    init {
        loadRouteHistory()
    }
    
    private fun loadRouteHistory() {
        viewModelScope.launch {
            app.routeRepository.allRoutes.collectLatest { routes ->
                _routeHistory.value = routes
            }
        }
    }
    
    fun clearCompletedRoutes() {
        viewModelScope.launch {
            app.routeRepository.deleteCompletedRoutes()
        }
    }
}
