package com.example.newsapp.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.viewpager.widget.ViewPager
import com.example.newsapp.R
import com.example.newsapp.ui.adapter.SectionPagerAdapter
import com.google.android.material.tabs.TabLayout

class HomeFragment : Fragment(R.layout.fragment_home) {
    private lateinit var  v: View
    private lateinit var viewPager: ViewPager
    private lateinit var tabLayout: TabLayout

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        v = inflater.inflate(R.layout.fragment_home, container, false)

        viewPager = v.findViewById(R.id.viewPager)
        tabLayout = v.findViewById(R.id.tabLayout)
        //tabLayout.getTabAt(0)?.select()
        tabLayout.setScrollPosition(0,0f,true)
        viewPager.currentItem = 0

        return v
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        setUpViewPager (viewPager)
        tabLayout.setupWithViewPager(viewPager)

    }

    private fun setUpViewPager(viewPager: ViewPager) {
        val adapter = SectionPagerAdapter (childFragmentManager)
        adapter.addFragment(TopNewsFragment(), "Top News")
        adapter.addFragment(BusinessFragment(), "Business")
        adapter.addFragment(HealthFragment(), "Health")
        adapter.addFragment(SportsFragment(), "Sports")
        adapter.addFragment(EntertainmentFragment(), "Entertainment")
        viewPager.adapter = adapter
    }
}