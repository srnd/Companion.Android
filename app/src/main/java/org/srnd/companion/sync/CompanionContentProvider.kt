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

package org.srnd.companion.sync

import android.content.ContentProvider
import android.content.ContentValues
import android.database.Cursor
import android.net.Uri

class CompanionContentProvider : ContentProvider() {
    override fun insert(uri: Uri?, values: ContentValues?): Uri? = null

    override fun query(uri: Uri?, projection: Array<out String>?, selection: String?, selectionArgs: Array<out String>?, sortOrder: String?): Cursor? =
            null

    override fun onCreate(): Boolean = true

    override fun update(uri: Uri?, values: ContentValues?, selection: String?, selectionArgs: Array<out String>?): Int =
            0

    override fun delete(uri: Uri?, selection: String?, selectionArgs: Array<out String>?): Int = 0

    override fun getType(uri: Uri?): String? = null
}