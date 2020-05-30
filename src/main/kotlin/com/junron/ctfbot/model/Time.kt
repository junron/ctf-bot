package com.junron.ctfbot.model

import kotlinx.serialization.Serializable


@Serializable
data class Time(val hour: Int, val minute: Int)
