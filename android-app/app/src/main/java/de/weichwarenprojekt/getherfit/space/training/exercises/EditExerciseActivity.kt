package de.weichwarenprojekt.getherfit.space.training.exercises

import android.content.res.ColorStateList
import android.os.Bundle
import android.view.View
import android.widget.TextView
import com.google.android.flexbox.FlexboxLayout
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import de.weichwarenprojekt.getherfit.R
import de.weichwarenprojekt.getherfit.data.Category
import de.weichwarenprojekt.getherfit.data.Category_
import de.weichwarenprojekt.getherfit.data.DataService
import de.weichwarenprojekt.getherfit.data.Exercise
import de.weichwarenprojekt.getherfit.shared.BaseActivity
import de.weichwarenprojekt.getherfit.shared.Utility
import de.weichwarenprojekt.getherfit.shared.components.ConfirmDialog

class EditExerciseActivity : BaseActivity() {

    companion object {
        /**
         * The result if an exercise was somehow changed (edited/added/removed)
         */
        const val EXERCISE_EDITED = 1

        /**
         * True if a new exercise shall be added (otherwise activity will be in modify mode)
         */
        private var addExercise = true

        /**
         * The currently modified exercise
         */
        private lateinit var exercise: Exercise

        /**
         * Prepare the activity (to either modify or add a space)
         *
         * @param addExercise True if a new exercise shall be added
         * @param exercise The exercise to be added/modified
         */
        fun prepare(addExercise: Boolean, exercise: Exercise = Exercise()) {
            Companion.addExercise = addExercise
            Companion.exercise = exercise
        }
    }

    /**
     * The selected categories
     */
    private val selectedCategories = ArrayList<Category>()

    /**
     * Initialize the activity
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_exercise)
        initView()
    }

    /**
     * Initialize the activity view
     */
    private fun initView() {
        // Update the layout
        if (addExercise) {
            findViewById<TextView>(R.id.title).text = getString(R.string.edit_exercise_add)
            findViewById<View>(R.id.button_delete).visibility = View.GONE
        }
        findViewById<TextInputEditText>(R.id.input_name).setText(exercise.name)
        findViewById<TextInputEditText>(R.id.input_reps).setText(exercise.reps)
        findViewById<TextInputEditText>(R.id.input_weight).setText(exercise.weight)
        findViewById<TextInputEditText>(R.id.input_description).setText(exercise.description)

        // Fill in the categories
        val categories = findViewById<FlexboxLayout>(R.id.categories)
        for (category in DataService.categoryBox.query().order(Category_.name).build().find()) {
            // Add the view
            val chip = Chip(this)
            val params =
                ChipGroup.LayoutParams(
                    ChipGroup.LayoutParams.WRAP_CONTENT,
                    ChipGroup.LayoutParams.WRAP_CONTENT
                )
            params.setMargins(
                0,
                0,
                resources.getDimension(R.dimen.chip_spacing).toInt(),
                0
            )
            chip.layoutParams = params
            chip.text = category.name
            Utility.setChipStyle(this, chip, R.style.unselected_chip)
            categories.addView(chip)

            // Check if the exercise includes this category
            for (exerciseCategory in exercise.categories) {
                if (exerciseCategory.name == category.name) {
                    chip.chipBackgroundColor = ColorStateList.valueOf(category.color)
                    selectedCategories.add(exerciseCategory)
                }
            }

            // Listen for click events
            chip.setOnClickListener {
                if (selectedCategories.contains(category)) {
                    selectedCategories.remove(category)
                    Utility.setChipStyle(this, chip, R.style.unselected_chip)
                } else {
                    selectedCategories.add(category)
                    chip.chipBackgroundColor = ColorStateList.valueOf(category.color)
                }
            }
        }
    }

    /**
     * Add/Edit the exercise
     */
    fun apply(v: View) {
        // Check if a name is given
        val newName: String = findViewById<TextInputEditText>(R.id.input_name).text.toString()
        if (newName.isEmpty()) {
            val nameInputLayout: TextInputLayout = findViewById(R.id.name_input_layout)
            nameInputLayout.isErrorEnabled = true
            nameInputLayout.error = getString(R.string.edit_space_name_error)
            return
        }

        // Check if categories are selected
        if (selectedCategories.isEmpty()) {
            findViewById<View>(R.id.categories_error).visibility = View.VISIBLE
            return
        }

        // Update the data
        exercise.name = newName
        exercise.categories.clear()
        exercise.categories.addAll(selectedCategories)
        exercise.reps = findViewById<TextInputEditText>(R.id.input_reps).text.toString()
        exercise.weight = findViewById<TextInputEditText>(R.id.input_weight).text.toString()
        exercise.description =
            findViewById<TextInputEditText>(R.id.input_description).text.toString()
        DataService.exerciseBox.put(exercise)

        // Set the result and return
        setResult(EXERCISE_EDITED)
        onBackPressed()
    }

    /**
     * Delete the exercise
     */
    fun deleteExercise(v: View) {
        ConfirmDialog(
            getString(R.string.edit_exercise_delete),
            getString(R.string.edit_exercise_delete_description)
        ) {
            setResult(EXERCISE_EDITED)
            DataService.exerciseBox.remove(exercise)
            onBackPressed()
        }.show(supportFragmentManager, "confirm_delete")
    }

    /**
     * Close the activity
     */
    fun close(v: View) {
        onBackPressed()
    }
}