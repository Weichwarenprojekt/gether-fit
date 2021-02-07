package de.weichwarenprojekt.getherfit.settings

import android.app.Activity
import android.content.Context
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

/**
 * The settings preferences
 */
object Settings {

    /**
     * The key for the settings preferences
     */
    const val PREFERENCES = "Settings"

    /**
     * True if the settings were already loaded
     */
    private var loaded = false

    /**
     * True if the KLVAD theme is active
     */
    var theme = Value("theme", false)

    /**
     * True if the bottom navigation should be shown
     */
    var showBottomNav = Value("show_bottom_nav", true)

    /**
     * True if the personal overview should also reflect group data
     */
    var personalOverview = Value("personal_overview", true)

    /**
     * Load the settings
     */
    fun load(activity: Activity) {
        if (loaded) return
        theme.load(activity)
        showBottomNav.load(activity)
        loaded = true
    }

    /**
     * Value pair
     */
    class Value<T>(var key: String, var value: T) {

        /**
         * The gson loader
         */
        private var gson = Gson()

        /**
         * Update the value
         */
        fun update(newValue: T, activity: Activity) {
            value = newValue
            val editor = activity.getSharedPreferences(PREFERENCES, Context.MODE_PRIVATE).edit()
            val list = gson.toJson(newValue)
            editor.putString(key, list)
            editor.apply()
        }

        /**
         * Load the value
         */
        fun load(activity: Activity) {
            val savedValue =
                activity.getSharedPreferences(PREFERENCES, Context.MODE_PRIVATE).getString(key, "")
            if (savedValue != "") {
                val type = object : TypeToken<T>() {}.type
                value = gson.fromJson<T>(savedValue, type)
            }
        }
    }
}