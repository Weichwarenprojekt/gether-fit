package de.progresstinators.getherfit.group

import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.widget.AppCompatButton
import com.google.android.material.textfield.TextInputEditText
import de.progresstinators.getherfit.R
import de.progresstinators.getherfit.data.Group
import de.progresstinators.getherfit.settings.User
import de.progresstinators.getherfit.shared.BaseActivity
import de.progresstinators.getherfit.shared.components.ImageBottomSheet

class EditGroupActivity : BaseActivity() {

    companion object {
        /**
         * The result if a group was added
         */
        const val GROUP_ADDED = 1

        /**
         * The result if a group was modified
         */
        const val GROUP_MODIFIED = 2

        /**
         * True if a new group shall be added (otherwise activity will be in modify mode)
         */
        private var addGroup = true

        /**
         * The currently modified group
         */
        var group = Group()

        /**
         * Prepare the activity (to either modify or add a group)
         *
         * @param addGroup True if a new group shall be added
         * @param group The group to be added/modified
         */
        fun prepare(addGroup: Boolean, group: Group) {
            EditGroupActivity.addGroup = addGroup
            EditGroupActivity.group = group
        }
    }

    /**
     * Initialize the activity
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_group)
        updateView()
    }

    /**
     * Update the view
     */
    private fun updateView() {
        // Update the title
        val title = findViewById<TextView>(R.id.title)
        title.text = when (addGroup) {
            true -> getString(R.string.edit_group)
            else -> getString(R.string.edit_group_modify)
        }
        updateImage()

        // Update the name input
        val nameInput = findViewById<TextInputEditText>(R.id.name_input)
        nameInput.setText(group.name)

        // Update the leave/delete buttons
        if (addGroup) return
        val deleteButton = findViewById<AppCompatButton>(R.id.delete_button)
        deleteButton.visibility = when(User.email){
            group.id -> View.VISIBLE
            else -> View.GONE
        }
        val leaveButton = findViewById<AppCompatButton>(R.id.leave_button)
        leaveButton.visibility = when(User.email){
            group.id -> View.GONE
            else -> View.VISIBLE
        }
    }

    /**
     * Update the user's image
     */
    private fun updateImage() {
        val logo = findViewById<ImageView>(R.id.group_image)
        if (group.image != null) logo.setImageBitmap(group.image)
        else logo.setImageResource(R.drawable.new_group)
    }

    /**
     * Modify the group image
     */
    fun modifyImage(v: View) {
        v.isEnabled = false
        ImageBottomSheet { result, image ->
            if (result == ImageBottomSheet.ADD_IMAGE) group.image =
                image
            else if (result == ImageBottomSheet.DELETE_IMAGE) group.image =
                null
            if (result != ImageBottomSheet.EMPTY) updateImage()
            v.isEnabled = true
        }.show(supportFragmentManager, "modify_group_image")
    }

    /**
     * Apply the creation/modification of the group
     */
    fun apply(v: View) {
        setResult(when(addGroup) {
            true -> GROUP_ADDED
            else -> GROUP_MODIFIED
        })
        onBackPressed()
    }

    /**
     * Close the activity
     */
    fun close(v: View) {
        onBackPressed()
    }
}