package org.booncode.thebabyapp.ui.main

import android.content.Context
import android.os.Parcel
import android.os.Parcelable
import android.util.AttributeSet
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.widget.ImageView
import org.booncode.thebabyapp.R
import org.booncode.thebabyapp.common.*


/**
 * TODO: document your custom view class.
 */
class BraView(context: Context, attrs: AttributeSet? = null) : ImageView(context, attrs) {
    private val TAG: String = "TBA.BraView"
    private var state: BraState = BraState.None
    private var active: Boolean = true
    private var left: Boolean = false
    private var right: Boolean = false

    var isActive: Boolean
        get() = active
        set(value) {
            active = value
            changeBraState(state)
        }

    var braState: BraState
        get() = state
        set(value) {
            syncSelectionToState(value)
            changeBraState(value)
        }

    init {
        changeBraState(BraState.None)
        isClickable = true
    }

    private fun changeBraState(newState: BraState) {
        Log.d(TAG, "Changed state from ${newState.name} to ${state.name}")
        state = newState

        if (active) {
            when (state) {
                BraState.None -> setImageResource(R.drawable.ic_bra)
                BraState.Left -> setImageResource(R.drawable.ic_bra_left)
                BraState.Right -> setImageResource(R.drawable.ic_bra_right)
            }
        } else {
            when (state) {
                BraState.None -> setImageResource(R.drawable.ic_bra_disabled)
                BraState.Left -> setImageResource(R.drawable.ic_bra_disabled_left)
                BraState.Right -> setImageResource(R.drawable.ic_bra_disabled_right)
            }
        }
    }

    private fun selectionToState(): BraState {
        if (left) {
            return BraState.Left
        } else if (right) {
            return BraState.Right
        } else {
            return BraState.None
        }
    }

    private fun syncSelectionToState(requestedState: BraState) {
        left = false
        right = false
        when (requestedState) {
            BraState.Left -> left = true
            BraState.Right -> right = true
            BraState.None -> {}
        }
    }

    override fun onTouchEvent(event: MotionEvent?): Boolean {
        Log.d(TAG, "Got a touch event: ${event?.action}, clickable: ${isClickable}")
        if ((event?.action == MotionEvent.ACTION_DOWN) && isClickable && isActive) {
            if (event.x > (this.width / 2)) {
                Log.d(TAG, "Left Boob")
                left = !left
                right = false
            } else {
                Log.d(TAG, "Right Boob")
                right = !right
                left = false
            }
            changeBraState(selectionToState())
        }
        return super.onTouchEvent(event)
    }

    override fun onSaveInstanceState(): Parcelable? {
        val state = SavedBraState(super.onSaveInstanceState())
        state.isActive = isActive
        state.braState = braState
        return state
    }

    override fun onRestoreInstanceState(state: Parcelable?) {
        if (state is SavedBraState) {
            super.onRestoreInstanceState(state.superState)
            isActive = state.isActive
            braState = state.braState
        } else {
            super.onRestoreInstanceState(state)
        }
    }

    /** Implement Parcelable interface */
    internal class SavedBraState : View.BaseSavedState {
        val TAG = "TBA.BraState"
        private val byte0 = 0.toByte()
        private val byte1 = 1.toByte()

        private var active: Byte = byte1
        var isActive: Boolean
            get() = (active != byte0)
            set(value) {
                active = if (value) byte1 else byte0
            }
        var braState: BraState = BraState.None

        constructor(source: Parcel) : super(source) {
            active = source.readByte()
            val stateValue = source.readInt()
            try {
                braState = BraState.from(stateValue)
            } catch (e: IllegalArgumentException) {
                Log.d(TAG, "Ignore illegal braState: ${stateValue}")
            }
        }

        constructor(source: Parcelable?) : super(source)

        override fun writeToParcel(parcel: Parcel, flags: Int) {
            super.writeToParcel(parcel, flags)
            parcel.writeByte(active)
            parcel.writeInt(braState.getValue())
        }

        override fun describeContents(): Int {
            return 0
        }

        companion object CREATOR : Parcelable.Creator<SavedBraState> {
            override fun createFromParcel(parcel: Parcel): SavedBraState {
                return SavedBraState(parcel)
            }

            override fun newArray(size: Int): Array<SavedBraState?> {
                return arrayOfNulls(size)
            }
        }

    }
}
