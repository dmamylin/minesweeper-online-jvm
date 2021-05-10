package org.madbunny.minesweeper.client

import io.ktor.client.*
import io.ktor.client.features.websocket.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.http.cio.websocket.*
import kotlinx.coroutines.CoroutineScope
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
) {
    private companion object {
        const val DEFAULT_HOST = "0.0.0.0"
        const val DEFAULT_PORT = 8080
        val DEFAULT_PING_TIMEOUT: Duration = Duration.ofSeconds(30)
        val DEFAULT_REGISTER_TIMEOUT: Duration = Duration.ofMinutes(1)

        val LOGGER: Logger = LoggerFactory.getLogger(Client::class.java.simpleName)
    }

    class Builder {
        private var host = DEFAULT_HOST
        private var port = DEFAULT_PORT

        fun setHost(value: String): Builder { host = value; return this }
        fun setPort(value: Int): Builder { port = value; return this }

        fun build() = Client(
            host,
            port,
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
        var pingOk: Boolean
        try {
            runBlocking {
                withContext(Dispatchers.IO) {
                    val response = waitForResponse(timeout) {
                        client.get<String>("http", host, port, "/ping")
                    }
                    pingOk = response == "pong"
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
    )
    fun register(playerName: String, timeout: Duration = DEFAULT_REGISTER_TIMEOUT): RegisterResult {
        LOGGER.debug("Trying to register a new player: $playerName")
        return try {
            runBlocking {
                withContext(Dispatchers.IO) {
                    val request = RegisterRequest(playerName)
                    val response = waitForResponse(timeout) {
                        client.post<String>("http", host, port, "/register", body = json.toJson(request))
                    }

                    response.ifEmpty {
                        throw ServerIncorrectResponseFormatException("Empty reply from the server")
                    }

                    asObject(response)
                }
            }
        } catch (e: Exception) {
            throw ServerIncorrectResponseFormatException("An unknown error occurred: ${e.message}")
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
    private suspend fun <T> waitForResponse(timeout: Duration, worker: suspend CoroutineScope.() -> T): T {
        return withTimeoutOrNull(timeout, worker) ?: throw ServerTimedOutException(timeout)
    }

    @Throws(ServerTimedOutException::class)
    private suspend fun waitForFrame(session: DefaultClientWebSocketSession, timeout: Duration): Frame {
        val maybeFrame = withTimeoutOrNull(timeout) {
            session.incoming.receive()
        }
        return maybeFrame ?: throw ServerTimedOutException(timeout)
    }

    @Throws(ServerIncorrectResponseFormatException::class)
    private inline fun <reified T : Any> asObject(text: String): T {
        try {
            return json.fromJson(text, T::class.java)
        } catch (e: Exception) {
            throw ServerIncorrectResponseFormatException("Cannot parse the response due to error: ${e.message}")
        }
    }

    private fun registerRequest(playerName: String): String {
        return json.toJson(RegisterRequest(playerName))
    }
}
