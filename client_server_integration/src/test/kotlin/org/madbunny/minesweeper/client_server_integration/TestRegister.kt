package org.madbunny.minesweeper.client_server_integration

import kotlinx.coroutines.runBlocking
import kotlin.test.assertTrue
import org.junit.jupiter.api.Test
import org.madbunny.minesweeper.client.Client
import org.madbunny.minesweeper.core.register.response.RegisterFail
import org.madbunny.minesweeper.core.register.response.RegisterFailStatusCode
import org.madbunny.minesweeper.core.register.response.RegisterSuccess
import kotlin.test.assertEquals

class TestRegister {
    private companion object {
        const val HOST = "0.0.0.0"
        const val PORT = 17051
        const val PLAYER_1 = "player_1"
        const val PLAYER_2 = "player_2"
    }

    @Test
    fun testRegisterSuccess() {
        val server = EmbeddedServer(HOST, PORT).start().waitAlive()
        val client = Client.Builder().setHost(server.host).setPort(server.port).build()
        val result = client.register(PLAYER_1)
        assertTrue(result is RegisterSuccess)
        assertTrue(result.playerId.isNotEmpty())
        server.stop()
    }

    @Test
    fun testRegisterFailServerIsFull() {
        val server = EmbeddedServer(HOST, PORT) {
            setMaxPlayers(1)
        }.start().waitAlive()
        val client = Client.Builder().setHost(server.host).setPort(server.port).build()

        val resultPlayer1 = client.register(PLAYER_1)
        assertTrue(resultPlayer1 is RegisterSuccess)

        val resultPlayer2 = client.register(PLAYER_2)
        assertTrue(resultPlayer2 is RegisterFail)
        assertEquals(RegisterFailStatusCode.SERVER_IS_FULL, resultPlayer2.statusCode)

        server.stop()
    }

    @Test
    fun testRegisterFailPlayerNameAlreadyInUse() {
        val server = EmbeddedServer(HOST, PORT) {
            setMaxPlayers(2)
        }.start().waitAlive()
        val client = Client.Builder().setHost(server.host).setPort(server.port).build()

        val resultPlayer1 = client.register(PLAYER_1)
        assertTrue(resultPlayer1 is RegisterSuccess)

        val resultPlayer1Again = client.register(PLAYER_1)
        assertTrue(resultPlayer1Again is RegisterFail)
        assertEquals(RegisterFailStatusCode.PLAYER_NAME_ALREADY_IN_USE, resultPlayer1Again.statusCode)

        server.stop()
    }
}
