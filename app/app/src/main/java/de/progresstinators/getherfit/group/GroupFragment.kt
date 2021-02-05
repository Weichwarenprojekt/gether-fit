package de.progresstinators.getherfit.group

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.bottomnavigation.BottomNavigationView
import de.progresstinators.getherfit.R
import de.progresstinators.getherfit.settings.Settings
import de.progresstinators.getherfit.shared.OverviewFragment
import de.progresstinators.getherfit.shared.TrainingFragment

class GroupFragment : Fragment() {

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
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(R.layout.fragment_group, container, false)

        // Instantiate the view pager
        viewPager = view.findViewById(R.id.view_pager)
        viewPager.adapter = ScreenSlidePagerAdapter(this)
        viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                when (position) {
                    1 -> bottomNav.selectedItemId = R.id.training
                    2 -> bottomNav.selectedItemId = R.id.recipes
                    else -> bottomNav.selectedItemId = R.id.overview
                }
                super.onPageSelected(position)
            }
        })

        // Initialize the bottom navigation bar
        bottomNav = view.findViewById(R.id.bottom_navigation)
        bottomNav.visibility = when(Settings.showBottomNav.value) {
            true -> View.VISIBLE
            else -> View.GONE
        }
        bottomNav.setOnNavigationItemSelectedListener { item ->
            when (item.itemId) {
                R.id.training -> {
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
     * A simple pager adapter that represents 5 ScreenSlidePageFragment objects, in
     * sequence.
     */
    private inner class ScreenSlidePagerAdapter(fragment: Fragment) : FragmentStateAdapter(fragment) {
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
            return when (position) {
                1 -> TrainingFragment()
                2 -> RecipesFragment()
                else -> OverviewFragment()
            }
        }
    }
}