package de.marat.revolut.db

import de.marat.revolut.model.Client
import de.marat.revolut.model.Money
import de.marat.revolut.model.NegativeAmountException
import kotlinx.coroutines.runBlocking
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
    fun createClient() = runBlocking {
        bank.createClient("client")
    }

    @Test
    fun createClient_AlreadyExist() = runBlocking {
        val email = "client1"
        bank.createClient(email)
        assertThrows<AlreadyExistException> { runBlocking { bank.createClient(email) } }
        return@runBlocking
    }

    @Test
    fun deposit() = runBlocking {
        val email = "email1"
        prepareBankAccountWithHundredBux(email)
        bank.deposit(Client(email), Money(BigDecimal.valueOf(50.0)))
        assertThat(bank.balance(Client(email))).isEqualTo(Money(BigDecimal.valueOf(150.0)))
        return@runBlocking
    }

    @Test
    fun deposit_negative() = runBlocking {
        val email = "email1111"
        prepareBankAccountWithHundredBux(email)
        assertThrows<NegativeAmountException> {
            runBlocking {
                bank.deposit(Client(email), Money(BigDecimal.valueOf(-50.0)))
            }
        }
        return@runBlocking
    }

    @Test
    fun deposit_notFoundClient() = runBlocking {
        assertThrows<ClientNotFoundException> {
            runBlocking {
                bank.deposit(Client("not_found"), Money())
            }
        }
        return@runBlocking
    }

    @Test
    fun withdraw() = runBlocking {
        val client = "test"
        prepareBankAccountWithHundredBux(client)
        bank.withdraw(Client(client), Money(BigDecimal.valueOf(50.0)))
        assertThat(bank.balance(Client(client))).isEqualTo(Money(BigDecimal.valueOf(50.0)))
        return@runBlocking
    }

    @Test
    fun withdraw_notFoundClient() {
        prepareBankAccountWithHundredBux("email_1")
        assertThrows<ClientNotFoundException> {
            runBlocking {
                bank.withdraw(Client("email_2"), Money())
            }
        }
    }

    @Test
    fun withdraw_notEnoughMoney() {
        val email = "schnulibuh"
        prepareBankAccountWithHundredBux(email)
        val tooMuchMoneyToTake = BigDecimal.valueOf(150)
        assertThrows<InsufficientFundsException> {
            runBlocking {
                bank.withdraw(Client(email), Money(tooMuchMoneyToTake))
            }
        }

    }

    @Test
    fun transfer() = runBlocking {
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
        return@runBlocking
    }

    private fun prepareBankAccountWithHundredBux(email: String) = runBlocking {
        bank.createClient(email)
        bank.deposit(Client(email), Money())
        bank.deposit(Client(email), Money(BigDecimal.valueOf(100.0)))
    }
}
