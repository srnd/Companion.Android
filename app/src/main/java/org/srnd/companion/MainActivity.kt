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

import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.SystemClock
import android.support.design.widget.BottomNavigationView
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentTransaction
import android.support.v7.app.AppCompatActivity
import android.util.Log
import org.srnd.companion.fragments.CheckInFragment
import org.srnd.companion.fragments.FeedFragment
import org.srnd.companion.fragments.ScheduleFragment

class MainActivity : AppCompatActivity() {
    private var navigation: BottomNavigationView? = null
    private var normalBrightness: Float = 0F

    private val mOnNavigationItemSelectedListener = BottomNavigationView.OnNavigationItemSelectedListener { item ->
        if(item.itemId == navigation!!.selectedItemId) return@OnNavigationItemSelectedListener false

        var fragment: Fragment? = null

        when (item.itemId) {
            R.id.navigation_dashboard -> fragment = FeedFragment()
            R.id.navigation_schedule -> fragment = ScheduleFragment()
            R.id.navigation_checkin -> fragment = CheckInFragment()
        }

        val attrs = window.attributes

        if(item.itemId == R.id.navigation_checkin) {
            normalBrightness = attrs.screenBrightness
            attrs.screenBrightness = 1F
        } else if(navigation!!.selectedItemId == R.id.navigation_checkin) {
            attrs.screenBrightness = normalBrightness
        }

        window.attributes = attrs

        if(fragment != null) {
            showFragment(fragment)
            return@OnNavigationItemSelectedListener true
        } else return@OnNavigationItemSelectedListener false
    }

    private fun showFragment(fragment: Fragment) {
        val transaction = supportFragmentManager.beginTransaction()
        transaction.replace(R.id.main_fragment, fragment)
        transaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
        transaction.commit()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        title = getString(R.string.app_name)

        Log.d("Clock", SystemClock.elapsedRealtime().toString())

        navigation = findViewById(R.id.navigationBar)
        navigation!!.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener)

        val app = application as CompanionApplication

        // allow check-in in these cases:
        //  - app is in debug mode
        //  - it's codeday
        //  - the ticket type isn't student
        if(BuildConfig.DEBUG
                || app.isItCodeDay()
                || app.getUserData().getString("type") != "student")
            navigation!!.menu.getItem(2).isEnabled = true


        if(intent!!.getStringExtra("action") == "selfCheckIn") {
            Log.d("MainActivity", "ahhhhh")

            val notifManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notifManager.cancel(intent.getIntExtra("notifId", 0))

            navigation!!.selectedItemId = R.id.navigation_checkin

            val fragment = CheckInFragment(showSelfCheckIn = true)
            showFragment(fragment)
        } else {
            showFragment(FeedFragment())
        }
    }

    override fun onResume() {
        super.onResume()
        (application as CompanionApplication).sync()
        if(!(application as CompanionApplication).isSignedIn()) {
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
            finish()
        }
    }
}
