package de.marat.revolut

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.KotlinModule
import com.fasterxml.jackson.module.kotlin.readValue
import de.marat.revolut.model.Client
import de.marat.revolut.service.ErrorMessage
import io.ktor.client.HttpClient
import io.ktor.client.engine.apache.Apache
import io.ktor.client.features.json.JacksonSerializer
import io.ktor.client.features.json.JsonFeature
import io.ktor.client.request.put
import io.ktor.client.response.HttpResponse
import io.ktor.http.contentLength
import kotlinx.coroutines.*
import kotlinx.coroutines.io.readFully
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

@ExperimentalCoroutinesApi
class TransactionManagementAppTest {
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
        val httpResponse = createUser(email)
        val deferredResponse = httpResponse.await()
        assertThat(deferredResponse.status.value).isEqualTo(201)
        val client: Client = convertToClient(deferredResponse)
        assertThat(client).isEqualTo(Client(email))
        return@runBlocking
    }


    @Test
    fun createUser_AlreadyExist_SomeoneTriedToUseSameMailSameTime() = runBlocking {
        val email = "user2"
        val httpResponse = createUser(email)
        delay(100)
        val httpResponse2Time = createUser(email)
        val deferredResponse = httpResponse.await()
        val deferredResponse2Time = httpResponse2Time.await()
        assertThat(deferredResponse.status.value).isEqualTo(201)
        assertThat(deferredResponse2Time.status.value).isEqualTo(409)
        val error: ErrorMessage = convertToError(deferredResponse2Time)
        assertThat(error).isEqualTo(ErrorMessage("User $email already exist."))
        return@runBlocking
    }

    @Test
    fun balance() {
//        val email = "user3"

    }

    private suspend fun convertToClient(deferredResponse: HttpResponse): Client {
        val mapper = ObjectMapper().registerModule(KotlinModule())
        val byteArray = ByteArray(deferredResponse.contentLength()!!.toInt())
        deferredResponse.content.readFully(byteArray)
        return mapper.readValue(byteArray)
    }

    private suspend fun convertToError(deferredResponse: HttpResponse): ErrorMessage {
        val mapper = ObjectMapper().registerModule(KotlinModule())
        val byteArray = ByteArray(deferredResponse.contentLength()!!.toInt())
        deferredResponse.content.readFully(byteArray)
        return mapper.readValue(byteArray)
    }

    private fun CoroutineScope.createUser(email: String) =
            async { client.put<HttpResponse>(port = 8080, path = "/create/$email") }
}
