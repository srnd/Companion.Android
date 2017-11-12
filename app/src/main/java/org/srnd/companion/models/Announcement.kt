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

package org.srnd.companion.models

import android.graphics.drawable.Drawable
import com.orm.SugarRecord
import com.orm.dsl.Ignore
import org.joda.time.DateTime
import java.util.*

data class Announcement(
        var clearId: String = "",
        var title: String = "Title",
        var message: String = "Message",
        var linkText: String? = null,
        var linkUri: String? = null,
        var postedAt: String? = null,
        var authorUsername: String? = null,
        var authorName: String? = null,
        @Ignore
        var imageResource: Drawable? = null
) : SugarRecord<Announcement>() {
        val jodaPostedAt: DateTime
        get() {
                return DateTime.parse(postedAt)
        }
}