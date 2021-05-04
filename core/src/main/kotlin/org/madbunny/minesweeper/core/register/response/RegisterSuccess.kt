package org.madbunny.minesweeper.core.register.response

class RegisterSuccess (
    val playerId: String,
) : RegisterResult(RegisterResultType.SUCCESS)
