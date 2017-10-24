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

package org.srnd.companion.dayof

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.constraint.ConstraintLayout
import android.support.graphics.drawable.AnimatorInflaterCompat
import android.view.Window
import android.widget.TextView
import org.srnd.companion.CompanionApplication

import org.srnd.companion.R

class SelfCheckInActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)

        requestWindowFeature(Window.FEATURE_NO_TITLE)
        setContentView(R.layout.activity_self_check_in)

        val app = application as CompanionApplication
        val code = app.getAccountData("check_in_code")

        val nameText = findViewById<TextView>(R.id.attendee_name)
        nameText.text = app.getUserData().getString("name")

        val codeText = findViewById<TextView>(R.id.event_code)
        codeText.text = code

        val background = findViewById<ConstraintLayout>(R.id.background)

        val animatorSet = AnimatorInflaterCompat.loadAnimator(this, R.animator.check_in_background)

        animatorSet.addListener(object: AnimatorListenerAdapter() {
            override fun onAnimationEnd(animation: Animator?) {
                super.onAnimationEnd(animation)
                animatorSet.start()
            }
        })

        animatorSet.setTarget(background)
        animatorSet.start()
    }

    override fun onBackPressed() {
        super.onBackPressed()
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
    }
}
