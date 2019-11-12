package de.marat.revolut

import de.marat.revolut.model.Client
import de.marat.revolut.model.Money
import de.marat.revolut.service.ErrorMessage
import io.ktor.client.HttpClient
import io.ktor.client.engine.apache.Apache
import io.ktor.client.features.json.JacksonSerializer
import io.ktor.client.features.json.JsonFeature
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.request.put
import io.ktor.client.response.HttpResponse
import io.ktor.http.HttpStatusCode
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.runBlocking
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import java.math.BigDecimal

@ExperimentalCoroutinesApi
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class TransactionManagementAppTest : HttpResponseConverter() {
    private lateinit var client: HttpClient
    private lateinit var transactionManagementApp: TransactionManagementApp

    @BeforeAll
    fun init() = runBlocking {
        transactionManagementApp = TransactionManagementApp()
        transactionManagementApp.startServer()
        client = HttpClient(Apache) {
            install(JsonFeature) {
                serializer = JacksonSerializer {
                }
            }
            expectSuccess = false
        }
    }

    @AfterAll
    fun shutdown() = runBlocking {
        client.close()
        transactionManagementApp.stopServer()
    }

    @Test
    fun createUser() = runBlocking {
        assertUserCreated("email1")
        return@runBlocking
    }


    @Test
    fun createUser_AlreadyExist() = runBlocking {
        val email = "user2"
        assertUserCreated(email)

        val deferredResponse2Time = createUserAsync(email)
        assertThat(deferredResponse2Time.status).isEqualTo(HttpStatusCode.Conflict)
        val error: ErrorMessage = convertToError(deferredResponse2Time)
        assertThat(error).isEqualTo(ErrorMessage("User $email already exist."))
        return@runBlocking
    }

    @Test
    fun balance() {
        runBlocking {
            val email = "user3"
            assertUserCreated(email)
            assertBalance(email, BigDecimal.ZERO)
            return@runBlocking
        }
    }

    @Test
    fun deposit() = runBlocking {
        val email = "user4"
        val amount = BigDecimal(100.0)
        assertUserCreated(email)
        assertDepositSuccessful(email, amount)
        return@runBlocking
    }


    @Test
    fun deposit_negative() = runBlocking {
        val email = "user4444"
        assertUserCreated(email)
        val deposit = depositAsync(email, BigDecimal(-1000.0))
        val error: ErrorMessage = convertToError(deposit)
        assertThat(deposit.status).isEqualTo(HttpStatusCode.BadRequest)
        assertThat(error).isEqualTo(ErrorMessage("Illegal argument: amount cannot be negative."))
        return@runBlocking
    }

    @Test
    fun transfer() = runBlocking {
        val sender = "user5"
        val amountSenderHas = BigDecimal(100.0)
        val receiver = "user6"
        val amountReceiverhas = BigDecimal(50.0)
        assertUserCreated(sender)
        assertUserCreated(receiver)
        assertDepositSuccessful(sender, amountSenderHas)
        val transfer = transferAsync(sender, receiver, BigDecimal(50.0))
        assertThat(transfer.status).isEqualTo(HttpStatusCode.OK)
        assertBalance(receiver, amountReceiverhas)
    }


    @Test
    fun transfer_InsufficientFunds() = runBlocking {
        val sender = "user7"
        val amountSenderHas = BigDecimal(100.0)
        val receiver = "user8"
        assertUserCreated(sender)
        assertUserCreated(receiver)
        assertDepositSuccessful(sender, amountSenderHas)
        val transfer = transferAsync(sender, receiver, BigDecimal(1000.0))
        val error: ErrorMessage = convertToError(transfer)
        assertThat(transfer.status).isEqualTo(HttpStatusCode.BadRequest)
        assertThat(error).isEqualTo(ErrorMessage("Insufficient funds"))
        return@runBlocking
    }


    @Test
    fun withdraw() = runBlocking {
        val email = "user9"
        assertUserCreated(email)
        depositAsync(email, BigDecimal(1000))
        val withdraw = withdrawAsync(email, BigDecimal(100))
        assertThat(withdraw.status).isEqualTo(HttpStatusCode.OK)
        return@runBlocking
    }

    @Test
    fun withdraw_InsufficientFunds() = runBlocking {
        val email = "user10"
        assertUserCreated(email)
        val withdraw = withdrawAsync(email, BigDecimal(100))
        val error: ErrorMessage = convertToError(withdraw)
        assertThat(withdraw.status).isEqualTo(HttpStatusCode.BadRequest)
        assertThat(error).isEqualTo(ErrorMessage("Insufficient funds"))
        return@runBlocking
    }

    private suspend fun assertUserCreated(email: String) {
        val createUserResponse = createUserAsync(email)
        assertThat(createUserResponse.status).isEqualTo(HttpStatusCode.Created)
        val client: Client = convertToClient(createUserResponse)
        assertThat(client).isEqualTo(Client(email))
    }

    private suspend fun assertBalance(email: String, amount: BigDecimal) {
        val balanceUserResponse = balanceAsync(email)
        val balance = convertToMoney(balanceUserResponse)
        assertThat(balance).isEqualTo(Money(amount))
    }


    private suspend fun assertDepositSuccessful(email: String, amount: BigDecimal) {
        val depositUserResponse = depositAsync(email, amount)
        assertThat(depositUserResponse.status).isEqualTo(HttpStatusCode.OK)
    }

    private suspend fun createUserAsync(email: String) =
            coroutineScope { async { client.put<HttpResponse>(port = 8080, path = "/create/$email") }.await() }

    private suspend fun balanceAsync(email: String) =
            coroutineScope { async { client.get<HttpResponse>(port = 8080, path = "/balance/$email") }.await() }

    private suspend fun depositAsync(email: String, amount: BigDecimal) =
            coroutineScope { async { client.post<HttpResponse>(port = 8080, path = "/deposit/$email/$amount") }.await() }

    private suspend fun withdrawAsync(email: String, amount: BigDecimal) =
            coroutineScope { async { client.post<HttpResponse>(port = 8080, path = "/withdraw/$email/$amount") }.await() }

    private suspend fun transferAsync(sender: String, receiver: String, amount: BigDecimal) =
            coroutineScope { async { client.post<HttpResponse>(port = 8080, path = "/transfer/$sender/$receiver/$amount") }.await() }
}
