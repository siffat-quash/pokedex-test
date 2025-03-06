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

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.skydoves.pokedex.R
import com.skydoves.pokedex.core.model.PokemonInfo
import java.util.Collections

class TeamMemberAdapter(
  private val onPokemonClick: (PokemonInfo) -> Unit,
  private val onRemovePokemon: (PokemonInfo) -> Unit
) : ListAdapter<PokemonInfo, TeamMemberAdapter.TeamMemberViewHolder>(TeamMemberDiffCallback()) {

  override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TeamMemberViewHolder {
    val view = LayoutInflater.from(parent.context)
      .inflate(R.layout.item_team_pokemon, parent, false)
    return TeamMemberViewHolder(view)
  }

  override fun onBindViewHolder(holder: TeamMemberViewHolder, position: Int) {
    holder.bind(getItem(position))
  }

  fun moveItem(fromPosition: Int, toPosition: Int) {
    val list = currentList.toMutableList()
    Collections.swap(list, fromPosition, toPosition)
    submitList(list)
  }

  inner class TeamMemberViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
    private val pokemonImage: ImageView = itemView.findViewById(R.id.pokemon_image)
    private val pokemonName: TextView = itemView.findViewById(R.id.pokemon_name)
    private val pokemonType: TextView = itemView.findViewById(R.id.pokemon_type)
    private val pokemonLevel: TextView = itemView.findViewById(R.id.pokemon_level)
    private val btnRemovePokemon: ImageButton = itemView.findViewById(R.id.btn_remove_pokemon)

    fun bind(pokemon: PokemonInfo) {
      // Set Pokemon name
      pokemonName.text = pokemon.name.capitalize()

      // Set Pokemon type (assuming first type)
      pokemonType.text = pokemon.types.firstOrNull()?.type?.name ?: "Unknown"

      // Set Pokemon level (using experience as a proxy)
      pokemonLevel.text = "Lv. ${calculateLevel(pokemon.exp)}"

      // Set click listeners
      itemView.setOnClickListener { onPokemonClick(pokemon) }
      btnRemovePokemon.setOnClickListener { onRemovePokemon(pokemon) }

      // TODO: Load Pokemon image using an image loading library like Glide or Coil
      // Example: Glide.with(itemView.context).load(pokemon.imageUrl).into(pokemonImage)
    }

    // Simple level calculation based on experience
    private fun calculateLevel(exp: Int): Int {
      return (exp / 100) + 1 // Adjust this formula as needed
    }
  }

  // DiffUtil callback for efficient list updates
  private class TeamMemberDiffCallback : DiffUtil.ItemCallback<PokemonInfo>() {
    override fun areItemsTheSame(oldItem: PokemonInfo, newItem: PokemonInfo): Boolean {
      return oldItem.id == newItem.id
    }

    override fun areContentsTheSame(oldItem: PokemonInfo, newItem: PokemonInfo): Boolean {
      return oldItem == newItem
    }
  }
}