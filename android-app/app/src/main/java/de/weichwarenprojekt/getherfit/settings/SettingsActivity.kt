package de.weichwarenprojekt.getherfit.settings

import android.os.Bundle
import android.view.View
import android.widget.ImageView
import androidx.appcompat.widget.SwitchCompat
import de.weichwarenprojekt.getherfit.R
import de.weichwarenprojekt.getherfit.shared.BaseActivity
import de.weichwarenprojekt.getherfit.shared.components.ConfirmDialog
import de.weichwarenprojekt.getherfit.shared.components.DescriptionItem
import de.weichwarenprojekt.getherfit.shared.components.ImageBottomSheet
import de.weichwarenprojekt.getherfit.shared.components.ImageButton


class SettingsActivity : BaseActivity() {

    /**
     * The activity results
     */
    companion object {
        const val CHANGED = 1
        const val LOGGED_OUT = 2
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
        updateImage()
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
            setResult(CHANGED)
        }

        // Set the switch for the bottom bar nav
        val personalOverview = findViewById<SwitchCompat>(R.id.switch_personal_overview)
        personalOverview.isChecked = Settings.personalOverview.value
        personalOverview.setOnCheckedChangeListener { _, isChecked ->
            Settings.personalOverview.update(isChecked, this)
            setResult(CHANGED)
        }
    }

    /**
     * Update the user's image
     */
    private fun updateImage() {
        val logo = findViewById<ImageView>(R.id.user_image)
        if (User.image != null) logo.setImageBitmap(User.image)
        else logo.setImageResource(R.drawable.person)
    }

    /**
     * Modify the user image
     */
    fun modifyImage(v: View) {
        v.isEnabled = false
        ImageBottomSheet { result, image ->
            if (result == ImageBottomSheet.ADD_IMAGE) User.image =
                image
            else if (result == ImageBottomSheet.DELETE_IMAGE) User.image =
                null
            if (result != ImageBottomSheet.EMPTY) updateImage()
            v.isEnabled = true
        }.show(supportFragmentManager, "modify_user_image")
    }

    /**
     * Log out the user
     */
    fun logOut(v: View) {
        ConfirmDialog(getString(R.string.settings_logout), getString(R.string.settings_logout_description)) {
            User.logOut(this)
            setResult(LOGGED_OUT)
            finish()
        }.show(supportFragmentManager, "confirm_logout")
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
        setResult(CHANGED)
        reload()
    }

    /**
     * Close the activity
     */
    fun close(v: View) {
        onBackPressed()
    }
}