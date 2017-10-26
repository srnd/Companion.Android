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
import android.support.v4.app.NotificationCompat
import android.support.v4.content.ContextCompat
import android.util.Log
import com.facebook.stetho.Stetho
import com.github.kittinunf.fuel.core.FuelManager
import com.orm.SugarApp
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
        JodaTimeAndroid.init(this)

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
            eventNotifsChannel.lockscreenVisibility = NotificationCompat.VISIBILITY_PUBLIC
            notifManager.createNotificationChannel(eventNotifsChannel)
        }

//        setAlarmIfNeeded()

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

    fun isItCodeDay(): Boolean =
            getCodeDayDate().withTimeAtStartOfDay() == DateTime.now().withTimeAtStartOfDay()

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
}