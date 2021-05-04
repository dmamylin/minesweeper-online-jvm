package org.madbunny.minesweeper.core.register.response

enum class RegisterFailStatusCode {
    INCORRECT_REQUEST_FORMAT,
    REQUEST_TIMEOUT,
    SERVER_IS_FULL,
    PLAYER_NAME_ALREADY_IN_USE,
    CANNOT_CREATE_PLAYER_ID,
    UNKNOWN_ERROR,
}
