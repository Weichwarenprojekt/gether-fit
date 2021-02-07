package de.weichwarenprojekt.getherfit.shared

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import de.weichwarenprojekt.getherfit.R
import de.weichwarenprojekt.getherfit.settings.Settings

abstract class BaseActivity : AppCompatActivity() {

    /**
     * The activity result if nothing happened
     */
    companion object {
        const val EMPTY = 0
    }

    /**
     * Initialize the activity
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        Settings.load(this)
        if (Settings.theme.value) setTheme(R.style.Theme_KLVAD)
        else setTheme(R.style.Theme_GetherFit)
        super.onCreate(savedInstanceState)
    }

    /**
     * Set the result and close the activity
     */
    override fun onBackPressed() {
        window.setWindowAnimations(0)
        finish()
    }

    fun reload() {
        finish()
        startActivity(intent)
        overridePendingTransition(0, 0)
    }
}