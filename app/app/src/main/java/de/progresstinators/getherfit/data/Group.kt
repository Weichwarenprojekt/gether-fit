package de.progresstinators.getherfit.data

import android.graphics.Bitmap

/**
 * This class describes the data model of a group
 *
 * @param id The id of the group (email address of the creator)
 */
class Group(var id: String = "") {

    /**
     * The name of the group
     */
    var name: String = ""

    /**
     * The image
     */
    var image: Bitmap? = null

}