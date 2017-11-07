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
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.view.View
import android.widget.*
import com.daimajia.androidanimations.library.Techniques
import com.daimajia.androidanimations.library.YoYo
import com.github.kittinunf.fuse.core.Cache
import com.github.kittinunf.fuse.core.Fuse
import com.github.kittinunf.fuse.core.fetch.get
import com.github.kittinunf.result.success
import com.segment.analytics.Analytics
import com.segment.analytics.Properties
import org.srnd.companion.CompanionApplication
import org.srnd.companion.R
import org.srnd.companion.models.Announcement
import org.srnd.companion.util.getCircularBitmap
import org.srnd.gosquared.util.BitmapUtils
import java.net.URL

class AnnouncementCompanionCard(val context: Context, private val announcement: Announcement) : CompanionCard() {
    override val layout: Int? = R.layout.card_announcement

    override fun populateView(view: View) {
        val title = view.findViewById<TextView>(R.id.card_title)
        title.text = announcement.title

        val message = view.findViewById<TextView>(R.id.card_message)
        message.text = announcement.message

        val authorSection = view.findViewById<LinearLayout>(R.id.author)
        val authorImage = view.findViewById<ImageView>(R.id.author_pic)
        val authorName = view.findViewById<TextView>(R.id.author_name)
        val image = view.findViewById<ImageView>(R.id.card_image)
        val divider = view.findViewById<View>(R.id.card_divider)
        val actions = view.findViewById<RelativeLayout>(R.id.card_actions)
        val action = view.findViewById<Button>(R.id.card_action_1)

        if(announcement.linkText != null && announcement.linkUri != null) {
            divider.visibility = View.VISIBLE
            actions.visibility = View.VISIBLE
            action.text = announcement.linkText
            action.setOnClickListener {
                val properties = Properties()
                        .putValue("announcementId", announcement.clearId)
                        .putValue("cardType", "announcement")
                        .putUrl(announcement.linkUri)
                        .putTitle(announcement.linkText)

                Analytics.with(context).track("Tapped card action", properties)

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

        if(announcement.authorUsername != null && announcement.authorName != null) {
            authorSection.visibility = View.VISIBLE
            title.visibility = View.GONE
            authorName.text = announcement.authorName

            Fuse.bytesCache.get(URL("https://s5.studentrnd.org/photo/${announcement.authorUsername}_128.png")) { result, type ->
                result.success {
                    val bitmap = BitmapFactory.decodeByteArray(it, 0, it.size)
                    authorImage.setImageBitmap(bitmap.getCircularBitmap())

                    if(type == Cache.Type.NOT_FOUND) {
                        authorImage.visibility = View.INVISIBLE
                        YoYo.with(Techniques.FadeIn)
                                .duration(500)
                                .playOn(authorImage)
                        authorImage.visibility = View.VISIBLE
                    }
                }
            }
        } else {
            authorSection.visibility = View.GONE
            title.visibility = View.VISIBLE
        }
    }

    override fun getId(): String = announcement.clearId
}