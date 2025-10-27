package pt.isel.daw.pokerDice.domain.games

import kotlin.random.Random

sealed class Dice(val value: Int, val label: String) { // garante tipo seguro para cada face
    data object Nine : Dice(9, "nine")

    data object Ten : Dice(10, "ten")

    data object Jack : Dice(11, "jack")

    data object Queen : Dice(12, "queen")

    data object King : Dice(13, "king")

    data object Ace : Dice(14, "ace")

    companion object {
        val faces: List<Dice> = listOf(Nine, Ten, Jack, Queen, King, Ace)

        fun random(rng: Random = Random.Default): Dice = faces[rng.nextInt(faces.size)]

        fun from(label: String): Dice =
            faces.firstOrNull { it.label.equals(label, ignoreCase = true) }
                ?: error("Invalid face: $label")
    }
}
