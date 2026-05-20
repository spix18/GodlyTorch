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

import android.content.res.ColorStateList
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.preference.PreferenceManager
import com.teamdarkness.godlytorch.R
import com.teamdarkness.godlytorch.Utils.AppThemes
import com.teamdarkness.godlytorch.Utils.Prefs
import com.teamdarkness.godlytorch.Utils.Utils
import com.teamdarkness.godlytorch.Utils.Utils.askRoot
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/** Splash-like first screen: shows root + device probe before routing to a knob fragment. */
class LaunchFragment : Fragment() {

    private var selectedDevice = ""
    private var isDualTone = true

    private var themeLogText: TextView? = null
    private var themeProgressBar: ProgressBar? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(R.layout.fragment_launch, container, false)

        val logText: TextView = view.findViewById(R.id.logText)
        val progressBar: ProgressBar = view.findViewById(R.id.progressBar)
        val logo: ImageView = view.findViewById(R.id.logoImage)
        val statusCard: LinearLayout = view.findViewById(R.id.statusCard)
        themeLogText = logText
        themeProgressBar = progressBar
        applyTheme(view, logText, progressBar)

        logo.alpha = 0f
        logo.translationY = 24f
        statusCard.alpha = 0f
        logo.animate().alpha(1f).translationY(0f).setDuration(200).start()
        statusCard.animate().alpha(1f).setStartDelay(80).setDuration(180).start()

        context?.let { ctx ->
            val prefs = PreferenceManager.getDefaultSharedPreferences(ctx)
            selectedDevice = prefs.getString(Prefs.SELECTED_DEVICE, "") ?: ""
            isDualTone = prefs.getBoolean(Prefs.IS_DUAL_TONE, true)
        }

        logText.text = getString(R.string.root_check)
        logText.alpha = 0.75f
        logText.animate().alpha(1f).setDuration(180).start()
        progressBar.visibility = View.VISIBLE

        viewLifecycleOwner.lifecycleScope.launch {
            delay(1000)
            if (askRoot()) {
                logText.text = getString(R.string.check_device)
                logText.alpha = 0.75f
                logText.animate().alpha(1f).setDuration(180).start()
                if (selectedDevice.isNotEmpty() || Utils.checkSupport(context)) {
                    launchKnobs()
                } else {
                    activity?.supportFragmentManager?.beginTransaction()
                        ?.replace(R.id.mainFrame, IncompatibleFragment())
                        ?.commit()
                }
            } else {
                logText.text = getString(R.string.root_denied) + " · tap to retry"
                progressBar.visibility = View.INVISIBLE
                statusCard.setOnClickListener {
                    parentFragmentManager.beginTransaction()
                        .replace(R.id.mainFrame, LaunchFragment())
                        .commit()
                }
            }
        }
        return view
    }

    private fun applyTheme(view: View, logText: TextView, progressBar: ProgressBar) {
        val theme = AppThemes.current(view.context)
        view.setBackgroundColor(theme.backgroundColor)
        view.findViewById<LinearLayout>(R.id.statusCard).background = GradientDrawable().apply {
            cornerRadius = 32f
            setColor(theme.surfaceColor)
        }
        view.findViewById<ImageView>(R.id.logoImage).imageTintList = ColorStateList.valueOf(theme.accentColor)
        view.findViewById<TextView>(R.id.brandText).setTextColor(theme.mutedColor)
        view.findViewById<TextView>(R.id.helperText).setTextColor(theme.mutedColor)
        view.findViewById<View>(R.id.frameLayout).setBackgroundColor(theme.progressSecondaryColor)
        logText.setTextColor(theme.labelColor)
        progressBar.indeterminateTintList = ColorStateList.valueOf(theme.accentColor)
        progressBar.progressTintList = ColorStateList.valueOf(theme.accentColor)
    }

    fun refreshTheme() {
        val v = view ?: return
        val logText = themeLogText ?: return
        val progressBar = themeProgressBar ?: return
        applyTheme(v, logText, progressBar)
    }

    private fun launchKnobs() {
        val fragment = if (isDualTone) ThreeKnobFragment() else SingleKnobFragment()
        activity?.supportFragmentManager?.beginTransaction()
            ?.replace(R.id.mainFrame, fragment)
            ?.commit()
    }
}
