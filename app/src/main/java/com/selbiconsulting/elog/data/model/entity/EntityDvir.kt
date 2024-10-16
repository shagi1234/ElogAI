package com.selbiconsulting.elog.data.model.entity

import android.os.Parcelable
import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.versionedparcelable.ParcelField
import com.selbiconsulting.elog.data.model.dto.DtoDefect
import com.selbiconsulting.elog.data.model.request.DvirStatus
import com.selbiconsulting.elog.data.model.request.RequestCreateDvir
import kotlinx.parcelize.Parcelize
import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable

@Parcelize
@Serializable
@Entity(tableName = "dvir")
data class EntityDvir(
    @PrimaryKey(autoGenerate = true)
    var localId: Long? = null,
    var id: String? = null,
    var contactId: String? = "",
    var dvirStatus: String? = "Working",
    var vehicle: String? = "",
    var trailer: String? = "",
    var location: String? = "",
    var odometer: String? = "",
    var createdAt: String = "",
    var unitDefects: List<DtoDefect>? = emptyList(),
    var trailerDefects: List<DtoDefect>? = emptyList(),
    var driverSignature: String? = "",
    var mechanicSignature: String? = "",
    var status : String? = DvirStatus.WORKING.value
) : Parcelable {
    companion object {
        fun EntityDvir.toRequestCreateDvir(): RequestCreateDvir {
            return RequestCreateDvir(
                id = this.id,
                contactId = this.contactId ?: "",
                mechanicSignature = this.mechanicSignature,
                driverSignature = this.driverSignature,
                createdAt = this.createdAt,
                trailerDefects = this.trailerDefects?.joinToString { it.name },
                unitDefects = this.unitDefects?.joinToString { it.name },
                location = this.location,
                vehicle = this.vehicle,
                trailer = this.trailer,
                status = this.dvirStatus,
                odometer = this.odometer,
                localid = this.localId?.toString()
            )
        }
    }
}