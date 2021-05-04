package org.madbunny.minesweeper.core.register.response

class RegisterFail (
    val errorMessage: String,
    val statusCode: RegisterFailStatusCode,
) : RegisterResult(RegisterResultType.FAIL)
