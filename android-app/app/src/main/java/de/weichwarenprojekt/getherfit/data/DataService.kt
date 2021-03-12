package de.weichwarenprojekt.getherfit.data

import android.app.Activity
import io.objectbox.Box
import io.objectbox.BoxStore
import io.objectbox.kotlin.boxFor
import java.util.*

object DataService {

    /**
     * The box store
     */
    private lateinit var boxStore: BoxStore

    /**
     * The space box
     */
    lateinit var spaceBox: Box<Space>
        private set

    /**
     * The exercise box
     */
    lateinit var exerciseBox: Box<Exercise>
        private set

    /**
     * The performed exercise box
     */
    lateinit var performedExerciseBox: Box<PerformedExercise>
        private set

    /**
     * The categories box
     */
    lateinit var categoryBox: Box<Category>
        private set

    /**
     * Initialize the data service
     */
    fun init(activity: Activity) {
        boxStore = MyObjectBox.builder().androidContext(activity.applicationContext).build()
        spaceBox = boxStore.boxFor()
        exerciseBox = boxStore.boxFor()
        performedExerciseBox = boxStore.boxFor()
        categoryBox = boxStore.boxFor()
        initDefaults()
    }

    /**
     * Remove the default values
     */
    private fun removeDefaults() {
        categoryBox.removeAll()
        exerciseBox.removeAll()
    }

    /**
     * Initialize the categories and the exercises
     */
    private fun initDefaults() {
        if (!categoryBox.isEmpty) return

        // Define the default categories
        val arms = Category("Arms", 0xFF43B480.toInt())
        val back = Category("Back", 0xFF039F88.toInt())
        val cardio = Category("Cardio", 0xFF008988.toInt())
        val chest = Category("Chest", 0xFF007380.toInt())
        val core = Category("Core", 0xFF235D6F.toInt())
        val fullBody = Category("Full Body", 0xFF0070B7.toInt())
        val legs = Category("Legs", 0xFF2F4858.toInt())
        val shoulders = Category("Shoulders", 0xFF007FAF.toInt())

        // Add them to the database
        val categories = listOf(arms, cardio, chest, core, legs, back, fullBody, shoulders)
        categoryBox.put(categories)

        // Define the exercises
        addExercise("Ab Wheel", listOf(core))
        addExercise("Arnold Press", listOf(shoulders))
        addExercise("Around The World", listOf(chest))
        addExercise("Back Extension", listOf(back))
        addExercise("Battle Ropes", listOf(cardio))
        addExercise("Bench Press", listOf(chest, arms))
        addExercise("Bent Over Row", listOf(back))
        addExercise("Box Jump", listOf(legs))
        addExercise("Boxing", listOf(cardio))
        addExercise("Burpees", listOf(fullBody))
        addExercise("Cable Crossover", listOf(chest))
        addExercise("Cable Squat Rows", listOf(back, legs))
        addExercise("Cable Twist", listOf(core))
        addExercise("Calve Press", listOf(legs))
        addExercise("Chest Press", listOf(chest))
        addExercise("Chin Up", listOf(back))
        addExercise("Climbing", listOf(cardio))
        addExercise("Crunches", listOf(core))
        addExercise("Curl", listOf(arms))
        addExercise("Cycling", listOf(cardio))
        addExercise("Deadlift", listOf(back, arms, legs))
        addExercise("Decline Bench Press", listOf(chest, arms))
        addExercise("Dips", listOf(arms, chest))
        addExercise("Face Pull", listOf(shoulders))
        addExercise("Flyes", listOf(chest))
        addExercise("Front Raise", listOf(shoulders))
        addExercise("Hammer Curl", listOf(arms))
        addExercise("Hanging Leg Raise", listOf(core, legs, arms))
        addExercise("High Knee Skips", listOf(cardio))
        addExercise("Hip Abductor", listOf(legs))
        addExercise("Hip Thrust", listOf(legs))
        addExercise("Incline Bench Press", listOf(chest, arms))
        addExercise("Incline Curl", listOf(arms))
        addExercise("Incline Row", listOf(back))
        addExercise("Inverted Row", listOf(back))
        addExercise("Jump Rope", listOf(cardio))
        addExercise("Jump Squat", listOf(legs))
        addExercise("Jumping Jack", listOf(cardio))
        addExercise("Kettlebell Swing", listOf(fullBody))
        addExercise("Knees To Elbows", listOf(core))
        addExercise("Lat Pulldown", listOf(back))
        addExercise("Lateral Chest Press", listOf(chest, arms))
        addExercise("Lateral Raise", listOf(shoulders))
        addExercise("Leg Extension", listOf(legs))
        addExercise("Leg Press", listOf(legs))
        addExercise("Leg Raise", listOf(core))
        addExercise("Lunge", listOf(legs))
        addExercise("Lying Leg Curl", listOf(legs))
        addExercise("Mountain Climber", listOf(legs))
        addExercise("Muscle Up", listOf(back, arms))
        addExercise("Overhead Press", listOf(shoulders))
        addExercise("Pistol Squat", listOf(legs))
        addExercise("Plank", listOf(core))
        addExercise("Preacher Curl", listOf(arms))
        addExercise("Pull Up", listOf(back))
        addExercise("Pullover", listOf(back, chest))
        addExercise("Push Up", listOf(chest, arms))
        addExercise("Rowing", listOf(cardio))
        addExercise("Running", listOf(cardio))
        addExercise("Russian Twist", listOf(core))
        addExercise("Shoulder Press", listOf(shoulders))
        addExercise("Shrug", listOf(shoulders))
        addExercise("Side Bend", listOf(core))
        addExercise("Side Plank", listOf(core))
        addExercise("Sit Up", listOf(core))
        addExercise("Skull Crusher", listOf(arms))
        addExercise("Squat", listOf(legs))
        addExercise("Step Up", listOf(legs))
        addExercise("Superman", listOf(core))
        addExercise("Swimming", listOf(cardio))
        addExercise("Triceps Extension", listOf(arms))
        addExercise("Triceps Pushdown", listOf(arms))
        addExercise("Upright Row", listOf(back, shoulders))
    }

    /**
     * Add an exercise
     */
    private fun addExercise(name: String, categories: List<Category>) {
        val exercise = Exercise(name)
        exercise.categories.addAll(categories)
        exerciseBox.put(exercise)
    }
}