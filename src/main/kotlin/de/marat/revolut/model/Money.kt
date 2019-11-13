package de.marat.revolut.model

import java.math.BigDecimal

data class Money(var balance: BigDecimal = BigDecimal.ZERO) {
    init {
        if (balance < BigDecimal.ZERO) {
            throw NegativeAmountException("Illegal argument: balance cannot be negative.")
        }
    }

    fun add(money: Money): Money {
        balance = balance.add(money.balance)
        return this
    }

    fun subtract(money: Money): Money {
        balance = balance.minus(money.balance)
        return this

    }
}

class NegativeAmountException(message: String) : IllegalArgumentException(message)
