package co.lithobyte.networkrequests.models

import co.lithobyte.functionalkotlin.into
import com.cesarferreira.pluralize.pluralize

fun String.toSnakeCase(): String {
    val regex = "([a-z])([A-Z]+)".toRegex()
    val replacement = "$1_$2"
    return this.replace(regex, replacement).toLowerCase()
}

inline fun <reified T> snakeCaseKeyForType(): String {
    return (T::class.java).simpleName.toSnakeCase()
}

fun pluralized(string: String): String {
    if (string.endsWith("tion")) return string + "s"
    return string.pluralize()
}

inline fun <reified T> pluralKeyForType() = snakeCaseKeyForType<T>() into ::pluralized
