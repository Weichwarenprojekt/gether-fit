package de.weichwarenprojekt.getherfit.space.training

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import de.weichwarenprojekt.getherfit.R
import de.weichwarenprojekt.getherfit.data.DataService
import de.weichwarenprojekt.getherfit.data.PerformedExercise
import de.weichwarenprojekt.getherfit.data.PerformedExercise_
import de.weichwarenprojekt.getherfit.shared.ScrollWatcher
import de.weichwarenprojekt.getherfit.space.training.exercises.ExerciseActivity
import de.weichwarenprojekt.getherfit.space.training.exercises.PerformExerciseActivity
import java.util.*

class TrainingFragment : Fragment() {

    /**
     * The request codes
     */
    companion object {
        const val EXERCISES = 1
    }

    /**
     * The list adapter
     */
    private lateinit var adapter: HistoryAdapter

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

        // Set up the click events for the cards
        view.findViewById<View>(R.id.card_exercises).setOnClickListener {
            ExerciseActivity.onItemSelected { exercise ->
                PerformExerciseActivity.prepare(exercise, activity!!)
                val intent = Intent(activity, PerformExerciseActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_NO_ANIMATION
                activity!!.startActivityForResult(intent, EXERCISES)
            }
            val intent = Intent(activity, ExerciseActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NO_ANIMATION
            activity!!.startActivity(intent)
        }
        view.findViewById<View>(R.id.card_workouts).setOnClickListener {
            val intent = Intent(activity, WorkoutActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NO_ANIMATION
            activity!!.startActivity(intent)
        }

        // Set up the recycler view
        val list: RecyclerView = view.findViewById(R.id.list_history)
        adapter = HistoryAdapter(activity!!, emptyList())
        list.adapter = adapter

        // Set up click event for the show more button
        view.findViewById<View>(R.id.button_more).setOnClickListener {
            adapter.maxItems += 10
            updateView()
        }

        return view
    }

    /**
     * Update the view as soon as the layout's initialized
     */
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        updateView()
    }

    /**
     * Update the view
     */
    private fun updateView() {
        // Get the latest data
        adapter.dates = getHistoryDates()
        adapter.notifyDataSetChanged()

        // Show or hide the information that the history is empty
        view!!.findViewById<View>(R.id.text_no_exercises).visibility =
            if (adapter.dates.isEmpty()) View.VISIBLE else View.GONE

        // Show or hide the show more button
        view!!.findViewById<View>(R.id.button_more).visibility =
            if (adapter.dates.size <= adapter.maxItems) View.GONE else View.VISIBLE
    }

    /**
     * Query all distinct dates and sort them (latest first)
     */
    @Suppress("CanBeVal", "JavaCollectionsStaticMethodOnImmutableList")
    private fun getHistoryDates(): List<String> {
        // Get the dates
        val query = DataService.performedExerciseBox.query()
        var dates: List<String> = query.build().property(PerformedExercise_.date).distinct().findStrings().toList()

        // Sort the dates
        val dateFormatter = PerformedExercise.DATE_FORMATTER
        Collections.sort(dates) { s1, s2 -> dateFormatter.parse(s2)!!.compareTo(dateFormatter.parse(s1)) }
        return dates
    }

    /**
     * Check if an exercise or workout was performed
     */
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == EXERCISES && resultCode == PerformExerciseActivity.EXERCISE_PERFORMED) {
            updateView()
        }
    }
}