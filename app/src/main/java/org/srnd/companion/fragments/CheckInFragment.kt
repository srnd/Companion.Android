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

import android.app.AlertDialog
import android.app.Dialog
import android.app.ProgressDialog
import android.content.DialogInterface
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.Typeface
import android.net.Uri
import android.os.Bundle
import android.os.Looper
import android.support.customtabs.CustomTabsIntent
import android.support.v4.app.Fragment
import android.support.v4.content.ContextCompat
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
import com.segment.analytics.Analytics
import com.segment.analytics.Properties
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.uiThread
import org.json.JSONObject
import org.srnd.companion.CompanionApplication
import org.srnd.companion.R
import org.srnd.companion.dayof.BleSignatureManager
import org.srnd.companion.dayof.SelfCheckInActivity

class CheckInFragment : Fragment() {
    private var user: JSONObject? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        super.onCreateView(inflater, container, savedInstanceState)

        Analytics.with(context).screen("Check-in")

        val view = inflater.inflate(R.layout.fragment_check_in, container, false)

        val app = context!!.applicationContext as CompanionApplication
        user = app.getUserData()

        val title = view.findViewById<TextView>(R.id.ticket_title)
        title.text = getString(R.string.your_ticket, user!!.getString("first_name"))

        val avenirNext = Typeface.createFromAsset(context!!.assets, "fonts/AvenirNextDemiBold.ttf")
        title.typeface = avenirNext

        val ticketType = view.findViewById<TextView>(R.id.ticket_type)
        ticketType.text = "${user!!.getString("type").substring(0, 1).toUpperCase()}${user!!.getString("type").substring(1)}"

        val eventName = view.findViewById<TextView>(R.id.event_name)
        eventName.text = user!!.getJSONObject("event").getString("name").replace("CodeDay ", "")
        var eventNameTaps = 0

        eventName.setOnClickListener {
            eventNameTaps++
            if(eventNameTaps == 10) {
                eventNameTaps = 0
                val debugEnabled = app.prefs!!.getBoolean("dayof_debug", false)
                Toast.makeText(context, "Day-of debug mode ${if (debugEnabled) "disabled" else "enabled"}", Toast.LENGTH_LONG).show()

                val editor = app.prefs!!.edit()
                editor.putBoolean("dayof_debug", !debugEnabled)
                editor.apply()
            }
        }

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

        return view
    }

    private fun promptParentInfo() {
        val app = context!!.applicationContext as CompanionApplication

        val alertDialog = AlertDialog.Builder(context)
                .setTitle("One more step!")
                .setMessage("Before you check in, you will need to enter your parent's info and get them to sign the waiver.")
                .setPositiveButton("Ok, take me to the form", { _: DialogInterface, _: Int ->
                    val user = app.getUserData()
                    val url = if(!user.getBoolean("has_parent")) {
                        "https://codeday.vip/${user.getString("id")}/parent"
                    } else {
                        "https://codeday.vip/${user.getString("id")}/waiver"
                    }

                    val builder = CustomTabsIntent.Builder()
                    builder.setToolbarColor(ContextCompat.getColor(context!!, R.color.colorPrimary))
                    val customTabsIntent = builder.build()
                    customTabsIntent.launchUrl(context, Uri.parse(url))
                }).create()

        alertDialog.show()
    }

    private fun attemptSelfCheckIn(app: CompanionApplication, dialog: ProgressDialog, bleToken: String? = null) {
        dialog.setMessage(context!!.getString(R.string.check_in_loading))

        val params = if(bleToken != null)
            listOf("token" to bleToken)
        else
            listOf()

        Fuel.get("/checkin/${user!!.getString("id")}", params).responseJson { _, _, result ->
            val res = result.get().obj()

            Log.d("CheckIn", res.toString())

            if(res.getBoolean("ok") && res.has("code")) {
                app.setAccountData("check_in_code", res.getString("code"))
                dialog.dismiss()
                val intent = Intent(context, SelfCheckInActivity::class.java)
                startActivity(intent)
            } else if(res.has("error_code") && res.getString("error_code") == "BEACON_REQUIRED") {
                val signatureManager = BleSignatureManager(context!!)

                if(signatureManager.requestPermissions(activity!!)) {
                    signatureManager.getToken({
                        dialog.setMessage(it)
                    }, { token, error ->
                        if(error != null) {
                            Toast.makeText(context, error, Toast.LENGTH_LONG).show()
                            dialog.dismiss()
                        } else {
                            attemptSelfCheckIn(app, dialog, token)
                        }
                    })
                } else {
                    dialog.dismiss()
                }
            } else {
                // TODO: We should probably use an error_code for this instead lol
                if(res.getString("error") == "Please fill out your parent info and sign the waiver before checking in.") {
                    promptParentInfo()
                } else {
                    Toast.makeText(context, res.getString("error"), Toast.LENGTH_LONG).show()
                }

                dialog.dismiss()
            }
        }
    }

    private fun openSelfCheckIn() {
        val app = context!!.applicationContext as CompanionApplication

        Analytics.with(context).track("Tapped self check-in button")

        if(user!!.getString("type") == "student" && (!user!!.getBoolean("has_parent") || !user!!.getBoolean("has_waiver"))) {
            promptParentInfo()
            return
        }

        val dialog = ProgressDialog(context)
        dialog.setTitle(R.string.loading_title)
        dialog.setCancelable(false)

        if(app.getAccountData("check_in_code") == null) {
            dialog.setMessage(context!!.getString(R.string.check_in_loading))
            dialog.show()

            try {
                attemptSelfCheckIn(app, dialog)
            } catch(e: Exception) {
                Toast.makeText(context, "Something went wrong while checking you in.", Toast.LENGTH_LONG).show()
                dialog.dismiss()
            }
        } else {
            val intent = Intent(context, SelfCheckInActivity::class.java)
            startActivity(intent)
        }
    }
}