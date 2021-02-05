package de.progresstinators.getherfit.shared

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import de.progresstinators.getherfit.R
import de.progresstinators.getherfit.settings.Settings

abstract class BaseActivity : AppCompatActivity() {

    /**
     * The settings
     */
    protected var settings = Settings.instance

    /**
     * Initialize the activity
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        settings.load(this)
        if (settings.theme.value) setTheme(R.style.Theme_KLVAD)
        else setTheme(R.style.Theme_GetherFit)
        super.onCreate(savedInstanceState)
    }
}