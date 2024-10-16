package com.selbiconsulting.elog.data.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Transaction
import com.selbiconsulting.elog.data.model.entity.EntityDriver
import com.selbiconsulting.elog.data.model.entity.EntityLog
import kotlinx.coroutines.flow.Flow

@Dao
interface DaoDriver : DaoBase<EntityDriver> {
    @Query("SELECT * FROM drivers WHERE id = :id LIMIT 1")
    fun getDriverById(id: String): EntityDriver?

    @Transaction
    fun upsertDriver(driver: EntityDriver): String {
        val existingDriver = getDriverById(driver.id)

        return if (existingDriver == null) {
            insert(driver)

            driver.id
        } else {
            val updatedDriver = driver.copy(localId = existingDriver.localId)
            update(updatedDriver)

            updatedDriver.id
        }

    }
}