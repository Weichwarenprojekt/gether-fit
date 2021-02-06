package de.progresstinators.getherfit.shared

import android.app.Activity.RESULT_OK
import android.content.DialogInterface
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.os.Bundle
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import de.progresstinators.getherfit.R


class ImageBottomSheet(var onResult: (result: Int, image: Bitmap?) -> Unit) : BottomSheetDialogFragment() {

    /**
     * The options
     */
    companion object {
        const val EMPTY = 0
        const val ADD_IMAGE = 1
        const val DELETE_IMAGE = 2
        private const val FROM_CAMERA = 3
        private const val FROM_GALLERY = 4
    }

    /**
     * The result of the bottom sheet
     */
    private var result = EMPTY

    /**
     * The image output
     */
    private var image: Bitmap? = null

    /**
     * Create the view
     */
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        // Get the main view
        val view = inflater.inflate(R.layout.bottom_sheet_image, container, false)

        // Setup the click events
        view.findViewById<TextView>(R.id.from_camera).setOnClickListener {
            val takePicture = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
            startActivityForResult(takePicture, FROM_CAMERA)
        }
        view.findViewById<TextView>(R.id.from_gallery).setOnClickListener {
            val pickPhoto = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
            startActivityForResult(pickPhoto, FROM_GALLERY)
        }
        view.findViewById<TextView>(R.id.delete).setOnClickListener {
            result = DELETE_IMAGE
            dismiss()
        }

        return view
    }

    /**
     * Check if an image was taken or selected
     */
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == FROM_CAMERA && resultCode == RESULT_OK && data != null) {
            // Get the image and set the result
            image = data.extras!!.get("data") as Bitmap
            result = ADD_IMAGE
            dismiss()
        } else if (requestCode == FROM_GALLERY && resultCode == RESULT_OK) {
            try {
                // Get the image
                val inputStream = context!!.contentResolver.openInputStream(data!!.data!!)
                image = BitmapFactory.decodeStream(inputStream)

                // Rotate the image
                val matrix = Matrix()
                matrix.postRotate(90.0f)
                image = Bitmap.createBitmap(image!!, 0, 0, image!!.width, image!!.height, matrix, true)

                // Set the result and hide the bottom sheet
                result = ADD_IMAGE
                dismiss()
            } catch (e: Exception) {
                Toast.makeText(activity, R.string.bottom_image_gallery_error, Toast.LENGTH_LONG).show()
            }
        }
    }

    /**
     * Call the callback
     */
    override fun onDismiss(dialog: DialogInterface) {
        super.onDismiss(dialog)
        onResult(result, image)
    }
}