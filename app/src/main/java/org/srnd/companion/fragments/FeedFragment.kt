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

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v4.widget.SwipeRefreshLayout
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.firebase.analytics.FirebaseAnalytics
import com.orm.SugarRecord
import com.segment.analytics.Analytics
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.uiThread
import org.srnd.companion.CompanionApplication
import org.srnd.companion.R
import org.srnd.companion.cards.*
import org.srnd.companion.cards.adapters.FeedAdapter
import org.srnd.companion.models.Announcement
import org.srnd.companion.sync.CompanionSyncAdapter
import org.srnd.gosquared.GoSquared
import org.srnd.gosquared.chat.GoSquaredSession


class FeedFragment : Fragment() {
    private var recycler: RecyclerView? = null
    private var refresher: SwipeRefreshLayout? = null
    private var adapter: FeedAdapter? = null

    private val syncFinishReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            initData()
            refresher!!.isRefreshing = false
        }
    }

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        super.onCreateView(inflater, container, savedInstanceState)
        val view = inflater!!.inflate(R.layout.fragment_feed, container, false)

        Analytics.with(context).screen("Dashboard")

        recycler = view.findViewById<RecyclerView>(R.id.recycler)

        val layoutManager = LinearLayoutManager(context)
        layoutManager.orientation = LinearLayoutManager.VERTICAL
        recycler!!.layoutManager = layoutManager
        recycler!!.setHasFixedSize(true)

        refresher = view.findViewById(R.id.refresher)
        refresher!!.setColorSchemeResources(R.color.colorPrimary, R.color.colorGreen, R.color.colorBlue)
        refresher!!.setOnRefreshListener {
            (context.applicationContext as CompanionApplication).sync()
        }

        initData()
        return view
    }

    override fun onResume() {
        super.onResume()
        val app = context.applicationContext as CompanionApplication

        if(app.isItCodeDay())
            context.registerReceiver(syncFinishReceiver, CompanionSyncAdapter.NOW_PLAYING_SYNC_FINISHED)
        else
            context.registerReceiver(syncFinishReceiver, CompanionSyncAdapter.SYNC_FINISHED)
    }

    override fun onPause() {
        super.onPause()
        context.unregisterReceiver(syncFinishReceiver)
    }

    fun initData() {
        Log.d("init", "calling initdata")
        doAsync {
            val app = context.applicationContext as CompanionApplication

            val announcements = SugarRecord.listAll(Announcement::class.java)

            val cards: MutableList<CompanionCard> = mutableListOf(
                    WelcomeCompanionCard(context)
            )

            val date = app.getCodeDayDate()

            if(!app.isItCodeDay())
                cards.add(CountdownCompanionCard(context))

            if(!app.getUserData().getBoolean("has_age") || !app.getUserData().getBoolean("has_parent")) {
                cards.add(AnnouncementCompanionCard(context, Announcement(
                        clearId = "info_reminder",
                        title = getString(R.string.parent_info_title),
                        message = getString(R.string.parent_info_desc),
                        linkText = getString(R.string.parent_info_link),
                        linkUri = "https://codeday.vip/${app.getUserData().getString("id")}"
                )))
            } else if(!app.getUserData().getBoolean("has_waiver")) {
                cards.add(AnnouncementCompanionCard(context, Announcement(
                        clearId = "waiver_reminder",
                        title = getString(R.string.waiver_title),
                        message = getString(R.string.waiver_desc),
                        linkText = getString(R.string.waiver_link),
                        linkUri = "https://codeday.vip/${app.getUserData().getString("id")}/waiver"
                )))
            }

            val nowPlaying = app.getNowPlaying()

            if(app.isItCodeDay() && nowPlaying != null && !nowPlaying.isNull("now_playing"))
                cards.add(SpotifyCompanionCard(context))

            if(app.isItCodeDay())
                cards.add(UberCompanionCard(context))

            announcements.forEach { announcement ->
                cards.add(AnnouncementCompanionCard(context, announcement))
            }

            if(!app.isItCodeDay()) {
                cards.add(AnnouncementCompanionCard(context, Announcement(
                        clearId = "before_day_of",
                        title = getString(R.string.before_day_of_title),
                        message = getString(R.string.before_day_of_desc)
                )))

                cards.add(AnnouncementCompanionCard(context, Announcement(
                        clearId = "skip_the_lines",
                        title = getString(R.string.skip_the_lines_title),
                        message = getString(R.string.skip_the_lines_desc)
                )))

                cards.add(AnnouncementCompanionCard(context, Announcement(
                        clearId = "share",
                        title = getString(R.string.share_title),
                        message = getString(R.string.share_desc),
                        linkText = getString(R.string.share_title),
                        linkUri = "https://codeday.org/share",
                        imageResource = context.getDrawable(R.drawable.codeday_header)
                )))
            }

            if(app.isItCodeDay())
                cards.add(AnnouncementCompanionCard(context, Announcement(
                        clearId = "day_of",
                        title = getString(R.string.day_of_title),
                        message = getString(R.string.day_of_desc),
                        imageResource = context.getDrawable(R.drawable.jump)
                )))

            uiThread {
                if(adapter == null) {
                    adapter = FeedAdapter(cards)
                    recycler!!.adapter = adapter
                } else {
                    adapter!!.cards = cards
                    adapter!!.notifyDataSetChanged()
                }
            }
        }
    }
}
