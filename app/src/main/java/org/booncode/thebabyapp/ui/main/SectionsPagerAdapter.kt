package org.booncode.thebabyapp.ui.main

import android.content.Context
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import org.booncode.thebabyapp.R

private val TAB_TITLES = arrayOf(
    R.string.tab1_title,
    R.string.tab2_title,
    R.string.tab3_title
)

/**
 * A [FragmentPagerAdapter] that returns a fragment corresponding to
 * one of the sections/tabs/pages.
 */
class SectionsPagerAdapter(private val context: Context, fm: FragmentManager) : FragmentPagerAdapter(fm) {
    val placeholder = arrayOf(
        MainControlFragment.newInstance(),
        PlaceholderFragment.newInstance(2),
        PlaceholderFragment.newInstance(3)

    )

    override fun getItem(position: Int): Fragment {
        // getItem is called to instantiate the fragment for the given page.
        // Return a PlaceholderFragment (defined as a static inner class below).
        return placeholder[position]
    }

    override fun getPageTitle(position: Int): CharSequence? {
        return context.resources.getString(TAB_TITLES[position])
    }

    override fun getCount(): Int {
        return TAB_TITLES.size
    }
}