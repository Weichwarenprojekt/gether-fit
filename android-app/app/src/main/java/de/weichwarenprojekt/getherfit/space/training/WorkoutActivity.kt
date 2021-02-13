package de.weichwarenprojekt.getherfit.space.training

import android.os.Bundle
import android.view.View
import de.weichwarenprojekt.getherfit.R
import de.weichwarenprojekt.getherfit.shared.BaseActivity

class WorkoutActivity : BaseActivity() {

    /**
     * Initialize the activity
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_workout)
    }

    /**
     * Close the activity
     */
    fun close(v: View) {
        onBackPressed()
    }
}