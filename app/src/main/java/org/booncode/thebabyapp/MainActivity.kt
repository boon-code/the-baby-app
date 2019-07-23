package org.booncode.thebabyapp

import android.os.Bundle
import android.util.Log
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.tabs.TabLayout
import androidx.viewpager.widget.ViewPager
import androidx.appcompat.app.AppCompatActivity
import org.booncode.thebabyapp.db.NursingEntry
import org.booncode.thebabyapp.ui.main.SectionsPagerAdapter
import org.booncode.thebabyapp.ui.ntl.NursingTimeFragment


class MainActivity : AppCompatActivity(),
    NursingTimeFragment.OnListFragmentInteractionListener {

    private val TAG = "TBA.Main"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val sectionsPagerAdapter = SectionsPagerAdapter(this, supportFragmentManager)
        val viewPager: ViewPager = findViewById(R.id.view_pager)
        viewPager.adapter = sectionsPagerAdapter
        val tabs: TabLayout = findViewById(R.id.tabs)
        tabs.setupWithViewPager(viewPager)
        setSupportActionBar(findViewById(R.id.toolbar))
    }

    override fun onBackPressed() {
        moveTaskToBack(true)
    }

    override fun onNurseTimeEntryInteraction(item: NursingEntry) {
        val txt = item.toString()
        Log.d(TAG, "Clicked on item ${txt}")
    }

}