package org.madbunny.minesweeper.server

import io.ktor.application.*

// {"playerName": "vasya"}
// {"eventType": "OPEN_FIELD", "playerId": "8881dcb4-bf2b-49a8-84ba-c22f0e595a50", "x": 0, "y": 0}

fun main(args: Array<String>) = io.ktor.server.netty.EngineMain.main(args)

@Suppress("unused")
fun Application.module() {
    val server = Server.Builder().build()
    server.run(this)
}
