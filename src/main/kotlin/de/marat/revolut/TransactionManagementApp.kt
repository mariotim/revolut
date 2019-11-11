package de.marat.revolut

import com.fasterxml.jackson.core.util.DefaultIndenter
import com.fasterxml.jackson.core.util.DefaultPrettyPrinter
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.SerializationFeature
import de.marat.revolut.service.TransactionHandler
import io.ktor.application.Application
import io.ktor.application.call
import io.ktor.application.install
import io.ktor.features.ContentNegotiation
import io.ktor.jackson.jackson
import io.ktor.routing.get
import io.ktor.routing.put
import io.ktor.routing.routing
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import io.ktor.server.netty.NettyApplicationEngine
import java.util.concurrent.TimeUnit

class TransactionManagementApp {
    private val server: NettyApplicationEngine

    init {
        val transactionHandler = TransactionHandler()
        server = initEmbeddedServer(transactionHandler)
    }

    fun startServer() {
        server.start()
    }

    fun stopServer() {
        server.stop(0, 0, TimeUnit.MILLISECONDS)
    }

    private fun initEmbeddedServer(transactionHandler: TransactionHandler): NettyApplicationEngine {
        return embeddedServer(Netty, port = 8080) {
            install(ContentNegotiation) {
                jackson {
                    jacksonConfiguration()
                }
                routing(transactionHandler)
            }
        }
    }

    private fun Application.routing(transactionHandler: TransactionHandler) {
        routing {
            put("/create/{email}") {
                transactionHandler.createUser(call)
            }
            get("/balance/{email}") {
                transactionHandler.balance(call)
            }
        }
    }

    private fun ObjectMapper.jacksonConfiguration() {
        configure(SerializationFeature.INDENT_OUTPUT, true)
        setDefaultPrettyPrinter(DefaultPrettyPrinter().apply {
            indentArraysWith(DefaultPrettyPrinter.FixedSpaceIndenter.instance)
            indentObjectsWith(DefaultIndenter("  ", "\n"))
        })
    }
}

fun main() {
    TransactionManagementApp().startServer()
}

