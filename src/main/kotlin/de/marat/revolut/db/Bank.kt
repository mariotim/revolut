package de.marat.revolut.db

import de.marat.revolut.model.Client
import de.marat.revolut.model.Money

interface Bank {
    fun createClient(email: String)
    fun balance(client: Client): Money
    fun deposit(client: Client, money: Money)
    fun withdraw(client: Client, money: Money)
    fun transfer(sender: Client, receiver: Client, amountToSend: Money)
}
