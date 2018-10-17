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

import java.util.*

object Constants {
    const val CHECK_IN_ALARM = 113
    const val ALARM_TAG = "CompanionAlarms"

    val BLE_CODEDAY_SERVICE = UUID.fromString("00000000-0000-0000-0000-00000c0dedae")
    val BLE_CODEDAY_CHARACTERISTIC = UUID.fromString("00000000-0000-0000-0000-00000c0dedaf")
    val BLE_CODEDAY_EVENT_DESCRIPTOR = UUID.fromString("00000000-0000-0000-0000-00000c0dedad")
}