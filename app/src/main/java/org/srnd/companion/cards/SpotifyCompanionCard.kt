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
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Log
import android.view.View
import android.view.animation.AnimationUtils
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import com.daimajia.androidanimations.library.Techniques
import com.daimajia.androidanimations.library.YoYo
import com.github.kittinunf.fuse.core.Cache
import com.github.kittinunf.fuse.core.Fuse
import com.github.kittinunf.fuse.core.fetch.get
import com.github.kittinunf.result.success
import org.srnd.companion.CompanionApplication
import org.srnd.companion.R
import java.net.URL

class SpotifyCompanionCard(private val context: Context) : CompanionCard() {
    override val layout: Int? = R.layout.card_spotify
    override fun getId(): String = "spotify_now_playing"

    override fun populateView(view: View) {
        val app = context.applicationContext as CompanionApplication
        val nowPlaying = app.getNowPlaying()!!.getJSONObject("now_playing")

        val trackTitle = view.findViewById<TextView>(R.id.track_title)
        val artistTitle = view.findViewById<TextView>(R.id.artist_title)

        val oldTitle = trackTitle.text
        val oldArtist = artistTitle.text
//
//        Log.d("SpotifyCard", "new track title ${nowPlaying.getString("track")}")
//        Log.d("SpotifyCard", "new artist title ${nowPlaying.getString("artist")}")

        val action = view.findViewById<Button>(R.id.spotify_action)
        action.setOnClickListener {
            val intent = Intent(Intent.ACTION_VIEW)
            intent.data = Uri.parse(nowPlaying.getString("link"))
            context.startActivity(intent)
        }

        val albumArt = view.findViewById<ImageView>(R.id.album_art)
//        albumArt.visibility = View.INVISIBLE

        Fuse.bytesCache.get(URL(nowPlaying.getJSONObject("album").getString("image"))) { result, _ ->
            result.success { bytes ->
                val bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
                albumArt.setImageBitmap(bitmap)

                if(oldTitle != nowPlaying.getString("track") && oldTitle != "Song Title") {
                    YoYo.with(Techniques.FadeOutRight)
                            .duration(100)
                            .onEnd {
                                trackTitle.text = nowPlaying.getString("track")
                                YoYo.with(Techniques.FadeInLeft)
                                        .duration(100)
                                        .playOn(trackTitle)
                            }
                            .playOn(trackTitle)
                } else {
                    trackTitle.text = nowPlaying.getString("track")
                }

                if(oldArtist != nowPlaying.getString("artist") && oldArtist != "Artist Name") {
                    YoYo.with(Techniques.FadeOutRight)
                            .duration(100)
                            .onEnd {
                                artistTitle.text = nowPlaying.getString("artist")
                                YoYo.with(Techniques.FadeInLeft)
                                        .duration(100)
                                        .playOn(artistTitle)
                            }
                            .playOn(artistTitle)
                } else {
                    artistTitle.text = nowPlaying.getString("artist")
                }

//                val fadeInAnim = AnimationUtils.loadAnimation(context, android.R.anim.fade_in)
//                if(type == Cache.Type.NOT_FOUND) albumArt.startAnimation(fadeInAnim)
//                albumArt.visibility = View.VISIBLE
            }
        }
    }
}