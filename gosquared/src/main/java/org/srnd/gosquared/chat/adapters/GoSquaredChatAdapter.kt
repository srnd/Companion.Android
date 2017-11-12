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

package org.srnd.gosquared.chat.adapters

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.support.v7.widget.RecyclerView
import android.text.util.Linkify
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import com.github.kittinunf.fuse.core.Fuse
import com.github.kittinunf.fuse.core.fetch.get
import com.github.kittinunf.result.success
import org.srnd.gosquared.R
import org.srnd.gosquared.chat.holders.GoSquaredChatHolder
import org.srnd.gosquared.models.Agent
import org.srnd.gosquared.models.Message
import org.srnd.gosquared.util.BitmapUtils
import java.net.URL

class GoSquaredChatAdapter(var messages: List<Message>) : RecyclerView.Adapter<GoSquaredChatHolder>() {
    init {
        setHasStableIds(true)
    }

    override fun getItemId(position: Int): Long = messages[position].timestamp!!

    override fun getItemCount(): Int {
        return messages.count()
    }

    override fun onBindViewHolder(holder: GoSquaredChatHolder?, position: Int) {
        val view = holder!!.itemView
        val message = messages[position]

        val messageText = view.findViewById<TextView>(R.id.message_text)
        messageText.text = message.contentEmojified

        Linkify.addLinks(messageText, Linkify.ALL)

        val messageLayout = view.findViewById<LinearLayout>(R.id.message_layout)
        val messageAvatar = view.findViewById<ImageView>(R.id.message_avatar)
        val messageSender = view.findViewById<TextView>(R.id.message_sender)

        if(message.from == "client") {
            messageLayout.gravity = Gravity.END
            messageAvatar.visibility = View.GONE
        } else {
            messageLayout.gravity = Gravity.START
            messageAvatar.visibility = View.VISIBLE
        }

        var sender: Agent? = null
        if(message.agent != null) {
            sender = message.agent
        } else if(message.bot != null) {
            sender = message.bot
        }

        if(position != 0 && sender != null && messages[position - 1].agent != null && messages[position - 1].agent!!.id == sender.id) {
            messageAvatar.visibility = View.INVISIBLE
            messageSender.visibility = View.GONE
        } else if(sender != null && sender.avatar != null) {
            messageSender.visibility = View.VISIBLE
            messageSender.text = sender.name

            Fuse.bytesCache.get(URL(sender.avatar)) { result, _ ->
                result.success { bytes ->
                    val bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
                    val resized = Bitmap.createScaledBitmap(bitmap, 100, 100, false)
                    messageAvatar.setImageBitmap(BitmapUtils.getRoundedCornerBitmap(resized, 20))
                    messageAvatar.visibility = View.VISIBLE
                }
            }
        } else {
            messageAvatar.visibility = View.INVISIBLE
            messageSender.visibility = View.GONE
        }

    }

    override fun onCreateViewHolder(parent: ViewGroup?, viewType: Int): GoSquaredChatHolder {
        val view = LayoutInflater.from(parent!!.context).inflate(R.layout.chat_message, parent, false)
        return GoSquaredChatHolder(view)
    }
}