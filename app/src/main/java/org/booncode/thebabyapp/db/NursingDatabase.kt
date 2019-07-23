package org.booncode.thebabyapp.db

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.util.Log
import org.booncode.thebabyapp.common.*
import java.lang.IllegalArgumentException


data class NursingEntry(var braSide: BraState = BraState.None,
                        var start: Long = -1,
                        var stop: Long = -1,
                        var duration: Long = -1,
                        var state: NursingState = NursingState.INVALID,
                        var id: Long? = null) {

    companion object {
        val TABLE =  "NursingTable"
        val ID = "NursingId"
        val BRA_SIDE = "BraSide"
        val START = "NursingStart"
        val STOP = "NursingStop"
        val DURATION = "NursingDurationSeconds"
        val STATE = "NursingState"
    }
}


class NursingDatabase(context: Context,
                      factory: SQLiteDatabase.CursorFactory?) :
        SQLiteOpenHelper(context, DB_NAME, factory, DB_VERSION) {

    private var savedEntryListener: OnSavedEntryListener? = null

    interface OnSavedEntryListener {
        fun onSavedEntry()
    }

    fun setOnSavedEntryListener(listener: OnSavedEntryListener?) {
        savedEntryListener = listener
    }

    init {
        dbInstances.add(this)
    }

    override fun onCreate(db: SQLiteDatabase?) {
        Log.d(TAG, "Database created")
        db!!.execSQL("""CREATE TABLE
            ${NursingEntry.TABLE} (
            ${NursingEntry.ID} INTEGER PRIMARY KEY AUTOINCREMENT,
            ${NursingEntry.BRA_SIDE} INTEGER,
            ${NursingEntry.START} INTEGER,
            ${NursingEntry.STOP} INTEGER,
            ${NursingEntry.DURATION} INTEGER,
            ${NursingEntry.STATE} INTEGER)
        """.trimIndent())
    }

    override fun onUpgrade(db: SQLiteDatabase?, thisVersion: Int, newVersion: Int) {
        TODO("Not yet necessary")
    }

    private fun updateLastEntry(func: (entry: NursingEntry) -> Unit): NursingEntry {
        val db = this.writableDatabase
        db.beginTransaction()
        try {
            var entry = lastEntry(db)
            if (entry == null)
                entry = createNew(db)
            func(entry)
            Log.d(TAG, "Saving ${entry.braSide}, ${entry.duration} ${entry.id}")
            db.update(
                NursingEntry.TABLE,
                toCV(entry),
                "${NursingEntry.ID} = ?",
                arrayOf(entry.id.toString())
            )
            db.setTransactionSuccessful()
            return entry
        } finally {
            db.endTransaction()
            db.close()
        }
    }

    fun setBraSide(braSide: BraState): NursingEntry {
        return updateLastEntry { entry ->
            when (entry.state) {
                NursingState.INVALID -> entry.braSide = braSide
                else -> {
                    Log.d(TAG, "Illegal operation, abort")
                }
            }
        }
    }

    fun setPending(braSide: BraState, start: Long): NursingEntry {
        when (braSide) {
            BraState.None -> throw IllegalArgumentException("BraState must be Left or Right")
            else -> { }
        }

        if (start < 0) {
            throw IllegalArgumentException("start time must be >= 0")
        }

        return updateLastEntry { entry ->
            when (entry.state) {
                NursingState.RUNNING, NursingState.INVALID -> {
                    entry.braSide = braSide
                    entry.start = start
                    entry.state = NursingState.RUNNING
                }

                else -> { }
            }
        }
    }

    fun cancelPending(): NursingEntry {
        return updateLastEntry { entry ->
            when (entry.state) {
                NursingState.RUNNING -> entry.state = NursingState.INVALID
                else -> { }
            }
        }
    }

    fun saveEntry(stop: Long, duration: Long = -1): NursingEntry {
        return updateLastEntry { entry ->
            when (entry.state) {
                NursingState.RUNNING -> {
                    entry.stop = stop
                    if (duration < 0) {
                        entry.duration = entry.stop - entry.start
                    } else {
                        entry.duration = duration
                    }
                    entry.state = NursingState.SAVED
                    onSavedEntry()
                }

                else -> { }
            }
        }
    }

    private fun createNew(db: SQLiteDatabase): NursingEntry {
        val cv = toCV(NursingEntry(state = NursingState.INVALID))
        db.insert(NursingEntry.TABLE, null, cv)
        val entry = lastEntry(db)!!
        Log.d(TAG, "Create new Entry: ${entry.id}")
        return entry
    }

    private fun queryEntry(cursor: Cursor): NursingEntry? {
        try {
            if (cursor.count == 0) {
                return null
            } else {
                cursor.moveToFirst()
                return toNursingEntry(cursor)
            }
        } finally {
            cursor.close()
        }
    }

    private fun lastEntry(db: SQLiteDatabase): NursingEntry? = queryEntry(queryLastEntry(db))

    private fun lastSavedEntry(db: SQLiteDatabase): NursingEntry? = queryEntry(queryLastSavedEntry(db))

    fun getLastSavedEntry(): NursingEntry? {
        val db = this.readableDatabase
        try {
            return lastSavedEntry(db)
        } finally {
            db.close()
        }
    }

    fun getSavedEntries(): Cursor {
        val db = this.readableDatabase
        return db.rawQuery("""
                SELECT * from ${NursingEntry.TABLE}
                WHERE ${NursingEntry.STATE} = ?
                ORDER BY ${NursingEntry.ID}
                DESC
            """.trimIndent(), arrayOf(NursingState.SAVED.value.toString()))
    }

    fun getUnfinishedEntry(): NursingEntry {
        val db = this.writableDatabase
        db.beginTransaction()
        try {
            var entry = lastEntry(db)
            when (entry?.state) {
                NursingState.RUNNING, NursingState.INVALID -> { }
                else -> { entry = createNew(db) }
            }
            db.setTransactionSuccessful()
            return entry
        } finally {
            db.endTransaction()
            db.close()
        }
    }

    private fun queryLastSavedEntry(db: SQLiteDatabase): Cursor {
        return db.rawQuery("""
            SELECT * from ${NursingEntry.TABLE}
            WHERE ${NursingEntry.STATE} = ?
            ORDER BY ${NursingEntry.ID}
            DESC LIMIT 1
        """.trimIndent(), arrayOf(NursingState.SAVED.value.toString()))
    }

    private fun queryLastEntry(db: SQLiteDatabase): Cursor {
        return db.rawQuery("""
            SELECT * from ${NursingEntry.TABLE}
            ORDER BY ${NursingEntry.ID}
            DESC LIMIT 1
        """.trimIndent(), null)
    }

    protected fun finalize() {
        dbInstances.remove(this)
    }

    companion object {
        private val DB_NAME = "NursingDB"
        private val DB_VERSION = 1
        private val TAG = "TBA.NursingDB"
        private val dbInstances = mutableListOf<NursingDatabase>()

        private fun onSavedEntry() {
            dbInstances.forEach {
                it.savedEntryListener?.onSavedEntry()
            }
        }

        fun toNursingEntry(cursor: Cursor): NursingEntry {
            val braSideIdx = cursor.getColumnIndexOrThrow(NursingEntry.BRA_SIDE)
            val startIdx = cursor.getColumnIndexOrThrow(NursingEntry.START)
            val stopIdx = cursor.getColumnIndexOrThrow(NursingEntry.STOP)
            val durationIdx = cursor.getColumnIndexOrThrow(NursingEntry.DURATION)
            val stateIdx = cursor.getColumnIndexOrThrow(NursingEntry.STATE)
            val idIdx = cursor.getColumnIndexOrThrow(NursingEntry.ID)

            return NursingEntry(
                braSide = BraState.from(cursor.getInt(braSideIdx)),
                start = cursor.getLong(startIdx),
                stop = cursor.getLong(stopIdx),
                duration = cursor.getLong(durationIdx),
                state = NursingState.from(cursor.getInt(stateIdx)),
                id = cursor.getLong(idIdx)
            )
        }

        fun toCV(entry: NursingEntry): ContentValues {
            val cvs = ContentValues()
            with(cvs) {
                put(NursingEntry.BRA_SIDE, entry.braSide.getValue())
                put(NursingEntry.START, entry.start)
                put(NursingEntry.STOP, entry.stop)
                put(NursingEntry.DURATION, entry.duration)
                put(NursingEntry.STATE, entry.state.getValue())
            }
            return cvs
        }
    }
}