package org.madbunny.minesweeper.core.json

import com.google.gson.GsonBuilder
import com.google.gson.typeadapters.RuntimeTypeAdapterFactory
import org.madbunny.minesweeper.core.game_event.*
import org.madbunny.minesweeper.core.player_event.request.*
import org.madbunny.minesweeper.core.player_event.response.*
import org.madbunny.minesweeper.core.register.response.RegisterFail
import org.madbunny.minesweeper.core.register.response.RegisterResult
import org.madbunny.minesweeper.core.register.response.RegisterResultType
import org.madbunny.minesweeper.core.register.response.RegisterSuccess

internal class GsonWrapper : JsonWrapper {
    private val gson = GsonBuilder()
        .registerTypeAdapterFactory(
            RuntimeTypeAdapterFactory.of(RegisterResult::class.java, "registerResultType")
                .registerSubtype(RegisterFail::class.java, RegisterResultType.FAIL.name)
                .registerSubtype(RegisterSuccess::class.java, RegisterResultType.SUCCESS.name)
        )
        .registerTypeAdapterFactory(
            RuntimeTypeAdapterFactory.of(PlayerEvent::class.java, "playerEventType")
                .registerSubtype(OpenField::class.java, PlayerEventType.OPEN_FIELD.name)
                .registerSubtype(RememberMe::class.java, PlayerEventType.REMEMBER_ME.name)
        )
        .registerTypeAdapterFactory(
            RuntimeTypeAdapterFactory.of(PlayerEventResult::class.java, "eventResultType")
                .registerSubtype(ResultFail::class.java, PlayerEventResultType.FAIL.name)
                .registerSubtype(ResultOk::class.java, PlayerEventResultType.OK.name)
        )
        .registerTypeAdapterFactory(
            RuntimeTypeAdapterFactory.of(GameEvent::class.java, "gameEventType")
                .registerSubtype(FieldChanged::class.java, GameEventType.FIELD_CHANGED.name)
        )
        .create()

    override fun toJson(obj: Any): String {
        return gson.toJson(obj)
    }

    override fun <T> fromJson(json: String, obj: Class<T>): T {
        return gson.fromJson(json, obj)
    }
}
