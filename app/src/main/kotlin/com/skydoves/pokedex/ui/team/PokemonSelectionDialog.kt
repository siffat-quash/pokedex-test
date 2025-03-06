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

import android.app.Dialog
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.skydoves.pokedex.R
import com.skydoves.pokedex.core.model.PokemonInfo
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import timber.log.Timber

@AndroidEntryPoint
class PokemonSelectionDialog : DialogFragment() {

  private val viewModel: TeamManagementViewModel by viewModels()
  private var recyclerView: RecyclerView? = null
  private var emptyStateText: TextView? = null

  private lateinit var pokemonSelectionAdapter: PokemonSelectionAdapter

  private var onPokemonSelectedListener: ((PokemonInfo) -> Unit)? = null

  override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
    Log.d("PokemonSelectionDialog", "onCreateDialog called")

    // Inflate the custom layout
    val view = layoutInflater.inflate(R.layout.dialog_pokemon_selection, null)

    // Initialize views
    recyclerView = view.findViewById(R.id.recycler_view)
    emptyStateText = view.findViewById(R.id.empty_state_text)

    Log.d("PokemonSelectionDialog", "recyclerView found: ${recyclerView != null}")
    Log.d("PokemonSelectionDialog", "emptyStateText found: ${emptyStateText != null}")

    // Initialize adapter
    pokemonSelectionAdapter = PokemonSelectionAdapter { selectedPokemon ->
      onPokemonSelectedListener?.invoke(selectedPokemon)
      dismiss()
    }

    // Setup RecyclerView
    recyclerView?.apply {
      layoutManager = GridLayoutManager(context, 3)
      adapter = pokemonSelectionAdapter
      Log.d("PokemonSelectionDialog", "RecyclerView setup complete")
    }

    // Create test data immediately
    val testPokemon = listOf(
      PokemonInfo(
        id = 25,
        name = "Pikachu",
        height = 40,
        weight = 60,
        experience = 112,
        types = listOf(PokemonInfo.TypeResponse(1, PokemonInfo.Type("electric"))),
        hp = 35,
        attack = 55,
        defense = 40,
        speed = 90,
        exp = 112,
        isFavorite = false
      ),
      PokemonInfo(
        id = 6,
        name = "Charizard",
        height = 170,
        weight = 905,
        experience = 240,
        types = listOf(PokemonInfo.TypeResponse(1, PokemonInfo.Type("fire"))),
        hp = 78,
        attack = 84,
        defense = 78,
        speed = 100,
        exp = 240,
        isFavorite = false
      )
    )

    Log.d("PokemonSelectionDialog", "Submitting list with ${testPokemon.size} Pokemon")
    pokemonSelectionAdapter.submitList(testPokemon)

    // Update empty state visibility
    emptyStateText?.visibility = if (testPokemon.isEmpty()) View.VISIBLE else View.GONE

    // Create and return the dialog
    return AlertDialog.Builder(requireContext())
      .setTitle("Select Pokemon")
      .setView(view)
      .setNegativeButton("Cancel") { _, _ -> dismiss() }
      .create()
  }

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)
    Log.d("PokemonSelectionDialog", "onViewCreated called")

    // Move Pokemon list observation here, after view is created
    observePokemonList()
  }

  private fun observePokemonList() {
    Log.d("PokemonSelectionDialog", "observePokemonList called")

    // Create test data immediately outside the coroutine
    val testPokemon = listOf(
      PokemonInfo(
        id = 25,
        name = "Pikachu",
        height = 40,
        weight = 60,
        experience = 112,
        types = listOf(PokemonInfo.TypeResponse(1, PokemonInfo.Type("electric"))),
        hp = 35,
        attack = 55,
        defense = 40,
        speed = 90,
        exp = 112,
        isFavorite = false
      ),
      PokemonInfo(
        id = 6,
        name = "Charizard",
        height = 170,
        weight = 905,
        experience = 240,
        types = listOf(PokemonInfo.TypeResponse(1, PokemonInfo.Type("fire"))),
        hp = 78,
        attack = 84,
        defense = 78,
        speed = 100,
        exp = 240,
        isFavorite = false
      )
    )

    Log.d("PokemonSelectionDialog", "Created test data with ${testPokemon.size} Pokemon")

    // Try directly submitting the list first, outside the lifecycle coroutine
    pokemonSelectionAdapter.submitList(testPokemon)
    Log.d("PokemonSelectionDialog", "Directly submitted list to adapter")

    // Also try updating visibility
    emptyStateText?.visibility = if (testPokemon.isEmpty()) View.VISIBLE else View.GONE
    Log.d("PokemonSelectionDialog", "Updated empty state visibility")

    // Still keep the lifecycle approach as a backup
    viewLifecycleOwner.lifecycleScope.launch {
      Log.d("PokemonSelectionDialog", "Inside lifecycleScope.launch")
      repeatOnLifecycle(Lifecycle.State.STARTED) {
        Log.d("PokemonSelectionDialog", "Inside repeatOnLifecycle STARTED")

        Log.d("PokemonSelectionDialog", "Submitting list in coroutine: ${testPokemon.size} Pokemon")
        pokemonSelectionAdapter.submitList(testPokemon)

        // Update empty state
        emptyStateText?.visibility = if (testPokemon.isEmpty()) View.VISIBLE else View.GONE
        Log.d("PokemonSelectionDialog", "Updated visibility in coroutine")
      }
    }
  }

  // Method to set the listener for Pokemon selection
  fun setOnPokemonSelectedListener(listener: (PokemonInfo) -> Unit) {
    onPokemonSelectedListener = listener
  }

  override fun onDestroyView() {
    super.onDestroyView()
    recyclerView = null
    emptyStateText = null
  }

  companion object {
    fun newInstance(): PokemonSelectionDialog {
      return PokemonSelectionDialog()
    }
  }
}