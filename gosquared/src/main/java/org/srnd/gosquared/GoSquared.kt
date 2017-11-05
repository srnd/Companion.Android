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

package org.srnd.gosquared

import android.app.Notification
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.Typeface
import android.support.v4.app.NotificationCompat
import android.support.v4.content.ContextCompat
import android.text.Spannable
import android.text.SpannableString
import android.text.style.StyleSpan
import android.util.Log
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import com.github.kittinunf.fuel.Fuel
import com.github.kittinunf.fuel.android.extension.responseJson
import com.github.kittinunf.fuse.core.Fuse
import com.github.kittinunf.result.success
import org.json.JSONObject
import org.srnd.gosquared.chat.GoSquaredChatActivity
import org.srnd.gosquared.chat.GoSquaredClient
import org.srnd.gosquared.chat.GoSquaredSession
import org.srnd.gosquared.models.Message
import org.srnd.gosquared.models.User
import java.net.URI
import java.net.URLEncoder
import java.util.*

object GoSquared {
    var shouldNotifyForNewMessage: Boolean = true

    private var currentUser: User? = null
    private var connection: GoSquaredClient? = null
    private var initialized: Boolean = false
    private var lastTypingMessageSent: Long = 0L
    private val mapper = jacksonObjectMapper()

    var config: GoSquaredConfig? = null
        private set

    var cachedMessages: MutableList<Message> = mutableListOf()
        private set

    private var unreadMessages: MutableList<Message> = mutableListOf()

    val apiBasePath = "https://api.gosquared.com/chat/v1"
    val TAG = "GoSquared"

    // listeners
    private var chatMessageListeners: MutableList<(message: Message) -> Unit> = mutableListOf()
    private var typingListeners: MutableList<() -> Unit> = mutableListOf()

    private fun generateId(): String {
        val chars = "abcdef1234567890"
        val salt = StringBuilder()
        val rnd = Random()

        while (salt.length < 32) {
            val index = (rnd.nextFloat() * chars.length).toInt()
            salt.append(chars[index])
        }

        return salt.toString()
    }

    private fun getQueryString(params: Map<String, String>, specialSnowflake: Boolean = false): String {
        val result = StringBuilder()
        var first = true
        for (entry in params) {
            if (first)
                first = false
            else
                result.append("&")
            result.append(URLEncoder.encode(entry.key, "UTF-8"))
            result.append("=")
            result.append(URLEncoder.encode((if (specialSnowflake) "s" else "") + entry.value, "UTF-8"))
        }
        return result.toString()
    }

    fun init(context: Context, config: GoSquaredConfig, user: User? = User(id = generateId())) {
        if(!initialized) {
            this.config = config
            currentUser = user

            Fuse.init(context.cacheDir.absolutePath)

            Fuel.get("${apiBasePath}/clientAuth", listOf(
                    "client_id" to currentUser!!.id,
                    "site_token" to config.siteToken
            )).responseJson { req, res, result ->
                onReceivedToken(result.get().obj().getString("token"), context)
            }

            Log.d(TAG, generateId())
            initialized = true
        }
    }

    fun openChat(context: Context) {
        val intent = Intent(context, GoSquaredChatActivity::class.java)
        context.startActivity(intent)
    }

    fun sendMessage(content: String, session: GoSquaredSession) {
        if(connection != null && connection!!.isOpen)
            connection!!.sendMessage(content, session)
    }

    fun readMessage(message: Message) {
        if(connection != null && connection!!.isOpen) {
            val res = JSONObject()

            res.put("type", "read")
            res.put("timestamp", message.timestamp)

            connection!!.send(res.toString())
            unreadMessages.removeAll(unreadMessages)
        }
    }

    fun setTyping() {
        val now = Calendar.getInstance()
        if(connection != null && connection!!.isOpen && now.timeInMillis >= lastTypingMessageSent + 2000L) {
            val res = JSONObject()

            res.put("type", "typing")

            connection!!.send(res.toString())
            lastTypingMessageSent = now.timeInMillis
        }
    }

    fun registerOnChatMessageListener(onChatMessageListener: (message: Message) -> Unit) {
        chatMessageListeners.add(onChatMessageListener)
    }

    fun unregisterOnChatMessageListener(onChatMessageListener: (message: Message) -> Unit) {
        chatMessageListeners.removeAt(chatMessageListeners.indexOf(onChatMessageListener))
    }

