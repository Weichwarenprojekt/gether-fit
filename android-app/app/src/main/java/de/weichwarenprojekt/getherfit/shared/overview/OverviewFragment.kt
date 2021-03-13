package de.weichwarenprojekt.getherfit.shared.overview

import android.app.Activity
import android.graphics.Canvas
import android.graphics.RectF
import android.os.Bundle
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Spinner
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.charts.RadarChart
import com.github.mikephil.charting.components.AxisBase
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.*
import com.github.mikephil.charting.formatter.ValueFormatter
import com.github.mikephil.charting.interfaces.datasets.IBarDataSet
import com.github.mikephil.charting.renderer.BarChartRenderer
import de.weichwarenprojekt.getherfit.R
import de.weichwarenprojekt.getherfit.data.Category_
import de.weichwarenprojekt.getherfit.data.DataService
import de.weichwarenprojekt.getherfit.data.PerformedExercise_
import de.weichwarenprojekt.getherfit.settings.Settings
import de.weichwarenprojekt.getherfit.shared.ScrollWatcher
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList
import kotlin.math.max
import kotlin.math.min


class OverviewFragment : Fragment() {

    /**
     * The possible filter times
     */
    enum class Periods {
        LAST_3_MONTHS,
        LAST_YEAR,
        ALL_DATA
    }

    /**
     * Create the view for the fragment
     */
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        val view = inflater.inflate(R.layout.fragment_overview, container, false)
        ScrollWatcher.setActiveScrollbar(view.findViewById(R.id.scroll_view))

