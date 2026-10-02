package com.umain.hylla.layout

import com.umain.hylla.posture.DpBounds
import com.umain.hylla.posture.HeightClass
import com.umain.hylla.posture.Posture
import com.umain.hylla.posture.WindowPosture

/**
 * Where the scanner's viewfinder and its controls go, in window coordinates.
 *
 * In tabletop the device stands on its own folded half: the viewfinder takes the top segment,
 * facing the shelf, and the claim form takes the bottom one, in the thumb zone. The crease is
 * between them, so nothing sits on it.
 */
data class ScanLayout(val viewfinder: DpBounds, val controls: DpBounds, val sideBySide: Boolean) {
    companion object {
        /** In a flat portrait window, the viewfinder's share of the height. */
        const val VIEWFINDER_SHARE = 0.45f

        fun compute(posture: WindowPosture): ScanLayout {
            val window = posture.window
            if (posture.posture == Posture.Tabletop) {
                // Folds as a list: with more than one horizontal hinge, the camera faces out of
                // the top segment and the controls take the bottom one.
                return ScanLayout(posture.segments.first(), posture.segments.last(), sideBySide = false)
            }
            if (posture.heightClass == HeightClass.Compact && posture.posture != Posture.Cover) {
                val middle = window.width / 2
                return ScanLayout(window.copy(right = middle), window.copy(left = middle), sideBySide = true)
            }
            val split = window.height * VIEWFINDER_SHARE
            return ScanLayout(window.copy(bottom = split), window.copy(top = split), sideBySide = false)
        }
    }
}
