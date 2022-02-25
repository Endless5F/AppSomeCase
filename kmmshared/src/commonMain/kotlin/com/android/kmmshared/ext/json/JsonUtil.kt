package com.android.kmmshared.ext.json

import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.*
import kotlin.native.concurrent.SharedImmutable

fun Any.toJsonString(): String {
    return Json.encodeToString(this)
}

fun String.toJsonObject(): JsonObject {
    return Json.parseToJsonElement(this).jsonObject
}

fun String.toJsonArray(): JsonArray {
    return Json.parseToJsonElement(this).jsonArray
}

fun JsonObject.getString(key: String): String = this.getString(key, "") ?: ""
fun JsonObject.getString(key: String, default: String?): String? =
    (this[key] as? JsonPrimitive)?.contentOrNull ?: default

fun JsonObject.getInt(key: String): Int = this.getInt(key, 0)
fun JsonObject.getInt(key: String, default: Int): Int =
    (this[key] as? JsonPrimitive)?.intOrNull ?: default

fun JsonObject.getLong(key: String): Long = this[key]?.jsonPrimitive?.longOrNull ?: 0

fun JsonObject.getFloat(key: String): Float = this[key]?.jsonPrimitive?.floatOrNull ?: 0f
fun JsonObject.getDouble(key: String): Double = this[key]?.jsonPrimitive?.doubleOrNull ?: 0.0

fun JsonObject.getBoolean(key: String): Boolean {
    val primitiveVal = this[key]?.jsonPrimitive
    val boolVal = primitiveVal?.booleanOrNull
    if (boolVal != null) {
        return boolVal
    }
    primitiveVal?.content?.let {
        return (it.compareTo("y", true) == 0 ||
                it.compareTo("1", true) == 0 ||
                it.compareTo("yes", true) == 0 ||
                it.compareTo("true", true) == 0 ||
                it.compareTo("t", true) == 0 ||
                it.compareTo("ok", true) == 0)
    }
    return false
}

fun JsonArray.getJsonObject(index: Int): JsonObject? {
    try {
        return this.getOrNull(index)?.jsonObject
    } catch (e: Exception) {
        e.printStackTrace()
    }
    return null
}

fun JsonArray.getJsonArray(index: Int): JsonArray? {
    try {
        return this.getOrNull(index)?.jsonArray
    } catch (e: Exception) {
        e.printStackTrace()
    }
    return null
}

fun JsonObject.getJsonObject(key: String): JsonObject? {
    try {
        return this[key]?.jsonObject
    } catch (e: Exception) {
        e.printStackTrace()
    }
    return null
}

fun JsonObject.getJsonArray(key: String): JsonArray? {
    try {
        return this[key]?.jsonArray
    } catch (e: Exception) {
        e.printStackTrace()
    }
    return null
}

fun JsonObject.getIntArray(key: String): List<Int> {
    val rst = mutableListOf<Int>()
    try {
        val jaT = getJsonArray(key)
        jaT?.let {
            for (element in it) {
                rst.add(element.jsonPrimitive.int)
            }
        }
    } catch (e: Exception) {
        e.printStackTrace()
    }
    return rst
}

fun JsonObject.getStringArray(key: String): List<String> {
    val rst = mutableListOf<String>()
    try {
        val jaT = getJsonArray(key)
        jaT?.let {
            for (element in it) {
                rst.add(element.jsonPrimitive.content)
            }
        }
    } catch (e: Exception) {
        e.printStackTrace()
    }
    return rst
}

fun List<*>.toJsonElement(): JsonElement {
    val list: MutableList<JsonElement> = mutableListOf()
    this.forEach {
        val value = it ?: return@forEach
        when (value) {
            is JsonElement -> list.add(value)
            is Map<*, *> -> list.add((value).toJsonElement())
            is List<*> -> list.add(value.toJsonElement())
            else -> list.add(JsonPrimitive(value.toString()))
        }
    }
    return JsonArray(list)
}

fun Map<*, *>.toJsonElement(): JsonElement {
    val map: MutableMap<String, JsonElement> = mutableMapOf()
    this.forEach {
        val key = it.key as? String ?: return@forEach
        val value = it.value ?: return@forEach
        when (value) {
            is JsonElement -> map[key] = value
            is Map<*, *> -> map[key] = (value).toJsonElement()
            is List<*> -> map[key] = value.toJsonElement()
            else -> map[key] = JsonPrimitive(value.toString())
        }
    }
    return JsonObject(map)
}

fun JsonElement.getRawContent(): Any? {
    val list = mutableListOf<Any?>()
    val map: MutableMap<String, Any?> = mutableMapOf()
    when (this) {
        is JsonObject -> {
            this.forEach {
                map[it.key] = (it.value as? JsonElement)?.getRawContent()
            }
        }
        is JsonArray -> {
            this.forEach {
                list.add(it.getRawContent())
            }
        }
        is JsonPrimitive -> {
            if (this.isString) {
                return this.content
            }
            if (this is JsonNull) {
                return null
            }
            if (this.content.contains("true", true) || this.content.contains("false", true)) {
                return this.booleanOrNull ?: false
            }
            if (this.content.contains(".")) {
                return this.doubleOrNull ?: 0.0
            }
            return this.longOrNull ?: 0
        }
        else -> return null
    }
    return if (map.isEmpty() && list.isEmpty()) {
        null
    } else if (map.isNotEmpty()) {
        map
    } else {
        list
    }
}

fun JsonElement?.isNull() = this == null || this is JsonNull

@SharedImmutable
val jsonInstance by lazy {
    Json {
        ignoreUnknownKeys = true
        isLenient = true
    }
}