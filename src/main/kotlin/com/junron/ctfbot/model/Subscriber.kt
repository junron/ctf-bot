package com.junron.ctfbot.model

import com.junron.pyrobase.jsoncache.IndexableItem
import kotlinx.serialization.Transient


abstract class Subscriber : IndexableItem {
    abstract val timings: List<Long>
    abstract val authorId: String

    @Transient
    override val id: String
        get() = authorId
}
