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

package org.srnd.companion.util

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.location.Geocoder
import com.google.android.gms.location.Geofence
import com.google.android.gms.location.GeofencingClient
import com.google.android.gms.location.GeofencingRequest
import org.srnd.companion.dayof.CompanionGeofenceService

class Geofencer {
    companion object {
        fun geofenceForAddress(context: Context, address: String) {
            if(Geocoder.isPresent()) {
                val geocoder = Geocoder(context)
                val results = geocoder.getFromLocationName(address, 1)

                if(results.size > 0) {
                    val fence = Geofence.Builder()
                            .setRequestId("venue_geofence")
                            .setCircularRegion(
                                    results[0].latitude,
                                    results[0].longitude,
                                    200.0f
                            )
                            .setExpirationDuration(Geofence.NEVER_EXPIRE)
                            .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER)
                            .build()

                    val request = GeofencingRequest.Builder()
                            .setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_ENTER)
                            .addGeofence(fence)
                            .build()

                    val intent = Intent(context, CompanionGeofenceService::class.java)
                    val pendingIntent = PendingIntent.getService(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT)

                    val client = GeofencingClient(context)
                    client.addGeofences(request, pendingIntent)
                }
            }
        }
    }
}