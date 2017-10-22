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

package org.srnd.companion.dayof

import android.net.Uri
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.widget.RelativeLayout
import android.widget.VideoView

import org.srnd.companion.R

class CheckInActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_check_in)

//        val layout = findViewById<RelativeLayout>(R.id.linearLayout)
//        val backgroundVideo = findViewById<VideoView>(R.id.background_video)
//        val uri = Uri.parse("android.resource://${packageName}/${R.raw.checkin}")
//
//        // backgroundVideo.minimumHeight = layout.height
//        // backgroundVideo.minimumWidth = layout.width
//
//        backgroundVideo.setVideoURI(uri)
//        backgroundVideo.start()
//
//        backgroundVideo.setOnCompletionListener {
//            backgroundVideo.start()
//        }
    }
}
