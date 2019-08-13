package co.lithobyte.networkrequests.models

import co.lithobyte.functionalkotlin.into
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import org.json.JSONObject

inline fun <reified T> Gson.fromJsonString(json: String?): T? {
    val token = object: TypeToken<T>() {}.type
    return this.fromJson<T>(json, token)
}

inline fun <reified T> jsonStringToModel(string: String?, gson: Gson, key: String = snakeCaseKeyForType<T>()): T? {
    return string?.jsonStringValueFor(key) into gson::fromJsonString
}

fun String.jsonStringValueFor(key: String) = JSONObject(this)[key].toString()
