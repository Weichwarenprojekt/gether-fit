package de.weichwarenprojekt.getherfit.space.training.exercises

import android.app.Activity
import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build
import android.os.Bundle
import android.text.Html
import android.text.format.DateUtils
import android.view.View
import android.view.WindowManager
import android.widget.ImageView
import android.widget.TextView
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.textfield.TextInputEditText
import de.weichwarenprojekt.getherfit.R
import de.weichwarenprojekt.getherfit.data.DataService
import de.weichwarenprojekt.getherfit.data.Exercise
import de.weichwarenprojekt.getherfit.data.PerformedExercise
import de.weichwarenprojekt.getherfit.data.PerformedExercise_
import de.weichwarenprojekt.getherfit.settings.Settings
import de.weichwarenprojekt.getherfit.shared.BaseActivity
import de.weichwarenprojekt.getherfit.shared.components.ConfirmDialog
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashMap
import kotlin.concurrent.schedule


/**
 * The data class holding the state of the perform exercise activity
 *
 * @param exercise The actual exercise
 * @param sets The amount of sets
 * @param totalTime The total exercise time
 * @param finished True if the exercise is finished
 * @param setTimes The times for each set (also includes pauses)
 */
data class PerformState(
    var exercise: Exercise?,
    var workout: String,
    var sets: Int = 0,
    var totalTime: Int = 0,
    var finished: Boolean = false,
    var setTimes: HashMap<Int, Int> = HashMap()
) {
    /**
     * True if the exercise at start
     */
    val start: Boolean
        get() {
            return sets <= 0
        }

    /**
     * True if the exercise is currently in pause
     */
    val pause: Boolean
        get() {
            return sets % 2 == 0
        }
}

class PerformExerciseActivity : BaseActivity() {

    companion object {
        /**
         * The result if an exercise was performed
         */
        const val EXERCISE_PERFORMED = 1

        /**
         * The actual id of the notification
         */
        private const val NOTIFICATION_ID = "perform_exercise"

        /**
         * The id of the notification
         */
        private const val NOTIFY_ID = 1

        /**
         * True if the activity is paused
         */
        private var pause = false

        /**
         * The state of the performed exercise
         */
        private val state: PerformState
            get() {
                return Settings.performState.value!!
            }

        /**
         * Prepare the activity
         *
         * @param exercise The exercise to be performed
         */
        fun prepare(exercise: Exercise, activity: Activity, workout: String = PerformedExercise.SINGLE_EXERCISE) {
            Settings.performState.update(PerformState(exercise, workout), activity)
        }
    }

