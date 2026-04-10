package com.gpssimulator.data.database

import androidx.room.TypeConverter
import com.gpssimulator.data.model.LocationPoint
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.util.Date

class Converters {
    private val gson = Gson()

    @TypeConverter
    fun fromLocationPointList(points: List<LocationPoint>?): String? {
        return gson.toJson(points)
    }

    @TypeConverter
    fun toLocationPointList(pointsString: String?): List<LocationPoint> {
        if (pointsString.isNullOrEmpty()) return emptyList()
        val type = object : TypeToken<List<LocationPoint>>() {}.type
        return gson.fromJson(pointsString, type)
    }

    @TypeConverter
    fun fromTimestamp(date: Date?): Long? {
        return date?.time
    }
    
    @TypeConverter
    fun toTimestamp(timestamp: Long?): Date? {
        return timestamp?.let { Date(it) }
    }
}