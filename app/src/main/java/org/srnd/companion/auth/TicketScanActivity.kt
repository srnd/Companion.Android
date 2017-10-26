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

package org.srnd.companion.auth

import android.Manifest
import android.app.Activity
import android.app.AlarmManager
import android.app.PendingIntent
import android.app.ProgressDialog
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.SystemClock
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import android.widget.Toast
import com.github.kittinunf.fuel.Fuel
import com.github.kittinunf.fuel.android.extension.responseJson
import com.google.zxing.Result
import com.orm.SugarRecord
import me.dm7.barcodescanner.zxing.ZXingScannerView
import org.srnd.companion.CompanionApplication
import org.srnd.companion.MainActivity
import org.srnd.companion.R
import org.srnd.companion.dayof.CompanionAlarmReceiver
import org.srnd.companion.models.Announcement
import org.srnd.companion.util.AccountAdder


class TicketScanActivity : Activity(), ZXingScannerView.ResultHandler {
    private val CAMERA_PERMISSION = 1
    var scanner: ZXingScannerView? = null
    var dialog: ProgressDialog? = null

    private fun shouldShowCamera(): Boolean {
        val permissionCheck = ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
        if(permissionCheck == PackageManager.PERMISSION_GRANTED) return true

        ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.CAMERA), CAMERA_PERMISSION)
        return false
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>?, grantResults: IntArray?) {
        if(requestCode == CAMERA_PERMISSION && grantResults!!.count() > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            scanner!!.startCamera()
        } else {
            Toast.makeText(this, "Please accept the camera permission so you can scan your CodeDay ticket.", Toast.LENGTH_LONG).show()
            finish()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        scanner = ZXingScannerView(this)
        setContentView(scanner)

        dialog = ProgressDialog(this)
        dialog!!.setCancelable(false)
    }

    override fun onResume() {
        super.onResume()
        scanner!!.setResultHandler(this)
        if(shouldShowCamera()) scanner!!.startCamera()
    }

    override fun onPause() {
        super.onPause()
        scanner!!.stopCamera()
    }

    override fun handleResult(res: Result?) {
        dialog!!.setTitle(getString(R.string.loading_title))
        dialog!!.setMessage(getString(R.string.loading_registration))
        dialog!!.show()

        Fuel.get("/ticket/${res.toString()}").responseJson { _, _, result ->
            val registration = result.get().obj()

            if(registration.getBoolean("ok")) {
                AccountAdder.addAccount(this, registration)

                (application as CompanionApplication).refreshUserData()
//                (application as CompanionApplication).setAlarmIfNeeded()
                SugarRecord.deleteAll(Announcement::class.java)

                dialog!!.hide()

                val intent = Intent(this, MainActivity::class.java)
                startActivity(intent)
                finish()
            } else {
                Toast.makeText(this, getString(R.string.error_scan), Toast.LENGTH_LONG).show()
                finish()
            }
        }
    }
}