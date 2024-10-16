package com.selbiconsulting.elog.data.model.request

import com.selbiconsulting.elog.data.model.dto.DtoDefect
import com.selbiconsulting.elog.data.model.entity.EntityDvir
import kotlinx.serialization.Serializable


@Serializable
data class RequestCreateDvir(
    var id: String? = null,
    var contactId: String? = null,
    var mechanicSignature: String? = null,
    var driverSignature: String? = null,
    var createdAt: String? = null,
    var trailerDefects: String? = null,
    var unitDefects: String? = null,
    var location: String? = null,
    var vehicle: String? = null,
    var trailer: String? = null,
    var status: String? = DvirStatus.WORKING.value,
    var odometer: String? = null,
    var localid: String? = null,
) {
    companion object {
            fun RequestCreateDvir.toEntityDvir(): EntityDvir =
            EntityDvir(
                id = this.id,
                localId = this.localid?.toLong() ?: 0,
                contactId = this.contactId,
                mechanicSignature = this.mechanicSignature,
                driverSignature = this.driverSignature,
                createdAt = this.createdAt ?: "",
                trailerDefects = trailerDefects?.split(",\\s*".toRegex())?.map { it.trim() }
                    ?.filter { it.isNotEmpty() }
                    ?.map { DtoDefect(name = it) },
                unitDefects = unitDefects?.split(",\\s*".toRegex())?.map { it.trim() }
                    ?.filter { it.isNotEmpty() }
                    ?.map { DtoDefect(it) },
                location = this.location,
                vehicle = this.vehicle,
                trailer = this.trailer,
                odometer = this.odometer,
                status = this.status
            )
    }
}

enum class DvirStatus(val value: String) {
    WORKING("Working"),
    FIXED("Fixed"),
}