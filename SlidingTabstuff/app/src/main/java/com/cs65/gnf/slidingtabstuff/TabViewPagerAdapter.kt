package com.cs65.gnf.slidingtabstuff

import android.app.Fragment
import android.app.FragmentManager

class TabViewPagerAdapter(fm: FragmentManager, private val fragments: ArrayList<Fragment>) :
        FragmentPagerAdapter(fm) {

    val count: Int
        get() = fragments.size

    fun getItem(position: Int): Fragment {
        return fragments[position]
    }
}
