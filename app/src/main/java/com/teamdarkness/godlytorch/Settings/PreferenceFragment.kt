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

package com.teamdarkness.godlytorch.Settings

import android.content.SharedPreferences
import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.PreferenceManager
import androidx.recyclerview.widget.RecyclerView
import com.teamdarkness.godlytorch.R
import com.teamdarkness.godlytorch.Utils.AppThemes
import com.teamdarkness.godlytorch.Utils.Prefs
import com.teamdarkness.godlytorch.Utils.Utils.getDeviceNameById

class PreferenceFragment : PreferenceFragmentCompat(),
    SharedPreferences.OnSharedPreferenceChangeListener {

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.preferences_main, rootKey)
        updatePrefs()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val recyclerView = listView
        recyclerView.setBackgroundColor(AppThemes.current(requireContext()).backgroundColor)
        recyclerView.addOnChildAttachStateChangeListener(object : RecyclerView.OnChildAttachStateChangeListener {
            override fun onChildViewAttachedToWindow(view: View) = tintPreferenceView(view)
            override fun onChildViewDetachedFromWindow(view: View) = Unit
        })
        for (i in 0 until recyclerView.childCount) tintPreferenceView(recyclerView.getChildAt(i))
    }

    override fun onSharedPreferenceChanged(p0: SharedPreferences?, p1: String?) {
        updatePrefs()
        if (p1 == Prefs.APP_THEME) {
            activity?.let { act ->
                AppThemes.applySplashTheme(act)
                (act as? SettingsActivity)?.applyThemeColors()
            }
            retintAllViews()
        }
    }

    override fun onResume() {
        super.onResume()
        preferenceManager.sharedPreferences?.registerOnSharedPreferenceChangeListener(this)
    }

    override fun onPause() {
        super.onPause()
        preferenceManager.sharedPreferences?.unregisterOnSharedPreferenceChangeListener(this)
    }

    private fun updatePrefs() {
        val ctx = activity ?: return
        val prefs = PreferenceManager.getDefaultSharedPreferences(ctx)

        findPreference<androidx.preference.Preference>("selectedDevice")?.let { p ->
            prefs.getString("selectedDevice", null)?.let { p.summary = getDeviceNameById(it) }
        }
        findPreference<androidx.preference.Preference>(Prefs.APP_THEME)?.summary =
            AppThemes.nameFor(prefs.getString(Prefs.APP_THEME, null))
        val toggleIntensity = findPreference<androidx.preference.Preference>("toggleIntensity")
        val intensitySteps = findPreference<androidx.preference.Preference>("intensitySteps")
        findPreference<androidx.preference.Preference>("tileBehaviour")?.let { tile ->
            tile.summary = when (prefs.getString("tileBehaviour", null)) {
                "2" -> {
                    toggleIntensity?.isVisible = false
                    intensitySteps?.isVisible = true
                    "Cycle through saved brightness steps"
                }
                "3" -> {
                    toggleIntensity?.isVisible = false
                    intensitySteps?.isVisible = false
                    "Open quick control popup"
                }
                else -> {
                    toggleIntensity?.isVisible = true
                    intensitySteps?.isVisible = false
                    "Toggle instantly using one brightness level"
                }
            }
        }
        intensitySteps?.summary = when (prefs.getString("intensitySteps", null)) {
            "1" -> "Cycle 2 brightness levels"; "2" -> "Cycle 3 brightness levels"; "3" -> "Cycle 4 brightness levels"
            "4" -> "Cycle 5 brightness levels"; "5" -> "Cycle 6 brightness levels"; else -> "Cycle 6 brightness levels"
        }
        toggleIntensity?.summary = when (prefs.getString("toggleIntensity", null)) {
            "15" -> "Turn on at 15%% brightness"; "35" -> "Turn on at 35%% brightness"; "50" -> "Turn on at 50%% brightness"
            "65" -> "Turn on at 65%% brightness"; "85" -> "Turn on at 85%% brightness"; else -> "Turn on at 100%% brightness"
        }
    }

    private fun tintPreferenceView(view: View) {
        val theme = AppThemes.current(view.context)
        view.setBackgroundColor(theme.backgroundColor)
        val title = view.findViewById<TextView>(android.R.id.title)
        val summary = view.findViewById<TextView>(android.R.id.summary)
        title?.textSize = 16f
        summary?.textSize = 13f
        title?.setTextColor(if (summary == null) theme.accentColor else theme.labelColor)
        summary?.setTextColor(theme.footerColor)
    }

    private fun retintAllViews() {
        val recyclerView = listView
        recyclerView.setBackgroundColor(AppThemes.current(requireContext()).backgroundColor)
        for (i in 0 until recyclerView.childCount) {
            tintPreferenceView(recyclerView.getChildAt(i))
        }
    }
}
