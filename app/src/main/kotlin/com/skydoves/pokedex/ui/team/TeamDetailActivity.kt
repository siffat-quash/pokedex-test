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

import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.EditText
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.snackbar.Snackbar
import com.skydoves.pokedex.R
import com.skydoves.pokedex.core.model.PokemonInfo
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class TeamDetailActivity : AppCompatActivity() {

  private val viewModel: TeamManagementViewModel by viewModels()

  // Views
  private lateinit var toolbar: Toolbar
  private lateinit var recyclerView: RecyclerView
  private lateinit var fabAddPokemon: FloatingActionButton

  // Adapter
  private lateinit var teamMemberAdapter: TeamMemberAdapter

  // Team ID passed from the previous activity
  private var teamId: Long = -1

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_team_detail)

    // Retrieve the team ID from the intent
    teamId = intent.getLongExtra("team_id", -1)
    if (teamId == -1L) {
      // Handle invalid team ID
      Snackbar.make(findViewById(android.R.id.content),
        "Invalid team selected",
        Snackbar.LENGTH_SHORT).show()
      finish()
      return
    }

    // Initialize views
    toolbar = findViewById(R.id.toolbar)
    recyclerView = findViewById(R.id.recycler_view)
    fabAddPokemon = findViewById(R.id.fab_add_pokemon)

    // Setup toolbar
    setSupportActionBar(toolbar)
    supportActionBar?.apply {
      setDisplayHomeAsUpEnabled(true)
      setDisplayShowTitleEnabled(true)
    }

    // Initialize RecyclerView and Adapter
    teamMemberAdapter = TeamMemberAdapter(
      onPokemonClick = { pokemon -> showPokemonDetails(pokemon) },
      onRemovePokemon = { pokemon -> removePokemonFromTeam(pokemon) }
    )
    recyclerView.apply {
      layoutManager = LinearLayoutManager(this@TeamDetailActivity)
      adapter = teamMemberAdapter
    }

    // Setup item touch helper for drag and drop
    val itemTouchHelper = ItemTouchHelper(TeamMemberTouchCallback(teamMemberAdapter))
    itemTouchHelper.attachToRecyclerView(recyclerView)

    initializeUI()
    observeTeamDetails()
  }

  private fun initializeUI() {
    // Setup fab to add new Pokemon to the team
    fabAddPokemon.setOnClickListener {
      showAddPokemonDialog()
    }

    // Observe team name changes
    lifecycleScope.launch {
      repeatOnLifecycle(Lifecycle.State.STARTED) {
        viewModel.selectedTeamName.collect { teamName ->
          supportActionBar?.title = teamName ?: "Team Details"
        }
      }
    }
  }

  private fun observeTeamDetails() {
    // Select the team in the ViewModel
    viewModel.selectTeam(teamId)

    lifecycleScope.launch {
      repeatOnLifecycle(Lifecycle.State.STARTED) {
        viewModel.teamMembers.collect { members ->
          Log.d("TeamDetail", "Received team members update, count: ${members.size}")
          // Update the adapter with team members
          teamMemberAdapter.submitList(members)

          // Show/hide empty state if needed
          // You might want to add an empty state view to your layout
        }
      }
    }
  }

  private fun showAddPokemonDialog() {
    val pokemonSelectionDialog = PokemonSelectionDialog.newInstance()
    pokemonSelectionDialog.setOnPokemonSelectedListener { selectedPokemon ->
      Log.d("TeamDetail", "Pokemon selected: ${selectedPokemon.name}, ID: ${selectedPokemon.id}")

      // For test data, add Pokemon directly to the team in memory
      viewModel.addPokemonToTeamDirectly(selectedPokemon)

      // If this were production code with real data, you would use:
      // viewModel.addPokemonToTeam(selectedPokemon.id)

      // Optional: Show a confirmation snackbar
      Snackbar.make(
        findViewById(android.R.id.content),
        "${selectedPokemon.name} added to team",
        Snackbar.LENGTH_SHORT
      ).show()
    }
    pokemonSelectionDialog.show(supportFragmentManager, "PokemonSelectionDialog")
  }

  private fun showPokemonDetails(pokemon: PokemonInfo) {
    // TODO: Implement show Pokemon details
    Snackbar.make(findViewById(android.R.id.content),
      "Showing details for ${pokemon.name}",
      Snackbar.LENGTH_SHORT).show()
  }

  private fun removePokemonFromTeam(pokemon: PokemonInfo) {
    viewModel.removePokemonFromTeam(pokemon.id)
    Snackbar.make(findViewById(android.R.id.content),
      "${pokemon.name} removed from team",
      Snackbar.LENGTH_SHORT).show()
  }

  override fun onCreateOptionsMenu(menu: Menu): Boolean {
    menuInflater.inflate(R.menu.menu_team_detail, menu)
    return true
  }

  override fun onOptionsItemSelected(item: MenuItem): Boolean {
    return when (item.itemId) {
      android.R.id.home -> {
        onBackPressed()
        true
      }
      R.id.action_rename_team -> {
        showRenameTeamDialog()
        true
      }
      else -> super.onOptionsItemSelected(item)
    }
  }

  private fun showRenameTeamDialog() {
    val dialogView = layoutInflater.inflate(R.layout.dialog_team_name, null)
    val teamNameEditText = dialogView.findViewById<EditText>(R.id.team_name_edit_text)

    // Set the current team name
    teamNameEditText.setText(viewModel.selectedTeamName.value)

    MaterialAlertDialogBuilder(this)
      .setTitle("Rename Team")
      .setView(dialogView)
      .setPositiveButton("Save") { _, _ ->
        val newName = teamNameEditText.text.toString().takeIf { it.isNotEmpty() } ?: "My Team"
        viewModel.renameTeam(newName)

        // Show confirmation
        Snackbar.make(
          findViewById(android.R.id.content),
          "Team renamed to $newName",
          Snackbar.LENGTH_SHORT
        ).show()
      }
      .setNegativeButton("Cancel", null)
      .show()
  }

  // Touch callback for drag and drop reordering
  private inner class TeamMemberTouchCallback(
    private val adapter: TeamMemberAdapter
  ) : ItemTouchHelper.Callback() {
    override fun getMovementFlags(
      recyclerView: RecyclerView,
      viewHolder: RecyclerView.ViewHolder
    ): Int = makeMovementFlags(
      ItemTouchHelper.UP or ItemTouchHelper.DOWN,
      0
    )

    override fun onMove(
      recyclerView: RecyclerView,
      viewHolder: RecyclerView.ViewHolder,
      target: RecyclerView.ViewHolder
    ): Boolean {
      // Reorder items in the adapter
      val fromPosition = viewHolder.adapterPosition
      val toPosition = target.adapterPosition
      adapter.moveItem(fromPosition, toPosition)

      // Update team order in ViewModel
      val currentList = adapter.currentList
      val orderedPokemonIds = currentList.map { it.id }
      viewModel.reorderTeam(orderedPokemonIds)

      return true
    }

    override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
      // Not used for this implementation
    }

    override fun isLongPressDragEnabled(): Boolean = true
    override fun isItemViewSwipeEnabled(): Boolean = false
  }
}