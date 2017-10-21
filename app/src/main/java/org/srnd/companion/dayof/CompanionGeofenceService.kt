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

import android.app.IntentService
import android.content.Intent
import android.support.v4.app.NotificationCompat
import com.google.android.gms.location.GeofencingEvent
import org.srnd.companion.R
import android.content.Context.NOTIFICATION_SERVICE
import android.app.NotificationManager
import android.content.Context


class CompanionGeofenceService(name: String) : IntentService(name) {
    override fun onHandleIntent(intent: Intent?) {
        val event = GeofencingEvent.fromIntent(intent)
        if(event.hasError()) return

        val notifBuilder = NotificationCompat.Builder(this, getString(R.string.default_notif_channel))
        notifBuilder.color = R.color.colorAccent
        notifBuilder.setContentTitle(getString(R.string.checkin_title))
        notifBuilder.setContentText(getString(R.string.checkin_body))
        notifBuilder.setSmallIcon(R.drawable.ic_codeday_white)

        val notif = notifBuilder.build()

        val notifManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notifManager.notify(1337, notif)
    }
}