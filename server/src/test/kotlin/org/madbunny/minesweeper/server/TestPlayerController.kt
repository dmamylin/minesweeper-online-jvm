package org.madbunny.minesweeper.server

import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.madbunny.minesweeper.core.player_event.request.OpenField
import org.madbunny.minesweeper.core.player_event.request.RememberMe
import org.madbunny.minesweeper.core.player_event.response.*
import org.madbunny.minesweeper.core.register.request.RegisterRequest
import org.madbunny.minesweeper.core.register.response.RegisterFail
import org.madbunny.minesweeper.core.register.response.RegisterFailStatusCode
import org.madbunny.minesweeper.core.register.response.RegisterSuccess
import org.madbunny.minesweeper.server.internal.PlayerController
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals
import kotlin.test.assertTrue

class TestPlayerController {
    companion object {
        const val PLAYER_1 = "player_1"
        const val PLAYER_2 = "player_2"
        const val PLAYER_3 = "player_3"
    }

    private var playerController: PlayerController? = null

    @BeforeEach
    fun initServer() {
        playerController = PlayerController(2)
    }

    @Test
    fun testRegister() {
        // Register 2 players
        val resultPlayer1 = playerController?.onRegister(RegisterRequest(PLAYER_1))
        assertTrue(resultPlayer1 is RegisterSuccess)
        val resultPlayer2 = playerController?.onRegister(RegisterRequest(PLAYER_2))
        assertTrue(resultPlayer2 is RegisterSuccess)
        assertNotEquals(resultPlayer1.playerId, resultPlayer2.playerId)

        // Trying to register a third player but the server is full
        val resultPlayer3 = playerController?.onRegister(RegisterRequest(PLAYER_3))
        assertTrue(resultPlayer3 is RegisterFail)
        assertEquals(RegisterFailStatusCode.SERVER_IS_FULL, resultPlayer3.statusCode)
    }

    @Test
    fun testRegisterWithSameName() {
        val resultPlayer1 = playerController?.onRegister(RegisterRequest(PLAYER_1))
        assertTrue(resultPlayer1 is RegisterSuccess)

        val resultPlayer1Again = playerController?.onRegister(RegisterRequest(PLAYER_1))
        assertTrue(resultPlayer1Again is RegisterFail)
        assertEquals(RegisterFailStatusCode.PLAYER_NAME_ALREADY_IN_USE, resultPlayer1Again.statusCode)
    }

    @Test
    fun testRememberMeEvent() {
        val resultPlayer1 = playerController?.onRegister(RegisterRequest(PLAYER_1))
        assertTrue(resultPlayer1 is RegisterSuccess)
        val eventResultPlayer1 = playerController?.onPlayerEvent(RememberMe(resultPlayer1.playerId))
        assertTrue(eventResultPlayer1 is ResultOk)

        val eventResultUnknownPlayer = playerController?.onPlayerEvent(RememberMe("UNKNOWN"))
        assertTrue(eventResultUnknownPlayer is ResultFail)
        assertEquals(ResultFailStatusCode.UNKNOWN_PLAYER, eventResultUnknownPlayer.statusCode)
    }

    @Test
    fun testOpenFieldEvent() {
        val resultPlayer1 = playerController?.onRegister(RegisterRequest(PLAYER_1))
        assertTrue(resultPlayer1 is RegisterSuccess)
        val openFieldResult = playerController?.onPlayerEvent(OpenField(resultPlayer1.playerId, 0, 0))
        assertTrue(openFieldResult is ResultOk)
    }
}
