package de.progresstinators.getherfit

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentTransaction
import de.progresstinators.getherfit.group.GroupFragment
import de.progresstinators.getherfit.personal.PersonalFragment


class MainActivity : AppCompatActivity() {

    /**
     * The button for the personal view
     */
    private lateinit var personalButton: NavButton

    /**
     * The button for the dummy group
     */
    private lateinit var groupButton: NavButton

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