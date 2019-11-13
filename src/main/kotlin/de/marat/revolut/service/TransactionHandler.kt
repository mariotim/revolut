package de.marat.revolut.service

import de.marat.revolut.db.AlreadyExistException
import de.marat.revolut.db.BankDao
import de.marat.revolut.db.ClientNotFoundException
import de.marat.revolut.db.InsufficientFundsException
import de.marat.revolut.model.Client
import de.marat.revolut.model.Money
import de.marat.revolut.model.NegativeAmountException
import io.ktor.application.ApplicationCall
import io.ktor.http.HttpStatusCode
import io.ktor.response.respond
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

class TransactionHandler {
    private val bank = BankDao.getInstance()

    suspend fun createClient(call: ApplicationCall) = runBlocking {
        try {
            val client = extractClientFromParam(call, "email")
            bank.createClient(client.email)
            call.respond(HttpStatusCode.Created)
        } catch (ex: AlreadyExistException) {
            call.respond(HttpStatusCode.Conflict, ErrorMessage(ex.message))
        }
    }

    suspend fun balance(call: ApplicationCall) = runBlocking {
        try {
            val client = extractClientFromParam(call, "email")
            val balance = bank.balance(client)
            call.respond(HttpStatusCode.OK, balance)
        } catch (ex: ClientNotFoundException) {
            respondNoSuchClient(call, ex)
        } catch (ex: Exception) {
            respondUnhandledException(call, ex)
        }
    }

    suspend fun deposit(call: ApplicationCall) {
        coroutineScope {
            launch {
                try {
                    val client = extractClientFromParam(call, "email")
                    val amount = extractMoneyFromParam(call)
                    bank.deposit(client, amount)
                    call.respond(HttpStatusCode.OK)
                } catch (ex: ClientNotFoundException) {
                    respondNoSuchClient(call, ex)
                } catch (ex: NegativeAmountException) {
                    respondNegativeNumber(call, ex)
                } catch (ex: Exception) {
                    respondUnhandledException(call, ex)
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
                } catch (ex: ClientNotFoundException) {
                    respondNoSuchClient(call, ex)
                } catch (ex: InsufficientFundsException) {
                    respondNotEnoughMoney(call, ex)
                } catch (ex: Exception) {
                    respondUnhandledException(call, ex)
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
                } catch (ex: InsufficientFundsException) {
                    respondNotEnoughMoney(call, ex)
                } catch (ex: ClientNotFoundException) {
                    respondNoSuchClient(call, ex)
                } catch (unhandledException: Exception) {
                    respondUnhandledException(call, unhandledException)
                }
            }
        }

    }

    private suspend fun respondNoSuchClient(call: ApplicationCall, ex: Exception) {
        call.respond(HttpStatusCode.NotFound, ErrorMessage(ex.message))
    }

    private suspend fun respondNotEnoughMoney(call: ApplicationCall, ex: Exception) {
        call.respond(HttpStatusCode.Forbidden, ErrorMessage(ex.message))
    }

    private suspend fun respondNegativeNumber(call: ApplicationCall, ex: Exception) {
        call.respond(HttpStatusCode.BadRequest, ErrorMessage(ex.message))
    }

    private suspend fun respondUnhandledException(call: ApplicationCall, ex: Exception) {
        ex.printStackTrace()
        call.respond(HttpStatusCode.NotImplemented, ErrorMessage("Unhandled exception"))
    }

    private fun extractClientFromParam(call: ApplicationCall, param: String) =
            Client(call.parameters[param].toString())

    private fun extractMoneyFromParam(call: ApplicationCall) =
            Money(call.parameters["balance"]!!.toBigDecimal())


}

data class ErrorMessage(val error: String?)
