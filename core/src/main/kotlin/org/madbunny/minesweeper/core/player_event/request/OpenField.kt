package org.madbunny.minesweeper.core.player_event.request

class OpenField(
    playerId: String,
    val x: Int,
    val y: Int,
) : PlayerEvent(PlayerEventType.OPEN_FIELD, playerId)
