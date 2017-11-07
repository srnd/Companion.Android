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

package org.srnd.companion.util

import android.graphics.*

// https://stackoverflow.com/a/46613094/2449940
fun Bitmap.getCircularBitmap(config: Bitmap.Config = Bitmap.Config.ARGB_8888): Bitmap {
    // circle configuration
    val circlePaint = Paint().apply { isAntiAlias = true }
    val circleRadius = Math.max(width, height) / 2f

    // output bitmap
    val outputBitmapPaint = Paint(circlePaint).apply { xfermode = PorterDuffXfermode(PorterDuff.Mode.SRC_IN) }
    val outputBounds = Rect(0, 0, width, height)
    val output = Bitmap.createBitmap(width, height, config)

    return Canvas(output).run {
        drawCircle(circleRadius, circleRadius, circleRadius, circlePaint)
        drawBitmap(this@getCircularBitmap, outputBounds, outputBounds, outputBitmapPaint)
        output
    }
}