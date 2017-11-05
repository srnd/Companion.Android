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

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.widget.Button
import android.widget.EditText
import org.srnd.gosquared.GoSquared
import org.srnd.gosquared.R
import org.srnd.gosquared.chat.adapters.GoSquaredChatAdapter
import org.srnd.gosquared.models.Message

class GoSquaredChatActivity : AppCompatActivity() {
    private var messages: MutableList<Message> = mutableListOf()
    private var recycler: RecyclerView? = null
    private var adapter: GoSquaredChatAdapter? = null

    private val messageListener: (message: Message) -> Unit = { message ->
        Log.d(GoSquared.TAG, message.toString())

        GoSquared.readMessage(message)

        runOnUiThread {
            refresh()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_go_squared_chat)

        title = getString(R.string.live_chat_title)

        messages = GoSquared.cachedMessages
        recycler = findViewById<RecyclerView>(R.id.recycler)

        val layoutManager = LinearLayoutManager(this)
        layoutManager.orientation = LinearLayoutManager.VERTICAL
        layoutManager.stackFromEnd = true
        recycler!!.layoutManager = layoutManager
        recycler!!.setHasFixedSize(true)

        adapter = GoSquaredChatAdapter(messages)
        recycler!!.adapter = adapter

        val messageField = findViewById<EditText>(R.id.message_field)
        messageField.addTextChangedListener(object : TextWatcher {
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                GoSquared.setTyping()
            }

            override fun afterTextChanged(s: Editable?) { }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) { }
        })

        val sendButton = findViewById<Button>(R.id.message_send)
        sendButton.setOnClickListener {
            GoSquared.sendMessage(messageField.text.toString(), GoSquaredSession(
                    title = "App",
                    href = "http://app/AndroidApp"
            ))
            messageField.setText("")
        }
    }

    override fun onPause() {
        super.onPause()
        GoSquared.unregisterOnChatMessageListener(messageListener)
        GoSquared.shouldNotifyForNewMessage = true
    }

    override fun onResume() {
        super.onResume()
        GoSquared.registerOnChatMessageListener(messageListener)
        GoSquared.shouldNotifyForNewMessage = false

        refresh()
        if(messages.isNotEmpty()) GoSquared.readMessage(messages.last())
    }

    private fun refresh() {
        adapter!!.messages = messages
        adapter!!.notifyDataSetChanged()
        recycler!!.scrollToPosition(messages.count() - 1)
    }
}
