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

package org.srnd.companion.cards.adapters

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.ViewGroup
import org.srnd.companion.cards.CompanionCard
import org.srnd.companion.cards.holders.CompanionCardHolder

class FeedAdapter(val cards: List<CompanionCard>) : RecyclerView.Adapter<CompanionCardHolder>() {
    override fun getItemCount(): Int {
        return cards.size
    }

    override fun getItemViewType(position: Int): Int {
        return cards[position].layout!!
    }

    override fun onCreateViewHolder(parent: ViewGroup?, viewType: Int): CompanionCardHolder {
        val view = LayoutInflater.from(parent!!.context).inflate(viewType, parent, false)
        val holder = CompanionCardHolder(view)
        return holder
    }

    override fun onBindViewHolder(holder: CompanionCardHolder, position: Int) {
        cards[position].populateView(holder.itemView)
    }
}