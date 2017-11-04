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
import android.location.Geocoder
import android.view.View
import com.uber.sdk.android.core.UberSdk
import com.uber.sdk.android.rides.RideParameters
import com.uber.sdk.android.rides.RideRequestButton
import com.uber.sdk.rides.client.ServerTokenSession
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.uiThread
import org.srnd.companion.CompanionApplication
import org.srnd.companion.R

class UberCompanionCard(private val context: Context) : CompanionCard() {
    override val layout: Int? = R.layout.card_uber
    override fun getId(): String = "uber_ride"

    override fun populateView(view: View) {
        val app = context.applicationContext as CompanionApplication
        val user = app.getUserData()

        val rideButton = view.findViewById<RideRequestButton>(R.id.ride_button)

        doAsync {
            if(Geocoder.isPresent()) {
                val venueAddress = user.getJSONObject("event").getJSONObject("venue").getString("full_address")
                val geocoder = Geocoder(context)
                val addresses = geocoder.getFromLocationName(venueAddress, 1)

                if(!addresses.isEmpty()) {
                    val address = addresses[0]

                    uiThread {
                        val rideParams = RideParameters.Builder()
                                .setDropoffLocation(address.latitude, address.longitude, "CodeDay ${user.getJSONObject("event").getString("region")}", venueAddress)
                                .build()

                        rideButton.setRideParameters(rideParams)
                        rideButton.setSession(ServerTokenSession(UberSdk.getDefaultSessionConfiguration()))
                        rideButton.loadRideInformation()
                    }
                }
            }
        }
    }
}