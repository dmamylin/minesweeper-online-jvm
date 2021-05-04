package org.madbunny.minesweeper.server

import io.ktor.application.*
import io.ktor.http.cio.websocket.*
import io.ktor.request.*
import io.ktor.routing.*
import io.ktor.websocket.*
import kotlinx.coroutines.channels.ClosedReceiveChannelException
import kotlinx.coroutines.time.withTimeoutOrNull
import org.madbunny.minesweeper.core.player_event.request.PlayerEvent
import org.madbunny.minesweeper.core.register.request.RegisterRequest
import org.madbunny.minesweeper.core.json.JsonWrapper
import org.madbunny.minesweeper.core.register.response.RegisterFail
import org.madbunny.minesweeper.core.register.response.RegisterFailStatusCode
import org.madbunny.minesweeper.server.internal.RequestParser
import org.madbunny.minesweeper.server.internal.PlayerController
import java.time.Duration

class Server private constructor (
    maxPlayers: Int,
    private val registerTimeout: Duration,
    private val webSocketPingPeriod: Duration,
    private val webSocketConnectionTimeout: Duration,
) {
    private companion object {
        const val DEFAULT_MAX_PLAYERS = 4
        val DEFAULT_REGISTER_TIMEOUT: Duration = Duration.ofSeconds(30)
        val DEFAULT_PING_PERIOD: Duration = Duration.ofSeconds(20)
        val DEFAULT_CONNECTION_TIMEOUT: Duration = Duration.ofSeconds(60)
    }

    class Builder {
        private var maxPlayers = DEFAULT_MAX_PLAYERS
        private var registerTimeout = DEFAULT_REGISTER_TIMEOUT
        private var webSocketPingPeriod: Duration = DEFAULT_PING_PERIOD
        private var webSocketConnectionTimeout: Duration = DEFAULT_CONNECTION_TIMEOUT

        fun setMaxPlayers(value: Int): Builder { maxPlayers = value; return this }
        fun setRegisterTimeout(value: Duration): Builder { registerTimeout = value; return this }
        fun setWebSocketPingPeriod(value: Duration): Builder { webSocketPingPeriod = value; return this }
        fun setWebSocketConnectionTimeout(value: Duration): Builder { webSocketConnectionTimeout = value; return this }

        fun build() = Server(
            maxPlayers,
            registerTimeout,
            webSocketPingPeriod,
            webSocketConnectionTimeout,
        )
    }

    private val playerController = PlayerController(maxPlayers)
    private val json = JsonWrapper.createOverGson()
    private val requestParser = RequestParser(json)

    fun run(app: Application) {
        runImpl(app) {
            install(WebSockets) {
                pingPeriod = webSocketPingPeriod
                timeout = webSocketConnectionTimeout
            }

            routing {
                webSocket("/ping") {
                    onRequest(this) {
                        send("pong")
                    }
                }

                webSocket("/register") {
                    onRequest(this) {
                        val frame = withTimeoutOrNull(registerTimeout) {
                            incoming.receive()
                        }

                        if (frame != null) {
                            when (val parsedRequest = requestParser.parseRegisterRequest(frame)) {
                                !is RegisterRequest -> send(json.toJson(parsedRequest))
                                else -> {
                                    val registerResult = playerController.onRegister(parsedRequest)
                                    send(json.toJson(registerResult))
                                }
                            }
                        } else {
                            val fail = RegisterFail("Didn't receive any data",
                                RegisterFailStatusCode.REQUEST_TIMEOUT)
                            send(json.toJson(fail))
                        }
                    }
                }

                webSocket("/play") {
                    onRequest(this) {
                        for (frame in incoming) {
                            when (val parsedRequest = requestParser.parsePlayerEventRequest(frame)) {
                                !is PlayerEvent -> send(json.toJson(parsedRequest))
                                else -> {
                                    val playerEventResult = playerController.onPlayerEvent(parsedRequest)
                                    send(json.toJson(playerEventResult))
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    private fun runImpl(app: Application, worker: Application.() -> Unit) {
        worker(app)
    }
}

private suspend fun onRequest(session: DefaultWebSocketServerSession, handler: suspend DefaultWebSocketServerSession.() -> Unit) {
    val log = session.call.application.environment.log
    val uri = session.call.request.uri
    try {
        handler(session)
    } catch (e: ClosedReceiveChannelException) {
        log.info("Connection was closed while processing $uri request because: ${e.message}")
    } catch (e: Exception) {
        log.error("An error occurred while processing $uri request: ${e.message}")
    }
}
