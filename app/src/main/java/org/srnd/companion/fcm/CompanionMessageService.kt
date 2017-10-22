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

package org.srnd.companion.fcm

import android.app.Notification
import android.app.NotificationManager
import android.content.Context
import android.support.v4.app.NotificationCompat
import android.util.Log
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import org.srnd.companion.R
import java.util.*

class CompanionMessageService : FirebaseMessagingService() {
    override fun onMessageReceived(message: RemoteMessage?) {
        if(message!!.notification != null) {
            // why
            val notif = NotificationCompat.Builder(this, getString(R.string.default_notif_channel))
                    .setContentTitle(message.notification.title)
                    .setContentText(message.notification.body)
                    .setColor(getColor(R.color.colorAccent))
                    .setSmallIcon(R.drawable.ic_codeday_white)
                    .setStyle(NotificationCompat.BigTextStyle().bigText(message.notification.body))

            val notifManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notifManager.notify(Random().nextInt(), notif.build())
        }
    }
}