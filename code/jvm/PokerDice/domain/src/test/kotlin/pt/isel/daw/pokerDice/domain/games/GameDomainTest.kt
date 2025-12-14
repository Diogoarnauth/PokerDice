
package pt.isel.daw.pokerDice.domain.games

/*
import org.junit.jupiter.api.Nested
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals
*/
class GameDomainTest {
    /*
    val gameDomain = GameDomain()

    @Nested
    inner class DifferentCombinationTypes {
        @Test
        fun testFourOfAKindVsFullHouse() {
            val fourOfAKind = listOf(Dice.King, Dice.King, Dice.King, Dice.King, Dice.Ace)
            val fourOfAKindScore = gameDomain.score(fourOfAKind)
            println("Four of a kind score: $fourOfAKindScore")

            val fullHouse = listOf(Dice.Queen, Dice.Queen, Dice.Queen, Dice.Jack, Dice.Jack)
            val fullHouseScore = gameDomain.score(fullHouse)
            println("Full house score: $fullHouseScore")

            assertNotEquals(fourOfAKindScore, fullHouseScore, "Four of a kind should be higher than full house")
            assert(fourOfAKindScore > fullHouseScore) { "Four of a kind should win" }
        }

// TESTES RAW
        @Test
        fun testCombinationsRaw() {
            val fourOfAKind = listOf(Dice.King, Dice.King, Dice.King, Dice.King, Dice.Ace)
            val fourOfAKindScore = gameDomain.score(fourOfAKind)
            println("AHHHHHHHHHHHHHHH: $fourOfAKindScore")
        }

        @Test
        fun testCombinations() {
            val fourOfAKind = listOf(Dice.King, Dice.King, Dice.Nine, Dice.Ten, Dice.King)
            val fourOfAKindScore = gameDomain.score(fourOfAKind)
            println("AHHHHHHHHHHHHHHH: $fourOfAKindScore")

            val fourOfAKindTest = listOf(Dice.King, Dice.King, Dice.Nine, Dice.Jack, Dice.Queen)
            val fourOfAKindTestScore = gameDomain.score(fourOfAKindTest)
            println("BHHHHHHHHHHHHHHHHHHHHH: $fourOfAKindTestScore")
        }

        // TESTES RAW
        @Test
        fun testStraightVsThreeOfAKind() {
            val straight = listOf(Dice.Ten, Dice.Jack, Dice.Queen, Dice.King, Dice.Ace)
            val straightScore = gameDomain.score(straight)
            println("Straight score: $straightScore")

            val threeOfAKind = listOf(Dice.King, Dice.King, Dice.King, Dice.Ace, Dice.Jack)
            val threeOfAKindScore = gameDomain.score(threeOfAKind)
            println("Three of a kind score: $threeOfAKindScore")

            assertNotEquals(straightScore, threeOfAKindScore, "Straight should be higher than three of a kind")
            assert(straightScore > threeOfAKindScore) { "Straight should win" }
        }

        @Test
        fun testFullHouseVsThreeOfAKind() {
            val fullHouse = listOf(Dice.Jack, Dice.Jack, Dice.Jack, Dice.Queen, Dice.Queen)
            val fullHouseScore = gameDomain.score(fullHouse)
            println("Full house score: $fullHouseScore")

            val threeOfAKind = listOf(Dice.Ten, Dice.Ten, Dice.Ten, Dice.Ace, Dice.King)
            val threeOfAKindScore = gameDomain.score(threeOfAKind)
            println("Three of a kind score: $threeOfAKindScore")

            assertNotEquals(fullHouseScore, threeOfAKindScore, "Full house should be higher than three of a kind")
            assert(fullHouseScore > threeOfAKindScore) { "Full house should win" }
        }

        @Test
        fun testFullHouseVsTwoPairs() {
            val fullHouse = listOf(Dice.King, Dice.King, Dice.King, Dice.Queen, Dice.Queen)
            val fullHouseScore = gameDomain.score(fullHouse)
            println("Full house score: $fullHouseScore")

            val twoPairs = listOf(Dice.Jack, Dice.Jack, Dice.Ten, Dice.Ten, Dice.Ace)
            val twoPairsScore = gameDomain.score(twoPairs)
            println("Two pairs score: $twoPairsScore")

            assertNotEquals(fullHouseScore, twoPairsScore, "Full house should be higher than two pairs")
            assert(fullHouseScore > twoPairsScore) { "Full house should win" }
        }

        @Test
        fun testThreeOfAKindVsOnePair() {
            val threeOfAKind = listOf(Dice.Ace, Dice.Ace, Dice.Ace, Dice.King, Dice.Queen)
            val threeOfAKindScore = gameDomain.score(threeOfAKind)
            println("Three of a kind score: $threeOfAKindScore")

            val onePair = listOf(Dice.Ten, Dice.Ten, Dice.King, Dice.Ace, Dice.Queen)
            val onePairScore = gameDomain.score(onePair)
            println("One pair score: $onePairScore")

            assertNotEquals(threeOfAKindScore, onePairScore, "Three of a kind should be higher than one pair")
            assert(threeOfAKindScore > onePairScore) { "Three of a kind should win" }
        }

        @Test
        fun testStraightVsTwoPairs() {
            val straight = listOf(Dice.Ten, Dice.Jack, Dice.Queen, Dice.King, Dice.Ace)
            val straightScore = gameDomain.score(straight)
            println("Straight score: $straightScore")

            val twoPairs = listOf(Dice.Queen, Dice.Queen, Dice.King, Dice.King, Dice.Ace)
            val twoPairsScore = gameDomain.score(twoPairs)
            println("Two pairs score: $twoPairsScore")

            assertNotEquals(straightScore, twoPairsScore, "Straight should be higher than two pairs")
            assert(straightScore > twoPairsScore) { "Straight should win" }
        }
    }

    @Nested
    inner class SameCombinationsDifferentFaces {
        @Test
        fun testPairVsPair() {
            val firstCombination = listOf(Dice.Ten, Dice.Queen, Dice.Ace, Dice.King, Dice.Ten)
            val firstScore = gameDomain.score(firstCombination)
            println("first $firstScore")

            val secondCombination = listOf(Dice.Ace, Dice.King, Dice.Ten, Dice.Queen, Dice.Queen)
            val secondScore = gameDomain.score(secondCombination)
            println("second $secondScore")

            assertNotEquals(firstScore, secondScore, "Must be different")
        }

        @Test
        fun testTwoPairsVsTwoPairs() {
            val firstCombination = listOf(Dice.Ten, Dice.Ten, Dice.King, Dice.King, Dice.Ace)
            val firstScore = gameDomain.score(firstCombination)
            println("first $firstScore")

            val secondCombination = listOf(Dice.Queen, Dice.Queen, Dice.King, Dice.King, Dice.Ace)
            val secondScore = gameDomain.score(secondCombination)
            println("second $secondScore")

            assertNotEquals(firstScore, secondScore, "Must be different")
        }

        @Test
        fun testTwoPairsVsTwoPairsExpecific() {
            val firstCombination = listOf(Dice.Nine, Dice.Nine, Dice.King, Dice.King, Dice.Ace)
            val firstScore = gameDomain.score(firstCombination)
            println("first $firstScore")

            val secondCombination = listOf(Dice.Ten, Dice.Ten, Dice.Jack, Dice.Jack, Dice.Ace)
            val secondScore = gameDomain.score(secondCombination)
            println("second $secondScore")

            assertNotEquals(firstScore, secondScore, "Must be different")
        }

        @Test
        fun testThreeOfAKindVsThreeOfAKind() {
            val firstCombination = listOf(Dice.Ten, Dice.Ten, Dice.Ten, Dice.Ace, Dice.King)
            val firstScore = gameDomain.score(firstCombination)
            println("first $firstScore")

            val secondCombination = listOf(Dice.Queen, Dice.Queen, Dice.Queen, Dice.King, Dice.Ace)
            val secondScore = gameDomain.score(secondCombination)
            println("second $secondScore")

            assertNotEquals(firstScore, secondScore, "Must be different")
        }

        @Test
        fun testFullHouseVsFullHouse() {
            val firstCombination = listOf(Dice.Jack, Dice.Jack, Dice.Jack, Dice.Queen, Dice.Queen)
            val firstScore = gameDomain.score(firstCombination)
            println("first $firstScore")

            val secondCombination = listOf(Dice.Ace, Dice.Ace, Dice.Ace, Dice.King, Dice.King)
            val secondScore = gameDomain.score(secondCombination)
            println("second $secondScore")

            assertNotEquals(firstScore, secondScore, "Must be different")
        }

        @Test
        fun testFourOfAKindVsFourOfAKind() {
            val firstCombination = listOf(Dice.Ten, Dice.Ten, Dice.Ten, Dice.Ten, Dice.Ace)
            val firstScore = gameDomain.score(firstCombination)
            println("first $firstScore")

            val secondCombination = listOf(Dice.King, Dice.King, Dice.King, Dice.King, Dice.Queen)
            val secondScore = gameDomain.score(secondCombination)
            println("second $secondScore")

            assertNotEquals(firstScore, secondScore, "Must be different")
        }

        @Test
        fun testFiveOfAKindVsFiveOfAKind() {
            val firstCombination = listOf(Dice.King, Dice.King, Dice.King, Dice.King, Dice.King)
            val firstScore = gameDomain.score(firstCombination)
            println("first $firstScore")

            val secondCombination = listOf(Dice.Ace, Dice.Ace, Dice.Ace, Dice.Ace, Dice.Ace)
            val secondScore = gameDomain.score(secondCombination)
            println("second $secondScore")

            assertNotEquals(firstScore, secondScore, "Must be different")
        }

        @Test
        fun testStraightVsStraight() {
            val firstCombination = listOf(Dice.Ten, Dice.Jack, Dice.Queen, Dice.King, Dice.Ace)
            val firstScore = gameDomain.score(firstCombination)
            println("first $firstScore")

            val secondCombination = listOf(Dice.Nine, Dice.Ten, Dice.Jack, Dice.Queen, Dice.King)
            val secondScore = gameDomain.score(secondCombination)
            println("second $secondScore")

            assertNotEquals(firstScore, secondScore, "Must be different")
        }
    }

     */
}
