package de.marat.revolut

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
class BankAccountManagerAppTest : HttpResponseConverter() {
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
    fun createClient() = runBlocking {
        assertClientCreated("client_1")
        return@runBlocking
    }


    @Test
    fun createClient_AlreadyExist() = runBlocking {
        val email = "client2"
        assertClientCreated(email)

        val deferredResponse2Time = createClientAsync(email)
        assertThat(deferredResponse2Time.status).isEqualTo(HttpStatusCode.Conflict)
        val error: ErrorMessage = convertToError(deferredResponse2Time)
        assertThat(error).isEqualTo(ErrorMessage("Client $email already exist."))
        return@runBlocking
    }

    @Test
    fun balance() {
        runBlocking {
            val email = "client3"
            assertClientCreated(email)
            assertBalance(email, BigDecimal.ZERO)
            return@runBlocking
        }
    }

    @Test
    fun balance_ClientNotFound() = runBlocking {
        val balance = balanceAsync("non_existing")
        assertThat(balance.status).isEqualTo(HttpStatusCode.NotFound)
        val error: ErrorMessage = convertToError(balance)
        assertThat(error).isEqualTo(ErrorMessage("Client non_existing does not exist."))
        return@runBlocking
    }

    @Test
    fun deposit() = runBlocking {
        val email = "client4"
        val amount = BigDecimal(100.0)
        assertClientCreated(email)
        assertDepositSuccessful(email, amount)
        return@runBlocking
    }


    @Test
    fun deposit_negative() = runBlocking {
        val email = "client4444"
        assertClientCreated(email)
        val deposit = depositAsync(email, BigDecimal(-1000.0))
        val error: ErrorMessage = convertToError(deposit)
        assertThat(deposit.status).isEqualTo(HttpStatusCode.BadRequest)
        assertThat(error).isEqualTo(ErrorMessage("Illegal argument: balance cannot be negative."))
        return@runBlocking
    }

    @Test
    fun transfer() = runBlocking {
        val sender = "client5"
        val amountSenderHas = BigDecimal(100.0)
        val receiver = "client6"
        val amountReceiverhas = BigDecimal(50.0)
        assertClientCreated(sender)
        assertClientCreated(receiver)
        assertDepositSuccessful(sender, amountSenderHas)
        val transfer = transferAsync(sender, receiver, BigDecimal(50.0))
        assertThat(transfer.status).isEqualTo(HttpStatusCode.OK)
        assertBalance(receiver, amountReceiverhas)
    }


    @Test
    fun transfer_InsufficientFunds() = runBlocking {
        val sender = "client7"
        val amountSenderHas = BigDecimal(100.0)
        val receiver = "client8"
        assertClientCreated(sender)
        assertClientCreated(receiver)
        assertDepositSuccessful(sender, amountSenderHas)
        val transfer = transferAsync(sender, receiver, BigDecimal(1000.0))
        val error: ErrorMessage = convertToError(transfer)
        assertThat(transfer.status).isEqualTo(HttpStatusCode.Forbidden)
        assertThat(error).isEqualTo(ErrorMessage("Insufficient funds"))
        return@runBlocking
    }


    @Test
    fun transfer_ReceiverNotFound() = runBlocking {
        val sender = "client11111"
        val amountSenderHas = BigDecimal(100.0)
        val receiver = "not_existing_receiver"
        assertClientCreated(sender)
        assertDepositSuccessful(sender, amountSenderHas)
        val transfer = transferAsync(sender, receiver, BigDecimal(10.0))
        val error: ErrorMessage = convertToError(transfer)
        assertThat(transfer.status).isEqualTo(HttpStatusCode.NotFound)
        assertThat(error).isEqualTo(ErrorMessage("Client $receiver does not exist."))
        return@runBlocking
    }



    @Test
    fun withdraw() = runBlocking {
        val email = "client9"
        assertClientCreated(email)
        depositAsync(email, BigDecimal(1000))
        val withdraw = withdrawAsync(email, BigDecimal(100))
        assertThat(withdraw.status).isEqualTo(HttpStatusCode.OK)
        return@runBlocking
    }

    @Test
    fun withdraw_InsufficientFunds() = runBlocking {
        val email = "client10"
        assertClientCreated(email)
        val withdraw = withdrawAsync(email, BigDecimal(100))
        val error: ErrorMessage = convertToError(withdraw)
        assertThat(withdraw.status).isEqualTo(HttpStatusCode.Forbidden)
        assertThat(error).isEqualTo(ErrorMessage("Insufficient funds"))
        return@runBlocking
    }

    private suspend fun assertClientCreated(email: String) {
        val createClientResponse = createClientAsync(email)
        assertThat(createClientResponse.status).isEqualTo(HttpStatusCode.Created)
    }

    private suspend fun assertBalance(email: String, amount: BigDecimal) {
        val balanceResponse = balanceAsync(email)
        assertThat(balanceResponse.status).isEqualTo(HttpStatusCode.OK)
        val balance = convertToMoney(balanceResponse)
        assertThat(balance).isEqualTo(Money(amount))
    }


    private suspend fun assertDepositSuccessful(email: String, amount: BigDecimal) {
        val depositResponse = depositAsync(email, amount)
        assertThat(depositResponse.status).isEqualTo(HttpStatusCode.OK)
    }

    private suspend fun createClientAsync(email: String) =
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
