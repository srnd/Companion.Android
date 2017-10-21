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

import android.app.Service
import android.content.Intent
import android.os.IBinder

class CompanionAuthenticatorService : Service() {
    var authenticator: CompanionAuthenticator? = null

    override fun onCreate() {
        super.onCreate()
        authenticator = CompanionAuthenticator(this)
    }

    override fun onBind(intent: Intent?): IBinder {
        return authenticator!!.iBinder
    }
}