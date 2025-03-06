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

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.skydoves.pokedex.R
import com.skydoves.pokedex.core.model.PokemonInfo

class PokemonSelectionAdapter(
  private val onPokemonSelected: (PokemonInfo) -> Unit
) : ListAdapter<PokemonInfo, PokemonSelectionAdapter.PokemonSelectionViewHolder>(PokemonDiffCallback()) {

  override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PokemonSelectionViewHolder {
    val view = LayoutInflater.from(parent.context)
      .inflate(R.layout.item_pokemon_selection, parent, false)
    return PokemonSelectionViewHolder(view, onPokemonSelected)
  }

  override fun onBindViewHolder(holder: PokemonSelectionViewHolder, position: Int) {
    val pokemon = getItem(position)
    Log.d("PokemonSelectionAdapter", "Binding Pokemon: ${pokemon.name} at position: $position")
    holder.bind(pokemon)
  }

  class PokemonSelectionViewHolder(
    itemView: View,
    private val onPokemonSelected: (PokemonInfo) -> Unit
  ) : RecyclerView.ViewHolder(itemView) {
    private val pokemonImage: ImageView = itemView.findViewById(R.id.pokemon_image)
    private val pokemonName: TextView = itemView.findViewById(R.id.pokemon_name)
    private val pokemonType: TextView = itemView.findViewById(R.id.pokemon_type)

    fun bind(pokemon: PokemonInfo) {
      // Set Pokemon name
      pokemonName.text = pokemon.name.capitalize()

      // Set Pokemon type (assuming first type)
      pokemonType.text = pokemon.types.firstOrNull()?.type?.name ?: "Unknown"

      // Set click listener
      itemView.setOnClickListener { onPokemonSelected(pokemon) }

      // TODO: Load Pokemon image
      // Example: Glide.with(itemView.context).load(pokemon.imageUrl).into(pokemonImage)
    }
  }

  // DiffUtil callback for efficient list updates
  private class PokemonDiffCallback : DiffUtil.ItemCallback<PokemonInfo>() {
    override fun areItemsTheSame(oldItem: PokemonInfo, newItem: PokemonInfo): Boolean {
      return oldItem.id == newItem.id
    }

    override fun areContentsTheSame(oldItem: PokemonInfo, newItem: PokemonInfo): Boolean {
      return oldItem == newItem
    }
  }
}