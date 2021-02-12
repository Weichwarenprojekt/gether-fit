package de.weichwarenprojekt.getherfit.shared.components

import android.app.Activity.RESULT_OK
import android.content.DialogInterface
import android.content.Intent
import android.database.Cursor
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.FileProvider
import androidx.exifinterface.media.ExifInterface
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import de.weichwarenprojekt.getherfit.R
import java.io.File
import java.io.IOException


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
     * The path to the last image that was taken
     */
    lateinit var imagePath: String

    /**
     * Create the view
     */
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        // Get the main view
        val view = inflater.inflate(R.layout.bottom_sheet_image, container, false)

        // Setup the click events
        view.findViewById<TextView>(R.id.from_camera).setOnClickListener {
            takePicture()
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
     * Start the camera to take a picture
     */
    private fun takePicture() {
        Intent(MediaStore.ACTION_IMAGE_CAPTURE).also { takePictureIntent ->
            takePictureIntent.resolveActivity(activity!!.packageManager)?.also {
                // Create the temporary file
                val photoFile: File? = try {
                    val storageDir: File = activity!!.getExternalFilesDir(Environment.DIRECTORY_PICTURES)!!
                    File(storageDir, "temp.jpg").apply { imagePath = absolutePath }
                } catch (ex: IOException) {
                    null
                }

                // Specify the output URI
                photoFile?.also {
                    val photoURI: Uri = FileProvider.getUriForFile(
                        activity!!,
                        "de.weichwarenprojekt.getherfit.fileprovider",
                        it
                    )
                    takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI)
                    startActivityForResult(takePictureIntent, FROM_CAMERA)
                }
            }
        }
    }

    /**
     * Check if an image was taken or selected
     */
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == FROM_CAMERA && resultCode == RESULT_OK) {
            try {
                // Try to get the image and check if it needs to be rotated
                image = BitmapFactory.decodeFile(imagePath)
                rotateImageFromCamera()

                // Delete the temporary image and return the taken image
                File(imagePath).delete()
                result = ADD_IMAGE
                dismiss()
            } catch (e: Exception) {
                Toast.makeText(activity, R.string.bottom_image_gallery_error, Toast.LENGTH_LONG).show()
            }
        } else if (requestCode == FROM_GALLERY && resultCode == RESULT_OK) {
            try {
                // Try to get the image and check if it needs to be rotated
                val inputStream = context!!.contentResolver.openInputStream(data!!.data!!)
                image = BitmapFactory.decodeStream(inputStream)
                rotateImageFromGallery(data.data!!)

                // Return the image
                result = ADD_IMAGE
                dismiss()
            } catch (e: Exception) {
                Toast.makeText(activity, R.string.bottom_image_gallery_error, Toast.LENGTH_LONG).show()
            }
        }
    }

    /**
     * Check if an image taken directly from the camera needs to be rotated
     */
    private fun rotateImageFromCamera() {
        val ei = ExifInterface(imagePath)
        when (ei.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL)) {
            ExifInterface.ORIENTATION_ROTATE_90 -> rotateImage(90.0f)
            ExifInterface.ORIENTATION_ROTATE_180 -> rotateImage(180.0f)
            ExifInterface.ORIENTATION_ROTATE_270 -> rotateImage(270.0f)
        }
    }

    /**
     * Check if an image taken from the gallery needs to be rotated (only works for version 29+)
     */
    private fun rotateImageFromGallery(imageData: Uri) {
        if (android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.Q) return
        val filePathColumn = arrayOf(MediaStore.Images.ImageColumns.ORIENTATION)
        val cursor: Cursor? = activity!!.contentResolver.query(
            imageData, filePathColumn, null, null, null
        )
        cursor!!.moveToFirst()
        rotateImage(cursor.getInt(0).toFloat())
        cursor.close()
    }

    /**
     * Rotate the image
     *
     * @degree The angle the by which the image shall be rotated
     */
    private fun rotateImage(degree: Float) {
        val matrix = Matrix()
        matrix.postRotate(degree)
        image = Bitmap.createBitmap(image!!, 0, 0, image!!.width, image!!.height, matrix, true)
    }

    /**
     * Call the callback
     */
    override fun onDismiss(dialog: DialogInterface) {
        super.onDismiss(dialog)
        onResult(result, image)
    }
}