package org.booncode.thebabyapp.db

import org.booncode.thebabyapp.common.Valuable
import org.booncode.thebabyapp.common.fromEnum

enum class NursingState(val value: Int) : Valuable<Int> {
    INVALID(0),
    RUNNING(10),
    SAVED(20),
    DELETED(30);

    override fun getValue(): Int {
        return value
    }

    companion object {
        fun from(value: Int): NursingState = fromEnum(value)
    }
}