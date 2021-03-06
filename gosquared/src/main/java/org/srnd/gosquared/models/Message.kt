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

package org.srnd.gosquared.models

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.vdurmont.emoji.EmojiParser

@JsonIgnoreProperties(ignoreUnknown = true)
data class Message (
        var id: String? = null,
        var content: String? = null,
        var from: String = "agent",
        var timestamp: Long? = null,
        var agent: Agent? = null,
        var bot: Agent? = null
) {
    val contentEmojified: String
    get() {
        return EmojiParser.parseToUnicode(content)
    }
}