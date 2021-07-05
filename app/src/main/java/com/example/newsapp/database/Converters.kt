package com.example.newsapp.database

import androidx.room.TypeConverter
import com.example.newsapp.model.Source

class Converters {
    @TypeConverter
    fun fromSrc (source: Source) : String {
        return source.name
    }

    @TypeConverter
    fun toSrc (name: String) : Source {
        return  Source(name, name)
    }
}