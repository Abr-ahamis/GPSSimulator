package com.gpssimulator

import android.app.Application
import com.gpssimulator.data.database.AppDatabase
import com.gpssimulator.data.repository.RouteRepository

class GPSimulatorApp : Application() {
    val database by lazy { AppDatabase.getDatabase(this) }
    val routeRepository by lazy { RouteRepository(database.routeDao()) }
}
