package de.weichwarenprojekt.getherfit.space

import android.graphics.Bitmap
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.widget.AppCompatButton
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import de.weichwarenprojekt.getherfit.R
import de.weichwarenprojekt.getherfit.data.DataService
import de.weichwarenprojekt.getherfit.data.Space
import de.weichwarenprojekt.getherfit.shared.BaseActivity
import de.weichwarenprojekt.getherfit.shared.components.ConfirmDialog
import de.weichwarenprojekt.getherfit.shared.components.ImageBottomSheet

class EditSpaceActivity : BaseActivity() {

    companion object {
        /**
         * The result if a space was modified/added
         */
        const val SPACE_MODIFIED = 1

        /**
         * The result if a space was deleted
         */
        const val SPACE_REMOVED = 2

        /**
         * True if a new space shall be added (otherwise activity will be in modify mode)
         */
        private var addSpace = true

        /**
         * The currently modified space
         */
        private lateinit var space: Space

        /**
         * The modified image
         */
        private var image: Bitmap? = null

        /**
         * Prepare the activity (to either modify or add a space)
         *
         * @param addSpace True if a new space shall be added
         * @param space The space to be added/modified
         */
        fun prepare(addSpace: Boolean, space: Space = Space()) {
            EditSpaceActivity.addSpace = addSpace
            EditSpaceActivity.space = space
            image = space.image
        }
    }

    /**
     * The layout of the name input
     */
    lateinit var nameInputLayout: TextInputLayout

    /**
     * The actual name input
     */
    lateinit var nameInput: TextInputEditText

    /**
     * Initialize the activity
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_space)
        nameInputLayout = findViewById(R.id.name_input_layout)
        nameInput = findViewById(R.id.name_input)
        updateView()
    }

    /**
     * Update the view
     */
    private fun updateView() {
        // Update the title
        val title = findViewById<TextView>(R.id.title)
        title.text = when (addSpace) {
            true -> getString(R.string.edit_space)
            else -> getString(R.string.edit_space_modify)
        }
        updateImage()

        // Update the name input
        nameInput.setText(space.name)

        // Update the leave/delete buttons
        if (addSpace) return
        val leaveButton = findViewById<AppCompatButton>(R.id.leave_button)
        leaveButton.visibility = View.VISIBLE
    }

    /**
     * Update the user's image
     */
    private fun updateImage() {
        val logo = findViewById<ImageView>(R.id.space_image)
        if (image != null) logo.setImageBitmap(image)
        else logo.setImageResource(R.drawable.space)
    }

    /**
     * Modify the space image
     */
    fun modifyImage(v: View) {
        v.isEnabled = false
        ImageBottomSheet { result, image ->
            if (result == ImageBottomSheet.ADD_IMAGE) EditSpaceActivity.image = image
            else if (result == ImageBottomSheet.DELETE_IMAGE) EditSpaceActivity.image = null
            if (result != ImageBottomSheet.EMPTY) updateImage()
            v.isEnabled = true
        }.show(supportFragmentManager, "modify_space_image")
    }

    /**
     * Apply the creation/modification of the space
     */
    fun apply(v: View) {
        // Check if a name is given
        val newName: String = nameInput.text.toString()
        if (newName.isEmpty()) {
            nameInputLayout.error = getString(R.string.edit_space_name_error)
            return
        }

        // Check if space is new and needs an id first
        if (addSpace) DataService.spaceBox.put(space)

        // Update the data
        space.name = newName
        space.setImage(this, image)
        DataService.spaceBox.put(space)

        // Set the result and return
        setResult(SPACE_MODIFIED)
        onBackPressed()
    }

    /**
     * Leave the space
     */
    fun leaveSpace(v: View) {
        ConfirmDialog(getString(R.string.edit_space_leave), getString(R.string.edit_space_leave_description)) {
            setResult(SPACE_REMOVED)
            space.removeImage()
            DataService.spaceBox.remove(space)
            onBackPressed()
        }.show(supportFragmentManager, "confirm_leave")
    }

    /**
     * Delete the space
     */
    fun deleteSpace(v: View) {
        ConfirmDialog(getString(R.string.edit_space_delete), getString(R.string.edit_space_delete_description)) {
            setResult(SPACE_REMOVED)
            space.removeImage()
            DataService.spaceBox.remove(space)
            onBackPressed()
        }.show(supportFragmentManager, "confirm_delete")
    }

    /**
     * Close the activity
     */
    fun close(v: View) {
        onBackPressed()
    }
}