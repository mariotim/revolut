package de.marat.revolut.model

import java.math.BigDecimal

data class Money(var amount: BigDecimal = BigDecimal.ZERO) {
    init {
        if (amount < BigDecimal.ZERO) {
            throw NegativeAmountException("Illegal argument: amount cannot be negative.")
        }
    }

    fun add(money: Money): Money {
        amount = amount.add(money.amount)
        return this
    }

    fun subtract(money: Money): Money {
        amount = amount.minus(money.amount)
        return this

    }
}

class NegativeAmountException(message: String) : IllegalArgumentException(message)
