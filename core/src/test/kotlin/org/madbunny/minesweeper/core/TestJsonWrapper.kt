package org.madbunny.minesweeper.core

import org.junit.jupiter.api.Test
import org.madbunny.minesweeper.core.game_event.*
import org.madbunny.minesweeper.core.player_event.request.*
import org.madbunny.minesweeper.core.player_event.response.*
import org.madbunny.minesweeper.core.json.JsonWrapper
import org.madbunny.minesweeper.core.register.response.RegisterFail
import org.madbunny.minesweeper.core.register.response.RegisterFailStatusCode
import org.madbunny.minesweeper.core.register.response.RegisterResult
import org.madbunny.minesweeper.core.register.response.RegisterSuccess
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class TestGsonWrapper : TestJsonWrapper {
    override fun parser() = JsonWrapper.createOverGson()
}

interface TestJsonWrapper {
    fun parser(): JsonWrapper

    companion object {
        const val SOME_ID = "some_id"
        const val SOME_MSG = "some message"
    }

    private inline fun <reified T : Any> asObject(json: String): T {
        return parser().fromJson(json, T::class.java)
    }

    @Test
    fun deserializeFromStringRegisterFail() {
        val json = """{
            "registerResultType": "FAIL",
            "errorMessage": "$SOME_MSG",
            "statusCode": "INCORRECT_REQUEST_FORMAT"
        }"""
        val result = asObject<RegisterResult>(json)
        assertTrue(result is RegisterFail)
        assertEquals(result.errorMessage, SOME_MSG)
        assertEquals(result.statusCode, RegisterFailStatusCode.INCORRECT_REQUEST_FORMAT)
    }

    @Test
    fun serializeAndDeserializeRegisterFail() {
        val json = parser().toJson(RegisterFail(SOME_MSG, RegisterFailStatusCode.INCORRECT_REQUEST_FORMAT))
        val result = asObject<RegisterResult>(json)
        assertTrue(result is RegisterFail)
        assertEquals(result.errorMessage, SOME_MSG)
        assertEquals(result.statusCode, RegisterFailStatusCode.INCORRECT_REQUEST_FORMAT)
    }

    @Test
    fun deserializeFromStringRegisterSuccess() {
        val json = """{
            "registerResultType": "SUCCESS",
            "playerId": "$SOME_ID"
        }"""
        val result = asObject<RegisterResult>(json)
        assertTrue(result is RegisterSuccess)
        assertEquals(result.playerId, SOME_ID)
    }

    @Test
    fun serializeAndDeserializeRegisterSuccess() {
        val json = parser().toJson(RegisterSuccess(SOME_ID))
        val result = asObject<RegisterResult>(json)
        assertTrue(result is RegisterSuccess)
        assertEquals(result.playerId, SOME_ID)
    }

    @Test
    fun deserializeFromStringRememberMeEvent() {
        val json = """{
            "playerEventType": "REMEMBER_ME",
            "playerId": "$SOME_ID"
        }"""
        val result = asObject<PlayerEvent>(json)
        assertTrue(result is RememberMe)
        assertEquals(SOME_ID, result.playerId)
    }

    @Test
    fun serializeAndDeserializeRememberMeEvent() {
        val json = parser().toJson(RememberMe(SOME_ID))
        val rememberMe = asObject<PlayerEvent>(json)
        assertTrue(rememberMe is RememberMe)
        assertEquals(SOME_ID, rememberMe.playerId)
    }

    @Test
    fun deserializeFromStringOpenFieldEvent() {
        val json = """{
            "playerEventType": "OPEN_FIELD",
            "playerId": "$SOME_ID",
            "x": 1,
            "y": 2
        }"""
        val result = asObject<PlayerEvent>(json)
        assertTrue(result is OpenField)
        assertEquals(SOME_ID, result.playerId)
        assertEquals(1, result.x)
        assertEquals(2, result.y)
    }

    @Test
    fun serializeAndDeserializeOpenFieldEvent() {
        val json = parser().toJson(OpenField(SOME_ID, 1, 2))
        val openField = asObject<PlayerEvent>(json)
        assertTrue(openField is OpenField)
        assertEquals(SOME_ID, openField.playerId)
        assertEquals(1, openField.x)
        assertEquals(2, openField.y)
    }

    @Test
    fun deserializeFromStringEventResultFail() {
        val json = """{
            "eventResultType": "FAIL",
            "errorMessage": "$SOME_MSG",
            "statusCode": "UNKNOWN_PLAYER"
        }"""
        val result = asObject<PlayerEventResult>(json)
        assertTrue(result is ResultFail)
        assertEquals(SOME_MSG, result.errorMessage)
        assertEquals(ResultFailStatusCode.UNKNOWN_PLAYER, result.statusCode)
    }

    @Test
    fun serializeAndDeserializeEventResultFail() {
        val json = parser().toJson(ResultFail(SOME_MSG, ResultFailStatusCode.UNKNOWN_PLAYER))
        val fail = asObject<PlayerEventResult>(json)
        assertTrue(fail is ResultFail)
        assertEquals(SOME_MSG, fail.errorMessage)
        assertEquals(ResultFailStatusCode.UNKNOWN_PLAYER, fail.statusCode)
    }

    @Test
    fun deserializeFromStringEventResultOk() {
        val json = """{"eventResultType": "OK"}"""
        val result = asObject<PlayerEventResult>(json)
        assertTrue(result is ResultOk)
    }

    @Test
    fun serializeAndDeserializeEventResultOk() {
        val json = parser().toJson(ResultOk())
        val ok = asObject<PlayerEventResult>(json)
        assertTrue(ok is ResultOk)
    }

    @Test
    fun deserializeFromStringFieldChangedEvent() {
        val json = """{"gameEventType": "FIELD_CHANGED"}"""
        val result = asObject<GameEvent>(json)
        assertTrue(result is FieldChanged)
    }

    @Test
    fun serializeAndDeserializeFieldChangedEvent() {
        val json = parser().toJson(FieldChanged())
        val result = asObject<GameEvent>(json)
        assertTrue(result is FieldChanged)
    }
}
