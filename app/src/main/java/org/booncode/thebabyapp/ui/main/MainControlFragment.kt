package org.booncode.thebabyapp.ui.main

import android.app.AlertDialog
import android.content.DialogInterface
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.widget.TextView
import android.widget.Button
import androidx.lifecycle.Observer
import android.util.Log
import android.view.*
import org.booncode.thebabyapp.common.*
import org.booncode.thebabyapp.R
import org.booncode.thebabyapp.db.NursingDatabase
import org.booncode.thebabyapp.db.NursingEntry
import org.booncode.thebabyapp.db.NursingState
import java.text.SimpleDateFormat
import java.util.*


class MainControlFragment : Fragment() {
    companion object {
        fun newInstance() = MainControlFragment()
    }

    private enum class State {
        IDLE, READY, RUNNING
    }

    private val TAG: String = "TBA.MainControl"

    private var buttonNurseStart: Button? = null
    private var buttonNurseStop: Button? = null
    private var currentBraView: BraView? = null
    private var lastBraView: BraView? = null
    private var mnuClearTimer: MenuItem? = null

    private var ntStart: TextView? = null
    private var ntStop: TextView? = null
    private var ntLastDuration: TextView? = null
    private var ntDuration: TextView? = null

    private val stopWatch = StopWatch(100)

    private var db: NursingDatabase? = null
    private var currentEntry :NursingEntry? = null
    private var lastSavedEntry: NursingEntry? = null
    private var currentState: State = State.IDLE

    private val TIME_FORMAT = SimpleDateFormat("HH:mm:ss")


    private fun startNursing() {
        Log.d(TAG, "Start nursing")
        if (currentState != State.READY)
            return

        val braState = currentEntry!!.braSide
        if (braState == BraState.None) {
            Log.d(TAG, "Wrong bra state: None")
            return
        }

        stopWatch.start()
        currentEntry = db!!.setPending(braState, stopWatch.startTime)
        updateAllState()
    }

    private fun stopNursing() {
        Log.d(TAG, "Stop nursing")

        if (currentState != State.RUNNING)
            return

        val builder = AlertDialog.Builder(activity!!);
        with(builder) {
            setTitle(R.string.nurse_dialog_title)
            setPositiveButton(R.string.nurse_dialog_positive) { _: DialogInterface?, _: Int ->
                stopWatch.stop()
                db!!.saveEntry(stopWatch.stopTime)
                currentEntry = db!!.getUnfinishedEntry()
                lastSavedEntry = db!!.getLastSavedEntry()
                updateAllState()
            }
            setNegativeButton(R.string.nurse_dialog_negative, null)
            show()
        }
    }

    private fun changeBoobSide() {
        Log.d(TAG, "Change boob side: ${currentBraView!!.braState.name}")
        if (currentState == State.RUNNING)
            return

        val braState = currentBraView!!.braState
        currentEntry = db!!.setBraSide(braState)

        updateAllState()
    }

    private fun readDB() {
        if (db == null) {
            db = NursingDatabase(this.context!!, null)
        }

        currentEntry = db!!.getUnfinishedEntry()
        lastSavedEntry = db!!.getLastSavedEntry()

        stopWatch.stop()

        // Recover state here and sync timer
        currentState = getState(currentEntry!!)
        if (currentState == State.RUNNING) {
            stopWatch.start(currentEntry!!.start)
        }
    }

    private fun getState(entry: NursingEntry): State {
        when (entry.state) {
            NursingState.INVALID -> {
                when (entry.braSide) {
                    BraState.None -> return State.IDLE
                    BraState.Left, BraState.Right -> return State.READY
                }
            }

            NursingState.RUNNING -> return State.RUNNING

            else -> {
                Log.e(TAG, "Invalid state")
                return State.IDLE
            }
        }
    }

    private fun updateAllState() {
        currentState = getState(currentEntry!!)
        syncUI(currentState)
    }

    private fun fmtDuration(title: String, millis: Long): String {
        val tmp = formatDuration(millis).padStart(10)
        return "${title} ${tmp}"
    }

    private fun syncUI(state: State) {
        var showStart = false
        var running = false

        when (state) {
            State.READY -> showStart = true
            State.RUNNING -> running = true
            State.IDLE -> {}
        }

        buttonNurseStart?.isEnabled = showStart
        buttonNurseStop?.isEnabled = running
        currentBraView?.isClickable = !running
        currentBraView?.braState = currentEntry!!.braSide
        mnuClearTimer?.isEnabled = running

        lastBraView?.braState = if (lastSavedEntry != null) lastSavedEntry!!.braSide else BraState.None

        if (lastSavedEntry != null) {
            if (!running) {
                val strStart = TIME_FORMAT.format(Date(lastSavedEntry!!.start))
                val strStop = TIME_FORMAT.format(Date(lastSavedEntry!!.stop))
                ntStart?.text = "Start: ${strStart}"
                ntStop?.text = "Start: ${strStop}"
                ntDuration?.text = ""
            }
            ntLastDuration?.text = fmtDuration("Last Duration: ", lastSavedEntry!!.duration)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        Log.d(TAG, "onCreateView")
        readDB()
        val root = inflater.inflate(R.layout.main_control_fragment, container, false)
        ntStart = root.findViewById(R.id.ntStart)
        ntStop = root.findViewById(R.id.ntStop)
        ntLastDuration = root.findViewById(R.id.ntLastDuration)
        buttonNurseStart = root.findViewById<Button>(R.id.buttonNursingStart)
        buttonNurseStart!!.setOnClickListener { startNursing() }
        buttonNurseStop = root.findViewById<Button>(R.id.buttonNursingStop)
        buttonNurseStop!!.setOnClickListener { stopNursing() }

        ntDuration = root.findViewById<TextView>(R.id.ntDuration)
        stopWatch.duration.observe(this, Observer<Long> {
            if (currentState == State.RUNNING) {
                val now = Date(System.currentTimeMillis())
                val strStop = TIME_FORMAT.format(now)
                val strStart = TIME_FORMAT.format(Date(currentEntry!!.start))
                ntStart?.text = "Start: ${strStart}"
                ntStop?.text = "Stop: ${strStop}"
                ntDuration?.text = fmtDuration("Duration: ", it)
            }
        })

        currentBraView = root.findViewById<BraView>(R.id.currentBoob)
        currentBraView!!.setOnClickListener { changeBoobSide() }

        lastBraView = root.findViewById<BraView>(R.id.lastBoob)
        lastBraView!!.isActive = false

        updateAllState()

        return root
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        readDB()
        setHasOptionsMenu(true)
    }

    override fun onCreateOptionsMenu(menu: Menu?, inflater: MenuInflater?) {
        inflater?.inflate(R.menu.main_control_menu, menu)
        mnuClearTimer = menu?.findItem(R.id.action_nt_clear_timer)
        Log.d(TAG, "Create option menu")
        readDB()
        updateAllState()
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        return when (item?.itemId) {
            R.id.action_nt_clear_timer -> {
                Log.d(TAG, "Show discard timer dialog")
                val builder = AlertDialog.Builder(activity!!);
                with(builder) {
                    setTitle(R.string.nurse_dlg_discard_title)
                    setPositiveButton(R.string.nurse_dlg_discard_positive) { _: DialogInterface?, _: Int ->
                        stopWatch.stop()
                        currentEntry = db!!.cancelPending()
                        updateAllState()
                    }
                    setNegativeButton(R.string.nurse_dlg_discard_negative, null)
                    show()
                }
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
}
