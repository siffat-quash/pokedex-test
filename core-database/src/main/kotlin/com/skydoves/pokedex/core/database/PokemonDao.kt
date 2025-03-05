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
import com.skydoves.pokedex.core.database.entity.PokemonEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface PokemonDao {

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertPokemonList(pokemonList: List<PokemonEntity>)

  @Query("SELECT * FROM PokemonEntity WHERE page = :page_")
  suspend fun getPokemonList(page_: Int): List<PokemonEntity>

  @Query("SELECT * FROM PokemonEntity WHERE page <= :page_")
  suspend fun getAllPokemonList(page_: Int): List<PokemonEntity>

  /**
   * Get a specific Pokemon by name
   */
  @Query("SELECT * FROM PokemonEntity WHERE name = :name")
  suspend fun getPokemonByName(name: String): PokemonEntity?

  /**
   * Get Pokemon list as a Flow for reactive updates
   */
  @Query("SELECT * FROM PokemonEntity WHERE page <= :page_")
  fun getAllPokemonListFlow(page_: Int): Flow<List<PokemonEntity>>

  /**
   * Search Pokemon by name pattern
   */
  @Query("SELECT * FROM PokemonEntity WHERE name LIKE '%' || :query || '%'")
  suspend fun searchPokemon(query: String): List<PokemonEntity>

  /**
   * Get a list of Pokemon by their IDs
   * This is useful for team functionality
   */
  @Query("SELECT * FROM PokemonEntity WHERE url LIKE '%/' || :id || '/'")
  suspend fun getPokemonById(id: Int): PokemonEntity?

  /**
   * Get multiple Pokemon by their IDs
   */
  @Query("SELECT * FROM PokemonEntity WHERE url IN (:urls)")
  suspend fun getPokemonByUrls(urls: List<String>): List<PokemonEntity>
}