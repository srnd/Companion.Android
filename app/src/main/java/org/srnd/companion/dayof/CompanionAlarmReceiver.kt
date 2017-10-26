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

import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.support.v4.app.NotificationCompat
import android.support.v4.content.ContextCompat
import org.srnd.companion.MainActivity
import org.srnd.companion.R
import java.util.*

class CompanionAlarmReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        if(intent!!.extras["alarmType"] == "checkInNotification") {
            val notificationId = Random().nextInt()

            val clickIntent = Intent(context!!, MainActivity::class.java)
            clickIntent.putExtra("action", "selfCheckIn")
            clickIntent.putExtra("notifId", notificationId)

            val pendingIntent = PendingIntent.getActivity(context, Random().nextInt(), clickIntent, 0)

            val notif = NotificationCompat.Builder(context, context.getString(R.string.default_notif_channel))
                    .setContentTitle(context.getString(R.string.checkin_title))
                    .setContentText(context.getString(R.string.checkin_body))
                    .setColor(ContextCompat.getColor(context, R.color.colorAccent))
                    .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                    .setSmallIcon(R.drawable.ic_codeday_white)
                    .setStyle(NotificationCompat.BigTextStyle().bigText(context.getString(R.string.checkin_body)))
                    .setContentIntent(pendingIntent)

            val notifManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notifManager.notify(notificationId, notif.build())
        }
    }
}