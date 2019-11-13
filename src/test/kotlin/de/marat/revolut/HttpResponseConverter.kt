package de.marat.revolut

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.KotlinModule
import com.fasterxml.jackson.module.kotlin.readValue
import de.marat.revolut.model.Client
import de.marat.revolut.model.Money
import de.marat.revolut.service.ErrorMessage
import io.ktor.client.response.HttpResponse
import io.ktor.http.contentLength
import kotlinx.coroutines.io.readFully

open class HttpResponseConverter {
    companion object {
        suspend fun convertToClient(deferredResponse: HttpResponse): Client {
            val mapper = ObjectMapper().registerModule(KotlinModule())
            val byteArray = ByteArray(deferredResponse.contentLength()!!.toInt())
            deferredResponse.content.readFully(byteArray)
            return mapper.readValue(byteArray)
        }

        suspend fun convertToError(deferredResponse: HttpResponse): ErrorMessage {
            val mapper = ObjectMapper().registerModule(KotlinModule())
            val byteArray = ByteArray(deferredResponse.contentLength()!!.toInt())
            deferredResponse.content.readFully(byteArray)
            return mapper.readValue(byteArray)
        }


        suspend fun convertToMoney(deferredResponse: HttpResponse): Money {
            val mapper = ObjectMapper().registerModule(KotlinModule())
            val byteArray = ByteArray(deferredResponse.contentLength()!!.toInt())
            deferredResponse.content.readFully(byteArray)
            return mapper.readValue(byteArray)
        }
    }
}
