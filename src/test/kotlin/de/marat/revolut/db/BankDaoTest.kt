package de.marat.revolut.db

import de.marat.revolut.model.Client
import de.marat.revolut.model.Money
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.math.BigDecimal

class BankDaoTest {
    private lateinit var bank: Bank

    @BeforeEach
    fun init() {
        bank = BankDao.getInstance()
    }

    @Test
    fun constructor() {
        assertThat(bank).isNotNull
    }

    @Test
    fun createClient() {
        bank.createClient("client")
    }

    @Test
    fun createClient_AlreadyExist() {
        val email = "client1"
        bank.createClient(email)
        assertThrows<AlreadyExistException> { bank.createClient(email) }
    }

    @Test
    fun deposit() {
        val email = "email1"
        prepareBankAccountWithHundredBux(email)
        bank.deposit(Client(email), Money(BigDecimal.valueOf(50.0)))
        assertThat(bank.balance(Client(email))).isEqualTo(Money(BigDecimal.valueOf(150.0)))
    }

    @Test
    fun deposit_notFoundClient() {
        assertThrows<ClientNotFoundException> { bank.deposit(Client("not_found"), Money()) }
    }

    @Test
    fun withdraw() {
        val client = "test"
        prepareBankAccountWithHundredBux(client)
        bank.withdraw(Client(client), Money(BigDecimal.valueOf(50.0)))
        assertThat(bank.balance(Client(client))).isEqualTo(Money(BigDecimal.valueOf(50.0)))
    }

    @Test
    fun withdraw_notFoundClient() {
        prepareBankAccountWithHundredBux("email_1")
        assertThrows<ClientNotFoundException> { bank.withdraw(Client("email_2"), Money()) }
    }

    @Test
    fun withdraw_notEnoughMoney() {
        val email = "schnulibuh"
        prepareBankAccountWithHundredBux(email)
        val tooMuchMoneyToTake = BigDecimal.valueOf(150)
        assertThrows<InsufficientFundsException> { bank.withdraw(Client(email), Money(tooMuchMoneyToTake)) }
    }

    @Test
    fun transfer() {
        val emailMami = "mami"
        prepareBankAccountWithHundredBux(emailMami)
        val emailPapi = "papi"
        prepareBankAccountWithHundredBux(emailPapi)
        val amountToSend = BigDecimal.valueOf(10.0)
        val sender = Client(emailMami)
        val receiver = Client(emailPapi)
        bank.transfer(sender, receiver, Money(amountToSend))
        val expectedMoneyOfSender = BigDecimal.valueOf(90.0)
        val expectedMoneyOfReceiver = BigDecimal.valueOf(110.0)
        assertThat(bank.balance(sender)).isEqualTo(Money(expectedMoneyOfSender))
        assertThat(bank.balance(receiver)).isEqualTo(Money(expectedMoneyOfReceiver))
    }

    private fun prepareBankAccountWithHundredBux(email: String) {
        bank.createClient(email)
        bank.deposit(Client(email), Money())
        bank.deposit(Client(email), Money(BigDecimal.valueOf(100.0)))
    }
}