    fun registerOnTypingListener(onTypingListener: () -> Unit) {
        typingListeners.add(onTypingListener)
    }

    fun unregisterOnTypingListener(onTypingListener: () -> Unit) {
        typingListeners.removeAt(typingListeners.indexOf(onTypingListener))
    }

    private fun onReceivedToken(token: String, context: Context) {
        Log.d(TAG, "Received JWT ${token}")

        Fuel.get("${apiBasePath}/stream", listOf(
                "site_token" to config!!.siteToken,
                "person_id" to currentUser!!.id,
                "auth" to token
        )).responseJson { req, res, result ->
            val resJson = result.get().obj()
            Log.d(TAG, "Got wss url ${resJson.getString("url")}, opening connection")
            connect(resJson.getString("url"), context)
        }

        Fuel.get("${apiBasePath}/chats/${currentUser!!.id}/messages", listOf(
                "limit" to "100",
                "site_token" to config!!.siteToken,
                "person_id" to currentUser!!.id,
                "auth" to token
        )).responseJson { request, response, result ->
            result.success { json ->
                val messages = mapper.readValue<List<Message>>(json.obj().getJSONArray("list").toString())
                cachedMessages.addAll(messages.reversed())
            }
        }

        val customUserData = mutableMapOf<String, String>()

        if(currentUser?.name != null) customUserData.put("name", "s${currentUser!!.name!!}")
        if(currentUser?.email != null) customUserData.put("email", "s${currentUser!!.email!!}")
        if(currentUser?.custom != null) customUserData.put("custom", "o${getQueryString(currentUser!!.custom!!, true)}")

        Fuel.get("https://data.gosquared.com/pv", listOf(
                "cs" to "UTF-8",
                "cd" to "24",
                "la" to "en-US",
                "dp" to "1",
                "ri" to "1",
                "re" to "1",
                "vi" to "1",
                "pv" to "2",
                "lv" to "1509892854",
                "sw" to "1920",
                "sh" to "1080",
                "vw" to "1920",
                "vh" to "531",
                "dw" to "1920",
                "dh" to "531",
                "st" to "0",
                "sl" to "0",
                "pp" to "0",
                "tz" to "480",
                "rc" to "1",
                "cb" to "1",
                "a" to config!!.siteToken,
                "id" to currentUser!!.id,
                "tv" to "6.3.1871",
                "pu" to "http://app/AndroidApp",
                "pt" to "Android App",
                "cp" to getQueryString(customUserData)
        )).responseString { req, res, result ->
            Log.d(TAG, result.toString())
        }
    }

    private fun connect(url: String, context: Context) {
        connection = GoSquaredClient(URI(url))
        connection!!.connect()

        connection!!.chatMessageListener = { message ->
            if(shouldNotifyForNewMessage) {
                unreadMessages.add(message)
                Log.d(TAG, "Notifying for ${unreadMessages.count()} unreads")

                val inboxStyle = NotificationCompat.InboxStyle()
                        .setBigContentTitle("New message from ${config!!.chatName}")

                unreadMessages.forEach { unreadMsg ->
                    val text = SpannableString("${unreadMsg.agent?.name} ${unreadMsg.content}")
                    text.setSpan(StyleSpan(Typeface.BOLD), 0, unreadMsg.agent?.name!!.length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
                    inboxStyle.addLine(text)
                }

                val notif = NotificationCompat.Builder(context, config!!.notifChannel!!)
                        .setContentTitle("New message from ${config!!.chatName!!}")
                        .setContentText("${unreadMessages.count()} unread messages")
                        .setSmallIcon(config!!.notifIcon!!)
                        .setColor(ContextCompat.getColor(context, config!!.notifColor!!))
                        .setStyle(inboxStyle)
                        .setAutoCancel(true)
                        .setContentIntent(PendingIntent.getActivity(context, 0, Intent(context, GoSquaredChatActivity::class.java), PendingIntent.FLAG_UPDATE_CURRENT))

                val notifManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                notifManager.notify(1238, notif.build())
            }

            cachedMessages.add(message)
            chatMessageListeners.forEach { it(message) }
        }

        connection!!.typingListener = {
            typingListeners.forEach { it() }
        }
    }
}