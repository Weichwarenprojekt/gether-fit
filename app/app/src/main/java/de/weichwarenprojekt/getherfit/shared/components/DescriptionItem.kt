package de.weichwarenprojekt.getherfit.shared.components

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import de.weichwarenprojekt.getherfit.R

class DescriptionItem @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyle: Int = 0,
    defStyleRes: Int = 0
) : ConstraintLayout(context, attrs, defStyle, defStyleRes) {

    /**
     * Initialize the view
     */
    init {
        LayoutInflater.from(context).inflate(R.layout.description_item, this, true)
        attrs?.let {
            val typedArray = context.obtainStyledAttributes(
                it,
                R.styleable.DescriptionItem, 0, 0
            )
            val titleText = resources.getText(
                typedArray.getResourceId(
                    R.styleable.DescriptionItem_description_title,
                    R.string.placeholder
                )
            )
            val title = findViewById<TextView>(R.id.title)
            title.text = titleText
            val descriptionText = resources.getText(
                typedArray.getResourceId(
                    R.styleable.DescriptionItem_description_text,
                    R.string.placeholder
                )
            )
            val description = findViewById<TextView>(R.id.description)
            description.text = descriptionText
            typedArray.recycle()
        }
    }

    /**
     * Set the text of the description field
     *
     * @param titleText The text to be shown in the title
     * @param descriptionText The text to be shown in the description
     */
    fun setText(titleText: String, descriptionText: String) {
        val title = findViewById<TextView>(R.id.title)
        title.text = titleText
        val description = findViewById<TextView>(R.id.description)
        description.text = descriptionText
    }
}