package de.weichwarenprojekt.getherfit.space.training

import android.content.Context
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
 * @param context The current application context
 * @param exercises The set of exercises
 */
class ExerciseAdapter(val context: Context, var exercises: List<Exercise>) :
    RecyclerView.Adapter<ExerciseAdapter.ViewHolder>() {

    /**
     * The view holder
     */
    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val name: TextView = view.findViewById(R.id.name)
        val tags: LinearLayout = view.findViewById(R.id.tags)
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

        // Update the name
        view.name.text = exercises[position].name

        // Update the tags
        view.tags.removeAllViews()
        for (category in exercises[position].categories) {
            val chip = Chip(context)
            val params =
                ChipGroup.LayoutParams(ChipGroup.LayoutParams.WRAP_CONTENT, ChipGroup.LayoutParams.MATCH_PARENT)
            params.setMargins(0, 0, context.resources.getDimension(R.dimen.chip_spacing).toInt(), 0)
            chip.layoutParams = params
            chip.text = category.name
            chip.setTextColor(Color.WHITE)
            chip.chipBackgroundColor = ColorStateList.valueOf(category.color)
            view.tags.addView(chip)
        }
    }

    /**
     * @return The size of the data
     */
    override fun getItemCount() = exercises.size
}