        // Set up the spinner
        val spinner: Spinner = view.findViewById(R.id.spinner_times)
        val adapter: ArrayAdapter<CharSequence> =
            ArrayAdapter.createFromResource(activity!!, R.array.periods, R.layout.spinner_item)
        adapter.setDropDownViewResource(R.layout.spinner_item)
        spinner.adapter = adapter
        spinner.setSelection(Settings.period.value)
        spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                Settings.period.update(position, activity!!)
                updateView()
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                Settings.period.update(Periods.LAST_3_MONTHS.ordinal, activity!!)
                updateView()
            }
        }

        return view
    }

    /**
     * Update the shown data
     */
    private fun updateView() {
        updateOverview()
        updateWorkoutChart()
        updateMuscleGroupsChart()
    }

    /**
     * Update the values for the overview card
     */
    private fun updateOverview() {
        // Find the right period
        val days = when (Settings.period.value) {
            Periods.LAST_3_MONTHS.ordinal -> 90
            Periods.LAST_YEAR.ordinal -> 365
            Periods.ALL_DATA.ordinal -> 0
            else -> 90
        }

        // Query the values
        val query = if (days <= 0) {
            DataService.performedExerciseBox.query().build()
        } else {
            val calendar = Calendar.getInstance()
            calendar.add(Calendar.DAY_OF_YEAR, -days)
            DataService.performedExerciseBox.query().greater(PerformedExercise_.timestamp, calendar.timeInMillis)
                .build()
        }
        val sessions = query.property(PerformedExercise_.date).distinct().findStrings().count().toLong()
        val exercises = query.count()

        // Update the views
        view!!.findViewById<TextView>(R.id.training_count).text = sessions.toString()
        view!!.findViewById<TextView>(R.id.exercise_count).text = exercises.toString()
    }

    /**
     * Update the values for the workout chart
     */
    private fun updateWorkoutChart() {
        // Update the title and get the matching data
        val title = view!!.findViewById<TextView>(R.id.title_chart_sessions)
        val (values, maxValue) = when (Settings.period.value) {
            Periods.LAST_YEAR.ordinal -> {
                title.text = activity!!.getText(R.string.overview_sessions_per_month)
                getSessionsPerMonth()
            }
            Periods.ALL_DATA.ordinal -> {
                title.text = activity!!.getText(R.string.overview_all_sessions_all_months)
                getAllSessionsPerMonth()
            }
            else -> {
                title.text = activity!!.getText(R.string.overview_sessions_per_week)
                getSessionsPerWeek()
            }
        }

        // Set up the chart
        val lineDataSet = BarDataSet(values, "workouts")
        val dataSets = arrayListOf<IBarDataSet>(lineDataSet)
        val chartData = BarData(dataSets)
        val chart = view!!.findViewById<BarChart>(R.id.chart_sessions)
        val typedValue = TypedValue()
        activity!!.theme.resolveAttribute(R.attr.colorSecondary, typedValue, true)
        lineDataSet.color = typedValue.data
        lineDataSet.setDrawValues(false)
        chart.data = chartData
        chart.legend.isEnabled = false
        chart.axisLeft.setDrawLabels(false)
        chart.axisLeft.setDrawAxisLine(false)
        chart.axisLeft.setDrawGridLines(false)
        chart.axisLeft.axisMinimum = 0f
        chart.axisLeft.axisMaximum = maxValue
        chart.axisLeft.labelCount = 0
        chart.axisRight.setDrawAxisLine(false)
        chart.axisRight.gridColor = activity!!.getColor(R.color.grey_5)
        chart.axisRight.textColor = activity!!.getColor(R.color.white)
        chart.axisRight.labelCount = min(maxValue.toInt(), 10)
        chart.axisRight.axisMinimum = 0f
        chart.axisRight.axisMaximum = maxValue
        chart.axisRight.isGranularityEnabled = true
        chart.axisRight.granularity = 1f
        chart.xAxis.textColor = activity!!.getColor(R.color.white)
        chart.xAxis.gridColor = activity!!.getColor(R.color.grey_5)
        chart.xAxis.labelRotationAngle = if (Settings.period.value == Periods.ALL_DATA.ordinal) 0f else -45f
        chart.xAxis.labelCount = values.size
        chart.xAxis.position = XAxis.XAxisPosition.BOTTOM
        chart.xAxis.setDrawGridLines(false)
        chart.xAxis.valueFormatter = BarChartFormatter(activity!!, values.size)
        chart.renderer = RoundedBarRenderer(activity!!, chart)
        chart.description.isEnabled = false
        chart.setTouchEnabled(false)
        chart.notifyDataSetChanged()
        chart.invalidate()
        chart.visibility = View.VISIBLE
    }

    /**
     * Get the sessions per week for the last 90 days
     */
    private fun getSessionsPerWeek(): Pair<ArrayList<BarEntry>, Float> {
        val values = ArrayList<BarEntry>()
        var maxValue = 0f
        val startDate = Calendar.getInstance()
        startDate.add(Calendar.DATE, -7)
        val lastDate = Calendar.getInstance()
        for (i in 11 downTo 0) {
            val count = DataService.performedExerciseBox.query()
                .between(PerformedExercise_.timestamp, startDate.timeInMillis, lastDate.timeInMillis).build()
                .property(PerformedExercise_.date).distinct().findStrings().count()
            maxValue = max(count.toFloat(), maxValue)
            values.add(BarEntry(i.toFloat(), count.toFloat()))
            startDate.add(Calendar.DATE, -7)
            lastDate.add(Calendar.DATE, -7)
        }
        return Pair(values, maxValue)
    }

    /**
     * Get the sessions per month
     */
    private fun getSessionsPerMonth(): Pair<ArrayList<BarEntry>, Float> {
        val values = ArrayList<BarEntry>()
        var maxValue = 0f
        val startDate = Calendar.getInstance()
        startDate.add(Calendar.MONTH, -1)
        val lastDate = Calendar.getInstance()
        for (i in 11 downTo 0) {
            val count = DataService.performedExerciseBox.query()
                .between(PerformedExercise_.timestamp, startDate.timeInMillis, lastDate.timeInMillis).build()
                .property(PerformedExercise_.date).distinct().findStrings().count()
            maxValue = max(count.toFloat(), maxValue)
            values.add(BarEntry(i.toFloat(), count.toFloat()))
            startDate.add(Calendar.MONTH, -1)
            lastDate.add(Calendar.MONTH, -1)
        }
        return Pair(values, maxValue)
    }

    /**
     * Get all sessions for each month
     */
    private fun getAllSessionsPerMonth(): Pair<ArrayList<BarEntry>, Float> {
        val values = ArrayList<BarEntry>()
        val sessions =
            DataService.performedExerciseBox.query().build().property(PerformedExercise_.date).distinct().findStrings()
        var maxValue = 0f
        for (i in 11 downTo 0) {
            var count = 0
            for (session in sessions) {
                val date = Calendar.getInstance()
                date.time = DateFormat.getDateInstance().parse(session)!!
                if (i == date.get(Calendar.MONTH)) count++
            }
            maxValue = max(count.toFloat(), maxValue)
            values.add(BarEntry(i.toFloat(), count.toFloat()))
        }
        return Pair(values, maxValue)
    }

    /**
     * The formatter for the x-axis of the bar chart
     */
    class BarChartFormatter(private val activity: Activity, private val valueSize: Int) : ValueFormatter() {
        override fun getAxisLabel(value: Float, axis: AxisBase): String {
            // Show the months if overview is currently showing all data
            if (Settings.period.value == Periods.ALL_DATA.ordinal) {
                return activity.resources.getStringArray(R.array.months)[value.toInt()]
            }

            // Show labels for sessions per week/month
            val updatedValue = value.toInt() + 1
            return if (updatedValue >= valueSize) {
                activity.getString(R.string.overview_today)
            } else {
                val date = Calendar.getInstance()
                if (Settings.period.value == Periods.LAST_3_MONTHS.ordinal)
                    date.add(Calendar.DATE, 7 * (updatedValue - valueSize))
                else
                    date.add(Calendar.MONTH, updatedValue - valueSize)
                SimpleDateFormat.getDateInstance(DateFormat.SHORT).format(date.time)
            }
        }
    }

    /**
     * The renderer for rounded bars for the bar charts
     */
    class RoundedBarRenderer(private val activity: Activity, chart: BarChart) :
        BarChartRenderer(chart, chart.animator, chart.viewPortHandler) {
        override fun drawDataSet(c: Canvas?, dataSet: IBarDataSet?, index: Int) {
            val trans = mChart.getTransformer(dataSet!!.axisDependency)
            mShadowPaint.color = dataSet.barShadowColor
            val phaseX = mAnimator.phaseX
            val phaseY = mAnimator.phaseY

            // Initialize the buffer
            val buffer = mBarBuffers[index]
            buffer.setPhases(phaseX, phaseY)
            buffer.setDataSet(index)
            buffer.setBarWidth(mChart.barData.barWidth)
            buffer.setInverted(mChart.isInverted(dataSet.axisDependency))
            buffer.feed(dataSet)
            trans.pointValuesToPixel(buffer.buffer)

            // Draw the rounded rectangles
            mRenderPaint.color = dataSet.color
            val radius = activity.resources.getDimension(R.dimen.corner_radius)
            var j = 0
            while (j < buffer.size()) {
                if (!mViewPortHandler.isInBoundsLeft(buffer.buffer[j + 2])) {
                    j += 4
                    continue
                }
                if (!mViewPortHandler.isInBoundsRight(buffer.buffer[j])) break
                c!!.drawRoundRect(
                    RectF(
                        buffer.buffer[j], buffer.buffer[j + 1], buffer.buffer[j + 2],
                        buffer.buffer[j + 3]
                    ), radius, radius, mRenderPaint
                )
                c.drawRect(
                    buffer.buffer[j], buffer.buffer[j + 1] + radius, buffer.buffer[j + 2],
                    buffer.buffer[j + 3], mRenderPaint
                )
                j += 4
            }
        }
    }

    /**
     * Update the chart that displays the trained muscle groups
     */
    private fun updateMuscleGroupsChart() {
        // Create the data set
        val (values, labels, maxValue) = getMuscleGroups()
        val dataSet = RadarDataSet(values, "muscle_groups")
        val typedValue = TypedValue()
        activity!!.theme.resolveAttribute(R.attr.colorSecondary, typedValue, true)
        dataSet.color = typedValue.data
        dataSet.fillColor = typedValue.data
        dataSet.setDrawFilled(true)
        dataSet.fillAlpha = 100
        dataSet.lineWidth = 4f
        dataSet.valueTextSize = 0f

        // Set up the chart
        val chart = view!!.findViewById<RadarChart>(R.id.chart_muscle_groups)
        val data = RadarData()
        data.addDataSet(dataSet)
        chart.data = data
        chart.xAxis.valueFormatter = object : ValueFormatter() {
            override fun getAxisLabel(value: Float, axis: AxisBase): String {
                return if (value < 0 || value > labels.size - 1) ""
                else labels[value.toInt()]
            }
        }
        val padding = resources.getDimension(R.dimen.radar_chart_padding)
        chart.setExtraOffsets(padding, padding, padding, padding)
        chart.xAxis.gridColor = activity!!.getColor(R.color.grey_5)
        chart.yAxis.gridColor = activity!!.getColor(R.color.grey_5)
        chart.xAxis.textColor = activity!!.getColor(R.color.white)
        chart.yAxis.textColor = activity!!.getColor(R.color.white)
        chart.yAxis.axisMinimum = 0f
        chart.yAxis.axisMaximum = maxValue
        chart.yAxis.isGranularityEnabled = true
        chart.yAxis.granularity = 1f
        chart.yAxis.valueFormatter = object : ValueFormatter() {
            override fun getAxisLabel(value: Float, axis: AxisBase?): String {
                return if (value < 1) "" else value.toInt().toString()
            }
        }
        chart.webColor = activity!!.getColor(R.color.grey_5)
        chart.webColorInner = activity!!.getColor(R.color.grey_5)
        chart.webLineWidth = 0f
        chart.legend.isEnabled = false
        chart.description.isEnabled = false
        chart.setTouchEnabled(false)
        chart.notifyDataSetChanged()
        chart.invalidate()
        chart.visibility = if (values.size > 2) View.VISIBLE else View.GONE
        view!!.findViewById<View>(R.id.text_warning_muscles).visibility =
            if (values.size > 2) View.GONE else View.VISIBLE
    }

    /**
     * Get the trained muscle groups
     */
    private fun getMuscleGroups(): Triple<ArrayList<RadarEntry>, ArrayList<String>, Float> {
        val values = ArrayList<RadarEntry>()
        val labels = ArrayList(DataService.categoryBox.query().build().property(Category_.name).findStrings().toList())
        var maxValue = 0f
        val iterator = labels.iterator()

        while (iterator.hasNext()) {
            // Prepare the query to respect the period filter
            val label = iterator.next()
            val date = Calendar.getInstance()
            val query = when (Settings.period.value) {
                Periods.LAST_3_MONTHS.ordinal -> {
                    date.add(Calendar.MONTH, -3)
                    DataService.performedExerciseBox.query().greater(PerformedExercise_.timestamp, date.timeInMillis)
                }
                Periods.LAST_YEAR.ordinal -> {
                    date.add(Calendar.YEAR, -1)
                    DataService.performedExerciseBox.query().greater(PerformedExercise_.timestamp, date.timeInMillis)
                }
                else -> DataService.performedExerciseBox.query()
            }

            // Count the matching exercises
            val count = query.filter { exercise ->
                var includesCategory = false
                for (category in exercise.categories) if (category.name == label) includesCategory = true
                includesCategory
            }.build().find().count()
            maxValue = max(count.toFloat(), maxValue)

            // Only add the muscle group if it was trained at least once
            if (count <= 0) iterator.remove()
            else values.add(RadarEntry(count.toFloat()))
        }

        // Check if enough values were collected
        if (labels.size < 3) {
            labels.clear()
            values.clear()
        }
        return Triple(values, labels, maxValue)
    }
}