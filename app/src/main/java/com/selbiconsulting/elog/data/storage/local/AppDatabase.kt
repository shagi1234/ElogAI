package com.selbiconsulting.elog.data.storage.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.selbiconsulting.elog.data.dao.DaoDriver
import com.selbiconsulting.elog.data.dao.DaoDvir
import com.selbiconsulting.elog.data.dao.DaoLocation
import com.selbiconsulting.elog.data.dao.DaoLogs
import com.selbiconsulting.elog.data.dao.DaoMessage
import com.selbiconsulting.elog.data.mapper.Convertors
import com.selbiconsulting.elog.data.model.entity.EntityDriver
import com.selbiconsulting.elog.data.model.entity.EntityDvir
import com.selbiconsulting.elog.data.model.entity.EntityLog
import com.selbiconsulting.elog.data.model.entity.EntityLocation
import com.selbiconsulting.elog.data.model.entity.EntityMessage


/*
 * Created by shagi on 04.04.2024 00:26
 */

@Database(
    entities = [EntityLog::class, EntityLocation::class, EntityMessage::class, EntityDriver::class, EntityDvir::class],
    version = 1,
    exportSchema = false
)
@TypeConverters(Convertors::class)
abstract class AppDatabase : RoomDatabase() {

    abstract fun locationDao(): DaoLocation
    abstract fun messageDao(): DaoMessage

    abstract fun logsDao(): DaoLogs
    abstract fun driverDao(): DaoDriver
    abstract fun dvirDao(): DaoDvir


}