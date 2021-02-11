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
import de.weichwarenprojekt.getherfit.data.Group
import de.weichwarenprojekt.getherfit.group.EditGroupActivity
import de.weichwarenprojekt.getherfit.group.GroupFragment
import de.weichwarenprojekt.getherfit.home.HomeFragment
import de.weichwarenprojekt.getherfit.settings.SettingsActivity
import de.weichwarenprojekt.getherfit.settings.User
import de.weichwarenprojekt.getherfit.shared.BaseActivity
import de.weichwarenprojekt.getherfit.shared.components.ImageButton
import de.weichwarenprojekt.getherfit.shared.ScrollWatcher


class MainActivity : BaseActivity() {

    /**
     * The possible activity request codes
     */
    companion object {
        const val SETTINGS = 1
        const val EDIT_GROUP = 2
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
    private lateinit var personalButton: ImageButton

    /**
     * The container for the group buttons
     */
    private lateinit var groupView: LinearLayout

    /**
     * The currently shown content fragment
     */
    private lateinit var content: Fragment

    /**
     * The active groups
     */
    private var groups = HashMap<String, Pair<Group, ImageButton>>()

    /**
     * The currently opened group
     */
    private var group = Group()

    /**
     * A potential new group
     */
    private var newGroup = Group()

    /**
     * Initialize the activity
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        shareButton = findViewById(R.id.share_group)
        editButton = findViewById(R.id.edit_group)
        personalButton = findViewById(R.id.personal_space)
        groupView = findViewById(R.id.groups)
        updateView()
    }

    /**
     * Check if the settings changed something about the appearance
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
        } else if (requestCode == EDIT_GROUP && resultCode == EditGroupActivity.GROUP_ADDED) {
            addGroup(newGroup)
        } else if (requestCode == EDIT_GROUP && resultCode == EditGroupActivity.GROUP_MODIFIED) {
            updateGroups()
        } else if (requestCode == EDIT_GROUP && resultCode == EditGroupActivity.GROUP_DELETED) {
            groups.remove(group.id)
            updateGroups()
            openPersonal()
        } else if (requestCode == EDIT_GROUP && resultCode == EditGroupActivity.GROUP_LEFT) {
            groups.remove(group.id)
            updateGroups()
            openPersonal()
        }
    }

    /**
     * Update the activity view
     */
    private fun updateView() {
        // Open the right content and highlight the corresponding button
        openPersonal()

        // Fill the navigation with groups
        val groupList = ArrayList<Group>(
            listOf(
                Group(id = User.email, name = "Group"),
                Group(id = "213", name = "Another")
            )
        )
        for (group in groupList) addGroup(group)

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
     * Add a given group to the view
     *
     * @group The corresponding group
     */
    private fun addGroup(group: Group) {
        // Create the image button
        val imageButton = ImageButton(this)
        groups[group.id] = Pair(group, imageButton)
        groupView.addView(imageButton)
        updateGroups()

        // Listen for click events
        imageButton.setOnClickListener {
            openGroup(groups[group.id]!!)
        }
    }

    /**
     * Update the group view
     */
    private fun updateGroups() {
        groupView.removeAllViews()
        for (group in groups.values) {
            group.second.updateView(group.first.name, R.drawable.nav_button_highlighting, image = group.first.image)
            groupView.addView(group.second)
        }
    }

    /**
     * Open a group
     *
     * @param groupPair The corresponding Group/ImageButton pair
     */
    private fun openGroup(groupPair: Pair<Group, ImageButton>) {
        shareButton.visibility = View.VISIBLE
        editButton.visibility = View.VISIBLE
        personalButton.showHighlighting(false)
        for (group in groups.values) group.second.showHighlighting(false)
        groupPair.second.showHighlighting(true)
        group = groupPair.first
        showFragment(GroupFragment(groupPair.first))
    }

    /**
     * Open the personal view
     */
    private fun openPersonal() {
        shareButton.visibility = View.GONE
        editButton.visibility = View.GONE
        personalButton.showHighlighting(true)
        for (group in groups.values) group.second.showHighlighting(false)
        showFragment(HomeFragment())
    }

    /**
     * Open the personal view
     */
    fun openPersonal(v: View) {
        openPersonal()
    }

    /**
     * Add a group
     */
    fun addGroup(v: View) {
        newGroup = Group(User.email)
        EditGroupActivity.prepare(true, newGroup)
        val intent = Intent(this, EditGroupActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NO_ANIMATION
        startActivityForResult(intent, EDIT_GROUP)
    }

    /**
     * Edit the currently opened group
     */
    fun editGroup(v: View) {
        EditGroupActivity.prepare(false, group)
        val intent = Intent(this, EditGroupActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NO_ANIMATION
        startActivityForResult(intent, EDIT_GROUP)
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