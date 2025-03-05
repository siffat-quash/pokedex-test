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

/**
 * A team of Pokemon with helper methods for team building and analysis
 */
data class PokemonTeam(
  val name: String,
  val pokemons: MutableList<PokemonInfo> = mutableListOf(),
  val maxSize: Int = 6
) {
  /**
   * Add a Pokemon to the team if there's space
   * @param pokemon The Pokemon to add
   * @return Boolean indicating if the addition was successful
   */
  fun addPokemon(pokemon: PokemonInfo): Boolean {
    if (pokemons.size >= maxSize) {
      return false
    }

    pokemons.add(pokemon)
    return true
  }

  /**
   * Remove a Pokemon from the team
   * @param pokemon The Pokemon to remove
   * @return Boolean indicating if the removal was successful
   */
  fun removePokemon(pokemon: PokemonInfo): Boolean {
    return pokemons.remove(pokemon)
  }

  /**
   * Calculate overall team strength based on total stats
   * @return Int representing average team strength
   */
  fun calculateTeamStrength(): Int {
    if (pokemons.isEmpty()) return 0

    return pokemons.sumOf { it.calculateTotalStats() } / pokemons.size
  }

  /**
   * Calculate team type coverage - how many different types are covered
   * @return Int representing the number of unique types in the team
   */
  fun calculateTypeCoverage(): Int {
    return pokemons.flatMap { pokemonInfo ->
      pokemonInfo.types.map { it.type.name }
    }.toSet().size
  }

  /**
   * Get team synergy score based on Pokemon compatibility
   * @return Float representing average compatibility score
   */
  fun calculateTeamSynergy(): Float {
    if (pokemons.size <= 1) return 0f

    var totalScore = 0
    var comparisons = 0

    for (i in pokemons.indices) {
      for (j in i+1 until pokemons.size) {
        totalScore += pokemons[i].calculateTeamCompatibility(pokemons[j])
        comparisons++
      }
    }

    return if (comparisons > 0) {
      totalScore.toFloat() / comparisons
    } else {
      0f
    }
  }

  /**
   * Check if the team is balanced (has a good mix of stats)
   * @return Boolean indicating if the team is balanced
   */
  fun isBalanced(): Boolean {
    if (pokemons.size < 3) return false

    // Count Pokemon by their primary strength
    var attackers = 0
    var defenders = 0
    var speedsters = 0
    var tanks = 0

    pokemons.forEach { pokemon ->
      when {
        pokemon.hp >= maxOf(pokemon.attack, pokemon.defense, pokemon.speed) -> tanks++
        pokemon.attack >= maxOf(pokemon.hp, pokemon.defense, pokemon.speed) -> attackers++
        pokemon.defense >= maxOf(pokemon.hp, pokemon.attack, pokemon.speed) -> defenders++
        else -> speedsters++
      }
    }

    // A team is balanced if it has at least one of each type
    return attackers > 0 && defenders > 0 && (speedsters > 0 || tanks > 0)
  }

  /**
   * Get recommendations for what types of Pokemon to add to improve the team
   * @return List of String recommendations
   */
  fun getTeamImprovementSuggestions(): List<String> {
    val suggestions = mutableListOf<String>()

    // Check team size
    if (pokemons.isEmpty()) {
      return listOf("Add your first Pokemon to start building your team!")
    }

    if (pokemons.size < maxSize) {
      suggestions.add("Your team has ${pokemons.size}/$maxSize Pokemon. Consider adding more.")
    }

    // Check type coverage
    val coveredTypes = pokemons.flatMap { it.types.map { type -> type.type.name } }.toSet()
    if (coveredTypes.size < 4 && pokemons.size >= 3) {
      suggestions.add("Your team has limited type coverage. Consider adding Pokemon with different types.")
    }

    // Check stat balance
    var highHp = false
    var highAttack = false
    var highDefense = false
    var highSpeed = false

    pokemons.forEach { pokemon ->
      if (pokemon.hp > PokemonInfo.MAX_HP * 0.7) highHp = true
      if (pokemon.attack > PokemonInfo.MAX_ATTACK * 0.7) highAttack = true
      if (pokemon.defense > PokemonInfo.MAX_DEFENSE * 0.7) highDefense = true
      if (pokemon.speed > PokemonInfo.MAX_SPEED * 0.7) highSpeed = true
    }

    if (!highHp) suggestions.add("Your team lacks high HP Pokemon. Consider adding a tank.")
    if (!highAttack) suggestions.add("Your team lacks high Attack Pokemon. Consider adding an attacker.")
    if (!highDefense) suggestions.add("Your team lacks high Defense Pokemon. Consider adding a defender.")
    if (!highSpeed) suggestions.add("Your team lacks high Speed Pokemon. Consider adding a speedster.")

    // Check for type weaknesses
    val typeWeaknesses = mutableSetOf<String>()
    for (pokemon in pokemons) {
      val pokemonTypes = pokemon.types.map { it.type.name }
      for (type in COMMON_ATTACKING_TYPES) {
        val isWeak = pokemonTypes.all { pokemonType ->
          val effectiveness = PokemonInfo.TYPE_EFFECTIVENESS[type]?.get(pokemonType) ?: 1.0f
          effectiveness >= 1.0f // Consider neutral or super effective as a potential weakness
        }
        if (isWeak) {
          typeWeaknesses.add(type)
        }
      }
    }

    if (typeWeaknesses.isNotEmpty()) {
      suggestions.add("Your team may be vulnerable to ${typeWeaknesses.joinToString(", ")} type attacks.")
    }

    // Return all suggestions, or a positive message if no issues found
    return if (suggestions.isNotEmpty()) {
      suggestions
    } else {
      listOf("Your team looks well-balanced! Great job!")
    }
  }

  companion object {
    // Common attacking types to check for team vulnerabilities
    private val COMMON_ATTACKING_TYPES = listOf("water", "fire", "electric", "grass", "fighting", "psychic")
  }
}