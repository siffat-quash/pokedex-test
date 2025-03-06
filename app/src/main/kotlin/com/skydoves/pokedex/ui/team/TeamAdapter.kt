/*
 * Designed and developed by 2022 skydoves (Jaewoong Eum)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.skydoves.pokedex.ui.team

import android.graphics.Color
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.skydoves.pokedex.R
import com.skydoves.pokedex.core.database.entity.PokemonTeamEntity

class TeamAdapter(
  private val onTeamClick: (PokemonTeamEntity) -> Unit,
  private val onDeleteClick: (PokemonTeamEntity) -> Unit
) : ListAdapter<PokemonTeamEntity, TeamAdapter.TeamViewHolder>(TeamDiffCallback()) {

  override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TeamViewHolder {
    val view = LayoutInflater.from(parent.context)
      .inflate(R.layout.item_team, parent, false)
    return TeamViewHolder(view)
  }

  override fun onBindViewHolder(holder: TeamViewHolder, position: Int) {
    holder.bind(getItem(position))
  }

  inner class TeamViewHolder(
    private val view: View
  ) : RecyclerView.ViewHolder(view) {
    private val textTeamName: TextView = view.findViewById(R.id.text_team_name)
    private val buttonDelete: ImageButton = view.findViewById(R.id.button_delete)

    init {
      // Debug statement to verify the TextView was found
      Log.d("TeamAdapter", "TextView found: ${textTeamName != null}")

      view.setOnClickListener {
        val position = bindingAdapterPosition
        if (position != RecyclerView.NO_POSITION) {
          onTeamClick(getItem(position))
        }
      }

      buttonDelete.setOnClickListener {
        val position = bindingAdapterPosition
        if (position != RecyclerView.NO_POSITION) {
          onDeleteClick(getItem(position))
        }
      }
    }

    fun bind(team: PokemonTeamEntity) {
      // Debug information
      Log.d("TeamAdapter", "Binding team: id=${team.id}, name='${team.name}'")

      // Force textColor to ensure visibility
      textTeamName.setTextColor(Color.BLACK)

      // Set text with a prefix to make it more visible
      textTeamName.text = "Team: ${team.name}"

      // Debug the text that was set
      Log.d("TeamAdapter", "Set team name text to: '${textTeamName.text}'")

      // Ensure it's visible
      textTeamName.visibility = View.VISIBLE
    }
  }

  private class TeamDiffCallback : DiffUtil.ItemCallback<PokemonTeamEntity>() {
    override fun areItemsTheSame(oldItem: PokemonTeamEntity, newItem: PokemonTeamEntity): Boolean {
      return oldItem.id == newItem.id
    }

    override fun areContentsTheSame(oldItem: PokemonTeamEntity, newItem: PokemonTeamEntity): Boolean {
      return oldItem == newItem
    }
  }
}