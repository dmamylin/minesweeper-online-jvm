package org.madbunny.minesweeper.server.internal

import io.ktor.http.cio.websocket.*
import org.madbunny.minesweeper.core.player_event.request.PlayerEvent
import org.madbunny.minesweeper.core.player_event.response.ResultFail
import org.madbunny.minesweeper.core.player_event.response.ResultFailStatusCode
import org.madbunny.minesweeper.core.register.response.RegisterFail
import org.madbunny.minesweeper.core.register.response.RegisterFailStatusCode
import org.madbunny.minesweeper.core.register.request.RegisterRequest
import org.madbunny.minesweeper.core.json.JsonWrapper
import org.slf4j.Logger
import org.slf4j.LoggerFactory

internal class RequestParser(private val json: JsonWrapper) {
    private companion object {
        val LOGGER: Logger = LoggerFactory.getLogger(RequestParser::class.java.simpleName)
    }

    // Returns: RegisterFail or RegisterRequest
    fun parseRegisterRequest(frame: Frame): Any {
        val text = frame as? Frame.Text ?: return RegisterFail("Unknown input type, expected: Text",
            RegisterFailStatusCode.INCORRECT_REQUEST_FORMAT)
        return try {
            json.fromJson(text.readText(), RegisterRequest::class.java)
        } catch (e: Exception) {
            LOGGER.error("An error occurred while parsing a register request: ${e.message}")
            RegisterFail("Unknown format of a register request",
                RegisterFailStatusCode.INCORRECT_REQUEST_FORMAT)
        }
    }

    // Returns: PlayerEventFail or PlayerEvent
    fun parsePlayerEventRequest(frame: Frame): Any {
        val text = frame as? Frame.Text ?: return ResultFail("Unknown input type, expected: Text",
            ResultFailStatusCode.INCORRECT_REQUEST_FORMAT)
        return try {
            json.fromJson(text.readText(), PlayerEvent::class.java)
        } catch (e: Exception) {
            LOGGER.error("An error occurred while parsing a player event request: ${e.message}")
            ResultFail("Unknown format of a player event request",
                ResultFailStatusCode.INCORRECT_REQUEST_FORMAT)
        }
    }
}
