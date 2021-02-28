package de.weichwarenprojekt.getherfit.shared

import android.view.View

object ScrollWatcher {

    /**
     * The on scroll event
     */
    var onScroll: (scrollY: Int) -> Unit? = { _ -> }

    /**
     * Register a new scrollbar
     */
    fun setActiveScrollbar(scrollbar: View) {
        scrollbar.setOnScrollChangeListener { _, _, scrollY, _, _ ->
            onScroll(scrollY)
        }
    }

    /**
     * Reset the current scroll state
     */
    fun reset(scrollbar: View) {
        onScroll(scrollbar.scrollY)
    }
}