/*
 *     Copyright (C) 2017 srnd.org
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package org.srnd.gosquared.chat

import android.util.Log
import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import org.java_websocket.client.WebSocketClient
import org.java_websocket.handshake.ServerHandshake
import org.json.JSONObject
import org.srnd.gosquared.GoSquared
import org.srnd.gosquared.models.Message
import java.lang.Exception
import java.net.URI
import java.util.*
import kotlin.concurrent.fixedRateTimer
import kotlin.concurrent.timer
import kotlin.concurrent.timerTask

class GoSquaredClient(serverUri: URI?) : WebSocketClient(serverUri) {
    // keeps track of message sequence
    var seq: Int = 1
    private var pinger: Timer? = null
    private val mapper = jacksonObjectMapper()

    // listeners
    var chatMessageListener: ((message: Message) -> Unit?)? = null
    var typingListener: (() -> Unit)? = null
    var disconnectListener: ((code: Int, reason: String?, remote: Boolean) -> Unit)? = null

    override fun onOpen(handshakedata: ServerHandshake?) {
        pinger = timer(period = 30000L) {
            val ping = JSONObject()

            ping.put("type", "ping")
            ping.put("id", seq)

            send(ping.toString())
        }
    }

    override fun onClose(code: Int, reason: String?, remote: Boolean) {
        Log.d(GoSquared.TAG, "WSS Connection Closed, code: $code, reason: $reason, remote? $remote")
        if(disconnectListener != null) disconnectListener!!.invoke(code, reason, remote)
        pinger!!.cancel()
    }

    override fun onMessage(message: String?) {
        Log.d(GoSquared.TAG, "WSS Downstream: " + message!!)
        val msg = JSONObject(message)

        when(msg.getString("type")) {
            "message" -> {
                val res = JSONObject()

                res.put("timestamp", msg.getLong("timestamp"))
                res.put("type", "delivered")

                send(res.toString())

                val deserialized = mapper.readValue<Message>(message)
                if(chatMessageListener != null) chatMessageListener!!.invoke(deserialized)
            }

            "typing" -> {
                if(msg.getString("by") == "agent" && typingListener != null) {
                    typingListener!!.invoke()
                }
            }
        }
    }

    override fun onError(ex: Exception?) {

    }

    override fun send(text: String?) {
        Log.d(GoSquared.TAG, "WSS Upstream: " + text!!)
        seq++
        super.send(text)
    }

    fun sendMessage(content: String, session: GoSquaredSession) {
        val message = JSONObject()

        message.put("type", "message")
        message.put("id", seq)
        message.put("content", content)
        message.put("session", JSONObject(mapOf(
                "title" to session.title!!,
                "href" to session.href!!
        )))

        send(message.toString())
    }
}