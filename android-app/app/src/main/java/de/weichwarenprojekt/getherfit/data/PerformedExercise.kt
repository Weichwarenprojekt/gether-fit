package de.weichwarenprojekt.getherfit.data

import de.weichwarenprojekt.getherfit.space.training.exercises.PerformState
import io.objectbox.annotation.Entity
import io.objectbox.annotation.Id
import io.objectbox.relation.ToMany
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.*

/**
 * This class describes the data model of an performed exercise
 *
 * @param id The id of the space
 */
@Entity
data class PerformedExercise(@Id var id: Long = 0) {

    companion object {
        /**
         * The value for the workout if it was executed as single exercise
         */
        const val SINGLE_EXERCISE = ""

        /**
         * The data formatter
         */
        val DATE_FORMATTER: DateFormat = SimpleDateFormat.getDateInstance()
    }

    /**
     * The name of the exercise
     */
    var name: String = ""

    /**
     * The corresponding categories of the performed exercise
     */
    lateinit var categories: ToMany<Category>

    /**
     * The amount of sets that were done
     */
    var sets: Int = 1

    /**
     * The total time for the exercise
     */
    var totalTime: Int = 0

    /**
     * The average pause time
     */
    var avgPause: Float = 0.0f

    /**
     * The average set time
     */
    var avgSet: Float = 0.0f

    /**
     * The targeted reps for an exercise
     */
    var reps: String = ""

    /**
     * The targeted weight for an exercise
     */
    var weight: String = ""

    /**
     * The workout the exercise belongs too
     */
    var workout: String = ""

    /**
     * The date
     */
    val date: String = DATE_FORMATTER.format(Date())

    /**
     * The timestamp
     */
    val timestamp: Long = System.currentTimeMillis()

    /**
     * Update the values with a given state report
     *
     * @param state The state of the performed exercise
     */
    fun update(state: PerformState) {
        // The exercise data
        name = state.exercise!!.name
        reps = state.exercise!!.reps
        weight = state.exercise!!.weight
        categories = state.exercise!!.categories
        workout = state.workout

        // The performance data
        sets = (state.sets + 1) / 2
        totalTime = state.totalTime
        for (i in 1 until sets) avgPause += state.setTimes[2 * i]!!.toFloat() / (sets - 1)
        for (i in 1..sets) avgSet += state.setTimes[2 * i - 1]!!.toFloat() / (sets)
    }
}