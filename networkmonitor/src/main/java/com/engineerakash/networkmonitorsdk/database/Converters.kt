package com.engineerakash.networkmonitorsdk.database

import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

/**
 * Type converters for Room database
 * Converts complex types (Map, List) to/from JSON strings
 */
class Converters {
    
    private val gson = Gson()
    
    @TypeConverter
    fun fromStringMap(value: String): Map<String, String> {
        val mapType = object : TypeToken<Map<String, String>>() {}.type
        return gson.fromJson(value, mapType) ?: emptyMap()
    }
    
    @TypeConverter
    fun toStringMap(map: Map<String, String>): String {
        return gson.toJson(map)
    }
}

