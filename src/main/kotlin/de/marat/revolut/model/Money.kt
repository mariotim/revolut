package de.marat.revolut.model

import java.math.BigDecimal

data class Money(var amount: BigDecimal = BigDecimal.ZERO) {
    fun add(money: Money): Money {
        amount = amount.add(money.amount)
        return this
    }

    fun substract(money: Money): Money {
        amount = amount.minus(money.amount)
        return this

    }
}
