package de.weichwarenprojekt.getherfit.data

import android.app.Activity
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Environment
import io.objectbox.annotation.Entity
import io.objectbox.annotation.Id
import io.objectbox.annotation.Transient
import java.io.File
import java.io.FileOutputStream

/**
 * This class describes the data model of a space
 *
 * @param name The name of the space
 * @param imagePath The image path
 * @param id The id of the space
 */
@Entity
data class Space(
    var name: String = "",
    var imagePath: String = "",
    @Id var id: Long = 0
) {

    /**
     * The image of the space
     */
    @Transient
    var image: Bitmap? = null
        private set

    /**
     * Update the image
     */
    fun setImage(activity: Activity, image: Bitmap?) {
        try {
            // Try to save the image
            val storageDir: File = activity.getExternalFilesDir(Environment.DIRECTORY_PICTURES)!!
            val file = File(storageDir, "space_$id.jpg")
            val os = FileOutputStream(file)

            // Check if there's no image given
            if (image == null) {
                file.delete()
                throw Exception()
            } else {
                image.compress(Bitmap.CompressFormat.JPEG, 100, os)
                this.image = image
                this.imagePath = file.toString()
            }
        } catch (e: Exception) {
            this.image = null
            this.imagePath = ""
        }
    }

    /**
     * Load the image
     */
    fun loadImage() {
        if (imagePath.isEmpty()) return
        try {
            this.image = BitmapFactory.decodeFile(imagePath)
        } catch (e: Exception) {
            this.imagePath = ""
            this.image = null
        }
    }

    /**
     * Remove the image
     */
    fun removeImage() {
        try {
            File(imagePath).delete()
        } catch (e: Exception) {
        }
    }
}