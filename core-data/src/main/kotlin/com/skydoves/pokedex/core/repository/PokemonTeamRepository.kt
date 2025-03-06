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

package com.skydoves.pokedex.core.data.repository

import android.util.Log
import com.skydoves.pokedex.core.database.PokemonDao
import com.skydoves.pokedex.core.database.PokemonInfoDao
import com.skydoves.pokedex.core.database.PokemonTeamDao
import com.skydoves.pokedex.core.database.entity.PokemonEntity
import com.skydoves.pokedex.core.database.entity.PokemonInfoEntity
import com.skydoves.pokedex.core.database.entity.PokemonTeamEntity
import com.skydoves.pokedex.core.database.entity.PokemonTeamMemberEntity
import com.skydoves.pokedex.core.database.model.TeamWithMembers
import com.skydoves.pokedex.core.model.PokemonInfo
import com.skydoves.pokedex.core.model.PokemonTeam
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Single source of truth for Pokemon team operations
 */
@Singleton
class PokemonTeamRepository @Inject constructor(
  private val pokemonDao: PokemonDao,
  private val pokemonInfoDao: PokemonInfoDao,
  private val pokemonTeamDao: PokemonTeamDao
) {

  /**
   * Get all teams
   */
  fun getAllTeams(): Flow<List<PokemonTeamEntity>> = pokemonTeamDao.getAllTeams()

  /**
   * Get a team by ID
   */
  fun getTeamById(teamId: Long): Flow<PokemonTeamEntity> = pokemonTeamDao.getTeamById(teamId)

  /**
   * Get all teams with their members
   */
  fun getTeamsWithMembers(): Flow<List<TeamWithMembers>> = pokemonTeamDao.getTeamsWithMembers()

  /**
   * Get a specific team with its members
   */
  fun getTeamWithMembers(teamId: Long): Flow<TeamWithMembers> = pokemonTeamDao.getTeamWithMembers(teamId)

  /**
   * Get full Pokemon info for all team members
   */
  fun getTeamPokemon(teamId: Long): Flow<List<PokemonInfo>> {
    return pokemonTeamDao.getTeamMemberIds(teamId).map { ids ->
      // Use suspend function to get Pokemon info for all IDs
      pokemonInfoDao.getPokemonInfoByIds(ids).map { it.toPokemonInfo() }
    }
  }

  /**
   * Create a new team
   */
  suspend fun createTeam(name: String, pokemonIds: List<Int> = emptyList()): Long {
    return pokemonTeamDao.createTeamWithMembers(name, pokemonIds)
  }

  /**
   * Delete a team
   */
  suspend fun deleteTeam(teamId: Long) {
    pokemonTeamDao.deleteTeam(teamId)
  }

  /**
   * Update team name
   */
  suspend fun updateTeamName(teamId: Long, newName: String) {
    pokemonTeamDao.updateTeamName(teamId, newName)
  }

  /**
   * Add a Pokemon to a team
   */
  suspend fun addPokemonToTeam(teamId: Long, pokemonId: Int) {
    // First collect the current count
    val count = pokemonTeamDao.countTeamMembers(teamId).first()
    if (count < 6) { // Maximum 6 Pokemon per team
      pokemonTeamDao.insertTeamMember(
        PokemonTeamMemberEntity(teamId = teamId, pokemonId = pokemonId, position = count)
      )
    }
  }

  /**
   * Remove a Pokemon from a team
   */
  suspend fun removePokemonFromTeam(teamId: Long, pokemonId: Int) {
    pokemonTeamDao.removeTeamMember(teamId, pokemonId)
  }

  /**
   * Update the order of Pokemon in a team
   */
  suspend fun updateTeamOrder(teamId: Long, orderedPokemonIds: List<Int>) {
    pokemonTeamDao.updateTeamMemberOrder(teamId, orderedPokemonIds)
  }

  /**
   * Check if a Pokemon is already in a team
   */
  fun isPokemonInTeam(teamId: Long, pokemonId: Int): Flow<Boolean> {
    return pokemonTeamDao.isPokemonInTeam(teamId, pokemonId)
  }

  /**
   * Count members in a team
   */
  fun countTeamMembers(teamId: Long): Flow<Int> {
    return pokemonTeamDao.countTeamMembers(teamId)
  }

  /**
   * Toggle favorite status for a Pokemon
   */
  suspend fun toggleFavorite(pokemonId: Int, isFavorite: Boolean) {
    pokemonInfoDao.updateFavoriteStatus(pokemonId, isFavorite)
  }

  /**
   * Saves a Pokemon to the local database to ensure persistence.
   * This will save both basic Pokemon data and detailed info.
   *
   * @param pokemon The PokemonInfo model containing the Pokemon data
   * @return Boolean indicating whether the save was successful
   */
  suspend fun savePokemon(pokemon: PokemonInfo): Boolean {
    try {
      // First save the basic Pokemon data
      val basicPokemon = PokemonEntity(
        page = 0, // Default page value
        name = pokemon.name,
        url = "https://pokeapi.co/api/v2/pokemon/${pokemon.id}/" // Constructing a URL based on ID
      )
      pokemonDao.insertPokemonList(listOf(basicPokemon))

      // Then save the detailed Pokemon info
      val pokemonInfo = PokemonInfoEntity(
        id = pokemon.id,
        name = pokemon.name,
        height = pokemon.height,
        weight = pokemon.weight,
        experience = pokemon.experience,
        hp = pokemon.hp,
        attack = pokemon.attack,
        defense = pokemon.defense,
        speed = pokemon.speed,
        exp = pokemon.exp,
        isFavorite = pokemon.isFavorite,
        types = pokemon.types
      )
      pokemonInfoDao.insertPokemonInfo(pokemonInfo)

      return true
    } catch (e: Exception) {
      Log.e("PokemonTeamRepository", "Error saving Pokemon: ${e.message}", e)
      return false
    }
  }

  /**
   * Get all favorite Pokemon
   */
  fun getFavorites(): Flow<List<PokemonInfo>> {
    return pokemonInfoDao.getFavoritePokemon().map { entities ->
      entities.map { it.toPokemonInfo() }
    }
  }

  /**
   * Create a PokemonTeam domain model from entity data
   */
  suspend fun buildPokemonTeam(teamId: Long): PokemonTeam? {
    val teamEntity = pokemonTeamDao.getTeamById(teamId).first()
    val pokemonIds = pokemonTeamDao.getTeamMemberIds(teamId).first()
    val pokemons = pokemonInfoDao.getPokemonInfoByIds(pokemonIds).map { it.toPokemonInfo() }

    return PokemonTeam(
      name = teamEntity.name,
      pokemons = pokemons.toMutableList()
    )
  }
}

/**
 * Extension function to convert PokemonInfoEntity to domain model
 */
private fun com.skydoves.pokedex.core.database.entity.PokemonInfoEntity.toPokemonInfo(): PokemonInfo {
  return PokemonInfo(
    id = id,
    name = name,
    height = height,
    weight = weight,
    experience = experience,
    types = types,
    hp = hp,
    attack = attack,
    defense = defense,
    speed = speed,
    exp = exp,
    isFavorite = isFavorite
  )
}