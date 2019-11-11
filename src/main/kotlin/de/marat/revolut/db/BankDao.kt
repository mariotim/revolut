package de.marat.revolut.db

import de.marat.revolut.model.Client
import de.marat.revolut.model.Money
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ConcurrentMap


class BankDao private constructor() : Bank {
    private val bankAccounts: ConcurrentMap<Client, Money> = ConcurrentHashMap()

    //    @Synchronized
    override fun createClient(email: String) {
        val client = Client(email)
        if (bankAccounts[client] != null)
            throw AlreadyExistException()
        bankAccounts[client] = Money()
    }

    @Synchronized
    override fun balance(client: Client): Money {
        return bankAccounts.getOrElse(client, { throw ClientNotFoundException() })
    }

    @Synchronized
    override fun deposit(client: Client, money: Money) {
        if (bankAccounts.computeIfPresent(client) { _, u -> u.add(money) } == null) {
            throw ClientNotFoundException()
        }
    }

    @Synchronized
    override fun withdraw(client: Client, money: Money) {
        if (bankAccounts.computeIfPresent(client) { _, balance -> computeWithdrawal(balance, money) } == null) {
            throw ClientNotFoundException()
        }
    }

    @Synchronized
    override fun transfer(sender: Client, receiver: Client, amountToSend: Money) {
        balance(receiver)
        withdraw(sender, amountToSend)
        deposit(receiver, amountToSend)
    }


    companion object {
        private var INSTANCE: BankDao? = null
        fun getInstance(): BankDao {
            synchronized(this) {
                if (INSTANCE == null) {
                    INSTANCE = BankDao()
                }
                return INSTANCE!!
            }
        }

        private fun isEnoughMoney(balance: Money, requestedMoney: Money) {
            if (balance.amount <= requestedMoney.amount) {
                throw InsufficientFundsException()
            }
        }

        private fun computeWithdrawal(balance: Money, money: Money): Money {
            isEnoughMoney(balance, money);return balance.substract(money)
        }
    }
}

class AlreadyExistException : IllegalStateException()
class ClientNotFoundException : IllegalStateException()
class InsufficientFundsException : IllegalStateException()
