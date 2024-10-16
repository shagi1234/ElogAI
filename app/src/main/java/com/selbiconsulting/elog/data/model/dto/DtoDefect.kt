package com.selbiconsulting.elog.data.model.dto

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import kotlinx.serialization.Serializable

@Parcelize
@Serializable
data class DtoDefect (
    val name:String,
):Parcelable

