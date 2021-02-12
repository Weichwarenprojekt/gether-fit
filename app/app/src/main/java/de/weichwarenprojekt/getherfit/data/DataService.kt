package de.weichwarenprojekt.getherfit.data

import android.app.Activity
import io.objectbox.Box
import io.objectbox.BoxStore
import io.objectbox.kotlin.boxFor

object DataService {

    /**
     * The box store
     */
    lateinit var boxStore: BoxStore
        private set

    /**
     * The space box
     */
    lateinit var spaceBox: Box<Space>
        private set

    /**
     * Initialize the data service
     */
    fun init(activity: Activity) {
        boxStore = MyObjectBox.builder().androidContext(activity.applicationContext).build()
        spaceBox = boxStore.boxFor()
    }
}