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
import android.annotation.SuppressLint
import android.app.*
import android.content.BroadcastReceiver
import android.content.ContentResolver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.support.v4.app.NotificationCompat
import android.support.v4.content.ContextCompat
import android.util.Log
import com.facebook.stetho.Stetho
import com.github.kittinunf.fuel.core.FuelManager
import com.github.kittinunf.fuse.core.Fuse
import com.orm.SugarApp
import com.uber.sdk.android.core.UberSdk
import com.uber.sdk.core.auth.Scope
import com.uber.sdk.rides.client.Session
import com.uber.sdk.rides.client.SessionConfiguration
import main.java.com.mindscapehq.android.raygun4android.RaygunClient
import main.java.com.mindscapehq.android.raygun4android.messages.RaygunUserInfo
import net.danlew.android.joda.JodaTimeAndroid
import org.joda.time.DateTime
import org.json.JSONObject
import org.srnd.companion.dayof.CompanionAlarmReceiver
import org.srnd.companion.sync.CompanionSyncAdapter

class CompanionApplication : SugarApp() {
    private var cachedUserData: JSONObject? = null

    private val syncFinishReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            refreshUserData()
        }
    }

    override fun onCreate() {
        super.onCreate()
        // set up uber api
        val uberConfig = SessionConfiguration.Builder()
                .setClientId(getString(R.string.uber_client_id))
                .setServerToken(getString(R.string.uber_server_token))
                .setRedirectUri(getString(R.string.uber_redirect_uri))
                .setScopes(listOf(Scope.RIDE_WIDGETS))
                .setEnvironment(if (BuildConfig.DEBUG) SessionConfiguration.Environment.SANDBOX else SessionConfiguration.Environment.PRODUCTION)
                .build()

        UberSdk.initialize(uberConfig)

        JodaTimeAndroid.init(this)
        Fuse.init(cacheDir.absolutePath)

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
            eventNotifsChannel.lightColor = ContextCompat.getColor(this, R.color.colorPrimary)
            eventNotifsChannel.lockscreenVisibility = Notification.VISIBILITY_PUBLIC
            notifManager.createNotificationChannel(eventNotifsChannel)
        }

//        setAlarmIfNeeded()

        if(!BuildConfig.DEBUG) {
            RaygunClient.init(this)
            RaygunClient.attachExceptionHandler()
        }

        if(isSignedIn() && !BuildConfig.DEBUG) {
            val user = getUserData()
            val raygunUser = RaygunUserInfo()
            raygunUser.identifier = user.getString("id")
            raygunUser.firstName = user.getString("first_name")
            raygunUser.fullName = user.getString("name")
            raygunUser.setAnonymous(false)

            RaygunClient.setUser(raygunUser)
        }

        registerReceiver(syncFinishReceiver, CompanionSyncAdapter.USER_SYNC_FINISHED)
    }

    @SuppressLint("NewApi")
    fun setAlarmIfNeeded() {
        if(isSignedIn() && getAccountData("check_in_alarm_set") == null) {
            Log.d(Constants.ALARM_TAG, "Check-in alarm was not set, setting now")

            val alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager
            val time = (getUserData().getJSONObject("event").getLong("start_time")) - (2 * 60 * 60 * 1000)

            when {
                Build.VERSION.SDK_INT >= Build.VERSION_CODES.M -> alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, time, getCheckInAlarmPendingIntent())
                Build.VERSION.SDK_INT >= 19 -> alarmManager.setExact(AlarmManager.RTC_WAKEUP, time, getCheckInAlarmPendingIntent())
                else -> alarmManager.set(AlarmManager.RTC_WAKEUP, time, getCheckInAlarmPendingIntent())
            }

            setAccountData("check_in_alarm_set", "true")
        }
    }

    fun sync() {
        try {
            val accountManager = AccountManager.get(this)
            val accounts: Array<Account> = accountManager.getAccountsByType("codeday.org")

            val settingsBundle = Bundle()
            settingsBundle.putBoolean(
                    ContentResolver.SYNC_EXTRAS_MANUAL, true)
            settingsBundle.putBoolean(
                    ContentResolver.SYNC_EXTRAS_EXPEDITED, true)
            ContentResolver.requestSync(accounts[0], "org.srnd.companion.sync.provider", settingsBundle)
        } catch(e: Exception) {
            Log.e("CompanionSync", "Error during sync: ${e.stackTrace}")
            val i = Intent("SYNC_FINISHED")
            sendBroadcast(i)
        }
    }

    fun getCheckInAlarmPendingIntent(): PendingIntent {
        val receiverIntent = Intent(this, CompanionAlarmReceiver::class.java)
        receiverIntent.putExtra("alarmType", "checkInNotification")
        return PendingIntent.getBroadcast(this, Constants.CHECK_IN_ALARM, receiverIntent, 0)
    }

    fun getCodeDayDate(): DateTime =
            DateTime(getUserData().getJSONObject("event").getLong("starts_at") * 1000L)

    fun getCodeDayEndDate(): DateTime =
            DateTime(getUserData().getJSONObject("event").getLong("ends_at") * 1000L)

    fun isItCodeDay(): Boolean =
            if (BuildConfig.DEBUG) true else getCodeDayDate().withTimeAtStartOfDay() == DateTime.now().withTimeAtStartOfDay() || getCodeDayEndDate().withTimeAtStartOfDay() == DateTime.now().withTimeAtStartOfDay()

    fun isSignedIn(): Boolean {
        val accountManager = AccountManager.get(this)
        val accounts: Array<Account> = accountManager.getAccountsByType("codeday.org")
        return accounts.isNotEmpty()
    }

    fun setAccountData(key: String, value: String) {
        val accountManager = AccountManager.get(this)
        val accounts: Array<Account> = accountManager.getAccountsByType("codeday.org")
        accountManager.setUserData(accounts[0], key, value)
    }

    fun getAccountData(key: String): String? {
        val accountManager = AccountManager.get(this)
        val accounts: Array<Account> = accountManager.getAccountsByType("codeday.org")
        return accountManager.getUserData(accounts[0], key)
    }

    fun refreshUserData() {
        val accountManager = AccountManager.get(this)
        val accounts: Array<Account> = accountManager.getAccountsByType("codeday.org")
        cachedUserData = JSONObject(accountManager.getUserData(accounts[0], "raw"))
    }

    fun getUserData(): JSONObject {
        if(cachedUserData == null) refreshUserData()
        return cachedUserData!!
    }

    fun getNowPlaying(): JSONObject? {
        val accountManager = AccountManager.get(this)
        val accounts: Array<Account> = accountManager.getAccountsByType("codeday.org")
        val nowPlaying = accountManager.getUserData(accounts[0], "now_playing")

        return if(nowPlaying == null) {
            null
        } else {
            JSONObject(nowPlaying)
        }
    }
}