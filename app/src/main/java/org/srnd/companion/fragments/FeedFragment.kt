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

import android.accounts.Account
import android.accounts.AccountManager
import android.content.*
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import com.orm.SugarRecord
import org.srnd.companion.cards.AnnouncementCompanionCard
import org.srnd.companion.cards.CompanionCard
import org.srnd.companion.cards.CompanionWelcomeCard
import org.srnd.companion.cards.CountdownCompanionCard
import org.srnd.companion.cards.adapters.FeedAdapter
import org.srnd.companion.models.Announcement
import android.support.v4.widget.SwipeRefreshLayout
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import org.srnd.companion.R


class FeedFragment : Fragment() {
    private var recycler: RecyclerView? = null
    private var refresher: SwipeRefreshLayout? = null

    val syncFinishReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            initData()
            refresher!!.isRefreshing = false
        }
    }

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        super.onCreateView(inflater, container, savedInstanceState)
        val view = inflater!!.inflate(R.layout.fragment_feed, container, false)

        recycler = view.findViewById<RecyclerView>(R.id.recycler)

        val layoutManager = LinearLayoutManager(context)
        layoutManager.orientation = LinearLayoutManager.VERTICAL
        recycler!!.layoutManager = layoutManager
        recycler!!.setHasFixedSize(true)

        refresher = view.findViewById<SwipeRefreshLayout>(R.id.refresher)
        refresher!!.setColorSchemeResources(R.color.colorPrimary, R.color.colorGreen, R.color.colorBlue)
        refresher!!.setOnRefreshListener {
            refresh()
        }

        initData()
        return view
    }

    override fun onResume() {
        super.onResume()
        context.registerReceiver(syncFinishReceiver, IntentFilter("SYNC_FINISHED"))
    }

    override fun onPause() {
        super.onPause()
        context.unregisterReceiver(syncFinishReceiver)
    }

    fun refresh() {
        val accountManager = AccountManager.get(context)
        val accounts: Array<Account> = accountManager.getAccountsByType("codeday.org")

        val settingsBundle = Bundle()
        settingsBundle.putBoolean(
                ContentResolver.SYNC_EXTRAS_MANUAL, true)
        settingsBundle.putBoolean(
                ContentResolver.SYNC_EXTRAS_EXPEDITED, true)
        ContentResolver.requestSync(accounts[0], "org.srnd.companion.sync.provider", settingsBundle)
    }

    fun initData() {
        val shareAnnouncement: Announcement = Announcement(
                title = getString(R.string.share_title),
                message = getString(R.string.share_desc),
                linkText = getString(R.string.share_title),
                linkUri = "https://codeday.org/share",
                imageResource = context.getDrawable(R.drawable.codeday_header)
        )

        val comingSoonAnnouncement: Announcement = Announcement(
                title = getString(R.string.coming_soon_title),
                message = getString(R.string.coming_soon_desc),
                linkText = getString(R.string.go_to_slack),
                linkUri = "https://studentrnd.slack.com/messages/G19MM40S1/"
        )

        val announcements = SugarRecord.listAll(Announcement::class.java)

        val cards: MutableList<CompanionCard> = mutableListOf(
                CompanionWelcomeCard(context),
                CountdownCompanionCard(context),
                AnnouncementCompanionCard(context, shareAnnouncement),
                AnnouncementCompanionCard(context, comingSoonAnnouncement)
        )

        announcements.forEach { announcement ->
            cards.add(AnnouncementCompanionCard(context, announcement))
        }

        val adapter = FeedAdapter(cards)
        recycler!!.adapter = adapter
    }
}
