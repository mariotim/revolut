package de.marat.revolut.service

import de.marat.revolut.db.BankDao
import de.marat.revolut.model.Client
import de.marat.revolut.model.Money
import io.ktor.application.ApplicationCall
import io.ktor.http.HttpStatusCode
import io.ktor.response.respond
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

class TransactionHandler {
    private val bank = BankDao.getInstance()

    suspend fun createUser(call: ApplicationCall) = runBlocking {
        try {
            val client = extractClientFromParam(call, "email")
            bank.createClient(client.email)
            call.respond(HttpStatusCode.Created, client)
        } catch (ex: Exception) {
            respondWithError(call, ex)
        }
    }

    suspend fun balance(call: ApplicationCall) = runBlocking {
        val client = extractClientFromParam(call, "email")
        val balance = bank.balance(client)
        call.respond(HttpStatusCode.OK, balance)
    }

    suspend fun deposit(call: ApplicationCall) {
        coroutineScope {
            launch {
                try {
                    val client = extractClientFromParam(call, "email")
                    val amount = extractMoneyFromParam(call)
                    bank.deposit(client, amount)
                    call.respond(HttpStatusCode.OK)
                } catch (ex: Exception) {
                    respondWithError(call, ex)
                }
            }

        }
    }

    suspend fun withdraw(call: ApplicationCall) {
        coroutineScope {
            launch {
                try {
                    val client = extractClientFromParam(call, "email")
                    val amount = extractMoneyFromParam(call)
                    bank.withdraw(client, amount)
                    call.respond(HttpStatusCode.OK)
                } catch (ex: Exception) {
                    respondWithError(call, ex)
                }
            }
        }
    }


    suspend fun transfer(call: ApplicationCall) {
        coroutineScope {
            launch {
                try {
                    val sender = extractClientFromParam(call, "sender")
                    val receiver = extractClientFromParam(call, "receiver")
                    val amount = extractMoneyFromParam(call)
                    bank.transfer(sender, receiver, amount)
                    call.respond(HttpStatusCode.OK)
                } catch (ex: Exception) {
                    respondWithError(call, ex)
                }
            }
        }

    }

    private suspend fun respondWithError(call: ApplicationCall, ex: Exception) {
        call.respond(HttpStatusCode.BadRequest, ErrorMessage(ex.message))
    }

    private fun extractClientFromParam(call: ApplicationCall, param: String) =
            Client(call.parameters[param].toString())

    private fun extractMoneyFromParam(call: ApplicationCall) =
            Money(call.parameters["amount"]!!.toBigDecimal())


}

data class ErrorMessage(val error: String?)
