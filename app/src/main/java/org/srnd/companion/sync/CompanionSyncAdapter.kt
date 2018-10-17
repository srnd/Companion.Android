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

package org.srnd.companion.sync

import android.accounts.Account
import android.accounts.AccountManager
import android.content.*
import android.database.sqlite.SQLiteDatabaseLockedException
import android.os.Bundle
import android.util.Log
import com.github.kittinunf.fuel.Fuel
import com.github.kittinunf.fuel.android.extension.responseJson
import com.orm.SugarApp
import com.orm.SugarDb
import com.orm.SugarRecord
import org.srnd.companion.CompanionApplication
import org.srnd.companion.models.Announcement

class CompanionSyncAdapter(context: Context, autoInit: Boolean) : AbstractThreadedSyncAdapter(context, autoInit) {
    companion object {
        val SYNC_FINISHED = IntentFilter("SYNC_FINISHED")
        val USER_SYNC_FINISHED = IntentFilter("USER_SYNC_FINISHED")
        val NOW_PLAYING_SYNC_FINISHED = IntentFilter("NOW_PLAYING_FINISHED")
    }

    override fun onPerformSync(account: Account?, extras: Bundle?, authority: String?, provider: ContentProviderClient?, syncResult: SyncResult?) {
        val accountManager = AccountManager.get(context)
        val app = context.applicationContext as CompanionApplication

        try {
            Fuel.get("/ticket/${app.getUserData().getString("id")}").responseJson { _, _, userRes ->
                val res = userRes.get().obj()

                if(res.getBoolean("ok")) {
                    accountManager.setUserData(account, "raw", userRes.get().obj().toString())

                    val i = Intent("USER_SYNC_FINISHED")
                    context.sendBroadcast(i)
                }

                if(app.isItCodeDay()) {
                    Fuel.get("/nowplaying/${app.getUserData().getJSONObject("event").getString("id")}").responseJson { _, _, result ->
                        val nowPlaying = result.get().obj()
                        accountManager.setUserData(account, "now_playing", nowPlaying.toString())

                        val i = Intent("NOW_PLAYING_FINISHED")
                        context.sendBroadcast(i)
                    }
                }

                Fuel.get("/announcements/${app.getUserData().getJSONObject("event").getString("id")}").responseJson { _, _, result ->
                    try {
                        val announcements = result.get().array()

                        for(i in 0 until announcements.length()) {
                            val announcementObj = announcements.getJSONObject(i)
                            val existingAnnouncements = SugarRecord.count<Announcement>(Announcement::class.java, "clear_Id = ?", arrayOf(announcementObj.getString("id")))

                            if(existingAnnouncements == 0L) {
                                val author = announcementObj.getJSONObject("creator")

                                val announcement = Announcement(
                                        clearId = announcementObj.getString("id"),
                                        title = "Announcement",
                                        message = announcementObj.getString("body"),
                                        authorName = author.getString("name"),
                                        authorUsername = author.getString("username"),
                                        postedAt = announcementObj.getJSONObject("posted_at").getString("date")
                                )

                                if(!announcementObj.isNull("link")) {
                                    val link = announcementObj.getJSONObject("link")
                                    announcement.linkText = link.getString("text")
                                    announcement.linkUri = link.getString("url")
                                }

                                announcement.save()
                            } else {
                                val announcement = SugarRecord.find<Announcement>(Announcement::class.java, "clear_Id = ?", announcementObj.getString("id"))[0]

                                if(announcement.authorName == null || announcement.authorUsername == null) {
                                    val author = announcementObj.getJSONObject("creator")

                                    announcement.authorName = author.getString("name")
                                    announcement.authorUsername = author.getString("username")
                                }

                                if(announcement.postedAt == null)
                                    announcement.postedAt = announcementObj.getJSONObject("posted_at").getString("date")

                                announcement.save()
                            }
                        }
                    } catch(e: SQLiteDatabaseLockedException) {
                        Log.w("CompanionSync", "Couldn't sync announcements due to database being locked. Migrations are probably running.")
                    }

                    val i = Intent("SYNC_FINISHED")
                    context.sendBroadcast(i)
                }
            }
        } catch(e: Exception) {
            val i = Intent("SYNC_FINISHED")
            context.sendBroadcast(i)
        }
    }
}