package de.weichwarenprojekt.getherfit.space.training

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import de.weichwarenprojekt.getherfit.R
import de.weichwarenprojekt.getherfit.shared.ScrollWatcher
import de.weichwarenprojekt.getherfit.space.training.exercises.ExerciseActivity

class TrainingFragment : Fragment() {

    /**
     * Initialize the view
     */
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Get the fragment layout
        val view = inflater.inflate(R.layout.fragment_training, container, false)
        ScrollWatcher.setActiveScrollbar(view.findViewById(R.id.scroll_view))

        // Set up the click events for the buttons
        view.findViewById<View>(R.id.card_exercises).setOnClickListener {
            val intent = Intent(activity!!, ExerciseActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NO_ANIMATION
            startActivity(intent)
        }
        view.findViewById<View>(R.id.card_workouts).setOnClickListener {
            val intent = Intent(activity!!, WorkoutActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NO_ANIMATION
            startActivity(intent)
        }

        return view
    }
}