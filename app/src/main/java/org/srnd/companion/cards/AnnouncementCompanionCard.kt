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

package org.srnd.companion.cards

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.TextView
import org.srnd.companion.R
import org.srnd.companion.models.Announcement

class AnnouncementCompanionCard(val context: Context, val announcement: Announcement) : CompanionCard() {
    override val layout: Int? = R.layout.card_announcement

    override fun populateView(view: View) {
        val title = view.findViewById<TextView>(R.id.card_title)
        title.text = announcement.title

        val message = view.findViewById<TextView>(R.id.card_message)
        message.text = announcement.message

        val image = view.findViewById<ImageView>(R.id.card_image)
        val divider = view.findViewById<View>(R.id.card_divider)
        val actions = view.findViewById<RelativeLayout>(R.id.card_actions)
        val action = view.findViewById<Button>(R.id.card_action_1)

        if(announcement.linkText != null && announcement.linkUri != null) {
            divider.visibility = View.VISIBLE
            actions.visibility = View.VISIBLE
            action.text = announcement.linkText
            action.setOnClickListener {
                val intent = Intent(Intent.ACTION_VIEW)
                intent.data = Uri.parse(announcement.linkUri)
                context.startActivity(intent)
            }
        } else {
            divider.visibility = View.GONE
            actions.visibility = View.GONE
        }

        if(announcement.imageResource != null) {
            divider.visibility = View.GONE
            image.visibility = View.VISIBLE
            image.setImageDrawable(announcement.imageResource)
        } else {
            if(announcement.linkText != null && announcement.linkUri != null) divider.visibility = View.VISIBLE
            image.visibility = View.GONE
        }
    }
}