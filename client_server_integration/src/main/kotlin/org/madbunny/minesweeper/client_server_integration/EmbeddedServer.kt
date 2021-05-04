package org.madbunny.minesweeper.client_server_integration

import io.ktor.server.engine.*
import io.ktor.server.netty.*
import kotlinx.coroutines.*
import kotlinx.coroutines.time.delay
import org.madbunny.minesweeper.client.Client
import org.madbunny.minesweeper.server.Server
import java.time.Duration

internal class EmbeddedServer(
    val host: String,
    val port: Int,
    config: Server.Builder.() -> Unit,
) {
    constructor(host: String, port: Int) : this(host, port, {})

    private companion object {
        const val GRACE_PERIOD_MS = 500L
        const val TIMEOUT_MS = 1000L
        val PING_PERIOD: Duration = Duration.ofMillis(100)
    }

    private val server: Server
    private val client = Client.Builder().setHost(host).setPort(port).build()
    private var app: ApplicationEngine? = null

    init {
        val builder = Server.Builder()
        config(builder)
        server = builder.build()
    }

    fun start(): EmbeddedServer {
        app = embeddedServer(Netty, host = this@EmbeddedServer.host, port = this@EmbeddedServer.port) {
            server.run(this)
        }
        GlobalScope.launch {
            app?.start(true)
        }
        return this
    }

    fun waitAlive(): EmbeddedServer {
        runBlocking {
            while (!client.ping()) {
                delay(PING_PERIOD)
            }
        }
        return this
    }

    fun stop() {
        app?.stop(GRACE_PERIOD_MS, TIMEOUT_MS)
    }
}
