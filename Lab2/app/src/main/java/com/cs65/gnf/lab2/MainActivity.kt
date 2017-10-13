package com.cs65.gnf.lab2

import android.app.Activity
import android.os.Bundle
import android.app.FragmentManager
import android.app.Fragment
import android.support.v13.app.FragmentPagerAdapter
import android.support.v4.view.ViewPager

class MainActivity: Activity() {

    private lateinit var tabs: ArrayList<Fragment>


    override fun onCreate(savedInstanceState: Bundle?) {
        //Init
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        //Assign global vars
        val tabStrip: SlidingTabLayout = findViewById(R.id.tabs)
        val mViewPager: ViewPager = findViewById(R.id.pager)
        val adapter = TabAdapter(fragmentManager)

        //Add tabs
        tabs = ArrayList()
        tabs.add(PlayFrag())
        tabs.add(HistoryFrag())
        tabs.add(RankingFrag())
        tabs.add(SettingsFrag())

        //Assign adapter (see below)
        mViewPager.adapter = adapter

        //Set tab to evenly distribute, and then connect it to the viewPager
        tabStrip.setDistributeEvenly(true)
        tabStrip.setViewPager(mViewPager)
    }

    /**
     * Adapter for the tab in this activity
     */
    inner class TabAdapter constructor(fm: FragmentManager): FragmentPagerAdapter(fm) {

        override fun getCount(): Int {
            return tabs.size
        }

        override fun getItem(position: Int): Fragment {
            return tabs[position]
        }

        override fun getPageTitle(position: Int): CharSequence? {
            when (position) {
                0 -> return "Play"
                1 -> return "History"
                2 -> return "Rankings"
                3 -> return "Settings"
            }
            return null
        }
    }


}