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

package org.srnd.companion.fragments.schedule

import android.content.Context
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import org.json.JSONObject
import org.srnd.companion.R

class ScheduleDayAdapter(val context: Context, private val days: JSONObject) : RecyclerView.Adapter<ScheduleHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup?, viewType: Int): ScheduleHolder {
        val view = LayoutInflater.from(parent!!.context).inflate(R.layout.adapter_schedule_day, parent, false)
        return ScheduleHolder(view)
    }

    override fun onBindViewHolder(holder: ScheduleHolder?, position: Int) {
        val view = holder!!.itemView
        val recycler = view.findViewById<RecyclerView>(R.id.recycler)
        val day = days.getJSONArray(days.names()[position].toString())

        val dayTitle = view.findViewById<TextView>(R.id.day_title)
        dayTitle.text = days.names()[position].toString()

        val layoutManager = LinearLayoutManager(context)
        layoutManager.orientation = LinearLayoutManager.VERTICAL
        layoutManager.isAutoMeasureEnabled = true
        recycler!!.layoutManager = layoutManager
        recycler.setHasFixedSize(true)
        recycler.adapter = ScheduleActivityAdapter(context, day)
    }

    override fun getItemCount(): Int = days.names().length()
}