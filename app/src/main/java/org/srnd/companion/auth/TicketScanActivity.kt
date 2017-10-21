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
import android.os.Bundle
import com.google.zxing.Result
import me.dm7.barcodescanner.zxing.ZXingScannerView
import android.Manifest.permission
import android.Manifest.permission.WRITE_CALENDAR
import android.accounts.Account
import android.accounts.AccountManager
import android.app.ProgressDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import android.widget.Spinner
import android.widget.Toast
import com.github.kittinunf.fuel.Fuel
import com.github.kittinunf.fuel.android.extension.responseJson
import com.github.kittinunf.fuel.core.FuelManager
import org.srnd.companion.FeedActivity
import org.srnd.companion.R
import org.srnd.companion.dayof.AllSetActivity
import org.srnd.companion.fcm.FirebaseAssociator
import org.srnd.companion.util.Geofencer


class TicketScanActivity : Activity(), ZXingScannerView.ResultHandler {
    val CAMERA_PERMISSION = 1
    var scanner: ZXingScannerView? = null
    var dialog: ProgressDialog? = null

    fun shouldShowCamera(): Boolean {
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

            val account = Account(registration["name"] as String, "codeday.org")

            val extraData = Bundle()
            extraData.putString("raw", registration.toString())
            extraData.putString("event_id", registration.getJSONObject("event").getString("id"))

            AccountManager.get(this).addAccountExplicitly(account, registration["id"] as String, extraData)
            FirebaseAssociator.associateRegistration(registration["id"] as String)
            // Geofencer.geofenceForAddress(this, registration.getJSONObject("event").getJSONObject("venue").getString("full_address"))

            dialog!!.hide()

            val intent = Intent(this, FeedActivity::class.java)
            startActivity(intent)
            finish()
        }
    }
}