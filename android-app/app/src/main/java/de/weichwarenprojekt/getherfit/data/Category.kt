package de.weichwarenprojekt.getherfit.data

import io.objectbox.annotation.Entity
import io.objectbox.annotation.Id
import io.objectbox.annotation.Unique

/**
 * This class describes the data model of a category
 *
 * @param name The name of the category
 * @param color The color of the category
 * @param id The id of the space
 */
@Entity
data class Category(
    @Unique var name: String = "",
    @Unique var color: Int = 0xFFFFFFFF.toInt(),
    @Id var id: Long = 0
)