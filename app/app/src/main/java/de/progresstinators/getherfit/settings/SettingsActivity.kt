package de.progresstinators.getherfit.settings

import android.os.Bundle
import android.view.View
import androidx.appcompat.widget.SwitchCompat
import de.progresstinators.getherfit.R
import de.progresstinators.getherfit.shared.BaseActivity
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
        setContentView(R.layout.settings_activity)
        normalThemeButton = findViewById(R.id.button_normal_theme)
        klvadThemeButton = findViewById(R.id.button_klvad_theme)
        updateView()
    }

    /**
     * Update the activity view
     */
    private fun updateView() {
        // Set the highlighting for the active theme
        normalThemeButton.showHighlighting(!settings.theme.value)
        klvadThemeButton.showHighlighting(settings.theme.value)

        // Set the switch for the bottom bar nav
        val bottomBar = findViewById<SwitchCompat>(R.id.switch_bottom_bar)
        bottomBar.isChecked = settings.showBottomNav.value
        bottomBar.setOnCheckedChangeListener { _, isChecked ->
            settings.showBottomNav.update(isChecked, this)
            result = CHANGED
        }

        // Set the switch for the bottom bar nav
        val personalOverview = findViewById<SwitchCompat>(R.id.switch_personal_overview)
        personalOverview.isChecked = settings.personalOverview.value
        personalOverview.setOnCheckedChangeListener { _, isChecked ->
            settings.personalOverview.update(isChecked, this)
            result = CHANGED
        }
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
        if (settings.theme.value == theme) return
        settings.theme.update(theme, this)
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