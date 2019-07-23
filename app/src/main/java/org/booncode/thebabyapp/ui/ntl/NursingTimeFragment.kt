package org.booncode.thebabyapp.ui.ntl

import android.content.Context
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import org.booncode.thebabyapp.R
import org.booncode.thebabyapp.db.NursingDatabase
import org.booncode.thebabyapp.db.NursingEntry

/**
 * A fragment representing a list of Items.
 * Activities containing this fragment MUST implement the
 * [NursingTimeFragment.OnListFragmentInteractionListener] interface.
 */
class NursingTimeFragment : Fragment(),
        NursingDatabase.OnSavedEntryListener {

    private val TAG = "TBA.NTFragment"

    private var listener: OnListFragmentInteractionListener? = null
    private var adapter: NursingTimeAdapter? = null
    private var db: NursingDatabase? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_nursingtime_list, container, false)

        db = NursingDatabase(this.context!!, null)
        db!!.setOnSavedEntryListener(this)
        adapter = NursingTimeAdapter(listener)
        adapter!!.setCursor(db?.getSavedEntries())
        // Set the adapter
        if (view is RecyclerView) {
            view.layoutManager = LinearLayoutManager(context)
            view.adapter = adapter
        }
        return view
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is OnListFragmentInteractionListener) {
            listener = context
        } else {
            throw RuntimeException(context.toString() + " must implement OnListFragmentInteractionListener")
        }
    }

    override fun onDetach() {
        super.onDetach()
        listener = null
    }

    override fun onHiddenChanged(hidden: Boolean) {
        super.onHiddenChanged(hidden)
        Log.d(TAG, "hidden changed to: ${hidden}")
    }

    override fun onAttachFragment(childFragment: Fragment?) {
        super.onAttachFragment(childFragment)
        Log.d(TAG, "attached fragment")
    }

    override fun onViewStateRestored(savedInstanceState: Bundle?) {
        super.onViewStateRestored(savedInstanceState)
        Log.d(TAG, "View state restored")
    }

    override fun onSavedEntry() {
        Log.d(TAG, "Update fragment")
        adapter?.setCursor(db?.getSavedEntries())
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     *
     *
     * See the Android Training lesson
     * [Communicating with Other Fragments](http://developer.android.com/training/basics/fragments/communicating.html)
     * for more information.
     */
    interface OnListFragmentInteractionListener {
        // TODO: Update argument type and name
        fun onNurseTimeEntryInteraction(item: NursingEntry)
    }

    companion object {
        @JvmStatic
        fun newInstance() = NursingTimeFragment()
    }
}
