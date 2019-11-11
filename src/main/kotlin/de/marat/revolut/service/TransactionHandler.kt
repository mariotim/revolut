package de.marat.revolut.service

import de.marat.revolut.db.AlreadyExistException
import de.marat.revolut.db.BankDao
import de.marat.revolut.model.Client
import io.ktor.application.ApplicationCall
import io.ktor.http.HttpStatusCode
import io.ktor.response.respond
import kotlinx.coroutines.runBlocking

class TransactionHandler {
    private val bank = BankDao.getInstance()

    suspend fun createUser(call: ApplicationCall) = runBlocking {
        val client = extractClientFromParam(call)
        try {
            bank.createClient(client.email)
            call.respond(HttpStatusCode.Created, client)
        } catch (ex: AlreadyExistException) {
            call.respond(HttpStatusCode.Conflict, ErrorMessage("User ${client.email} already exist."))
        }
    }

    suspend fun balance(call: ApplicationCall) = runBlocking {
        val client = extractClientFromParam(call)
        val balance = bank.balance(client)
        call.respond(HttpStatusCode.OK, balance)
    }

    private fun extractClientFromParam(call: ApplicationCall) =
            Client(call.parameters["email"].toString())
}

data class ErrorMessage(val error: String)
