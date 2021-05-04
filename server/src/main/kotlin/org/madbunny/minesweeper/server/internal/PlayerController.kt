package org.madbunny.minesweeper.server.internal

import org.madbunny.minesweeper.core.player_event.request.*
import org.madbunny.minesweeper.core.player_event.response.*
import org.madbunny.minesweeper.core.register.request.RegisterRequest
import org.madbunny.minesweeper.core.register.response.RegisterFail
import org.madbunny.minesweeper.core.register.response.RegisterFailStatusCode
import org.madbunny.minesweeper.core.register.response.RegisterResult
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.util.concurrent.Executors

internal class PlayerController(maxPlayers: Int) {
    private companion object {
        val LOGGER: Logger = LoggerFactory.getLogger(PlayerController::class.java.simpleName)
    }

    private val playerRegistry = PlayerRegistry(maxPlayers)
    private val playerEventExecutor = Executors.newSingleThreadExecutor()
    private val gameController = GameController()

    fun onRegister(request: RegisterRequest): RegisterResult {
        return try {
            playerRegistry.registerPlayer(request.playerName)
        } catch (e: Exception) {
            LOGGER.error("Unknown error occurred on registering a new player: ${e.message}")
            RegisterFail("Unknown error occurred", RegisterFailStatusCode.UNKNOWN_ERROR)
        }
    }

    fun onPlayerEvent(playerEvent: PlayerEvent): PlayerEventResult {
        try {
            val isKnownPlayer = playerRegistry.onPlayerEvent(playerEvent.playerId)
            if (!isKnownPlayer) {
                return ResultFail("Unknown player id: ${playerEvent.playerId}", ResultFailStatusCode.UNKNOWN_PLAYER)
            }

            if (playerEvent !is RememberMe) {
                playerEventExecutor.execute {
                    gameController.handlePlayerEvent(playerEvent)
                }
            }
        } catch (e: Exception) {
            LOGGER.error("Unknown error occurred while processing a player event: ${e.message}")
            return ResultFail("Unknown error occurred", ResultFailStatusCode.UNKNOWN_ERROR)
        }
        return ResultOk()
    }
}
