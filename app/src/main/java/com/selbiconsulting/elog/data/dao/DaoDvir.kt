package com.selbiconsulting.elog.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Upsert
import com.selbiconsulting.elog.data.model.entity.EntityDvir
import kotlinx.coroutines.flow.Flow

@Dao
interface DaoDvir : DaoBase<EntityDvir> {
    @Query(
        "SELECT * " +
                "FROM dvir " +
                "ORDER BY localId DESC "
    )
    fun getAllDvir(): List<EntityDvir>

    @Query("SELECT * FROM dvir WHERE id = :id LIMIT 1")
    suspend fun getDvirByRemoteId(id: String): EntityDvir?

    @Query(
        "DELETE FROM dvir " +
                "WHERE localId = :localId"
    )
    fun deleteDvirByLocalId(localId: Long)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdate(dvir: EntityDvir)
}