package com.junron.ctfbot.model.ctftime

import com.junron.ctfbot.model.Subscriber
import kotlinx.serialization.Serializable

@Serializable
class CTFSubscriber(
    override val timings: List<Long>,
    override val authorId: String,
    val ctf: CTF
) : Subscriber() {
    override val id: String
        get() = authorId + ctf.id
}
