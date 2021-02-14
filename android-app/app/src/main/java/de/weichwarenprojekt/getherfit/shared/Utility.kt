package de.weichwarenprojekt.getherfit.shared

import android.app.Activity
import com.google.android.material.chip.Chip
import de.weichwarenprojekt.getherfit.R

object Utility {

    /**
     * Set the style of a chip
     *
     * @param activity The current context
     * @param chip The chip to be styled
     * @param style The style to be applied
     */
    @SuppressWarnings("ResourceType")
    fun setChipStyle(activity: Activity, chip: Chip, style: Int) {
        val ta = activity.obtainStyledAttributes(
            style,
            intArrayOf(android.R.attr.textColor, R.attr.chipBackgroundColor)
        )
        chip.setTextColor(ta.getColor(0, activity.getColor(R.color.white)))
        chip.chipBackgroundColor = activity.getColorStateList(ta.getResourceId(1, R.color.black))
        ta.recycle()
    }
}