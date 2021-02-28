package de.weichwarenprojekt.getherfit.space.training

import android.app.Activity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import de.weichwarenprojekt.getherfit.R
import de.weichwarenprojekt.getherfit.data.DataService
import de.weichwarenprojekt.getherfit.data.PerformedExercise
import de.weichwarenprojekt.getherfit.data.PerformedExercise_
import kotlin.math.min

/**
 * This class extends an adapter for a list of performed exercises
 */
class HistoryAdapter(val activity: Activity, var dates: List<String>, var maxItems: Int = 20) :
    RecyclerView.Adapter<HistoryAdapter.ViewHolder>() {

    /**
     * The view holder for an history item
     */
    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val date: TextView = view.findViewById(R.id.text_date)
        val exercises: LinearLayout = view.findViewById(R.id.list_exercises)
    }

    /**
     * @return The right view type (only differs for the first view)
     */
    override fun getItemViewType(position: Int): Int {
        return if (position == 0) 0 else 1
    }

    /**
     * Create a new view holder
     */
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            LayoutInflater.from(parent.context).inflate(R.layout.history_item, parent, false)
        )
    }

    /**
     * Update the values of a view holder
     */
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        // Get the performed exercises for the specified date
        val date: String = dates[position]
        val exercises = DataService.performedExerciseBox.query().equal(PerformedExercise_.date, date)
            .order(PerformedExercise_.timestamp).build().find()

        // Sort the exercises by workouts
        val workouts = HashMap<String, ArrayList<PerformedExercise>>()
        for (exercise in exercises) {
            if (workouts.containsKey(exercise.workout)) workouts[exercise.workout]!!.add(exercise)
            else workouts[exercise.workout] = arrayListOf(exercise)
        }

        // Set the date
        holder.date.text = date

        // Fill in the exercises
        holder.exercises.removeAllViews()
        for (workout in workouts) {
            // Add the first view with title
            val firstView = createExerciseView(workout.value[0], holder.exercises)
            val title: TextView = firstView.findViewById(R.id.text_title)
            title.visibility = View.VISIBLE
            title.text =
                if (workout.key == PerformedExercise.SINGLE_EXERCISE) activity.getString(R.string.training_single_exercises) else workout.key
            holder.exercises.addView(firstView)

            // Add the other views
            for (i in 1 until workout.value.size)
                holder.exercises.addView(createExerciseView(workout.value[i], holder.exercises))
        }
    }

    /**
     * Create an exercise view for a performed exercise
     *
     * @param exercise The performed exercise
     */
    private fun createExerciseView(exercise: PerformedExercise, parent: LinearLayout): View {
        val view = LayoutInflater.from(activity).inflate(R.layout.history_exercise_item, parent, false)
        val name = "${exercise.sets} x ${exercise.name}"
        view.findViewById<TextView>(R.id.text_name).text = name
        val totalTime = "${exercise.totalTime / 60}m ${exercise.totalTime % 60}s"
        view.findViewById<TextView>(R.id.text_time).text = totalTime
        val reps = if (exercise.reps.isEmpty()) "-" else exercise.reps
        view.findViewById<TextView>(R.id.text_reps).text = reps
        val weight = if (exercise.weight.isEmpty()) "-" else exercise.weight
        view.findViewById<TextView>(R.id.text_weight).text = weight
        return view
    }

    /**
     * @return The size of the data
     */
    override fun getItemCount(): Int = min(dates.size, maxItems)
}