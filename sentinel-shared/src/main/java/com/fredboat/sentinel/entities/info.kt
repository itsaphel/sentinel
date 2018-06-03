package com.fredboat.sentinel.entities

// Additional info about entities, which may be useful in only a few places

data class RoleInfo(
        val id: Long,
        val colorRgb: Int,
        val isHoisted: Boolean,
        val isMentiontionable: Boolean,
        val isManaged: Boolean
)

data class RoleInfoRequest(val id: Long)