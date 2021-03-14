package de.weichwarenprojekt.getherfit.data

import android.graphics.Bitmap
import io.objectbox.annotation.Entity
import io.objectbox.annotation.Id
import io.objectbox.annotation.Unique

@Entity
data class User(
    @Id var id: Long = 0
) {
    /**
     * The first name of the user
     */
    var firstName: String = ""

    /**
     * The first name of the user
     */
    var lastName: String = ""

    /**
     * The email of the user
     */
    @Unique
    var email: String = ""

    /**
     * The email of the user
     */
    @Transient
    var image: Bitmap? = null
}
