package de.marat.revolut.service

import de.marat.revolut.db.AlreadyExistException
import de.marat.revolut.db.BankDao
import de.marat.revolut.model.Client
import de.marat.revolut.model.Money
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

    suspend fun deposit(call: ApplicationCall) = runBlocking {
        val client = extractClientFromParam(call)
        val amount = extractMoneyFromParam(call)
        bank.deposit(client, amount)
        call.respond(HttpStatusCode.OK)
    }

    private fun extractClientFromParam(call: ApplicationCall) =
            Client(call.parameters["email"].toString())

    private fun extractMoneyFromParam(call: ApplicationCall) =
            Money(call.parameters["amount"]!!.toBigDecimal())


}

data class ErrorMessage(val error: String)
