/*
 *     Copyright (C) 2018 srnd.org
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

import android.Manifest
import android.app.Activity
import android.bluetooth.*
import android.bluetooth.le.*
import android.content.Context
import android.content.DialogInterface
import android.content.pm.PackageManager
import android.os.Looper
import android.os.ParcelUuid
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import android.support.v7.app.AlertDialog
import org.jetbrains.anko.runOnUiThread
import org.srnd.companion.CompanionApplication
import org.srnd.companion.Constants
import org.srnd.companion.R
import java.util.*

class BleSignatureManager(val context: Context) : BluetoothGattCallback() {
    private var scanner: BluetoothLeScanner = (context.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager).adapter.bluetoothLeScanner
    private lateinit var completeCallback: (token: String?, error: String?) -> Unit
    private lateinit var progressCallback: (message: String) -> Unit
    private var foundDevice = false

    fun requestPermissions(activity: Activity): Boolean {
        if(ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            val alert = AlertDialog.Builder(context)
                    .setPositiveButton(R.string.ok, { dialogInterface: DialogInterface, i: Int ->
                        ActivityCompat.requestPermissions(activity, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), 1)
                    })
                    .setTitle(context.getString(R.string.permission_location_title))
                    .setMessage(context.getString(R.string.permission_location_desc))
                    .create().show()

            return false
        } else {
            return true
        }
    }

    override fun onConnectionStateChange(gatt: BluetoothGatt?, status: Int, newState: Int) {
        if(gatt != null && newState == BluetoothProfile.STATE_CONNECTED) {
            progressCallback("Fetching check-in token...")
            gatt.discoverServices()
        }
    }

    override fun onCharacteristicRead(gatt: BluetoothGatt?, characteristic: BluetoothGattCharacteristic?, status: Int) {
        if(gatt != null && characteristic != null && characteristic.uuid == Constants.BLE_CODEDAY_CHARACTERISTIC) {
            completeCallback(characteristic.getStringValue(0), null)
            gatt.disconnect()
            gatt.close()
        }
    }

    override fun onDescriptorRead(gatt: BluetoothGatt?, descriptor: BluetoothGattDescriptor?, status: Int) {
        if(gatt != null && descriptor != null && descriptor.uuid == Constants.BLE_CODEDAY_EVENT_DESCRIPTOR) {
            val app = context.applicationContext as CompanionApplication
            if(String(descriptor.value) == app.getUserData().getJSONObject("event").getString("id")) {
                gatt.readCharacteristic(descriptor.characteristic)
            } else {
                gatt.disconnect()
                gatt.close()
                completeCallback(null, context.getString(R.string.wrong_codeday))
            }
        }
    }

    override fun onCharacteristicWrite(gatt: BluetoothGatt?, characteristic: BluetoothGattCharacteristic?, status: Int) {
        if(gatt != null && characteristic != null && characteristic.uuid == Constants.BLE_CODEDAY_CHARACTERISTIC) {
            gatt.readDescriptor(characteristic.getDescriptor(Constants.BLE_CODEDAY_EVENT_DESCRIPTOR))
        }
    }

    override fun onServicesDiscovered(gatt: BluetoothGatt?, status: Int) {
        if(gatt != null) {
            val app = context.applicationContext as CompanionApplication
            val characteristic = gatt.getService(Constants.BLE_CODEDAY_SERVICE).getCharacteristic(Constants.BLE_CODEDAY_CHARACTERISTIC)
            characteristic.setValue(app.getUserData().getString("id"))
            gatt.writeCharacteristic(characteristic)
        }
    }

    fun getToken(progCb: (message: String) -> Unit, completeCb: (token: String?, error: String?) -> Unit) {
        completeCallback = completeCb
        progressCallback = progCb

        progressCallback("Finding CodeDay beacon...")

        val timer = Timer()

        val scannerCallback = object : ScanCallback() {
            override fun onScanResult(callbackType: Int, result: ScanResult?) {
                if(result != null) {
                    foundDevice = true
                    timer.cancel()
                    timer.purge()

                    progressCallback("Connecting to CodeDay beacon...")

                    // Discover services for device
                    scanner.stopScan(this)
                    result.device.connectGatt(context, true, this@BleSignatureManager)
                }
            }
        }

        timer.schedule(object : TimerTask() {
            override fun run() {
                if(!foundDevice) {
                    scanner.stopScan(scannerCallback)
                    context.runOnUiThread {
                        completeCallback(null, "Couldn't find a CodeDay beacon. Make sure you are in the area marked \"Self Check-In Area\".")
                    }
                }
            }
        }, 10000L)

        scanner.startScan(mutableListOf(
                ScanFilter.Builder().setServiceUuid(ParcelUuid(UUID.fromString("00000000-0000-0000-0000-00000c0dedae"))).build()
        ), ScanSettings.Builder().setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY).build(), scannerCallback)
    }
}