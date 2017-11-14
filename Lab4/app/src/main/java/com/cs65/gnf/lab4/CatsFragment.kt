package com.cs65.gnf.lab4

import android.app.ListFragment
import android.content.*
import android.os.Bundle
import android.support.v4.content.LocalBroadcastManager
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.ImageView
import android.widget.TextView
import com.squareup.picasso.Picasso

/**
 * Fragment in the tab view that displays a list of all the cats currently saved
 */
class CatsFrag : ListFragment() {

    //For from shared preferences
    private val USER_PREFS = "profile_data" //Shared with other activities
    private val READY_STRING = "ready"

    private lateinit var broadcastReceiver: BroadcastReceiver

    private val BROADCAST_ACTION = "com.cs65.gnf.lab4.ready"

    private var catList: ArrayList<Cat>? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        broadcastReceiver = MyRecvr()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        //Inflate layout
        val rootView = inflater.inflate(R.layout.fragment_cats,container,false) as ViewGroup
        val prefs = activity.getSharedPreferences(USER_PREFS, Context.MODE_PRIVATE)

        if (!prefs.getBoolean(READY_STRING,false)) { //if not ready

            val i = IntentFilter(BROADCAST_ACTION)

            LocalBroadcastManager.getInstance(activity.applicationContext)
                    .registerReceiver(broadcastReceiver,i) //register broadcast receiver

        }
        else {
            Log.d("CATSFRAG"," was ready")
            getCats()
        }

        return rootView
    }

    /**
     * inner class that is a broadcast receiver, so that when the broadcast is received we can getCats
     */
    inner class MyRecvr : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            getCats()
        }
    }

    /**
     * Called either at the beginning of onCreateView, or whenever the MainActivity sends a
     * broadcast signifying that the input file is ready
     */
    private fun getCats() {

        val act = activity as MainActivity
        catList = act.catList

        //Sort them into an array (here, by name)
        //TODO if time, let user sort by different things
        catList?.sortedBy { it.name }
        val catArray = catList?.toTypedArray()

        if (catArray != null) {
            listAdapter = CatViewAdaptor(context,catArray)
        }

        retainInstance = true
    }
}

/**
 * Takes a pre-sorted array of cats and displays them
 */
class CatViewAdaptor(private val ctx: Context, private val catArray: Array<Cat>) : BaseAdapter() {

    override fun getCount(): Int {
        return catArray.size
    }

    override fun getItem(position: Int): Any {
        return catArray[position]
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    //Created a holder class — improves performance by reducing findView calls
    inner class ViewHolder(v : View) {
        val pic: ImageView = v.findViewById(R.id.catPic)
        val name: TextView = v.findViewById(R.id.catName)
        val lat: TextView = v.findViewById(R.id.catLat)
        val lng: TextView = v.findViewById(R.id.catLng)
        val petted: ImageView = v.findViewById(R.id.catPettedImage)
    }

    /**
     * Returns the necessary view, for the List View
     */
    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        val row: View
        val holder: ViewHolder

        if (convertView==null) { //if the row has nothing rn
            val inflater = LayoutInflater.from(ctx)
            row = inflater.inflate(R.layout.custom_row_view,parent,false)
            holder = ViewHolder(row) //set it to this holder
            row.tag = holder //set its tag to this holder
        }
        else { //if the row has  something rn
            row = convertView
            holder = row.tag as ViewHolder //get holder from its tag
        }

        //Get the cat that is at this position
        val cat = getItem(position) as Cat

        //Set all the fields in the view
        Picasso.with(ctx).load(cat.picUrl).placeholder(R.drawable.alice).into(holder.pic)
        holder.name.text = cat.name

        val latText = "Latitude: " + cat.lat.toString()
        holder.lat.text = latText

        val lngText = "Longitude: " + cat.lng.toString()
        holder.lng.text = lngText

        if (cat.petted) {
            holder.petted.setImageResource(R.drawable.petted)
        }
        else {
            holder.petted.setImageResource(R.drawable.not_petted)
        }

        return row
    }
}