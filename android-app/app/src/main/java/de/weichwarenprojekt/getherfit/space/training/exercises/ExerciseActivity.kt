package de.weichwarenprojekt.getherfit.space.training.exercises

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.LinearLayout
import androidx.core.widget.doOnTextChanged
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import com.google.android.material.textfield.TextInputEditText
import de.weichwarenprojekt.getherfit.R
import de.weichwarenprojekt.getherfit.data.*
import de.weichwarenprojekt.getherfit.shared.BaseActivity
import de.weichwarenprojekt.getherfit.shared.Utility
import java.util.*
import kotlin.collections.ArrayList


class ExerciseActivity : BaseActivity() {
    companion object {
        /**
         * The request code for the editing activity
         */
        const val EDIT_EXERCISE = 1

        /**
         * The action that shall be executed on item click
         */
        var itemAction: (exercise: Exercise) -> Unit = {}

        /**
         * Set an action that is executed on item select
         *
         * @param itemAction The action that shall be executed on item click
         */
        fun onItemSelected(itemAction: (exercise: Exercise) -> Unit) {
            ExerciseActivity.itemAction = itemAction
        }
    }

    /**
     * The sorting possibilities
     */
    enum class Sort {
        ALPHABETIC,
        RECENT,
        LEAST_USED,
        RANDOM
    }

    /**
     * The selected category
     */
    private var selectedCategory: Category? = null

    /**
     * The selected way of sorting the list
     */
    private var selectedSorting: Sort = Sort.ALPHABETIC

    /**
     * The name filter
     */
    private var nameFilter: String = ""

    /**
     * The category chips
     */
    private var categoryChips = ArrayList<Chip>()

    /**
     * The list adapter
     */
    private lateinit var adapter: ExerciseAdapter

