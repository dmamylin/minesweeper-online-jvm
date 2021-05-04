package org.madbunny.minesweeper.core.player_event.request

abstract class PlayerEvent (
    private val playerEventType: PlayerEventType,
    val playerId: String,
)
