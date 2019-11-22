package de.marat.revolut.db

import de.marat.revolut.model.Client
import de.marat.revolut.model.Money
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ConcurrentMap


object BankDao : Bank {
    private val bankAccounts: ConcurrentMap<Client, Money> = ConcurrentHashMap()

    override suspend fun createClient(email: String) {

        val client = Client(email)
        if (bankAccounts.containsKey(client))
            throw AlreadyExistException("Client ${client.email} already exist.")
        bankAccounts[client] = Money()
    }

    override suspend fun balance(client: Client): Money {
        return bankAccounts.getOrElse(client, { throw ClientNotFoundException("Client ${client.email} does not exist.") })
    }

    override suspend fun deposit(client: Client, money: Money) {
        if (bankAccounts.computeIfPresent(client) { _, u -> u.add(money) } == null) {
            throw ClientNotFoundException("Client ${client.email} does not exist.")
        }
    }

    override suspend fun withdraw(client: Client, money: Money) {
        if (bankAccounts.computeIfPresent(client) { _, balance -> computeWithdrawal(balance, money) } == null) {
            throw ClientNotFoundException("Client ${client.email} does not exist.")
        }
    }

    override suspend fun transfer(sender: Client, receiver: Client, amountToSend: Money) {
        balance(receiver)
        withdraw(sender, amountToSend)
        deposit(receiver, amountToSend)
    }


    private fun isEnoughMoney(balance: Money, requestedMoney: Money) {
        if (balance.balance <= requestedMoney.balance) {
            throw InsufficientFundsException("Insufficient funds")
        }
    }

    private fun computeWithdrawal(balance: Money, money: Money): Money {
        isEnoughMoney(balance, money);return balance.subtract(money)
    }

}

class AlreadyExistException(message: String) : IllegalStateException(message)
class ClientNotFoundException(message: String) : IllegalStateException(message)
class InsufficientFundsException(message: String) : IllegalStateException(message)
