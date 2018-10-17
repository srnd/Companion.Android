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
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.ListView
import android.widget.SimpleCursorAdapter
import com.segment.analytics.Analytics
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.uiThread
import org.srnd.companion.CompanionApplication
import org.srnd.companion.R
import org.srnd.companion.fragments.schedule.ScheduleDayAdapter

class ScheduleFragment : Fragment() {
    private var recycler: RecyclerView? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        super.onCreateView(inflater, container, savedInstanceState)

        Analytics.with(context).screen("Schedule")

        val app = context!!.applicationContext as CompanionApplication
        val view = inflater!!.inflate(R.layout.fragment_schedule, container, false)

        recycler = view.findViewById(R.id.recycler)

        val layoutManager = LinearLayoutManager(context)
        layoutManager.orientation = LinearLayoutManager.VERTICAL
        layoutManager.isAutoMeasureEnabled = true
        recycler!!.layoutManager = layoutManager
        recycler!!.setHasFixedSize(true)

        doAsync {
            val schedule = app.getUserData().getJSONObject("event").getJSONObject("schedule")
            val adapter = ScheduleDayAdapter(context!!, schedule)
            recycler!!.adapter = adapter
        }

        return view
    }
}