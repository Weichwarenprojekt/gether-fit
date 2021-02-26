package de.weichwarenprojekt.getherfit.space.training.exercises

import android.app.Activity
import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import de.weichwarenprojekt.getherfit.R
import de.weichwarenprojekt.getherfit.data.Exercise

/**
 * This class extends an adapter for a list of exercises
 *
 * @param activity The current application context
 * @param exercises The set of exercises
 * @param itemAction The action that shall be executed on item click
 */
class ExerciseAdapter(
    val activity: Activity,
    var exercises: List<Exercise>,
    var itemAction: (exercise: Exercise) -> Unit
) :
    RecyclerView.Adapter<ExerciseAdapter.ViewHolder>() {

    /**
     * The view holder
     */
    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val name: TextView = view.findViewById(R.id.name)
        val tags: LinearLayout = view.findViewById(R.id.tags)
        val edit: View = view.findViewById(R.id.button_edit)
        val darkBackground: View = view.findViewById(R.id.dark_background)
        val brightBackground: View = view.findViewById(R.id.bright_background)
    }

    /**
     * Create a new view holder
     */
    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            LayoutInflater.from(viewGroup.context).inflate(R.layout.exercise_item, viewGroup, false)
        )
    }

    /**
     * Update the values of a view holder
     */
    override fun onBindViewHolder(view: ViewHolder, position: Int) {
        // Update the background
        if (position % 2 == 0) {
            view.darkBackground.visibility = View.VISIBLE
            view.brightBackground.visibility = View.GONE
        } else {
            view.darkBackground.visibility = View.GONE
            view.brightBackground.visibility = View.VISIBLE
        }

        // Set the on click action
        view.itemView.setOnClickListener {
            itemAction(exercises[position])
        }

        // Update the name
        view.name.text = exercises[position].name

        // Update the tags
        view.tags.removeAllViews()
        for (category in exercises[position].categories) {
            val chip = Chip(activity)
            val params =
                ChipGroup.LayoutParams(
                    ChipGroup.LayoutParams.WRAP_CONTENT,
                    ChipGroup.LayoutParams.MATCH_PARENT
                )
            params.setMargins(
                0,
                0,
                activity.resources.getDimension(R.dimen.chip_spacing).toInt(),
                0
            )
            chip.layoutParams = params
            chip.text = category.name
            chip.setTextColor(Color.WHITE)
            chip.chipBackgroundColor = ColorStateList.valueOf(category.color)
            view.tags.addView(chip)
        }

        // Listen for edit click events
        view.edit.setOnClickListener {
            EditExerciseActivity.prepare(false, exercises[position])
            val intent = Intent(activity, EditExerciseActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NO_ANIMATION
            activity.startActivityForResult(intent, ExerciseActivity.EDIT_EXERCISE)
        }
    }

    /**
     * @return The size of the data
     */
    override fun getItemCount() = exercises.size
}