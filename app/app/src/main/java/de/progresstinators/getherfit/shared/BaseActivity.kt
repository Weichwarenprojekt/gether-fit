package de.progresstinators.getherfit.shared

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import de.progresstinators.getherfit.R
import de.progresstinators.getherfit.settings.Settings

abstract class BaseActivity : AppCompatActivity() {

    /**
     * Initialize the activity
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        Settings.load(this)
        if (Settings.theme.value) setTheme(R.style.Theme_KLVAD)
        else setTheme(R.style.Theme_GetherFit)
        super.onCreate(savedInstanceState)
    }
}