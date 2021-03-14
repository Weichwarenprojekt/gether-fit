package de.weichwarenprojekt.getherfit.space.training.exercises

import android.app.Activity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import de.weichwarenprojekt.getherfit.R
import de.weichwarenprojekt.getherfit.shared.components.DescriptionItem

class ChangeLogAdapter(val activity: Activity, var logs: List<Log>) :
    RecyclerView.Adapter<ChangeLogAdapter.ViewHolder>() {

    /**
     * The possible log types
     */
    enum class LogType {
        START,
        WEIGHT,
        REPS
    }

    /**
     * The class that describes a single log
     *
     * @param type The type of log that shall be displayed
     * @param title The title of the log
     * @param description The description of the log
     * @param date The matching date
     */
    data class Log(
        val type: LogType,
        val title: String,
        val description: String,
        val date: String
    )

    /**
     * The view holder for an history item
     */
    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val date: TextView = view.findViewById(R.id.date)
        val logo: ImageView = view.findViewById(R.id.logo)
        val description: DescriptionItem = view.findViewById(R.id.description_item)
        val start: View = view.findViewById(R.id.timeline_start)
        val full: View = view.findViewById(R.id.timeline_full)
        val end: View = view.findViewById(R.id.timeline_end)
    }

    /**
     * Create a new view holder
     */
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            LayoutInflater.from(parent.context).inflate(R.layout.change_log_item, parent, false)
        )
    }

    /**
     * Update the values of a view holder
     */
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        // Set the date
        holder.date.text = logs[position].date

        // Select the right timeline
        holder.start.visibility = if (position == 0 && logs.size > 1) View.VISIBLE else View.GONE
        holder.end.visibility = if (position > 0 && position == logs.size - 1) View.VISIBLE else View.GONE
        holder.full.visibility = if (position > 0 && position < logs.size - 1) View.VISIBLE else View.GONE

        // Update the logo
        holder.logo.setImageResource(
            when (logs[position].type) {
                LogType.REPS -> R.drawable.ic_baseline_replay_24
                LogType.WEIGHT -> R.drawable.weight
                else -> R.drawable.ic_baseline_play_arrow_24
            }
        )

        // Update title and description
        holder.description.setText(logs[position].title, logs[position].description)
    }

    /**
     * @return The size of the data
     */
    override fun getItemCount(): Int = logs.size
}