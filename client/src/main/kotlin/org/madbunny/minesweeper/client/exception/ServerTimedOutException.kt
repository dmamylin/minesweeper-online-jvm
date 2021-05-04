package org.madbunny.minesweeper.client.exception

import java.time.Duration

class ServerTimedOutException(
    timeout: Duration? = null,
) : Exception(formatErrorMessage(timeout)) {
    private companion object {
        fun formatErrorMessage(timeout: Duration?): String {
            var message = "The server did not respond in time"
            if (timeout != null) {
                message += ", timeout is: ${timeout.seconds} seconds"
            }
            return message
        }
    }
}