    /**
     * The notification builder
     */
    private val notification: NotificationCompat.Builder? = NotificationCompat.Builder(this, NOTIFICATION_ID)
        .setSmallIcon(R.drawable.ic_baseline_supervisor_account_24)
        .setOngoing(true)
        .setPriority(NotificationCompat.PRIORITY_DEFAULT)
        .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)

    /**
     * The text view that displays the current time
     */
    private lateinit var currentTimeView: TextView

    /**
     * The text view that displays the total time
     */
    private lateinit var totalTimeView: TextView

    /**
     * The timer
     */
    private var timer: Timer? = null

    /**
     * Initialize the activity
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        setContentView(R.layout.activity_perform_exercise)
        currentTimeView = findViewById(R.id.text_current_time)
        totalTimeView = findViewById(R.id.text_total_time)

        // Fill the text fields
        findViewById<TextInputEditText>(R.id.input_reps).setText(state.exercise!!.reps)
        findViewById<TextInputEditText>(R.id.input_weight).setText(state.exercise!!.weight)
        findViewById<TextInputEditText>(R.id.input_description).setText(state.exercise!!.description)

        // Update the UI
        updateView()
        if (state.sets > 0) startTimer()
    }

    /**
     * Hide the notification
     */
    override fun onResume() {
        super.onResume()
        pause = false
        with(NotificationManagerCompat.from(this)) {
            cancel(NOTIFY_ID)
        }
    }

    /**
     * Show the notification
     */
    override fun onPause() {
        super.onPause()
        pause = true
        synchronized(state) {
            if (!state.finished && state.sets > 0) showNotification()
        }
    }


    /**
     * Stop the timer if the activity is destroyed
     */
    override fun onDestroy() {
        super.onDestroy()
        timer?.cancel()
        Settings.performState.save(this)
        with(NotificationManagerCompat.from(this)) {
            cancel(NOTIFY_ID)
        }
    }

    /**
     * Ask the user if he is sure that he wants to quit
     */
    override fun onBackPressed() {
        synchronized(state) {
            ConfirmDialog(
                getString(R.string.perform_exercise_quit),
                getString(R.string.perform_exercise_quit_description)
            ) {
                state.finished = true
                super.onBackPressed()
            }.show(supportFragmentManager, "quit_exercise")
        }
    }

    /**
     * Show the notification with updated content
     */
    private fun showNotification() {
        if (!pause) return

        // Update the notification
        val minutes = state.setTimes[state.sets]!! / 60
        val seconds = state.setTimes[state.sets]!! % 60
        notification!!
            .setContentTitle(getTitleText())
            .setContentText("$minutes m $seconds s")

        // Show the notification
        with(NotificationManagerCompat.from(this)) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val channel = NotificationChannel(
                    NOTIFICATION_ID,
                    "GetherFit",
                    NotificationManager.IMPORTANCE_DEFAULT
                )
                createNotificationChannel(channel)
                notification.setChannelId(NOTIFICATION_ID)
            }
            notify(NOTIFY_ID, notification.build())
        }
    }

    /**
     * Update the activity view
     */
    private fun updateView() {
        // Update the header
        findViewById<TextView>(R.id.exercise_title).text = getTitleText()
        val logo = findViewById<ImageView>(R.id.logo)
        logo.visibility = if (state.start) View.GONE else View.VISIBLE
        logo.setImageResource(
            if (state.start || state.pause) R.drawable.ic_baseline_pause_circle_24
            else R.drawable.ic_baseline_play_circle_24
        )
        findViewById<View>(R.id.text_current_time).visibility = if (state.start) View.GONE else View.VISIBLE

        // Update the buttons
        findViewById<TextView>(R.id.text_button_next).text =
            if (state.start) getString(R.string.perform_exercise_start) else getString(R.string.perform_exercise_next)
        findViewById<View>(R.id.button_finish).visibility = if (state.start) View.GONE else View.VISIBLE

        // Update the timer text
        if (!state.start) setText(currentTimeView, state.setTimes[state.sets]!!)
        setText(totalTimeView, state.totalTime)

        // Get the matching performed exercises and update the time texts
        val exercises = DataService.performedExerciseBox.query()
            .equal(PerformedExercise_.name, state.exercise!!.name)
            .order(PerformedExercise_.timestamp).build().find()
        val (avgPause, avgSet) = calculateAvgTimes(exercises)
        findViewById<TextView>(R.id.text_last_performed).text =
            if (exercises.isEmpty()) "-"
            else {
                val flags = DateUtils.FORMAT_NO_YEAR or DateUtils.FORMAT_NUMERIC_DATE
                DateUtils.formatDateTime(this, exercises.last().timestamp, flags)
            }
        setText(findViewById(R.id.text_avg_pause), avgPause)
        setText(findViewById(R.id.text_avg_set), avgSet)

        // Update the change log list
        val changeLog = findViewById<RecyclerView>(R.id.change_log)
        val logs = createLogs(exercises)
        changeLog.adapter = ChangeLogAdapter(this, logs)
    }

    /**
     * Calculate the avg pause and set times for the exercise
     *
     * @param exercises The performed exercises matching the current exercise
     */
    private fun calculateAvgTimes(exercises: List<PerformedExercise>): Pair<Int, Int> {
        var avgPause = 0f
        var avgSet = 0f
        for (i in 0..5) {
            if (i >= exercises.size - 1 || i == 4) {
                avgSet /= i
                avgPause /= i
                break
            }
            avgPause += exercises[exercises.lastIndex - i].avgPause
            avgSet += exercises[exercises.lastIndex - i].avgSet
        }
        return Pair(avgPause.toInt(), avgSet.toInt())
    }

    /**
     * Create the logs by analyzing the performed exercises
     *
     * @param exercises The performed exercises matching the current exercise
     */
    private fun createLogs(exercises: List<PerformedExercise>): ArrayList<ChangeLogAdapter.Log> {
        val logs = ArrayList<ChangeLogAdapter.Log>()
        var lastExercise: PerformedExercise? = null
        for (exercise in exercises) {
            if (lastExercise != null && exercise.weight != lastExercise.weight) {
                logs.add(
                    0,
                    ChangeLogAdapter.Log(
                        ChangeLogAdapter.LogType.WEIGHT,
                        getString(R.string.perform_exercise_change_weight),
                        getFormattedString(
                            R.string.perform_exercise_change_weight_description,
                            lastExercise.weight,
                            exercise.weight
                        ),
                        exercise.date
                    )
                )
            }
            if (lastExercise != null && exercise.reps != lastExercise.reps) {
                logs.add(
                    0,
                    ChangeLogAdapter.Log(
                        ChangeLogAdapter.LogType.REPS,
                        getString(R.string.perform_exercise_change_reps),
                        getFormattedString(
                            R.string.perform_exercise_change_reps_description,
                            lastExercise.reps,
                            exercise.reps
                        ),
                        exercise.date
                    )
                )
            }
            lastExercise = exercise
        }
        if (logs.isEmpty() && exercises.isEmpty()) logs.add(
            ChangeLogAdapter.Log(
                ChangeLogAdapter.LogType.START,
                getString(R.string.perform_exercise_new),
                getString(R.string.perform_exercise_new_description),
                getString(R.string.overview_today)
            )
        ) else logs.add(
            ChangeLogAdapter.Log(
                ChangeLogAdapter.LogType.START,
                getString(R.string.perform_exercise_new_old),
                getFormattedString(
                    R.string.perform_exercise_new_old_description,
                    exercises[0].weight,
                    exercises[0].reps
                ),
                exercises[0].date
            )
        )
        return logs
    }

    /**
     * @return A formatted string containing two variables
     */
    private fun getFormattedString(stringRsc: Int, value1: String, value2: String): String {
        return getString(
            stringRsc,
            if (value1.isEmpty()) "0" else value1,
            if (value2.isEmpty()) "0" else value2
        )
    }

    /**
     * Back to the last set
     */
    fun back(v: View) {
        v.isEnabled = false
        timer?.cancel()
        synchronized(state) {
            if (state.sets <= 0) {
                state.finished = true
                super.onBackPressed()
            } else {
                state.sets--
                updateView()
                if (state.sets > 0) startTimer()
            }
        }
        v.isEnabled = true
    }

    /**
     * Start the next set
     */
    fun next(v: View) {
        v.isEnabled = false
        timer?.cancel()
        synchronized(state) {
            if (state.start) state.totalTime = 0
            state.sets++
            state.setTimes[state.sets] = 0
            updateView()
            startTimer()
        }
        v.isEnabled = true
    }

    /**
     * Finish the exercise
     */
    fun finish(v: View) {
        v.isEnabled = false
        ConfirmDialog(
            getString(R.string.perform_exercise_finish_title),
            getString(R.string.perform_exercise_finish_description)
        ) {
            timer?.cancel()
            synchronized(state) {
                // Update the exercise
                state.exercise!!.reps = findViewById<TextInputEditText>(R.id.input_reps).text.toString()
                state.exercise!!.weight = findViewById<TextInputEditText>(R.id.input_weight).text.toString()
                state.exercise!!.description = findViewById<TextInputEditText>(R.id.input_description).text.toString()
                DataService.exerciseBox.put(state.exercise!!)

                // Add the performed exercise entry
                val performedExercise = PerformedExercise()
                performedExercise.update(state)
                DataService.performedExerciseBox.put(performedExercise)

                // Clear the state and close the activity
                state.finished = true
                setResult(EXERCISE_PERFORMED)
                super.onBackPressed()
            }
        }.show(supportFragmentManager, "finish_exercise")
        v.isEnabled = true
    }

    /**
     * Update the title
     */
    private fun getTitleText(): String {
        return when {
            state.start -> "Prepare • ${state.exercise!!.name}"
            state.pause -> "Pause • ${state.exercise!!.name}"
            else -> "${(state.sets + 1) / 2}. Set • ${state.exercise!!.name}"
        }
    }

    /**
     * Start the timer
     */
    private fun startTimer() {
        timer = Timer()
        timer!!.schedule(1000, 1000) {
            synchronized(state) {
                state.setTimes[state.sets] = state.setTimes[state.sets]!! + 1
                state.totalTime++
                showNotification()
            }
            runOnUiThread {
                synchronized(state) {
                    setText(totalTimeView, state.totalTime)
                    setText(currentTimeView, state.setTimes[state.sets]!!)
                }
            }
        }
    }

    /**
     * Set the text for a given time view
     *
     * @param textView The time view
     * @param time The time that shall be displayed
     */
    private fun setText(textView: TextView, time: Int) {
        val minutes = time / 60
        val seconds = time % 60
        if (minutes > 0)
            textView.text = Html.fromHtml("$minutes<small>m</small> $seconds<small>s</small>", 0)
        else
            textView.text = Html.fromHtml("$seconds<small>s</small>", 0)
    }
}