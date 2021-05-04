package org.madbunny.minesweeper.client.exception

class ServerClosedConnectionException(reason: String? = null) : Exception() {
    private companion object {
        fun formatErrorMessage(reason: String?): String {
            if (reason == null) {
                return "The server closed the connection for an unknown reason"
            }
            return "The server closed the connection due to an error: $reason"
        }
    }
}
