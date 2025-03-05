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

package com.skydoves.pokedex.core.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import kotlin.random.Random

@JsonClass(generateAdapter = true)
data class PokemonInfo(
  @field:Json(name = "id")
  val id: Int,
  @field:Json(name = "name") val name: String,
  @field:Json(name = "height") val height: Int,
  @field:Json(name = "weight") val weight: Int,
  @field:Json(name = "base_experience") val experience: Int,
  @field:Json(name = "types") val types: List<TypeResponse>,
  val hp: Int = Random.nextInt(MAX_HP),
  val attack: Int = Random.nextInt(MAX_ATTACK),
  val defense: Int = Random.nextInt(MAX_DEFENSE),
  val speed: Int = Random.nextInt(MAX_SPEED),
  val exp: Int = Random.nextInt(MAX_EXP),
  val isFavorite: Boolean = false, // New field to mark favorites
) {

  fun getIdString(): String = String.format("#%03d", id)
  fun getWeightString(): String = String.format("%.1f KG", weight.toFloat() / 10)
  fun getHeightString(): String = String.format("%.1f M", height.toFloat() / 10)
  fun getHpString(): String = " $hp/$MAX_HP"
  fun getAttackString(): String = " $attack/$MAX_ATTACK"
  fun getDefenseString(): String = " $defense/$MAX_DEFENSE"
  fun getSpeedString(): String = " $speed/$MAX_SPEED"
  fun getExpString(): String = " $exp/$MAX_EXP"

  // New methods for enhanced functionality

  /**
   * Calculate total stats value for comparing Pokemon strength
   * @return The sum of all stat values
   */
  fun calculateTotalStats(): Int = hp + attack + defense + speed

  /**
   * Get a simple tier ranking based on total stats
   * @return String representing the Pokemon's tier (S, A, B, C, or D)
   */
  fun getTierRanking(): String {
    val total = calculateTotalStats()
    return when {
      total > 900 -> "S"
      total > 750 -> "A"
      total > 600 -> "B"
      total > 450 -> "C"
      else -> "D"
    }
  }

  /**
   * Check if this Pokemon has a type advantage against another Pokemon
   * @param other The Pokemon to compare against
   * @return A TypeEffectiveness enum indicating the advantage relationship
   */
  fun getTypeEffectivenessAgainst(other: PokemonInfo): TypeEffectiveness {
    val myTypes = types.map { it.type.name }
    val otherTypes = other.types.map { it.type.name }

    // Check if any of my types are super effective against any of their types
    val iSuperEffective = myTypes.any { myType ->
      otherTypes.any { otherType ->
        TYPE_EFFECTIVENESS[myType]?.get(otherType) == 2.0f
      }
    }

    // Check if any of their types are super effective against any of my types
    val theyAreSuperEffective = otherTypes.any { otherType ->
      myTypes.any { myType ->
        TYPE_EFFECTIVENESS[otherType]?.get(myType) == 2.0f
      }
    }

    return when {
      iSuperEffective && !theyAreSuperEffective -> TypeEffectiveness.ADVANTAGE
      !iSuperEffective && theyAreSuperEffective -> TypeEffectiveness.DISADVANTAGE
      iSuperEffective && theyAreSuperEffective -> TypeEffectiveness.NEUTRAL
      else -> TypeEffectiveness.NEUTRAL
    }
  }

  /**
   * Compute a team compatibility score with another Pokemon
   * Higher scores mean better team synergy
   * @param other The Pokemon to evaluate compatibility with
   * @return Int representing compatibility score (0-100)
   */
  fun calculateTeamCompatibility(other: PokemonInfo): Int {
    var score = 50 // Base score

    // Type diversity is good for teams
    val myTypes = types.map { it.type.name }.toSet()
    val otherTypes = other.types.map { it.type.name }.toSet()
    val uniqueTypeCount = (myTypes + otherTypes).size

    // Add points for type diversity
    score += (uniqueTypeCount - 1) * 10

    // Check for complementary stats (one high attack, one high defense is good)
    if ((attack > MAX_ATTACK * 0.7 && other.defense > MAX_DEFENSE * 0.7) ||
      (defense > MAX_DEFENSE * 0.7 && other.attack > MAX_ATTACK * 0.7)) {
      score += 15
    }

    // Check for speed differences (mix of fast and slow is good for diverse strategies)
    if (Math.abs(speed - other.speed) > MAX_SPEED * 0.3) {
      score += 10
    }

    // Cap the score at 100
    return minOf(score, 100)
  }

  /**
   * Get a descriptive string about this Pokemon's best attribute
   * @return String describing the Pokemon's strongest stat
   */
  fun getStrengthDescription(): String {
    return when {
      hp >= maxOf(attack, defense, speed) -> "Tank with high HP"
      attack >= maxOf(hp, defense, speed) -> "Attacker with high damage"
      defense >= maxOf(hp, attack, speed) -> "Defender with high protection"
      else -> "Speedster with high agility"
    }
  }

  @JsonClass(generateAdapter = true)
  data class TypeResponse(
    @field:Json(name = "slot") val slot: Int,
    @field:Json(name = "type") val type: Type,
  )

  @JsonClass(generateAdapter = true)
  data class Type(
    @field:Json(name = "name") val name: String,
  )

  /**
   * Enum representing possible type effectiveness relationships
   */
  enum class TypeEffectiveness {
    ADVANTAGE,    // Super effective
    DISADVANTAGE, // Not very effective
    NEUTRAL       // Normal effectiveness
  }

  companion object {
    const val MAX_HP = 300
    const val MAX_ATTACK = 300
    const val MAX_DEFENSE = 300
    const val MAX_SPEED = 300
    const val MAX_EXP = 1000

    // Simplified type effectiveness chart (just a subset of Pokemon types for demonstration)
    // Maps attacking type to a map of defending type to effectiveness multiplier
    val TYPE_EFFECTIVENESS = mapOf(
      "fire" to mapOf(
        "fire" to 0.5f,
        "water" to 0.5f,
        "grass" to 2.0f,
        "ice" to 2.0f,
        "bug" to 2.0f,
        "steel" to 2.0f,
        "rock" to 0.5f,
        "dragon" to 0.5f
      ),
      "water" to mapOf(
        "fire" to 2.0f,
        "water" to 0.5f,
        "grass" to 0.5f,
        "ground" to 2.0f,
        "rock" to 2.0f,
        "dragon" to 0.5f
      ),
      "grass" to mapOf(
        "fire" to 0.5f,
        "water" to 2.0f,
        "grass" to 0.5f,
        "poison" to 0.5f,
        "ground" to 2.0f,
        "flying" to 0.5f,
        "bug" to 0.5f,
        "rock" to 2.0f,
        "dragon" to 0.5f,
        "steel" to 0.5f
      ),
      "electric" to mapOf(
        "water" to 2.0f,
        "electric" to 0.5f,
        "grass" to 0.5f,
        "ground" to 0.0f,
        "flying" to 2.0f,
        "dragon" to 0.5f
      ),
      "normal" to mapOf(
        "rock" to 0.5f,
        "ghost" to 0.0f,
        "steel" to 0.5f
      ),
      "fighting" to mapOf(
        "normal" to 2.0f,
        "ice" to 2.0f,
        "poison" to 0.5f,
        "flying" to 0.5f,
        "psychic" to 0.5f,
        "bug" to 0.5f,
        "rock" to 2.0f,
        "ghost" to 0.0f,
        "dark" to 2.0f,
        "steel" to 2.0f,
        "fairy" to 0.5f
      )
    )
  }
}