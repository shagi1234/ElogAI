package com.selbiconsulting.elog.data.mapper

import androidx.room.TypeConverter
import com.google.common.reflect.TypeToken
import com.google.gson.Gson
import com.selbiconsulting.elog.data.model.dto.DtoDefect


/*
 * Created by shagi on 04.04.2024 00:27
 */

class Convertors {
    @TypeConverter
    fun fromDefectList(value: List<DtoDefect>?): String? {
        val gson = Gson()
        return gson.toJson(value)
    }

    @TypeConverter
    fun toDefectList(value: String?): List<DtoDefect>? {
        val listType = object : TypeToken<List<DtoDefect>>() {}.type
        return Gson().fromJson(value, listType)
    }
}