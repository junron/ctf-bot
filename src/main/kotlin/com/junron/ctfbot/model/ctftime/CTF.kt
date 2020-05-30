package com.junron.ctfbot.model.ctftime

import com.jessecorbett.diskord.dsl.author
import com.jessecorbett.diskord.dsl.embed
import com.jessecorbett.diskord.dsl.field
import com.jessecorbett.diskord.dsl.footer
import com.jessecorbett.diskord.util.Colors
import com.junron.ctfbot.util.displayCTFDate
import com.junron.ctfbot.util.toLocalDateTime
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient

@Serializable
data class CTF(
    val organizers: List<Organizer>,
    val onsite: Boolean,
    val start: String,
    val finish: String,
    val description: String,
    val title: String,
    val url: String,
    val restrictions: String,
    @SerialName("format_id")
    val formatId: Int,
    val logo: String,
    val location: String,
    val id: Int
) {
    @Transient
    val startTime = start.toLocalDateTime()

    @Transient
    val endTime = finish.toLocalDateTime()
    fun embed() = embed {
        println(startTime)
        println(start)
        title = this@CTF.title
        color = Colors.hex(0x1e88e5)
        description = this@CTF.description
        author("${this@CTF.title} (${getFormat()})") {
            authorImageUrl = logo
        }
        field("URL", this@CTF.url, false)
        field("Timing", getTimings()+"""
            
            React :alarm_clock: to be reminded 1 hour before the event.
        """.trimIndent(), false)
        field("CTFTime ID", id.toString(), false)
        footer("Hosted by ${this@CTF.organizers.joinToString(", ") {
            it.name
        }
        }")
    }

    private fun getFormat() = when (formatId) {
        1 -> "Jeopardy"
        2 -> "Attack-Defence"
        else -> "Unknown"
    }

    private fun getTimings() =
        "${startTime.displayCTFDate()} - ${endTime.displayCTFDate()}"
}
