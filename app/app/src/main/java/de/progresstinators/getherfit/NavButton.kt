package de.progresstinators.getherfit

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import de.hdodenhof.circleimageview.CircleImageView

class NavButton @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyle: Int = 0,
    defStyleRes: Int = 0
) : ConstraintLayout(context, attrs, defStyle, defStyleRes) {

    /**
     * Initialize the view
     */
    init {
        LayoutInflater.from(context).inflate(R.layout.nav_button, this, true)
        attrs?.let {
            val typedArray = context.obtainStyledAttributes(it, R.styleable.NavButton, 0, 0)
            val logoImage = typedArray.getResourceId(R.styleable.NavButton_logo, R.drawable.logo)
            val logo = findViewById<CircleImageView>(R.id.logo)
            logo.setImageResource(logoImage)
            val titleText =
                resources.getText(typedArray.getResourceId(R.styleable.NavButton_title, R.string.placeholder))
            val title = findViewById<TextView>(R.id.title)
            title.text = titleText
            typedArray.recycle()
        }
    }

    /**
     * Show the highlighting background of the button
     *
     * @param highlighted True if the background should be shown
     */
    fun showHighlighting(highlighted: Boolean) {
        val highlighting = findViewById<View>(R.id.highlighting)
        highlighting.visibility = when (highlighted) {
            true -> View.VISIBLE
            false -> View.INVISIBLE
        }
    }
}