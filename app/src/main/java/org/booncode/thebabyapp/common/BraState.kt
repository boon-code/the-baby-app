package org.booncode.thebabyapp.common


/** Current state of the view */
enum class BraState(val value: Int) : Valuable<Int> {
    None(0),
    Left(1),
    Right(2);

    override fun getValue(): Int {
        return value
    }

    companion object {
        fun from(value: Int): BraState = fromEnum(value)
    }
}