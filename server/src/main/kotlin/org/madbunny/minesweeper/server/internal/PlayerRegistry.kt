package org.madbunny.minesweeper.server.internal

import org.madbunny.minesweeper.core.register.response.RegisterFail
import org.madbunny.minesweeper.core.register.response.RegisterFailStatusCode
import org.madbunny.minesweeper.core.register.response.RegisterResult
import org.madbunny.minesweeper.core.register.response.RegisterSuccess
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.time.Duration
import java.time.Instant
import java.util.*
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import kotlin.collections.HashMap

internal class PlayerRegistry(private val maxPlayers: Int) {
    private companion object {
        const val CREATE_PLAYER_ID_RETRIES = 10
        val TIME_BETWEEN_REGISTER_AND_CONNECT: Duration = Duration.ofSeconds(20)
        val DISCONNECT_INACTIVE_AFTER: Duration = Duration.ofMinutes(5)
        val PLAYER_WATCHER_FREQUENCY: Duration = Duration.ofSeconds(60)
        val LOGGER: Logger = LoggerFactory.getLogger(PlayerRegistry::class.java.simpleName)
    }

    private enum class PlayerState { REGISTERED, IN_SESSION }
    private data class PlayerStatus(val name: String, var state: PlayerState, var atMoment: Instant)

    private val players = HashMap<String, PlayerStatus>()
    private val playerWatcher = Executors.newSingleThreadScheduledExecutor()

    init {
        playerWatcher.scheduleWithFixedDelay(this::watchPlayers, 0, PLAYER_WATCHER_FREQUENCY.seconds, TimeUnit.SECONDS)
    }

    fun registerPlayer(playerName: String): RegisterResult {
        synchronized(players) {
            if (players.size == maxPlayers) {
                LOGGER.info("Cannot create a new player: the server is full, the player limit is: $maxPlayers")
                return RegisterFail("The server is full", RegisterFailStatusCode.SERVER_IS_FULL)
            }

            for ((_, playerStatus) in players) {
                if (playerStatus.name == playerName) {
                    LOGGER.info("""Cannot create a new player: name "$playerName" is already in use""")
                    return RegisterFail("""Name "$playerName" is already in use""",
                        RegisterFailStatusCode.PLAYER_NAME_ALREADY_IN_USE)
                }
            }

            for (i in 1..CREATE_PLAYER_ID_RETRIES) {
                val newPlayerId = UUID.randomUUID().toString()
                if (!players.containsKey(newPlayerId)) {
                    players[newPlayerId] = PlayerStatus(playerName, PlayerState.REGISTERED, Instant.now())
                    LOGGER.info("""New player "$playerName" with id "$newPlayerId" was registered successfully""")
                    return RegisterSuccess(newPlayerId)
                }
            }

            LOGGER.warn("Cannot generate a new player id, number of retries exceeded: $CREATE_PLAYER_ID_RETRIES")
            return RegisterFail("Cannot create a player id after $CREATE_PLAYER_ID_RETRIES retries",
                RegisterFailStatusCode.CANNOT_CREATE_PLAYER_ID)
        }
    }

    fun onPlayerEvent(playerId: String): Boolean {
        synchronized(players) {
            val playerStatus = players[playerId] ?: return false
            playerStatus.state = PlayerState.IN_SESSION
            playerStatus.atMoment = Instant.now()
            return true
        }
    }

    private fun watchPlayers() {
        synchronized(players) {
            val currentMoment = Instant.now()
            val playersToRemove = HashMap<String, String>()
            for ((playerId, playerStatus) in players) {
                val timeout = when (playerStatus.state) {
                    PlayerState.REGISTERED -> TIME_BETWEEN_REGISTER_AND_CONNECT
                    PlayerState.IN_SESSION -> DISCONNECT_INACTIVE_AFTER
                }

                if (playerStatus.atMoment + timeout < currentMoment) {
                    playersToRemove[playerId] = playerStatus.name
                }
            }

            for ((playerId, playerName) in playersToRemove) {
                LOGGER.info("""Removing player "$playerName" with id "$playerId" due to inactivity""")
                players.remove(playerId)
            }
        }
    }
}
