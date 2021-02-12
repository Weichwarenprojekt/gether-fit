package de.weichwarenprojekt.getherfit.shared

import android.widget.ScrollView

object ScrollWatcher {

    /**
     * The on scroll event
     */
    var onScroll : (scrollY: Int) -> Unit? = {_ -> }

    /**
     * Register a new scrollbar
     */
    fun setActiveScrollbar(scrollbar: ScrollView) {
        scrollbar.setOnScrollChangeListener { _, _, scrollY, _, _ ->
            onScroll(scrollY)
        }
    }

    /**
     * Reset the current scroll state
     */
    fun reset(scrollbar: ScrollView) {
        onScroll(scrollbar.scrollY)
    }
}