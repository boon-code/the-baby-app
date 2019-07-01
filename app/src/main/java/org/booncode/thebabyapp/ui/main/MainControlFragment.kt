package org.booncode.thebabyapp.ui.main

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.ToggleButton
import android.widget.Button
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer

import org.booncode.thebabyapp.R
import java.util.*

class MainControlFragment : Fragment() {

    companion object {
        fun newInstance() = MainControlFragment()
    }

    private var startTime: Long = 0
    private var timerRunning: Boolean = false
    private var nursingTimer: Timer? = null
    private val duration = MutableLiveData<Long>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val root = inflater.inflate(R.layout.main_control_fragment, container, false)
        val button = root.findViewById<ToggleButton>(R.id.toggleButton)
        button.setOnClickListener {
            setTimerState(button.isChecked)
        }
        val textbox = root.findViewById<TextView>(R.id.text)
        duration.observe(this, Observer<Long> {
            val seconds = it / 1000.0
            textbox.text = "Duration is $seconds seconds"
        })
        return root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        if (savedInstanceState != null) {
            timerRunning = savedInstanceState.getBoolean("timerRunning", false)
            startTime = savedInstanceState.getLong("startTime", 0)
        }
        setTimerState(timerRunning)
    }

    private fun setTimerState(enabled: Boolean) {
        if (enabled) {
            nursingTimer?.cancel()
            if (!timerRunning) {
                startTime = Calendar.getInstance().timeInMillis
            }
            nursingTimer = Timer()
            nursingTimer!!.scheduleAtFixedRate(object : TimerTask() {
                override fun run() {
                    duration.postValue(Calendar.getInstance().timeInMillis - startTime)
                }
            }, 100, 100)
        } else {
            nursingTimer?.cancel()
            nursingTimer = null
        }
        timerRunning = enabled
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putBoolean("timerRunning", timerRunning)
        outState.putLong("startTime", startTime)
    }
}
