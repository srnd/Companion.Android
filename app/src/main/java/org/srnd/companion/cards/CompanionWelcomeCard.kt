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

package org.srnd.companion.cards

import android.content.Context
import android.view.View
import android.widget.TextView
import org.srnd.companion.CompanionApplication
import org.srnd.companion.R

class CompanionWelcomeCard(val context: Context) : CompanionCard() {
    override val layout: Int? = R.layout.welcome_title

    override fun populateView(view: View) {
        val app = context.applicationContext as CompanionApplication
        val welcomeTitle = view.findViewById<TextView>(R.id.welcome_text)
        welcomeTitle.text = context.getString(R.string.welcome_name, app.getUserData().getString("first_name"))
    }
}