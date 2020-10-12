package org.booncode.thebabyapp.common

import android.database.Cursor
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView

abstract class RecycleViewCursorAdapter<T: RecyclerView.ViewHolder>() : RecyclerView.Adapter<T>() {
    private var currentCursor: Cursor? = null;

    init {
        setHasStableIds(true);
    }

    override fun onBindViewHolder(holder: T, position: Int) {
        moveToPosition(position)
        onBindViewHolder(holder, currentCursor!!)
    }

    override fun getItemCount(): Int {
        if (currentCursor != null) {
            return currentCursor!!.count
        } else {
            return 0
        }
    }

    override fun getItemId(position: Int): Long {
        moveToPosition(position)
        return currentCursorId(currentCursor!!)
    }

    fun setCursor(cursor: Cursor?) {
        if (cursor != null) {
            currentCursor?.close()
            currentCursor = cursor
            notifyDataSetChanged();
        } else {
            notifyItemRangeRemoved(0, itemCount)
            currentCursor?.close()
            currentCursor = null
        }
    }

    fun moveToPosition(position: Int) {
        if (currentCursor != null) {
            if (!currentCursor!!.moveToPosition(position)) {
                throw IllegalStateException("Position ${position} is out of bounds (0..${currentCursor!!.count}")
            }
        } else {
            throw IllegalStateException("Cursor not set")
        }
    }

    abstract fun onBindViewHolder(holder: T, cursor: Cursor)
    abstract fun currentCursorId(cursor: Cursor): Long
}