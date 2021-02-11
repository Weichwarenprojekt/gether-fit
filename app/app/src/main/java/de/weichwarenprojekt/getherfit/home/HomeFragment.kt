package de.weichwarenprojekt.getherfit.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.bottomnavigation.BottomNavigationView
import de.weichwarenprojekt.getherfit.R
import de.weichwarenprojekt.getherfit.settings.Settings
import de.weichwarenprojekt.getherfit.shared.ScrollWatcher
import de.weichwarenprojekt.getherfit.shared.overview.OverviewFragment

class HomeFragment : Fragment() {

    /**
     * The view pager
     */
    private lateinit var viewPager: ViewPager2

    /**
     * The bottom navigation bar
     */
    private lateinit var bottomNav: BottomNavigationView

    /***
     * Initialize the view
     */
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        val view = inflater.inflate(R.layout.fragment_home, container, false)

        // Instantiate the view pager
        viewPager = view.findViewById(R.id.view_pager)
        val adapter = ScreenSlidePagerAdapter(this)
        viewPager.adapter = adapter
        viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                when (position) {
                    1 -> bottomNav.selectedItemId = R.id.routines
                    2 -> bottomNav.selectedItemId = R.id.recipes
                    else -> bottomNav.selectedItemId = R.id.overview
                }
                super.onPageSelected(position)
                val content: Fragment? = adapter.views[position]
                if (content != null) ScrollWatcher.reset(content.view!!.findViewById(R.id.scroll_view))
            }
        })

        // Initialize the bottom navigation bar
        bottomNav = view.findViewById(R.id.bottom_navigation)
        bottomNav.visibility = when (Settings.showBottomNav.value) {
            true -> View.VISIBLE
            else -> View.GONE
        }
        bottomNav.setOnNavigationItemSelectedListener { item ->
            when (item.itemId) {
                R.id.routines -> {
                    viewPager.setCurrentItem(1, true)
                    true
                }
                R.id.recipes -> {
                    viewPager.setCurrentItem(2, true)
                    true
                }
                else -> {
                    viewPager.setCurrentItem(0, true)
                    true
                }
            }
        }

        return view
    }

    /**
     * The adapter for the view pager
     */
    private inner class ScreenSlidePagerAdapter(fragment: Fragment) : FragmentStateAdapter(fragment) {
        /**
         * The fragments
         */
        val views = Array<Fragment?>(3) { null }

        /**
         * @return The page count
         */
        override fun getItemCount(): Int = 3

        /**
         * Find the right view for a given position
         *
         * @param position The required position
         * @return The corresponding fragment
         */
        override fun createFragment(position: Int): Fragment {
            when (position) {
                1 -> views[1] = RoutinesFragment()
                2 -> views[2] = RecipesFragment()
                else -> views[0] = OverviewFragment()
            }
            return views[position]!!
        }
    }
}