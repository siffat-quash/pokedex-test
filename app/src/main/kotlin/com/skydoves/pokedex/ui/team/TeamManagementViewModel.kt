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

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.skydoves.pokedex.core.data.repository.PokemonTeamRepository
import com.skydoves.pokedex.core.model.PokemonInfo
import com.skydoves.pokedex.core.model.PokemonTeam
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class TeamManagementViewModel @Inject constructor(
  private val teamRepository: PokemonTeamRepository
) : ViewModel() {

  // Currently selected team ID
  private val _selectedTeamId = MutableStateFlow<Long?>(null)

  // Selected team name - simplified to avoid entity dependency
  private val _selectedTeamName = MutableStateFlow<String?>(null)
  val selectedTeamName: StateFlow<String?> = _selectedTeamName

  // Pokemon in the selected team
  val teamMembers: StateFlow<List<PokemonInfo>> = _selectedTeamId
    .flatMapLatest { teamId ->
      if (teamId != null) teamRepository.getTeamPokemon(teamId) else flowOf(emptyList())
    }
    .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

  // Suggested Pokemon to add to the team
  private val _suggestedPokemon = MutableStateFlow<List<PokemonInfo>>(emptyList())
  val suggestedPokemon: StateFlow<List<PokemonInfo>> = _suggestedPokemon

  // Team domain model - calculated from current team members
  private val _teamDomainModel = MutableStateFlow<PokemonTeam?>(null)

  init {
    // Update team domain model whenever team members change
    viewModelScope.launch {
      teamMembers.collectLatest { members ->
        if (members.isEmpty()) {
          _teamDomainModel.value = null
        } else {
          _teamDomainModel.value = PokemonTeam(
            name = _selectedTeamName.value ?: "My Team",
            pokemons = members.toMutableList()
          )
          updateSuggestedPokemon(members)
        }
      }
    }

    // Update team name whenever selected team changes
    viewModelScope.launch {
      _selectedTeamId.collectLatest { teamId ->
        if (teamId != null) {
          teamRepository.getTeamById(teamId).collectLatest { team ->
            _selectedTeamName.value = team.name
          }
        } else {
          _selectedTeamName.value = null
        }
      }
    }
  }

  /**
   * Select a team by ID
   */
  fun selectTeam(teamId: Long) {
    _selectedTeamId.value = teamId
  }

  /**
   * Deselect the current team
   */
  fun deselectTeam() {
    _selectedTeamId.value = null
  }

  /**
   * Create a new empty team
   */
  fun createNewTeam() {
    viewModelScope.launch {
      val teamId = teamRepository.createTeam("New Team")
      _selectedTeamId.value = teamId
    }
  }

  /**
   * Rename the selected team
   */
  fun renameTeam(newName: String) {
    val teamId = _selectedTeamId.value ?: return
    viewModelScope.launch {
      teamRepository.updateTeamName(teamId, newName)
      _selectedTeamName.value = newName
    }
  }

  /**
   * Delete the selected team
   */
  fun deleteTeam() {
    val teamId = _selectedTeamId.value ?: return
    viewModelScope.launch {
      teamRepository.deleteTeam(teamId)
      _selectedTeamId.value = null
    }
  }

  /**
   * Add a Pokemon to the selected team
   */
  fun addPokemonToTeam(pokemonId: Int) {
    val teamId = _selectedTeamId.value ?: return
    viewModelScope.launch {
      teamRepository.addPokemonToTeam(teamId, pokemonId)
    }
  }

  /**
   * Remove a Pokemon from the selected team
   */
  fun removePokemonFromTeam(pokemonId: Int) {
    val teamId = _selectedTeamId.value ?: return
    viewModelScope.launch {
      teamRepository.removePokemonFromTeam(teamId, pokemonId)
    }
  }

  /**
   * Toggle favorite status for a Pokemon
   */
  fun toggleFavorite(pokemonId: Int, isFavorite: Boolean) {
    viewModelScope.launch {
      teamRepository.toggleFavorite(pokemonId, isFavorite)
    }
  }

  /**
   * Reorder team members
   */
  fun reorderTeam(orderedPokemonIds: List<Int>) {
    val teamId = _selectedTeamId.value ?: return
    viewModelScope.launch {
      teamRepository.updateTeamOrder(teamId, orderedPokemonIds)
    }
  }

  /**
   * Calculate team strength from domain model
   */
  fun calculateTeamStrength(): Int {
    return _teamDomainModel.value?.calculateTeamStrength() ?: 0
  }

  /**
   * Calculate type coverage from domain model
   */
  fun calculateTypeCoverage(): Int {
    return _teamDomainModel.value?.calculateTypeCoverage() ?: 0
  }

  /**
   * Calculate team synergy from domain model
   */
  fun calculateTeamSynergy(): Float {
    return _teamDomainModel.value?.calculateTeamSynergy() ?: 0f
  }

  /**
   * Check if team is balanced from domain model
   */
  fun isTeamBalanced(): Boolean {
    return _teamDomainModel.value?.isBalanced() ?: false
  }

  /**
   * Get improvement suggestions from domain model
   */
  fun getTeamSuggestions(): List<String> {
    return _teamDomainModel.value?.getTeamImprovementSuggestions() ?:
    listOf("Add Pokémon to your team to get suggestions")
  }

  /**
   * Update suggested Pokemon based on team analysis
   * For now, this is just a placeholder until we implement
   * the actual recommendation logic
   */
  private fun updateSuggestedPokemon(currentTeam: List<PokemonInfo>) {
    // In a real implementation, this would call repository methods
    // to get recommendations based on team analysis
    _suggestedPokemon.value = emptyList()
  }

  /**
   * Show UI for adding Pokemon to team
   */
  fun showAddPokemonDialog() {
    // In a real app, this would trigger a dialog or navigation
    // For now, we'll just make a placeholder
  }
}