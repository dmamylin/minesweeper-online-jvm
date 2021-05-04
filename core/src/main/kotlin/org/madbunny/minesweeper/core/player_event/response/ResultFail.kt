package org.madbunny.minesweeper.core.player_event.response

class ResultFail(
    val errorMessage: String,
    val statusCode: ResultFailStatusCode,
) : PlayerEventResult(PlayerEventResultType.FAIL)
