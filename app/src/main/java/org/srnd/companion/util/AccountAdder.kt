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

import android.accounts.Account
import android.accounts.AccountManager
import android.content.Context
import android.os.Bundle
import com.segment.analytics.Analytics
import com.segment.analytics.Traits
import org.json.JSONObject
import org.srnd.companion.CompanionApplication
import org.srnd.companion.fcm.FirebaseAssociator

class AccountAdder {
    companion object {
        fun addAccount(context: Context, reg: JSONObject) {
            val account = Account(reg["name"] as String, "codeday.org")

            val extraData = Bundle()
            extraData.putString("raw", reg.toString())
            extraData.putString("event_id", reg.getJSONObject("event").getString("id"))

            AccountManager.get(context).addAccountExplicitly(account, reg["id"] as String, extraData)
            FirebaseAssociator.associateRegistration(reg["id"] as String)

            val userTraits = Traits()
                    .putName(reg.getString("name"))
                    .putFirstName(reg.getString("first_name"))
                    .putLastName(reg.getString("last_name"))
                    .putAvatar(reg.getString("profile_image"))
                    .putValue("event", reg.getJSONObject("event").getString("id"))
                    .putValue("ticketType", reg.getString("type"))

            Analytics.with(context)
                    .identify(reg.getString("id"), userTraits, null)

            val prefs = (context.applicationContext as CompanionApplication).prefs
            val editor = prefs!!.edit()
            editor.putBoolean("segment_identified", true)
            editor.apply()
        }
    }
}