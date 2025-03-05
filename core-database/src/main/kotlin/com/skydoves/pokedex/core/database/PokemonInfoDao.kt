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
import androidx.room.Update
import com.skydoves.pokedex.core.database.entity.PokemonInfoEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface PokemonInfoDao {

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertPokemonInfo(pokemonInfo: PokemonInfoEntity)

  @Query("SELECT * FROM PokemonInfoEntity WHERE name = :name_")
  suspend fun getPokemonInfo(name_: String): PokemonInfoEntity?

  /**
   * Get Pokemon info by ID
   */
  @Query("SELECT * FROM PokemonInfoEntity WHERE id = :id")
  suspend fun getPokemonInfoById(id: Int): PokemonInfoEntity?

  /**
   * Get Pokemon info as a Flow for reactive updates
   */
  @Query("SELECT * FROM PokemonInfoEntity WHERE name = :name_")
  fun getPokemonInfoFlow(name_: String): Flow<PokemonInfoEntity?>

  /**
   * Get Pokemon info by ID as a Flow
   */
  @Query("SELECT * FROM PokemonInfoEntity WHERE id = :id")
  fun getPokemonInfoByIdFlow(id: Int): Flow<PokemonInfoEntity?>

  /**
   * Update the entire Pokemon info entity
   */
  @Update
  suspend fun updatePokemonInfo(pokemonInfo: PokemonInfoEntity)

  /**
   * Toggle favorite status for a Pokemon
   */
  @Query("UPDATE PokemonInfoEntity SET isFavorite = :isFavorite WHERE id = :id")
  suspend fun updateFavoriteStatus(id: Int, isFavorite: Boolean)

  /**
   * Get all favorite Pokemon
   */
  @Query("SELECT * FROM PokemonInfoEntity WHERE isFavorite = 1")
  fun getFavoritePokemon(): Flow<List<PokemonInfoEntity>>

  /**
   * Get Pokemon info by multiple IDs
   * This is useful for team functionality
   */
  @Query("SELECT * FROM PokemonInfoEntity WHERE id IN (:ids)")
  suspend fun getPokemonInfoByIds(ids: List<Int>): List<PokemonInfoEntity>

  /**
   * Get Pokemon info by multiple IDs as a Flow
   */
  @Query("SELECT * FROM PokemonInfoEntity WHERE id IN (:ids)")
  fun getPokemonInfoByIdsFlow(ids: List<Int>): Flow<List<PokemonInfoEntity>>

  /**
   * Search Pokemon info by name
   */
  @Query("SELECT * FROM PokemonInfoEntity WHERE name LIKE '%' || :query || '%'")
  suspend fun searchPokemonInfo(query: String): List<PokemonInfoEntity>

  /**
   * Get Pokemon by type
   */
  @Query("SELECT * FROM PokemonInfoEntity WHERE types LIKE '%' || :type || '%'")
  suspend fun getPokemonByType(type: String): List<PokemonInfoEntity>

  /**
   * Get Pokemon by stats criteria - useful for team building recommendations
   */
  @Query("SELECT * FROM PokemonInfoEntity WHERE hp >= :minHp AND attack >= :minAttack AND defense >= :minDefense AND speed >= :minSpeed")
  suspend fun getPokemonByMinStats(
    minHp: Int = 0,
    minAttack: Int = 0,
    minDefense: Int = 0,
    minSpeed: Int = 0
  ): List<PokemonInfoEntity>
}