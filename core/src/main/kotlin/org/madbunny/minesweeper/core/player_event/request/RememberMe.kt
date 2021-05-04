package org.madbunny.minesweeper.core.player_event.request

class RememberMe(
    playerId: String,
) : PlayerEvent(PlayerEventType.REMEMBER_ME, playerId)
