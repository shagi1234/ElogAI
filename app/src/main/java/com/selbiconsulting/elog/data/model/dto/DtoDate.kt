package com.selbiconsulting.elog.data.model.dto

data class DtoDate(
    val dayOfWeek: String = "",
    val day: String = "",
    val formattedDate: String = "",//yyyy-MM-dd
    var isSelected: Boolean? = false,
    var isCertified: Boolean = false,
    var position: Int = 0
)
