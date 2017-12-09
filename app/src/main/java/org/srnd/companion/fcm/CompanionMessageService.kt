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
import android.content.Intent
import android.support.v4.app.NotificationCompat
import android.util.Log
import android.widget.Toast
import com.github.kittinunf.fuel.Fuel
import com.github.kittinunf.fuel.android.extension.responseJson
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.orm.SugarRecord
import org.srnd.companion.CompanionApplication
import org.srnd.companion.MainActivity
import org.srnd.companion.R
import org.srnd.companion.models.Announcement
import org.srnd.companion.util.AccountAdder
import java.util.*

class CompanionMessageService : FirebaseMessagingService() {
    override fun onMessageReceived(message: RemoteMessage?) {
        if(message!!.notification != null) {
            // why
            val notif = NotificationCompat.Builder(this, getString(R.string.default_notif_channel))
                    .setContentTitle(message.notification!!.title)
                    .setContentText(message.notification!!.body)
                    .setColor(getColor(R.color.colorAccent))
                    .setSmallIcon(R.drawable.ic_codeday_white)
                    .setStyle(NotificationCompat.BigTextStyle().bigText(message.notification!!.body))

            val notifManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notifManager.notify(Random().nextInt(), notif.build())
        }

        if(message.data.containsKey("type")) {
            val type = message.data["type"] as String

            when(type) {
                "sign_in" -> {
                    // delete all accounts first
                    AccountAdder.removeAllAccounts(this)

                    Fuel.get("/ticket/${message.data["id"]}").responseJson { _, _, result ->
                        val registration = result.get().obj()

                        if(registration.getBoolean("ok")) {
                            AccountAdder.addAccount(this, registration)

                            (application as CompanionApplication).refreshUserData()
                            SugarRecord.deleteAll(Announcement::class.java)

                            (application as CompanionApplication).refreshUserData()

                            val notif = NotificationCompat.Builder(this, getString(R.string.default_notif_channel))
                                    .setContentTitle("Hey ${registration.getString("first_name")}!")
                                    .setContentText("Thanks for registering for CodeDay again. We've automatically signed you in for the new season. You're all set.")
                                    .setColor(getColor(R.color.colorAccent))
                                    .setSmallIcon(R.drawable.ic_codeday_white)
                                    .setStyle(NotificationCompat.BigTextStyle().bigText("Thanks for registering for CodeDay again. We've automatically signed you in for the new season. You're all set."))

                            val notifManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                            notifManager.notify(Random().nextInt(), notif.build())
                        } else {
                            Log.e("Firebase", "Unable to signin new registration")
                        }
                    }
                }
            }
        }
    }
}