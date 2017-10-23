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

package org.srnd.companion.fragments

import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.ListView
import android.widget.SimpleCursorAdapter
import org.srnd.companion.CompanionApplication
import org.srnd.companion.R

class ScheduleFragment : Fragment() {
    private var recycler: ListView? = null

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        super.onCreateView(inflater, container, savedInstanceState)
        val app = context.applicationContext as CompanionApplication
        val view = inflater!!.inflate(R.layout.fragment_schedule, container, false)

        recycler = view.findViewById(R.id.recycler)

        val scheduleArray = mutableListOf<String>()
        val schedule = app.getUserData().getJSONObject("event").getJSONObject("schedule")

        schedule.keys().forEach { key ->
            val activities = schedule.getJSONArray(key)

            (0 until activities.length())
                    .map { activities.getJSONObject(it) }
                    .mapTo(scheduleArray) { "${it.getString("hour")}: ${it.getString("title")}" }
        }

        val adapter = ArrayAdapter<String>(context, android.R.layout.simple_list_item_1, android.R.id.text1, scheduleArray)
        recycler!!.adapter = adapter

        return view
    }
}