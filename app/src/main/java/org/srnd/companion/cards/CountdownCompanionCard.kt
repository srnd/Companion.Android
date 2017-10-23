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
import android.support.v7.widget.CardView
import android.view.View
import android.widget.TextView
import org.joda.time.DateTime
import org.joda.time.Days
import org.srnd.companion.CompanionApplication
import org.srnd.companion.R
import java.util.*

class CountdownCompanionCard(val context: Context) : CompanionCard() {
    override val layout: Int? = R.layout.card_countdown

    override fun populateView(view: View) {
        val app = context.applicationContext as CompanionApplication

        val currentDate = DateTime.now().withTimeAtStartOfDay()
        val date = app.getCodeDayDate().withTimeAtStartOfDay()

        val card = view.findViewById<CardView>(R.id.card_view)
        val title = card.findViewById<TextView>(R.id.card_title)
        title.text = context.getString(R.string.countdown, Days.daysBetween(currentDate, date).days.toString())
    }

    override fun getId(): String {
        return "countdown_card"
    }
}