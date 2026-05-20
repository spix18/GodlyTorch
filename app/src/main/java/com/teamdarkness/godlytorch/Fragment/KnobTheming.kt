/*
 * This file is part of Godly Torch.
 *
 *     Godly Torch is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     Godly Torch is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with Godly Torch.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.teamdarkness.godlytorch.Fragment

import android.animation.ValueAnimator
import android.graphics.drawable.GradientDrawable
import android.view.HapticFeedbackConstants
import android.widget.TextView
import com.sdsmdg.harjot.crollerTest.Croller
import com.teamdarkness.godlytorch.Utils.AppTheme

/** Shared theming + status-pill helpers for the knob fragments. */
object KnobTheming {

    fun applyCroller(croller: Croller, theme: AppTheme) {
        croller.setBackCircleColor(theme.backCircleColor)
        croller.setMainCircleColor(theme.mainCircleColor)
        croller.setIndicatorColor(theme.indicatorColor)
        croller.setProgressPrimaryColor(theme.progressColor)
        croller.setProgressSecondaryColor(theme.progressSecondaryColor)
        croller.setLabelColor(theme.labelColor)
    }

    fun applyStatusPill(view: TextView, theme: AppTheme) {
        view.setTextColor(theme.mutedColor)
        view.background = GradientDrawable().apply {
            cornerRadius = 999f
            setColor(theme.surfaceColor)
        }
    }

    enum class StatusMode { NORMAL, EMPHASIZE, ERROR }

    /**
     * Update a status pill with a brief fade animation and theme-appropriate
     * text colour. Emits CONFIRM haptic feedback on [StatusMode.EMPHASIZE].
     */
    fun setStatus(view: TextView, text: String, theme: AppTheme, mode: StatusMode = StatusMode.NORMAL) {
        view.text = text
        view.alpha = 0.6f
        view.animate().alpha(1f).setDuration(animDuration(180)).start()
        view.setTextColor(
            when (mode) {
                StatusMode.ERROR -> theme.warningColor
                StatusMode.EMPHASIZE -> theme.labelColor
                StatusMode.NORMAL -> theme.mutedColor
            }
        )
        if (mode == StatusMode.EMPHASIZE) view.performHapticFeedback(HapticFeedbackConstants.CONFIRM)
    }

    /** Subtle scale pulse used when the user releases a knob. */
    fun pulse(croller: Croller?) {
        croller?.animate()?.scaleX(1.02f)?.scaleY(1.02f)?.setDuration(animDuration(90))?.withEndAction {
            croller.animate().scaleX(1f).scaleY(1f).setDuration(animDuration(120)).start()
        }?.start()
    }

    fun animDuration(ms: Long): Long = if (ValueAnimator.areAnimatorsEnabled()) ms else 0L
}
