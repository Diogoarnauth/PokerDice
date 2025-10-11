package pt.isel.daw.pokerDice.repository

interface TransactionManager {
    fun <R> run(block: (Transaction) -> R): R
}
