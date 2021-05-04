package org.madbunny.minesweeper.client

import io.ktor.client.*
import io.ktor.client.features.websocket.*
import io.ktor.http.*
import io.ktor.http.cio.websocket.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.ClosedReceiveChannelException
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import kotlinx.coroutines.time.withTimeoutOrNull
import org.madbunny.minesweeper.client.exception.ServerClosedConnectionException
import org.madbunny.minesweeper.client.exception.ServerIncorrectResponseFormatException
import org.madbunny.minesweeper.client.exception.ServerTimedOutException
import org.madbunny.minesweeper.client.exception.ServerUnableToStartSessionException
import org.madbunny.minesweeper.client.session.GameSession
import org.madbunny.minesweeper.client.session.GameSessionImpl
import org.madbunny.minesweeper.core.json.JsonWrapper
import org.madbunny.minesweeper.core.register.request.RegisterRequest
import org.madbunny.minesweeper.core.register.response.RegisterResult
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.time.Duration

class Client private constructor(
    private val host: String,
    private val port: Int,
    private val registerTimeout: Duration,
) {
    private companion object {
        const val DEFAULT_HOST = "0.0.0.0"
        const val DEFAULT_PORT = 8080
        val DEFAULT_PING_TIMEOUT: Duration = Duration.ofSeconds(1)
        val DEFAULT_REGISTER_TIMEOUT: Duration = Duration.ofSeconds(15)

        val LOGGER: Logger = LoggerFactory.getLogger(Client::class.java.simpleName)
    }

    class Builder {
        private var host = DEFAULT_HOST
        private var port = DEFAULT_PORT
        private var registerTimeout = DEFAULT_REGISTER_TIMEOUT

        fun setHost(value: String): Builder { host = value; return this }
        fun setPort(value: Int): Builder { port = value; return this }
        fun setRegisterTimeout(value: Duration): Builder { registerTimeout = value; return this }

        fun build() = Client(
            host,
            port,
            registerTimeout,
        )
    }

    private val client = HttpClient {
        install(WebSockets)
    }
    private val json = JsonWrapper.createOverGson()

    init {
        LOGGER.debug("A client is ready. The target is: $host:$port")
    }

    fun ping(timeout: Duration = DEFAULT_PING_TIMEOUT): Boolean {
        var pingOk = false
        try {
            runBlocking {
                withContext(Dispatchers.IO) {
                    client.webSocket(HttpMethod.Get, host, port, "/ping") {
                        val frame = waitForFrame(this, timeout)
                        if (frame is Frame.Text) {
                            pingOk = frame.readText() == "pong"
                        }
                    }
                }
            }
        } catch (e: Exception) {
            pingOk = false
        }
        return pingOk
    }

    @Throws(
        ServerTimedOutException::class,
        ServerIncorrectResponseFormatException::class,
        ServerClosedConnectionException::class,
    )
    fun register(playerName: String): RegisterResult {
        LOGGER.debug("Trying to register a new player: $playerName")
        return try {
            runBlocking {
                withContext(Dispatchers.IO) {
                    var result: RegisterResult? = null
                    client.webSocket(HttpMethod.Get, host, port, "/register") {
                        send(registerRequest(playerName))
                        val frame = waitForFrame(this, registerTimeout)
                        val text = frame as? Frame.Text
                            ?: throw ServerIncorrectResponseFormatException("Expected a textual response")
                        result = asObject(text)
                    }
                    result ?: throw ServerIncorrectResponseFormatException("Empty reply from the server")
                }
            }
        } catch (e: ClosedReceiveChannelException) {
            throw ServerClosedConnectionException(e.message)
        }
    }

    @Throws(
        ServerTimedOutException::class,
        ServerIncorrectResponseFormatException::class,
        ServerClosedConnectionException::class,
        ServerUnableToStartSessionException::class,
    )
    fun startGameSession(playerId: String): GameSession {
        LOGGER.debug("Starting a new game session for a player with id: $playerId")
        return GameSessionImpl(client, host, port, json, playerId).start()
    }

    @Throws(ServerTimedOutException::class)
    private suspend fun waitForFrame(session: DefaultClientWebSocketSession, timeout: Duration): Frame {
        val maybeFrame = withTimeoutOrNull(timeout) {
            session.incoming.receive()
        }
        return maybeFrame ?: throw ServerTimedOutException(timeout)
    }

    @Throws(ServerIncorrectResponseFormatException::class)
    private inline fun <reified T : Any> asObject(text: Frame.Text): T {
        try {
            return json.fromJson(text.readText(), T::class.java)
        } catch (e: Exception) {
            throw ServerIncorrectResponseFormatException("Cannot parse the response due to error: ${e.message}")
        }
    }

    private fun registerRequest(playerName: String): String {
        return json.toJson(RegisterRequest(playerName))
    }
}
