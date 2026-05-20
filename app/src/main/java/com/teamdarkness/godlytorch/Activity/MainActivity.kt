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

package com.teamdarkness.godlytorch.Activity

import android.os.Bundle
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding
import androidx.preference.PreferenceManager
import com.teamdarkness.godlytorch.Fragment.IncompatibleFragment
import com.teamdarkness.godlytorch.Fragment.LaunchFragment
import com.teamdarkness.godlytorch.Fragment.SingleKnobFragment
import com.teamdarkness.godlytorch.Fragment.ThreeKnobFragment
import com.teamdarkness.godlytorch.R
import com.teamdarkness.godlytorch.Utils.AppThemes
import com.teamdarkness.godlytorch.Utils.Prefs
import com.teamdarkness.godlytorch.Utils.OnFragmentBackPressListener
import com.teamdarkness.godlytorch.Utils.ThemeApplier
import com.teamdarkness.godlytorch.Utils.Utils

class MainActivity : AppCompatActivity() {

    private var selectedDevice = ""
    private var isDualTone = true
    private var paused = false

    override fun onCreate(savedInstanceState: Bundle?) {
        setTheme(AppThemes.activityThemeRes(this))
        // Persist current theme as next cold-start splash (API 31+).
        AppThemes.applySplashTheme(this)
        // Edge-to-edge: required by Android 15 (API 35) for new apps
        WindowCompat.setDecorFitsSystemWindows(window, false)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        applyThemeColors()

        // Pad the root container so content respects status + nav bar insets (edge-to-edge).
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.mainFrame)) { v, insets ->
            val bars = insets.getInsets(WindowInsetsCompat.Type.systemBars() or WindowInsetsCompat.Type.displayCutout())
            v.updatePadding(left = bars.left, top = bars.top, right = bars.right, bottom = bars.bottom)
            WindowInsetsCompat.CONSUMED
        }

        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                .replace(R.id.mainFrame, LaunchFragment())
                .commit()
        }

        // Replace deprecated onBackPressed override (predictive back / API 33+)
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                val fragmentList = supportFragmentManager.fragments
                fragmentList.filterIsInstance<OnFragmentBackPressListener>().forEach { it.onBackPressed() }
                if (fragmentList.none { it is OnFragmentBackPressListener }) {
                    isEnabled = false
                    onBackPressedDispatcher.onBackPressed()
                }
            }
        })
    }

    override fun onPause() {
        super.onPause()
        paused = true
    }

    override fun onResume() {
        super.onResume()
        applyThemeColors()
        if (paused) {
            paused = false
            val prefs = PreferenceManager.getDefaultSharedPreferences(this)
            selectedDevice = prefs.getString(Prefs.SELECTED_DEVICE, "") ?: ""
            isDualTone = prefs.getBoolean(Prefs.IS_DUAL_TONE, true)

            if (selectedDevice.isNotEmpty()) {
                launchKnobs()
            } else if (Utils.checkSupport(this)) {
                launchKnobs()
            } else {
                supportFragmentManager.beginTransaction()
                    .replace(R.id.mainFrame, IncompatibleFragment())
                    .commit()
            }
        }
        // Refresh current fragment theme without restarting the activity.
        val currentFragment = supportFragmentManager.findFragmentById(R.id.mainFrame)
        when (currentFragment) {
            is ThreeKnobFragment -> currentFragment.refreshTheme()
            is SingleKnobFragment -> currentFragment.refreshTheme()
            is IncompatibleFragment -> currentFragment.refreshTheme()
            is LaunchFragment -> currentFragment.refreshTheme()
        }
    }

    private fun launchKnobs() {
        val fragment = if (isDualTone) ThreeKnobFragment() else SingleKnobFragment()
        supportFragmentManager.beginTransaction()
            .replace(R.id.mainFrame, fragment)
            .commit()
    }

    private fun applyThemeColors() {
        val theme = AppThemes.current(this)
        findViewById<android.view.View>(R.id.rootContainer)?.setBackgroundColor(theme.backgroundColor)
        findViewById<android.view.View>(R.id.mainFrame)?.setBackgroundColor(theme.backgroundColor)
        ThemeApplier.applySystemBars(window, theme)
    }
}
