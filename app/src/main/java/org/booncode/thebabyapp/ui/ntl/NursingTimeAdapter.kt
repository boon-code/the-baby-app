package org.booncode.thebabyapp.ui.ntl

import android.database.Cursor
import androidx.recyclerview.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import org.booncode.thebabyapp.R


import org.booncode.thebabyapp.ui.ntl.NursingTimeFragment.OnListFragmentInteractionListener

import kotlinx.android.synthetic.main.fragment_nursingtime.view.*
import org.booncode.thebabyapp.common.BraState
import org.booncode.thebabyapp.common.RecycleViewCursorAdapter
import org.booncode.thebabyapp.common.formatDuration
import org.booncode.thebabyapp.db.NursingDatabase
import org.booncode.thebabyapp.db.NursingEntry
import java.text.SimpleDateFormat
import java.util.*

/**
 * [RecyclerView.Adapter] that can display a [DummyItem] and makes a call to the
 * specified [OnListFragmentInteractionListener].
 * TODO: Replace the implementation with code for your data type.
 */
class NursingTimeAdapter(
    private val mListener: OnListFragmentInteractionListener?
) : RecycleViewCursorAdapter<NursingTimeAdapter.ViewHolder>() {

    private val mOnClickListener: View.OnClickListener
    private val TIME_FORMAT = SimpleDateFormat("dd.MM.yyyy HH:mm:ss")

    init {
        mOnClickListener = View.OnClickListener { v ->
            val item = v.tag as NursingEntry
            // Notify the active callbacks interface (the activity, if the fragment is attached to
            // one) that an item has been selected.
            mListener?.onNurseTimeEntryInteraction(item)
        }
    }

    override fun onBindViewHolder(holder: ViewHolder, cursor: Cursor) {
        val entry = NursingDatabase.toNursingEntry(cursor)
        holder.mStart.text = "${TIME_FORMAT.format(Date(entry.start))}"
        holder.mStop.text = "${TIME_FORMAT.format(Date(entry.stop))}"
        holder.mDuration.text = "${formatDuration(entry.duration)}"
        when (entry.braSide) {
            BraState.Right -> {
                holder.mSide.text = "Right"
                holder.mSideImage.setImageResource(R.drawable.ic_bra_right)
            }
            BraState.Left -> {
                holder.mSide.text = "Left"
                holder.mSideImage.setImageResource(R.drawable.ic_bra_left)
            }
            else -> {
                holder.mSide.text = "Invalid"
                holder.mSideImage.setImageResource(R.drawable.ic_bra_disabled_both)
            }
        }

        with(holder.mView) {
            tag = entry
            setOnClickListener(mOnClickListener)
        }
    }

    override fun currentCursorId(cursor: Cursor): Long {
        val entry = NursingDatabase.toNursingEntry(cursor)
        return entry.id!!
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.fragment_nursingtime, parent, false)
        return ViewHolder(view)
    }

    inner class ViewHolder(val mView: View) : RecyclerView.ViewHolder(mView) {
        val mStart: TextView = mView.start
        val mStop: TextView = mView.stop
        val mDuration: TextView = mView.duration
        val mSide: TextView = mView.braSide
        val mSideImage: ImageView = mView.imageBra

        override fun toString(): String {
            return "${super.toString()} '${mStart.text} - ${mStop.text} (${mDuration.text}, ${mSide.text})'"
        }
    }
}
