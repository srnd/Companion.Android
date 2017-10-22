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

package org.srnd.companion

import android.accounts.Account
import android.accounts.AccountManager
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.graphics.Color
import android.os.Build
import com.facebook.stetho.Stetho
import com.github.kittinunf.fuel.android.core.Json
import com.github.kittinunf.fuel.core.FuelManager
import com.orm.SugarApp
import com.orm.SugarDb
import com.orm.SugarRecord
import org.json.JSONObject
import org.srnd.companion.models.Announcement

class CompanionApplication : SugarApp() {
    var cachedUserData: JSONObject? = null

    override fun onCreate() {
        super.onCreate()

        if(BuildConfig.DEBUG) Stetho.initializeWithDefaults(this)
        FuelManager.instance.basePath = "https://app.codeday.vip/api"

        if (Build.VERSION.SDK_INT >= 26) {
            val notifManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            val eventNotifsChannelId = "event_notifications"
            val eventNotifsName = getString(R.string.event_notifications_name)
            val eventNotifsDesc = getString(R.string.event_notifications_desc)
            val eventNotifsImportance = NotificationManager.IMPORTANCE_MAX

            val eventNotifsChannel: NotificationChannel

            eventNotifsChannel = NotificationChannel(eventNotifsChannelId, eventNotifsName, eventNotifsImportance)
            eventNotifsChannel.description = eventNotifsDesc
            eventNotifsChannel.lightColor = Color.RED
            eventNotifsChannel.lockscreenVisibility = Notification.VISIBILITY_PUBLIC
            notifManager.createNotificationChannel(eventNotifsChannel)
        }
    }

    fun isSignedIn(): Boolean {
        val accountManager = AccountManager.get(this)
        val accounts: Array<Account> = accountManager.getAccountsByType("codeday.org")
        return accounts.isNotEmpty()
    }

    fun getUserData(): JSONObject {
        if(cachedUserData == null) {
            val accountManager = AccountManager.get(this)
            val accounts: Array<Account> = accountManager.getAccountsByType("codeday.org")
            cachedUserData = JSONObject(accountManager.getUserData(accounts[0], "raw"))
        }

        return cachedUserData!!
    }
}