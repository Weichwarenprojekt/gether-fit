package de.weichwarenprojekt.getherfit.settings

import android.app.Activity
import android.content.Context
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import de.weichwarenprojekt.getherfit.data.DataService
import de.weichwarenprojekt.getherfit.space.training.exercises.PerformState

/**
 * The settings preferences
 */
object Settings {

    /**
     * The key for the settings preferences
     */
    const val PREFERENCES = "Settings"

    /**
     * The selected index if the home view was opened last
     */
    const val HOME = -1

    /**
     * The period of the overview data
     */
    var period = Value("period", 0)

    /**
     * The state of the last performed exercise
     */
    var performState = Value<PerformState?>("perform_state", null)

    /**
     * The index of the last opened space
     */
    var lastOpenedSpace = Value("last_opened_space", HOME)

    /**
     * The index of the last opened tab
     */
    var lastOpenedTab = Value("last_opened_tab", 0)

    /**
     * True if the KLVAD theme is active
     */
    var theme = Value("theme", false)

    /**
     * True if the bottom navigation should be shown
     */
    var showBottomNav = Value("show_bottom_nav", true)

    /**
     * True if the personal overview should also reflect space data
     */
    var personalOverview = Value("personal_overview", true)

    /**
     * True if the settings were already loaded
     */
    private var loaded = false

    /**
     * Load the settings
     */
    fun load(activity: Activity) {
        if (loaded) return

        // Load the settings
        lastOpenedSpace.load(activity)
        lastOpenedTab.load(activity)
        theme.load(activity)
        showBottomNav.load(activity)
        performState.load(activity)
        period.load(activity)
        loaded = true

        // Init the db
        DataService.init(activity)
    }

    /**
     * Value pair
     *
     * @param key The key of the preference
     * @param value The default value
     */
    class Value<T>(private val key: String, value: T) {

        /**
         * The actual value
         */
        var value: T = value
            private set

        /**
         * The gson loader
         */
        private var gson = Gson()

        /**
         * Save the current value
         */
        fun save(activity: Activity) {
            val editor = activity.getSharedPreferences(PREFERENCES, Context.MODE_PRIVATE).edit()
            val list = gson.toJson(value)
            editor.putString(key, list)
            editor.apply()
        }

        /**
         * Update the value
         */
        fun update(newValue: T, activity: Activity) {
            value = newValue
            save(activity)
        }

        /**
         * Load the value
         */
        fun load(activity: Activity) {
            val savedValue =
                activity.getSharedPreferences(PREFERENCES, Context.MODE_PRIVATE).getString(key, "")
            if (savedValue != "") {
                val type = object : TypeToken<T>() {}.type
                value = gson.fromJson(savedValue, type)
            }
        }
    }
}