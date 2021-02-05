package de.progresstinators.getherfit.settings

import android.os.Bundle
import android.view.View
import android.widget.ImageView
import androidx.appcompat.widget.SwitchCompat
import de.progresstinators.getherfit.R
import de.progresstinators.getherfit.shared.BaseActivity
import de.progresstinators.getherfit.shared.DescriptionItem
import de.progresstinators.getherfit.shared.ImageButton


class SettingsActivity : BaseActivity() {

    /**
     * The activity results
     */
    companion object {
        const val EMPTY = 0
        const val ACTIVITY = 1
        const val CHANGED = 2
        var result = EMPTY
    }

    /**
     * The normal theme button
     */
    private lateinit var normalThemeButton: ImageButton

    /**
     * The klvad theme button
     */
    private lateinit var klvadThemeButton: ImageButton

    /**
     * Initialize the activity
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)
        normalThemeButton = findViewById(R.id.button_normal_theme)
        klvadThemeButton = findViewById(R.id.button_klvad_theme)
        updateView()
    }

    /**
     * Update the activity view
     */
    private fun updateView() {
        // Set the user data
        val logo = findViewById<ImageView>(R.id.user_image)
        if (User.image != null) logo.setImageBitmap(User.image)
        val credential = findViewById<DescriptionItem>(R.id.credentials)
        credential.setText(User.firstName + " " + User.lastName, User.email)

        // Set the highlighting for the active theme
        normalThemeButton.showHighlighting(!Settings.theme.value)
        klvadThemeButton.showHighlighting(Settings.theme.value)

        // Set the switch for the bottom bar nav
        val bottomBar = findViewById<SwitchCompat>(R.id.switch_bottom_bar)
        bottomBar.isChecked = Settings.showBottomNav.value
        bottomBar.setOnCheckedChangeListener { _, isChecked ->
            Settings.showBottomNav.update(isChecked, this)
            result = CHANGED
        }

        // Set the switch for the bottom bar nav
        val personalOverview = findViewById<SwitchCompat>(R.id.switch_personal_overview)
        personalOverview.isChecked = Settings.personalOverview.value
        personalOverview.setOnCheckedChangeListener { _, isChecked ->
            Settings.personalOverview.update(isChecked, this)
            result = CHANGED
        }
    }

    /**
     * Log out the user
     */
    fun logOut(v: View) {
        User.logOut(this)
    }

    /**
     * Show the normal theme
     */
    fun showNormalTheme(v: View) {
        setTheme(false)
    }

    /**
     * Show the KLVAD theme
     */
    fun showKLVADTheme(v: View) {
        setTheme(true)
    }

    /**
     * Change the theme
     */
    private fun setTheme(theme: Boolean) {
        // Check if theme needs to be changed
        if (Settings.theme.value == theme) return
        Settings.theme.update(theme, this)
        if (theme) setTheme(R.style.Theme_KLVAD)
        else setTheme(R.style.Theme_GetherFit)

        // Set the result to signal an appearance change
        result = CHANGED
        recreate()
    }

    /**
     * Set the result and close the activity
     */
    override fun onBackPressed() {
        window.setWindowAnimations(0)
        setResult(result)
        finish()
        result = EMPTY
    }

    /**
     * Close the activity
     */
    fun closeSettings(v: View) {
        onBackPressed()
    }
}