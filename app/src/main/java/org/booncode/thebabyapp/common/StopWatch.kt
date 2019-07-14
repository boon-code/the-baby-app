package org.booncode.thebabyapp.common

import androidx.lifecycle.MutableLiveData
import java.util.*

class StopWatch(val updateTimeMS: Long = 100) {
    val duration = MutableLiveData<Long>()
    val startTime: Long
        get() {
            return tstart
        }

    val stopTime: Long
        get() {
            return tstop
        }

    private var tstart: Long = -1
    private var tstop: Long = -1
    private var timer: Timer? = null

    fun start(startValue: Long = -1) {
        timer?.cancel()
        if (startValue >= 0) {
            tstart = startValue
        } else {
            tstart = System.currentTimeMillis()
        }

        timer = Timer()
        timer!!.scheduleAtFixedRate(object : TimerTask() {
            override fun run() {
                duration.postValue(System.currentTimeMillis() - tstart)
            }
        }, updateTimeMS, updateTimeMS)
    }

    fun stop() {
        tstop = System.currentTimeMillis()
        timer?.cancel()
        timer = null
    }
}