    /**
     * Initialize the activity
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_exercise)
        initView()
    }

    /**
     * Check if the list view has to be updated
     */
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == EDIT_EXERCISE && resultCode == EditExerciseActivity.EXERCISE_EDITED)
            updateList()
    }

    /**
     * Initialize the activity view
     */
    private fun initView() {
        // Fill in the categories
        val categoryAll = findViewById<Chip>(R.id.category_all)
        categoryChips.add(categoryAll)
        val categories = findViewById<LinearLayout>(R.id.categories_view)
        for (category in DataService.categoryBox.query().order(Category_.name).build().find()) {
            // Add the view
            val chip = Chip(this)
            val params =
                ChipGroup.LayoutParams(
                    ChipGroup.LayoutParams.WRAP_CONTENT,
                    ChipGroup.LayoutParams.WRAP_CONTENT
                )
            params.setMargins(resources.getDimension(R.dimen.chip_spacing).toInt(), 0, 0, 0)
            chip.layoutParams = params
            chip.text = category.name
            Utility.setChipStyle(this, chip, R.style.unselected_chip)
            categories.addView(chip)
            categoryChips.add(chip)

            // Listen for click events
            chip.setOnClickListener {
                for (categoryChip in categoryChips) Utility.setChipStyle(
                    this,
                    categoryChip,
                    R.style.unselected_chip
                )
                Utility.setChipStyle(this, chip, R.style.selected_chip)
                selectedCategory = category
                updateList()
            }
        }

        // Also listen for clicks on the category all button
        findViewById<View>(R.id.category_all).setOnClickListener {
            for (categoryChip in categoryChips) Utility.setChipStyle(
                this,
                categoryChip,
                R.style.unselected_chip
            )
            Utility.setChipStyle(this, categoryAll, R.style.selected_chip)
            selectedCategory = null
            updateList()
        }

        // Listen for search changes
        findViewById<TextInputEditText>(R.id.filter_input).doOnTextChanged { text, _, _, _ ->
            nameFilter = text.toString()
            updateList()
        }

        // Setup the list
        val list = findViewById<RecyclerView>(R.id.exercises)
        adapter = ExerciseAdapter(
            this,
            DataService.exerciseBox.query().order(Exercise_.name).build().find(),
            itemAction
        )
        list.adapter = adapter
    }

    /**
     * Update the list view
     */
    private fun updateList() {
        val query = DataService.exerciseBox.query()

        // Filter by name
        if (nameFilter.isNotEmpty()) {
            query.contains(Exercise_.name, nameFilter)
        }

        // Filter by category
        if (selectedCategory != null) {
            query.link(Exercise_.categories).equal(Category_.name, selectedCategory!!.name)
        }

        // Apply the other filters
        adapter.exercises = query.order(Exercise_.name).build().find()
        if (selectedSorting == Sort.RANDOM) adapter.exercises = adapter.exercises.shuffled(Random())
        adapter.notifyDataSetChanged()

        // Check if there was a result
        if (adapter.exercises.isEmpty()) {
            findViewById<View>(R.id.exercises).visibility = View.GONE
            findViewById<View>(R.id.no_result).visibility = View.VISIBLE
        } else {
            findViewById<View>(R.id.exercises).visibility = View.VISIBLE
            findViewById<View>(R.id.no_result).visibility = View.GONE
        }
    }

    /**
     * Select a random exercise
     */
    fun selectRandom(v: View) {
        v.isEnabled = false
        if (adapter.exercises.isNotEmpty()) itemAction(adapter.exercises[Random().nextInt(adapter.exercises.size)])
        v.isEnabled = true
    }

    /**
     * Open the search bar
     */
    fun openSearch(v: View) {
        v.isEnabled = false
        findViewById<View>(R.id.title).visibility = View.INVISIBLE
        findViewById<View>(R.id.search_button).visibility = View.GONE
        findViewById<View>(R.id.close_button).visibility = View.GONE
        findViewById<View>(R.id.back_button).visibility = View.VISIBLE
        val editText = findViewById<TextInputEditText>(R.id.filter_input)
        editText.visibility = View.VISIBLE
        if (editText.requestFocus()) {
            val imm: InputMethodManager =
                getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.showSoftInput(editText, InputMethodManager.SHOW_IMPLICIT)
        }
        v.isEnabled = true
    }

    /**
     * Close the search bar
     */
    fun closeSearch(v: View) {
        v.isEnabled = false
        findViewById<View>(R.id.title).visibility = View.VISIBLE
        findViewById<View>(R.id.search_button).visibility = View.VISIBLE
        findViewById<View>(R.id.close_button).visibility = View.VISIBLE
        findViewById<View>(R.id.back_button).visibility = View.GONE
        val editText = findViewById<TextInputEditText>(R.id.filter_input)
        editText.visibility = View.GONE
        editText.setText("")
        val imm = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(editText.windowToken, 0)
        nameFilter = ""
        updateList()
        v.isEnabled = true
    }

    /**
     * Sort the list alphabetic
     */
    fun sortAlphabetic(v: View) {
        v.isEnabled = false
        updateSorting(Sort.ALPHABETIC)
        v.isEnabled = true
    }

    /**
     * Sort the list with recent used exercises first
     */
    fun sortRecent(v: View) {
        v.isEnabled = false
        updateSorting(Sort.RECENT)
        v.isEnabled = true
    }

    /**
     * Sort the list with least used exercises first
     */
    fun sortLeastUsed(v: View) {
        v.isEnabled = false
        updateSorting(Sort.LEAST_USED)
        v.isEnabled = true
    }

    /**
     * Sort the list randomly
     */
    fun sortRandom(v: View) {
        v.isEnabled = false
        updateSorting(Sort.RANDOM)
        v.isEnabled = true
    }

    /**
     * Update the sorting
     *
     * @param sorting The new sorting value
     */
    private fun updateSorting(sorting: Sort) {
        if (selectedSorting == sorting) return
        selectedSorting = sorting
        updateSorting(findViewById(R.id.chip_alphabetical), selectedSorting == Sort.ALPHABETIC)
        updateSorting(findViewById(R.id.chip_recent), selectedSorting == Sort.RECENT)
        updateSorting(findViewById(R.id.chip_least_used), selectedSorting == Sort.LEAST_USED)
        updateSorting(findViewById(R.id.chip_random), selectedSorting == Sort.RANDOM)
        updateList()
    }

    /**
     * Update the visual of a given sorting chip
     *
     * @param chip The chip to be updated
     * @param selected The condition deciding whether the chip is selected
     */
    private fun updateSorting(chip: Chip, selected: Boolean) {
        if (selected) Utility.setChipStyle(this, chip, R.style.selected_chip)
        else Utility.setChipStyle(this, chip, R.style.unselected_chip)
    }

    /**
     * Add an exercise
     */
    fun addExercise(v: View) {
        v.isEnabled = false
        EditExerciseActivity.prepare(true)
        val intent = Intent(this, EditExerciseActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NO_ANIMATION
        startActivityForResult(intent, EDIT_EXERCISE)
        v.isEnabled = true
    }

    /**
     * Close the activity
     */
    fun close(v: View) {
        v.isEnabled = false
        onBackPressed()
        v.isEnabled = true
    }
}