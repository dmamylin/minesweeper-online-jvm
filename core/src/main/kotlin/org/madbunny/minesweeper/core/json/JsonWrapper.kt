package org.madbunny.minesweeper.core.json

interface JsonWrapper {
    fun toJson(obj: Any): String

    fun <T> fromJson(json: String, obj: Class<T>): T

    companion object {
        fun createOverGson(): JsonWrapper {
            return GsonWrapper()
        }
    }
}
