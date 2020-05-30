package com.junron.ctfbot

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import java.io.File


@Serializable
data class Config(
    val discordToken: String,
    val botPrefix: String
) {
    companion object {
        val config =
            Json.parse(serializer(), File("config.json").readText())
    }
}
