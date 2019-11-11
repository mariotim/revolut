package de.marat.revolut

import de.marat.revolut.model.Client
import de.marat.revolut.model.Money
import de.marat.revolut.service.ErrorMessage
import io.ktor.client.HttpClient
import io.ktor.client.engine.apache.Apache
import io.ktor.client.features.json.JacksonSerializer
import io.ktor.client.features.json.JsonFeature
import io.ktor.client.request.get
import io.ktor.client.request.put
import io.ktor.client.response.HttpResponse
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.async
import kotlinx.coroutines.runBlocking
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.math.BigDecimal

@ExperimentalCoroutinesApi
class TransactionManagementAppTest : HttpResponseConverter() {
    private lateinit var client: HttpClient
    private lateinit var transactionManagementApp: TransactionManagementApp

    @BeforeEach
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

    @AfterEach
    fun shutdown() = runBlocking {
        client.close()
        transactionManagementApp.stopServer()
    }

    @Test
    fun createUser() = runBlocking {
        val email = "user1"
        val httpResponse = createUserPutRequest(email)
        val deferredResponse = httpResponse.await()
        assertThat(deferredResponse.status.value).isEqualTo(201)
        val client: Client = convertToClient(deferredResponse)
        assertThat(client).isEqualTo(Client(email))
        return@runBlocking
    }


    @Test
    fun createUser_AlreadyExist() = runBlocking {
        val email = "user2"
        val httpResponse = createUserPutRequest(email)
        val deferredResponse = httpResponse.await()
        val httpResponse2Time = createUserPutRequest(email)
        val deferredResponse2Time = httpResponse2Time.await()

        assertThat(deferredResponse.status.value).isEqualTo(201)
        val client: Client = convertToClient(deferredResponse)
        assertThat(client).isEqualTo(Client(email))

        assertThat(deferredResponse2Time.status.value).isEqualTo(409)
        val error: ErrorMessage = convertToError(deferredResponse2Time)
        assertThat(error).isEqualTo(ErrorMessage("User $email already exist."))
        return@runBlocking
    }
    
    @Test
    fun balance() = runBlocking {
        val email = "user3"
        val httpResponse = createUserPutRequest(email)
        val deferredCreateUserResponse = httpResponse.await()
        val balanceUserResponse = balance(email)
        val deferredBalanceUserResponse = balanceUserResponse.await()

        assertThat(deferredCreateUserResponse.status.value).isEqualTo(201)
        val balance = convertToMoney(deferredBalanceUserResponse)
        assertThat(balance).isEqualTo(Money(BigDecimal.ZERO))
        return@runBlocking
    }


    private fun CoroutineScope.createUserPutRequest(email: String) =
            async { client.put<HttpResponse>(port = 8080, path = "/create/$email") }

    private fun CoroutineScope.balance(email: String) =
            async { client.get<HttpResponse>(port = 8080, path = "/balance/$email") }
}
