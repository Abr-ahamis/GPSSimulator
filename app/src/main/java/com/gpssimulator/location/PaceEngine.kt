package com.gpssimulator.location

class PaceEngine(private val baseSeconds: Int) {
    fun randomPace(): Int {
        val min = baseSeconds - 32
        val max = baseSeconds + 32
        return (min..max).random()
    }

    fun paceToSpeed(paceSeconds: Int): Float {
        // speed = meters / seconds (1000m per km)
        return 1000f / paceSeconds
    }
}
