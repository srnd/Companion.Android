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
import android.content.AbstractThreadedSyncAdapter
import android.content.ContentProviderClient
import android.content.Context
import android.content.SyncResult
import android.os.Bundle
import com.github.kittinunf.fuel.Fuel
import com.github.kittinunf.fuel.android.extension.responseJson
import com.orm.SugarRecord
import org.srnd.companion.models.Announcement
import android.content.Intent



class CompanionSyncAdapter(context: Context, autoInit: Boolean) : AbstractThreadedSyncAdapter(context, autoInit) {
    override fun onPerformSync(account: Account?, extras: Bundle?, authority: String?, provider: ContentProviderClient?, syncResult: SyncResult?) {
        val accountManager = AccountManager.get(context)

        Fuel.get("/announcements/${accountManager.getUserData(account, "event_id")}").responseJson { _, _, result ->
            val announcements = result.get().array()

            for(i in 0 until announcements.length()) {
                val announcementObj = announcements.getJSONObject(i)
                val existingAnnouncements = SugarRecord.count<Announcement>(Announcement::class.java, "clear_Id = ?", arrayOf(announcementObj.getString("id")))

                if(existingAnnouncements == 0L) {
                    val announcement = Announcement(
                            clearId = announcementObj.getString("id"),
                            title = "Announcement",
                            message = announcementObj.getString("body")
                    )

                    if(!announcementObj.isNull("link")) {
                        val link = announcementObj.getJSONObject("link")
                        announcement.linkText = link.getString("text")
                        announcement.linkUri = link.getString("url")
                    }

                    announcement.save()
                }
            }

            val i = Intent("SYNC_FINISHED")
            context.sendBroadcast(i)
        }
    }
}