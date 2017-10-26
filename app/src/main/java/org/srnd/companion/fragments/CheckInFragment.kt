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

import android.app.ProgressDialog
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.Typeface
import android.os.Bundle
import android.support.v4.app.Fragment
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import com.github.kittinunf.fuel.Fuel
import com.github.kittinunf.fuel.android.extension.responseJson
import com.google.zxing.BarcodeFormat
import com.google.zxing.EncodeHintType
import com.google.zxing.qrcode.QRCodeWriter
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.uiThread
import org.json.JSONObject
import org.srnd.companion.CompanionApplication
import org.srnd.companion.R
import org.srnd.companion.dayof.SelfCheckInActivity

class CheckInFragment(private val showSelfCheckIn: Boolean = false) : Fragment() {
    private var user: JSONObject? = null

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        super.onCreateView(inflater, container, savedInstanceState)
        val view = inflater!!.inflate(R.layout.fragment_check_in, container, false)

        user = (context.applicationContext as CompanionApplication).getUserData()

        val title = view.findViewById<TextView>(R.id.ticket_title)
        title.text = getString(R.string.your_ticket, user!!.getString("first_name"))

        val avenirNext = Typeface.createFromAsset(context.assets, "fonts/AvenirNextDemiBold.ttf")
        title.typeface = avenirNext

        val ticketType = view.findViewById<TextView>(R.id.ticket_type)
        ticketType.text = "${user!!.getString("type").substring(0, 1).toUpperCase()}${user!!.getString("type").substring(1)}"

        val eventName = view.findViewById<TextView>(R.id.event_name)
        eventName.text = user!!.getJSONObject("event").getString("name").replace("CodeDay ", "")

        val fadeInAnim = AnimationUtils.loadAnimation(context, android.R.anim.fade_in)

        val selfCheckInButton = view.findViewById<Button>(R.id.self_check_in)
        selfCheckInButton.setOnClickListener {
            openSelfCheckIn()
        }

        val qrImage = view!!.findViewById<ImageView>(R.id.ticket_qr)
        qrImage.visibility = View.INVISIBLE

        // render qr code asynchronously
        doAsync {
            val qrWriter = QRCodeWriter()

            val hints = mapOf(
                    Pair(EncodeHintType.MARGIN, 2)
            )

            val size = 250
            val matrix = qrWriter.encode(user!!.getString("id"), BarcodeFormat.QR_CODE, size, size, hints)
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

        if(showSelfCheckIn) openSelfCheckIn()

        return view
    }

    private fun openSelfCheckIn() {
        val app = context.applicationContext as CompanionApplication

        val dialog = ProgressDialog(context)
        dialog.setTitle(R.string.loading_title)
        dialog.setCancelable(false)

        if(app.getAccountData("check_in_code") == null) {
            dialog.setMessage(context.getString(R.string.check_in_loading))
            dialog.show()

            Fuel.get("/checkin/${user!!.getString("id")}").responseJson { _, _, result ->
                val res = result.get().obj()

                Log.d("CheckIn", res.toString())

                if(res.getBoolean("ok") && res.has("code")) {
                    app.setAccountData("check_in_code", res.getString("code"))
                    val intent = Intent(context, SelfCheckInActivity::class.java)
                    startActivity(intent)
                } else {
                    Toast.makeText(context, context.getString(R.string.check_in_error, res.getString("error")), Toast.LENGTH_LONG).show()
                }

                dialog.hide()
            }
        } else {
            val intent = Intent(context, SelfCheckInActivity::class.java)
            startActivity(intent)
        }
    }
}