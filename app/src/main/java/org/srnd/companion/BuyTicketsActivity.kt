/*
 *     Copyright (C) 2018 srnd.org
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

package org.srnd.companion

import android.app.AlertDialog
import android.app.ProgressDialog
import android.content.DialogInterface
import android.content.Intent
import android.graphics.Paint
import android.location.Geocoder
import android.net.Uri
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.customtabs.CustomTabsIntent
import android.support.v4.content.ContextCompat
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import com.github.kittinunf.fuel.Fuel
import com.github.kittinunf.fuel.android.extension.responseJson
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.stripe.android.Stripe
import com.stripe.android.TokenCallback
import com.stripe.android.model.Token
import com.stripe.android.view.CardInputWidget
import org.joda.time.DateTime
import org.srnd.companion.dayof.SelfCheckInActivity
import org.w3c.dom.Text
import java.util.*

class BuyTicketsActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_buy_tickets)

        val app = applicationContext as CompanionApplication
        val user = app.getUserData()

        val registrationForm = findViewById<LinearLayout>(R.id.registration_form)
        registrationForm.visibility = View.GONE

        val oldPrice = findViewById<TextView>(R.id.old_price)
        oldPrice.paintFlags = oldPrice.paintFlags or Paint.STRIKE_THRU_TEXT_FLAG

        val newPrice = findViewById<TextView>(R.id.new_price)
        val eventInfo = findViewById<TextView>(R.id.event_info)
        val attendeeInfo = findViewById<TextView>(R.id.attendee_info)
        val payButton = findViewById<Button>(R.id.buy_ticket)
//        val editButton = findViewById<TextView>(R.id.edit_info_button)

        val cardWidget = findViewById<CardInputWidget>(R.id.card_widget)
        cardWidget.clearFocus()

        val dialog = ProgressDialog(this)
        dialog.setTitle(R.string.loading_title)
        dialog.setCancelable(false)
        dialog.setMessage(getString(R.string.loading_event))
        dialog.show()

        try {
            Fuel.get("/event/${user.getJSONObject("event").getString("region_id")}").responseJson { _, _, result ->
                val res = result.get().obj()

                val startDate = DateTime(res.getLong("starts_at") * 1000L)
                val endDate = DateTime(res.getLong("ends_at") * 1000L)

                payButton.setOnClickListener {
                    val card = cardWidget.card
                    val activity = this

                    if(card == null) {
                        val alertDialog = AlertDialog.Builder(this)
                                .setTitle(getString(R.string.error))
                                .setMessage(getString(R.string.card_info_bad))
                                .setPositiveButton("Ok", null).create()

                        alertDialog.show()
                    } else {
                        card.name = user.getString("name")

                        val stripeKey = if(BuildConfig.DEBUG)
                            getString(R.string.stripe_pub_test)
                        else
                            res.getString("stripe_public_key")

                        val stripe = Stripe(this, stripeKey)
                        stripe.createToken(card, object : TokenCallback {
                            override fun onSuccess(token: Token?) {
                                Log.d("Stripe", token!!.id)
                            }

                            override fun onError(error: java.lang.Exception?) {
                                val alertDialog = AlertDialog.Builder(activity)
                                        .setTitle(getString(R.string.error))
                                        .setMessage(error!!.localizedMessage)
                                        .setPositiveButton("Ok", null).create()

                                alertDialog.show()
                            }
                        })
                    }
                }

//                editButton.setOnClickListener {
//
//                }

                registrationForm.visibility = View.VISIBLE

                if(!res.getBoolean("is_early_bird_pricing")) oldPrice.visibility = View.GONE

                newPrice.text = "$${"%.2f".format(res.getDouble("cost"))}"

                eventInfo.text = getString(R.string.hosted_at, "${startDate.monthOfYear().asText} ${startDate.dayOfMonth().asText}-${endDate.dayOfMonth().asText}, noon-noon", res.getJSONObject("venue").getString("name"))
                attendeeInfo.text = getString(R.string.attendee_info, user.getString("name"), user.getString("email"))

                val geocoder = Geocoder(this, Locale.getDefault())
                val addresses = geocoder.getFromLocationName(res.getJSONObject("venue").getString("full_address"), 1)

                val location = LatLng(addresses[0].latitude, addresses[0].longitude)

                val map = supportFragmentManager.findFragmentById(R.id.google_map) as SupportMapFragment
                map.getMapAsync {
                    it.uiSettings.isCompassEnabled = false
                    it.uiSettings.isMyLocationButtonEnabled = false
                    it.uiSettings.isRotateGesturesEnabled = false
                    it.uiSettings.isTiltGesturesEnabled = false
                    it.uiSettings.isScrollGesturesEnabled = false
                    it.uiSettings.isZoomControlsEnabled = false
                    it.uiSettings.isIndoorLevelPickerEnabled = false

                    val eventMarker = MarkerOptions().position(location).title(res.getJSONObject("venue").getString("name"))
                    it.addMarker(eventMarker)
                    it.moveCamera(CameraUpdateFactory.newLatLngZoom(location, 16.0f))
                }

                dialog.dismiss()
            }
        } catch(e: Exception) {

        }
    }
}
