package com.junron.ctfbot.model.ctftime

import com.junron.ctfbot.util.ignoreExtraJson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.UnstableDefault
import kotlinx.serialization.builtins.list
import okhttp3.OkHttpClient
import okhttp3.Request
import ru.gildor.coroutines.okhttp.await

@UnstableDefault
object CTFTime {
    private val client = OkHttpClient.Builder().build()
    suspend fun fetchNextWeek(): List<CTF>? {
        val request = Request.Builder()
            .url("https://ctftime.org/api/v1/events/").build()
        val result = client.newCall(request).await()
        if (!result.isSuccessful || result.body == null) return null
        return withContext(Dispatchers.IO) {
            val stringResponse = result.body!!.string()
            ignoreExtraJson.parse(CTF.serializer().list, stringResponse)
        }
    }

    suspend fun fetchCTF(id: Int): CTF? {
        val request = Request.Builder()
            .url("https://ctftime.org/api/v1/events/$id/").build()
        val result = client.newCall(request).await()
        if (!result.isSuccessful || result.body == null) return null
        return withContext(Dispatchers.IO) {
            ignoreExtraJson.parse(CTF.serializer(), result.body!!.string())
        }
    }
}
