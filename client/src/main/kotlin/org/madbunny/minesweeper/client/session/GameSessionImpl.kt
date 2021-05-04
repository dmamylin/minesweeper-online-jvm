package org.madbunny.minesweeper.client.session

import io.ktor.client.*
import io.ktor.client.features.websocket.*
import io.ktor.http.*
import io.ktor.http.cio.websocket.*
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.ClosedReceiveChannelException
import org.madbunny.minesweeper.client.exception.ServerClosedConnectionException
import org.madbunny.minesweeper.client.exception.ServerIncorrectResponseFormatException
import org.madbunny.minesweeper.client.exception.ServerTimedOutException
import org.madbunny.minesweeper.client.exception.ServerUnableToStartSessionException
import org.madbunny.minesweeper.core.json.JsonWrapper
import org.madbunny.minesweeper.core.player_event.request.PlayerEvent
import org.madbunny.minesweeper.core.player_event.request.RememberMe
import org.madbunny.minesweeper.core.register.response.RegisterResult

internal class GameSessionImpl(
    private val client: HttpClient,
    private val host: String,
    private val port: Int,
    private val json: JsonWrapper,
    private val playerId: String,
) : GameSession {
    private var sessionJob: Job? = null
    private val toSend = Channel<PlayerEvent>()

    @Throws(
        ServerTimedOutException::class,
        ServerIncorrectResponseFormatException::class,
        ServerClosedConnectionException::class,
        ServerUnableToStartSessionException::class,
    )
    fun start(): GameSession {
        sessionJob = GlobalScope.launch(Dispatchers.IO) {
            client.webSocket(HttpMethod.Get, host, port, "/play") {

            }
        }
        return this
    }

    override fun stop() {
    }
}
