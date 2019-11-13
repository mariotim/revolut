package de.marat.revolut.db

import de.marat.revolut.model.Client
import de.marat.revolut.model.Money

interface Bank {
    suspend fun createClient(email: String)
    suspend fun balance(client: Client): Money
    suspend fun deposit(client: Client, money: Money)
    suspend fun withdraw(client: Client, money: Money)
    suspend fun transfer(sender: Client, receiver: Client, amountToSend: Money)
}
