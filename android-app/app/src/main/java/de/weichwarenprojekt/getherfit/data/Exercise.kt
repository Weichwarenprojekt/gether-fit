package de.weichwarenprojekt.getherfit.data

import io.objectbox.annotation.Entity
import io.objectbox.annotation.Id
import io.objectbox.relation.ToMany

/**
 * This class describes the data model of an exercise
 *
 * @param name The name of the exercise
 * @param id The id of the space
 */
@Entity
data class Exercise(
    var name: String = "",
    @Id var id: Long = 0
) {
    /**
     * The corresponding categories of the exercise
     */
    lateinit var categories: ToMany<Category>

    /**
     * The targeted reps for an exercise
     */
    var reps: String = ""

    /**
     * The targeted weight for an exercise
     */
    var weight: String = ""

    /**
     * Additional description
     */
    var description: String = ""
}
