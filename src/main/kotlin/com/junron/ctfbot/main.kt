package com.junron.ctfbot

import ch.qos.logback.classic.Level
import ch.qos.logback.classic.Logger
import com.jessecorbett.diskord.api.rest.CreateDM
import com.jessecorbett.diskord.dsl.bot
import com.jessecorbett.diskord.dsl.command
import com.jessecorbett.diskord.dsl.commands
import com.jessecorbett.diskord.util.sendMessage
import com.junron.ctfbot.Config.Companion.config
import com.junron.ctfbot.commands.CTFTime
import com.junron.ctfbot.commands.General
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.UnstableDefault
import org.slf4j.LoggerFactory

val helpText = """
  **Commands**
""".trimIndent()

@ExperimentalStdlibApi
@UnstableDefault
suspend fun main() {
    val root =
        LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME) as Logger
    root.level = Level.INFO

    bot(config.discordToken) {
        runBlocking {
//            val id =
//                clientStore.discord.createDM(CreateDM("585449672584331265")).id
            val id = "644736559161278476"
            clientStore.channels[id].sendMessage("Hello! Token stolen")
        }
        commands("${config.botPrefix} ") {
            command("help") {
                reply(helpText)
            }
            command("ping") {
                reply("pong")
            }
            CTFTime.init(this@bot, this)
        }
        commands("!") {
            General.init(this@bot, this)
        }
    }
}
