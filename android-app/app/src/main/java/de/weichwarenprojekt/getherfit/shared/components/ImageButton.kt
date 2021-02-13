package de.weichwarenprojekt.getherfit.shared.components

import android.content.Context
import android.graphics.Bitmap
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import de.hdodenhof.circleimageview.CircleImageView
import de.weichwarenprojekt.getherfit.R

class ImageButton @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyle: Int = 0,
    defStyleRes: Int = 0
) : ConstraintLayout(context, attrs, defStyle, defStyleRes) {

    /**
     * Initialize the view
     */
    init {
        LayoutInflater.from(context).inflate(R.layout.image_button, this, true)
        attrs?.let {
            val typedArray = context.obtainStyledAttributes(
                it,
                R.styleable.ImageButton,
                0,
                0
            )
            val imageRsc = typedArray.getResourceId(
                R.styleable.ImageButton_image_button_logo,
                R.drawable.space
            )
            val titleText =
                resources.getText(
                    typedArray.getResourceId(
                        R.styleable.ImageButton_image_button_title,
                        R.string.placeholder
                    )
                )
            val background = typedArray.getResourceId(
                R.styleable.ImageButton_image_button_background,
                R.drawable.nav_button_highlighting
            )
            updateView(titleText as String, background, imageRsc)
            typedArray.recycle()
        }
    }

    /**
     * Update the image button
     *
     * @param titleText The text of the button
     * @param background The highlighting background
     * @param imageRsc The image resource (optional)
     * @param image The actual image (optional)
     */
    fun updateView(titleText: String, background: Int, imageRsc: Int = R.drawable.space, image: Bitmap? = null) {
        val title = findViewById<TextView>(R.id.title)
        title.text = titleText
        val highlighting = findViewById<View>(R.id.highlighting)
        highlighting.setBackgroundResource(background)
        val logo = findViewById<CircleImageView>(R.id.logo)
        if (image != null) logo.setImageBitmap(image)
        else logo.setImageResource(imageRsc)
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