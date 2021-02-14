package de.weichwarenprojekt.getherfit

import android.animation.ObjectAnimator
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentTransaction
import de.weichwarenprojekt.getherfit.data.DataService
import de.weichwarenprojekt.getherfit.data.Space
import de.weichwarenprojekt.getherfit.data.Space_
import de.weichwarenprojekt.getherfit.home.HomeFragment
import de.weichwarenprojekt.getherfit.settings.Settings
import de.weichwarenprojekt.getherfit.settings.SettingsActivity
import de.weichwarenprojekt.getherfit.shared.BaseActivity
import de.weichwarenprojekt.getherfit.shared.ScrollWatcher
import de.weichwarenprojekt.getherfit.shared.components.ImageButton
import de.weichwarenprojekt.getherfit.space.EditSpaceActivity
import de.weichwarenprojekt.getherfit.space.SpaceFragment


class MainActivity : BaseActivity() {

    companion object {
        /**
         * The request code if the settings are opened
         */
        const val SETTINGS = 1

        /**
         * The request code if the editing activity is opened
         */
        const val EDIT_SPACE = 2
    }

    /**
     * The share button
     */
    private lateinit var shareButton: ImageView

    /**
     * The edit button
     */
    private lateinit var editButton: ImageView

    /**
     * The button for the personal view
     */
    private lateinit var homeButton: ImageButton

    /**
     * The container for the space buttons
     */
    private lateinit var spaceView: LinearLayout

    /**
     * The currently shown content fragment
     */
    private lateinit var content: Fragment

    /**
     * The active spaces
     */
    private var spaces = ArrayList<Pair<Space, ImageButton>>()

    /**
     * Initialize the activity
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        shareButton = findViewById(R.id.share_space)
        editButton = findViewById(R.id.edit_space)
        homeButton = findViewById(R.id.personal_space)
        spaceView = findViewById(R.id.spaces)
        updateView()
    }

    /**
     * Check if the main view has to be updated
     */
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == SETTINGS) {
            if (resultCode == SettingsActivity.CHANGED) {
                reload()
            } else if (resultCode == SettingsActivity.LOGGED_OUT) {
                startActivity(Intent(this, LoginActivity::class.java))
                finish()
            }
        } else if (requestCode == EDIT_SPACE && resultCode == EditSpaceActivity.SPACE_EDITED) {
            updateSpaces()
        } else if (requestCode == EDIT_SPACE && resultCode == EditSpaceActivity.SPACE_REMOVED) {
            updateSpaces()
            openHome()
        }
    }

    /**
     * Update the activity view
     */
    private fun updateView() {
        // Fill the navigation with spaces
        updateSpaces()

        // Open the right content and highlight the corresponding button
        if (Settings.lastOpenedSpace.value == Settings.HOME) openHome()
        else openSpace(Settings.lastOpenedSpace.value)

        // Check if the user scrolled down and hide dynamically hide the navigation
        var visible = true
        val toolbarHeight = resources.getDimension(R.dimen.toolbar_height)
        val toolbarThreshold = resources.getDimension(R.dimen.toolbar_hide_threshold)
        val toolbar = findViewById<ViewGroup>(R.id.toolbar)
        val bottomNavHeight = resources.getDimension(R.dimen.bottom_nav_height)
        ScrollWatcher.onScroll = { scrollY ->
            val bottomNav = content.view!!.findViewById<View>(R.id.bottom_navigation)
            if (scrollY > toolbarThreshold && visible) {
                visible = false
                ObjectAnimator.ofFloat(toolbar, "y", -toolbarHeight).start()
                ObjectAnimator.ofFloat(bottomNav, "translationY", bottomNavHeight).start()
            } else if (scrollY < toolbarThreshold && !visible) {
                visible = true
                ObjectAnimator.ofFloat(toolbar, "y", 0.0f).start()
                ObjectAnimator.ofFloat(bottomNav, "translationY", 0.0f).start()
            }
        }
    }

    /**
     * Update the space view
     */
    private fun updateSpaces() {
        // Remove the previous views
        spaceView.removeAllViews()
        spaces.clear()

        // Fill the space button pairs
        val allSpaces = DataService.spaceBox.query().order(Space_.name).build().find()
        for (i in allSpaces.indices) {
            // Create the image button and add the space button pair
            allSpaces[i].loadImage()
            val imageButton = ImageButton(this)
            imageButton.updateView(
                allSpaces[i].name,
                R.drawable.nav_button_highlighting,
                image = allSpaces[i].image
            )
            spaceView.addView(imageButton)
            spaces.add(Pair(allSpaces[i], imageButton))

            // Listen for click events
            imageButton.setOnClickListener {
                Settings.lastOpenedTab.update(0, this)
                openSpace(i)
            }
        }

        // Set the highlighting of the button
        if (Settings.lastOpenedSpace.value >= 0 && Settings.lastOpenedSpace.value < allSpaces.size)
            spaces[Settings.lastOpenedSpace.value].second.showHighlighting(true)
        else homeButton.showHighlighting(true)
    }

    /**
     * Open a space
     *
     * @param position The position of the space
     */
    private fun openSpace(position: Int) {
        // Check if the group at this position exists
        if (position < 0 || position >= spaces.size) {
            Settings.lastOpenedTab.update(0, this)
            openHome()
            return
        }

        // Show the space
        shareButton.visibility = View.VISIBLE
        editButton.visibility = View.VISIBLE
        homeButton.showHighlighting(false)
        for (space in spaces) space.second.showHighlighting(false)
        spaces[position].second.showHighlighting(true)
        Settings.lastOpenedSpace.update(position, this)
        showFragment(SpaceFragment(spaces[position].first))
    }

    /**
     * Open the personal view
     */
    private fun openHome() {
        shareButton.visibility = View.GONE
        editButton.visibility = View.GONE
        homeButton.showHighlighting(true)
        for (space in spaces) space.second.showHighlighting(false)
        Settings.lastOpenedSpace.update(Settings.HOME, this)
        showFragment(HomeFragment())
    }

    /**
     * Open the personal view
     */
    fun openHome(v: View) {
        Settings.lastOpenedTab.update(0, this)
        openHome()
    }

    /**
     * Add a space
     */
    fun addSpace(v: View) {
        EditSpaceActivity.prepare(true)
        val intent = Intent(this, EditSpaceActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NO_ANIMATION
        startActivityForResult(intent, EDIT_SPACE)
    }

    /**
     * Edit the currently opened space
     */
    fun editSpace(v: View) {
        EditSpaceActivity.prepare(false, spaces[Settings.lastOpenedSpace.value].first)
        val intent = Intent(this, EditSpaceActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NO_ANIMATION
        startActivityForResult(intent, EDIT_SPACE)
    }

    /**
     * Switch to the settings
     */
    fun openSettings(v: View) {
        val intent = Intent(this, SettingsActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NO_ANIMATION
        startActivityForResult(intent, SETTINGS)
    }

    /**
     * Show a given fragment as main content
     *
     * @param fragment The content fragment
     */
    private fun showFragment(fragment: Fragment) {
        content = fragment
        val ft: FragmentTransaction = supportFragmentManager.beginTransaction()
        ft.replace(R.id.content, fragment)
        ft.commit()
    }
}