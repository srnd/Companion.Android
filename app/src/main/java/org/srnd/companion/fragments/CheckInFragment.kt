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

package org.srnd.companion.fragments

import android.graphics.Bitmap
import android.graphics.Color
import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.widget.ImageView
import android.widget.TextView
import com.google.zxing.BarcodeFormat
import com.google.zxing.EncodeHintType
import com.google.zxing.qrcode.QRCodeWriter
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.uiThread
import org.srnd.companion.CompanionApplication
import org.srnd.companion.R

class CheckInFragment : Fragment() {
    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        super.onCreateView(inflater, container, savedInstanceState)
        val view = inflater!!.inflate(R.layout.fragment_check_in, container, false)

        val user = (context.applicationContext as CompanionApplication).getUserData()

        val title = view.findViewById<TextView>(R.id.ticket_title)
        title.text = getString(R.string.your_ticket, user.getString("first_name"))

        val ticketType = view.findViewById<TextView>(R.id.ticket_type)
        ticketType.text = "${user.getString("type").substring(0, 1).toUpperCase()}${user.getString("type").substring(1)}"

        val eventName = view.findViewById<TextView>(R.id.event_name)
        eventName.text = user.getJSONObject("event").getString("name").replace("CodeDay ", "")

        val fadeInAnim = AnimationUtils.loadAnimation(context, android.R.anim.fade_in)

        val qrImage = view!!.findViewById<ImageView>(R.id.ticket_qr)
        qrImage.visibility = View.INVISIBLE

        doAsync {
            val qrWriter = QRCodeWriter()

            val hints = mapOf(
                    Pair(EncodeHintType.MARGIN, 2)
            )

            val size = 250

            val matrix = qrWriter.encode(user.getString("id"), BarcodeFormat.QR_CODE, size, size, hints)

            val bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.RGB_565)

            for (x in 0 until size) {
                for (y in 0 until size) {
                    bitmap.setPixel(x, y, if (matrix.get(x, y)) Color.BLACK else Color.WHITE)
                }
            }

            uiThread {
                qrImage.setImageBitmap(bitmap)
                qrImage.startAnimation(fadeInAnim)
                qrImage.visibility = View.VISIBLE
            }
        }

        return view
    }
}