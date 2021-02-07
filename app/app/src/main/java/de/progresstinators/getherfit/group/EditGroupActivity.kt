package de.progresstinators.getherfit.group

import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.widget.AppCompatButton
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import de.progresstinators.getherfit.R
import de.progresstinators.getherfit.data.Group
import de.progresstinators.getherfit.settings.User
import de.progresstinators.getherfit.shared.BaseActivity
import de.progresstinators.getherfit.shared.components.ConfirmDialog
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
         * The result if a group was deleted
         */
        const val GROUP_DELETED = 3

        /**
         * The result if user left the group
         */
        const val GROUP_LEFT = 4

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
        setContentView(R.layout.activity_edit_group)
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
        title.text = when (addGroup) {
            true -> getString(R.string.edit_group)
            else -> getString(R.string.edit_group_modify)
        }
        updateImage()

        // Update the name input
        nameInput.setText(group.name)

        // Update the leave/delete buttons
        if (addGroup) return
        val deleteButton = findViewById<AppCompatButton>(R.id.delete_button)
        deleteButton.visibility = when (User.email) {
            group.id -> View.VISIBLE
            else -> View.GONE
        }
        val leaveButton = findViewById<AppCompatButton>(R.id.leave_button)
        leaveButton.visibility = when (User.email) {
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
        else logo.setImageResource(R.drawable.group)
    }

    /**
     * Modify the group image
     */
    fun modifyImage(v: View) {
        v.isEnabled = false
        ImageBottomSheet { result, image ->
            if (result == ImageBottomSheet.ADD_IMAGE) group.image = image
            else if (result == ImageBottomSheet.DELETE_IMAGE) group.image = null
            if (result != ImageBottomSheet.EMPTY) updateImage()
            v.isEnabled = true
        }.show(supportFragmentManager, "modify_group_image")
    }

    /**
     * Apply the creation/modification of the group
     */
    fun apply(v: View) {
        // Check if a name is given
        val newName: String = nameInput.text.toString()
        if (newName.isEmpty()) {
            nameInputLayout.error = getString(R.string.edit_group_name_error)
            return
        }
        group.name = newName

        // Set the result and return
        setResult(
            when (addGroup) {
                true -> GROUP_ADDED
                else -> GROUP_MODIFIED
            }
        )
        onBackPressed()
    }

    /**
     * Leave the group
     */
    fun leaveGroup(v: View) {
        ConfirmDialog(getString(R.string.edit_group_leave), getString(R.string.edit_group_leave_description)) {
            setResult(GROUP_LEFT)
            onBackPressed()
        }.show(supportFragmentManager, "confirm_leave")
    }

    /**
     * Delete the group
     */
    fun deleteGroup(v: View) {
        ConfirmDialog(getString(R.string.edit_group_delete), getString(R.string.edit_group_delete_description)) {
            setResult(GROUP_DELETED)
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