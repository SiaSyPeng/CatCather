package com.cs65.gnf.slidingtabstuff

import android.app.Activity
import android.app.Fragment
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem

class MainActivity : Activity() {

    private lateinit var slidingTabLayout: SlidingTabLayout
    private lateinit var mViewPager: ViewPager
    private lateinit var fragments: ArrayList<Fragment>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        slidingTabLayout = findViewById(R.id.tab) as SlidingTabLayout
        mViewPager = findViewById(R.id.viewpager) as ViewPager

        fragments = ArrayList<Fragment>()

    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        val inflater = menuInflater
        inflater.inflate(R.menu.main)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        when (item?.itemId) {
            //bla bla bla
        }
        return super.onOptionsItemSelected(item)
    }
}