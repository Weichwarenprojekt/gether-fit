package de.weichwarenprojekt.getherfit.data

import android.graphics.Bitmap

/**
 * This class describes the data model of a group
 *
 * @param id The id of the group (email address of the creator)
 * @param name The name of the group
 * @param image The image of the group
 */
class Group(
    var id: String = "",
    var name: String = "",
    var image: Bitmap? = null
)