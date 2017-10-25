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
import android.content.Intent
import android.net.Uri
import android.support.v4.content.ContextCompat
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import org.json.JSONArray
import org.json.JSONObject
import org.srnd.companion.R
import android.util.TypedValue



class ScheduleActivityAdapter(val context: Context, private val day: JSONArray) : RecyclerView.Adapter<ScheduleHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup?, viewType: Int): ScheduleHolder {
        val view = LayoutInflater.from(parent!!.context).inflate(R.layout.adapter_schedule_activity, parent, false)
        return ScheduleHolder(view)
    }

    override fun onBindViewHolder(holder: ScheduleHolder?, position: Int) {
        val view = holder!!.itemView
        val activity = day.getJSONObject(position)

        val cardContent = view.findViewById<LinearLayout>(R.id.card_content)
        if(activity.isNull("url")) {
            cardContent.isClickable = false
            cardContent.isFocusable = false
            cardContent.background = null
        } else {
            cardContent.isClickable = true
            cardContent.isFocusable = false
            val outValue = TypedValue()
            context.theme.resolveAttribute(android.R.attr.selectableItemBackground, outValue, true)
            cardContent.setBackgroundResource(outValue.resourceId)
            cardContent.setOnClickListener {
                val intent = Intent(Intent.ACTION_VIEW)
                intent.data = Uri.parse(activity.getString("url"))
                context.startActivity(intent)
            }
        }

        val color = view.findViewById<View>(R.id.activity_color)
        when(activity.getString("type")) {
            "event" -> color.setBackgroundColor(ContextCompat.getColor(context, R.color.colorPrimary))
            else -> color.setBackgroundColor(ContextCompat.getColor(context, R.color.colorBlue))
        }

        val time = view.findViewById<TextView>(R.id.activity_time)
        time.text = activity.getString("hour")

        val name = view.findViewById<TextView>(R.id.activity_name)
        name.text = activity.getString("title")

        val desc = view.findViewById<TextView>(R.id.activity_desc)
        if(activity.isNull("description")) {
            desc.visibility = View.GONE
        } else {
            desc.visibility = View.VISIBLE
            desc.text = activity.getString("description")
        }
    }

    override fun getItemCount(): Int = day.length()
}