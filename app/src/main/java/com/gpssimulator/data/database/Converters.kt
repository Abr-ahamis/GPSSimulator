package com.gpssimulator.data.database

import androidx.room.TypeConverter
import com.gpssimulator.data.model.MovementType
import java.util.Date

class Converters {
    @TypeConverter
    fun fromMovementType(movementType: MovementType): String {
        return movementType.name
    }
    
    @TypeConverter
    fun toMovementType(movementTypeName: String): MovementType {
        return MovementType.valueOf(movementTypeName)
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