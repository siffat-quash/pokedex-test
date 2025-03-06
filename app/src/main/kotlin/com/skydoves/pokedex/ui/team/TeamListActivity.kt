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

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.EditText
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.RecyclerView
import androidx.appcompat.widget.Toolbar
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.skydoves.pokedex.R
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@AndroidEntryPoint
class TeamListActivity : AppCompatActivity() {

  private val viewModel: TeamManagementViewModel by viewModels()
  private lateinit var teamAdapter: TeamAdapter

  // Views
  private lateinit var recyclerView: RecyclerView
  private lateinit var emptyStateLayout: View
  private lateinit var fabCreateTeam: FloatingActionButton
  private lateinit var toolbar: Toolbar

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_team_list)

    // Initialize views
    recyclerView = findViewById(R.id.recycler_view)
    emptyStateLayout = findViewById(R.id.empty_state_layout)
    fabCreateTeam = findViewById(R.id.fab_create_team)
    toolbar = findViewById(R.id.toolbar)

    setSupportActionBar(toolbar)
    supportActionBar?.setDisplayHomeAsUpEnabled(true)

    initializeUI()
    observeViewModelState()
  }

  private fun initializeUI() {
    // Debug statement
    Log.d("TeamList", "Initializing UI")

    teamAdapter = TeamAdapter(
      onTeamClick = { team ->
        Log.d("TeamList", "Team clicked: id=${team.id}, name='${team.name}'")
        val intent = Intent(this, TeamDetailActivity::class.java).apply {
          putExtra("team_id", team.id)
        }
        startActivity(intent)
      },
      onDeleteClick = { team ->
        Log.d("TeamList", "Delete clicked for team: id=${team.id}, name='${team.name}'")
        viewModel.selectTeam(team.id)
        viewModel.deleteTeam()
      }
    )

    // Set layout manager
    recyclerView.layoutManager = LinearLayoutManager(this)
    recyclerView.adapter = teamAdapter

    // Debug statement after setting adapter
    Log.d("TeamList", "Adapter set on RecyclerView")

    fabCreateTeam.setOnClickListener {
      showCreateTeamDialog()
    }
  }

  private fun observeViewModelState() {
    lifecycleScope.launch {
      viewModel.getAllTeams().collectLatest { teams ->
        // Add debugging
        teams.forEach { team ->
          Log.d("TeamList", "Team from flow: id=${team.id}, name='${team.name}'")
        }

        if (teams.isEmpty()) {
          emptyStateLayout.visibility = View.VISIBLE
          recyclerView.visibility = View.GONE
        } else {
          emptyStateLayout.visibility = View.GONE
          recyclerView.visibility = View.VISIBLE
          teamAdapter.submitList(teams)
          // Force refresh to ensure UI update
          teamAdapter.notifyDataSetChanged()
        }
      }
    }
  }

  private fun createNewTeam() {
    viewModel.createNewTeam()
    // After creating, we could immediately navigate to the detail screen
    // However, since we're observing the team list, we'll see the new team appear
  }

  private fun showCreateTeamDialog() {
    val dialogView = layoutInflater.inflate(R.layout.dialog_team_name, null)
    val teamNameEditText = dialogView.findViewById<EditText>(R.id.team_name_edit_text)

    MaterialAlertDialogBuilder(this)
      .setTitle("Create New Team")
      .setView(dialogView)
      .setPositiveButton("Create") { _, _ ->
        val teamName = teamNameEditText.text.toString().takeIf { it.isNotEmpty() } ?: "My Team"
        createTeamWithName(teamName)
      }
      .setNegativeButton("Cancel", null)
      .show()
  }

  private fun createTeamWithName(teamName: String) {
    lifecycleScope.launch {
      val teamId = viewModel.teamRepository.createTeam(teamName)
      Log.d("TeamList", "Team created with ID: $teamId, name: $teamName")

      // Force a refresh of the team list
      // This is important - it ensures the UI updates with the new team
      (recyclerView.adapter as TeamAdapter).notifyDataSetChanged()

      // Navigate to the team detail screen
      val intent = Intent(this@TeamListActivity, TeamDetailActivity::class.java).apply {
        putExtra("team_id", teamId)
      }
      startActivity(intent)
    }
  }
}