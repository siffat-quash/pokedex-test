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

package com.skydoves.pokedex.core.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.skydoves.pokedex.core.database.entity.PokemonTeamEntity
import com.skydoves.pokedex.core.database.entity.PokemonTeamMemberEntity
import com.skydoves.pokedex.core.database.model.TeamWithMembers
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object for Pokemon team operations
 */
@Dao
interface PokemonTeamDao {

  /**
   * Insert a new team
   * @return the ID of the inserted team
   */
  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertTeam(team: PokemonTeamEntity): Long

  /**
   * Insert a Pokemon as a member of a team
   */
  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertTeamMember(teamMember: PokemonTeamMemberEntity)

  /**
   * Insert multiple team members at once
   */
  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertTeamMembers(teamMembers: List<PokemonTeamMemberEntity>)

  /**
   * Get a team by its ID
   */
  @Query("SELECT * FROM PokemonTeamEntity WHERE id = :teamId")
  fun getTeamById(teamId: Long): Flow<PokemonTeamEntity>

  /**
   * Get all teams
   */
  @Query("SELECT * FROM PokemonTeamEntity ORDER BY createdAt DESC")
  fun getAllTeams(): Flow<List<PokemonTeamEntity>>

  /**
   * Get all teams with their members
   */
  @Transaction
  @Query("SELECT * FROM PokemonTeamEntity ORDER BY createdAt DESC")
  fun getTeamsWithMembers(): Flow<List<TeamWithMembers>>

  /**
   * Get a specific team with its members
   */
  @Transaction
  @Query("SELECT * FROM PokemonTeamEntity WHERE id = :teamId")
  fun getTeamWithMembers(teamId: Long): Flow<TeamWithMembers>

  /**
   * Get all Pokemon IDs in a team
   */
  @Query("SELECT pokemonId FROM PokemonTeamMemberEntity WHERE teamId = :teamId ORDER BY position ASC, addedAt ASC")
  fun getTeamMemberIds(teamId: Long): Flow<List<Int>>

  /**
   * Remove a Pokemon from a team
   */
  @Query("DELETE FROM PokemonTeamMemberEntity WHERE teamId = :teamId AND pokemonId = :pokemonId")
  suspend fun removeTeamMember(teamId: Long, pokemonId: Int)

  /**
   * Delete a team and all its members
   */
  @Query("DELETE FROM PokemonTeamEntity WHERE id = :teamId")
  suspend fun deleteTeam(teamId: Long)

  /**
   * Update team name
   */
  @Query("UPDATE PokemonTeamEntity SET name = :newName WHERE id = :teamId")
  suspend fun updateTeamName(teamId: Long, newName: String)

  /**
   * Check if a Pokemon is already in a team
   */
  @Query("SELECT EXISTS(SELECT 1 FROM PokemonTeamMemberEntity WHERE teamId = :teamId AND pokemonId = :pokemonId)")
  fun isPokemonInTeam(teamId: Long, pokemonId: Int): Flow<Boolean>

  /**
   * Count members in a team
   */
  @Query("SELECT COUNT(*) FROM PokemonTeamMemberEntity WHERE teamId = :teamId")
  fun countTeamMembers(teamId: Long): Flow<Int>

  /**
   * Remove all members from a team
   */
  @Query("DELETE FROM PokemonTeamMemberEntity WHERE teamId = :teamId")
  suspend fun clearTeamMembers(teamId: Long)

  /**
   * Get all current members of a team
   */
  @Query("SELECT * FROM PokemonTeamMemberEntity WHERE teamId = :teamId")
  suspend fun getCurrentMembers(teamId: Long): List<PokemonTeamMemberEntity>

  /**
   * Create a new team with initial members
   */
  @Transaction
  suspend fun createTeamWithMembers(teamName: String, pokemonIds: List<Int>): Long {
    val team = PokemonTeamEntity(name = teamName)
    val teamId = insertTeam(team)

    val members = pokemonIds.mapIndexed { index, pokemonId ->
      PokemonTeamMemberEntity(teamId = teamId, pokemonId = pokemonId, position = index)
    }

    insertTeamMembers(members)
    return teamId
  }

  /**
   * Replace all team members
   */
  @Transaction
  suspend fun replaceTeamMembers(teamId: Long, pokemonIds: List<Int>) {
    clearTeamMembers(teamId)

    // Add new members
    val members = pokemonIds.mapIndexed { index, pokemonId ->
      PokemonTeamMemberEntity(teamId = teamId, pokemonId = pokemonId, position = index)
    }
    insertTeamMembers(members)
  }

  /**
   * Update team member order
   */
  @Transaction
  suspend fun updateTeamMemberOrder(teamId: Long, pokemonIds: List<Int>) {
    val currentMembers = getCurrentMembers(teamId)
    val currentIds = currentMembers.map { it.pokemonId }

    // Verify all IDs exist in the team
    if (currentIds.toSet() != pokemonIds.toSet()) {
      throw IllegalArgumentException("Cannot reorder: Pokemon IDs don't match current team members")
    }

    // Delete and reinsert with new positions
    clearTeamMembers(teamId)

    val updatedMembers = pokemonIds.mapIndexed { index, pokemonId ->
      val original = currentMembers.first { it.pokemonId == pokemonId }
      original.copy(position = index)
    }

    insertTeamMembers(updatedMembers)
  }
}