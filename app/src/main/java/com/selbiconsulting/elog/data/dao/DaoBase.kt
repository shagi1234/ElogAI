package com.selbiconsulting.elog.data.dao

import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy.Companion.REPLACE
import androidx.room.Update


/*
 * Created by shagi on 03.04.2024 14:47
 */

interface DaoBase<E> {
    @Insert(onConflict = REPLACE)
    fun insert(entity: E): Long
    @Insert
    fun insert(entity: List<E>)

    @Update
    fun update(entity: E)

    @Update
    fun update(vararg entity: E)

    @Delete
    fun delete(entity: E)

    @Delete
    fun delete(vararg entity: E)
}