package de.progresstinators.getherfit

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentTransaction
import de.progresstinators.getherfit.data.Group
import de.progresstinators.getherfit.group.EditGroupActivity
import de.progresstinators.getherfit.group.GroupFragment
import de.progresstinators.getherfit.personal.PersonalFragment
import de.progresstinators.getherfit.settings.SettingsActivity
import de.progresstinators.getherfit.settings.User
import de.progresstinators.getherfit.shared.BaseActivity
import de.progresstinators.getherfit.shared.components.ImageButton


class MainActivity : BaseActivity() {

    /**
     * The possible activity request codes
     */
    companion object {
        const val SETTINGS = 1
        const val ADD_GROUP = 2
    }

    /**
     * The button for the personal view
     */
    private lateinit var personalButton: ImageButton

    /**
     * The button for the dummy group
     */
    private lateinit var groupButton: ImageButton

    /**
     * Initialize the activity
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        personalButton = findViewById(R.id.personal_space)
        personalButton.showHighlighting(true)
        groupButton = findViewById(R.id.dummy_group)
        showFragment(PersonalFragment())
    }

    /**
     * Check if the settings changed something about the appearance
     */
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        println(resultCode)
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == SETTINGS) {
            if (resultCode == SettingsActivity.CHANGED) {
                reload()
            } else if (resultCode == SettingsActivity.LOGGED_OUT) {
                startActivity(Intent(this, LoginActivity::class.java))
                finish()
            }
        } else if (requestCode == ADD_GROUP && resultCode == EditGroupActivity.GROUP_ADDED) {
            println("New Group")
        }
    }

    /**
     * Open the personal view
     */
    fun openPersonal(v: View) {
        personalButton.showHighlighting(true)
        groupButton.showHighlighting(false)
        showFragment(PersonalFragment())
    }

    /**
     * Open a group
     */
    fun openGroup(v: View) {
        personalButton.showHighlighting(false)
        groupButton.showHighlighting(true)
        showFragment(GroupFragment())
    }

    /**
     * Add a group
     */
    fun addGroup(v: View) {
        EditGroupActivity.prepare(true, Group(User.email))
        val intent = Intent(this, EditGroupActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NO_ANIMATION
        startActivityForResult(intent, ADD_GROUP)
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
        val ft: FragmentTransaction = supportFragmentManager.beginTransaction()
        ft.replace(R.id.content, fragment)
        ft.commit()
    }
}