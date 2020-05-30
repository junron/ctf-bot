package com.junron.ctfbot.model

import com.junron.pyrobase.jsoncache.IndexableItem


abstract class Subscriber : IndexableItem {
    abstract val timings: List<Time>
    abstract val authorId: String
    override val id: String
        get() = authorId
}
