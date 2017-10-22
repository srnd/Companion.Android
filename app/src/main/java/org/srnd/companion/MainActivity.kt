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

import android.accounts.Account
import android.accounts.AccountManager
import android.content.ContentResolver
import android.content.Intent
import android.content.res.ColorStateList
import android.os.Bundle
import android.support.design.widget.BottomNavigationView
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentTransaction
import android.support.v4.content.ContextCompat
import android.support.v7.app.AppCompatActivity
import android.view.MenuItem
import android.widget.TextView
import org.srnd.companion.fragments.CheckInFragment
import org.srnd.companion.fragments.FeedFragment
import org.srnd.companion.fragments.ScheduleFragment

class MainActivity : AppCompatActivity() {
    // private var mTextMessage: TextView? = null
    private var navigation: BottomNavigationView? = null

    private val mOnNavigationItemSelectedListener = BottomNavigationView.OnNavigationItemSelectedListener { item ->
        if(item.itemId == navigation!!.selectedItemId) return@OnNavigationItemSelectedListener false

        var fragment: Fragment? = null

        when (item.itemId) {
            R.id.navigation_dashboard -> fragment = FeedFragment()
            R.id.navigation_schedule -> fragment = ScheduleFragment()
            R.id.navigation_checkin -> fragment = CheckInFragment()
        }

        if(fragment != null) {
            showFragment(fragment)
            return@OnNavigationItemSelectedListener true
        } else return@OnNavigationItemSelectedListener false
    }

    fun showFragment(fragment: Fragment) {
        val transaction = supportFragmentManager.beginTransaction()
        transaction.replace(R.id.main_fragment, fragment)
        transaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
        transaction.commit()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        title = getString(R.string.app_name)

        navigation = findViewById<BottomNavigationView>(R.id.navigationBar)
        navigation!!.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener)

        showFragment(FeedFragment())

        val accountManager = AccountManager.get(this)
        val accounts: Array<Account> = accountManager.getAccountsByType("codeday.org")

        val settingsBundle = Bundle()
        settingsBundle.putBoolean(
                ContentResolver.SYNC_EXTRAS_MANUAL, true)
        settingsBundle.putBoolean(
                ContentResolver.SYNC_EXTRAS_EXPEDITED, true)
        ContentResolver.requestSync(accounts[0], "org.srnd.companion.sync.provider", settingsBundle)
    }

    override fun onResume() {
        super.onResume()
        if(!(application as CompanionApplication).isSignedIn()) {
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
            finish()
        }
    }
}
