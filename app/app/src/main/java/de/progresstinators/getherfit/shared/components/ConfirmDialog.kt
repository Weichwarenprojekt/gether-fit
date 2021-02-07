package de.progresstinators.getherfit.shared.components

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.DialogFragment
import de.progresstinators.getherfit.R


/**
 * A confirm dialog
 *
 * @param title The title of the dialog
 * @param description The description of the dialog
 * @param onConfirm The action that should happen if the dialog is confirmed
 */
class ConfirmDialog(var title: String, var description: String, var onConfirm: () -> Unit) : DialogFragment() {

    /**
     * Create the view
     */
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        // Get the main view
        val view = inflater.inflate(R.layout.dialog_confirm, container)
        // Set the text
        view.findViewById<TextView>(R.id.title).text = title
        view.findViewById<TextView>(R.id.description).text = description

        // Set the click events
        view.findViewById<TextView>(R.id.close_button).setOnClickListener {
            dismiss()
        }
        view.findViewById<TextView>(R.id.confirm_button).setOnClickListener {
            onConfirm()
            dismiss()
        }

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        dialog!!.window!!.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
        dialog!!.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
    }
}