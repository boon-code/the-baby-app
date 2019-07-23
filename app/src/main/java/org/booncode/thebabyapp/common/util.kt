package org.booncode.thebabyapp.common

import java.lang.IllegalArgumentException


interface Valuable<T> {
    fun getValue(): T
}

inline fun <reified T, V> fromEnum(value: V): T
        where T : Enum<T>,
              T : Valuable<V> {

    for (i: T in enumValues<T>()) {
        if (i.getValue() == value) {
            return i
        }
    }
    throw IllegalArgumentException()
}


fun formatDuration(millis: Long): String {
    val seconds: Long = millis / 1000
    val minutesString = "%02d".format((seconds / 60))
    val secondsString = "%02d".format((seconds % 60))
    val tenthsString = "%d".format(((millis / 100) % 10))
    return "${minutesString}:${secondsString}:${tenthsString}"
}