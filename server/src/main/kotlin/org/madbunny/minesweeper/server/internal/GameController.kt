package org.madbunny.minesweeper.server.internal

import org.madbunny.minesweeper.core.player_event.request.OpenField
import org.madbunny.minesweeper.core.player_event.request.PlayerEvent
import org.slf4j.Logger
import org.slf4j.LoggerFactory

internal class GameController {
    companion object {
        val LOGGER: Logger = LoggerFactory.getLogger(GameController::class.java.simpleName)
    }

    fun handlePlayerEvent(playerEvent: PlayerEvent) {
        when (playerEvent) {
            is OpenField -> {
                LOGGER.info("OpenField: x=${playerEvent.x}, y=${playerEvent.y}")
            }
            else -> {
                LOGGER.warn("Has no handler for events of type: ${playerEvent::class.java.simpleName}")
            }
        }
    }
}
