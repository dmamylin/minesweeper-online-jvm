package org.madbunny.minesweeper.server.internal

import io.ktor.http.cio.websocket.*
import org.madbunny.minesweeper.core.game_event.GameEvent
import org.madbunny.minesweeper.core.json.JsonWrapper

internal class PlayerConnection(
    private val connection: DefaultWebSocketSession,
    private val playerController: PlayerController,
    private val json: JsonWrapper
) {
    suspend fun send(gameEvent: GameEvent) {
        connection.send(json.toJson(gameEvent))
    }

    fun close(reason: CloseReason) {
    }

    suspend fun listen() {
        for (frame in connection.incoming) {
        }
    }
}
