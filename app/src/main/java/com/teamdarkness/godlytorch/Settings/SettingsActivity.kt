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

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding
import com.teamdarkness.godlytorch.R
import com.teamdarkness.godlytorch.Utils.AppThemes
import com.teamdarkness.godlytorch.Utils.ThemeApplier

class SettingsActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        WindowCompat.setDecorFitsSystemWindows(window, false)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)
        applyThemeColors()
        val root = findViewById<android.view.View>(android.R.id.content)
        ViewCompat.setOnApplyWindowInsetsListener(root) { v, insets ->
            val bars = insets.getInsets(WindowInsetsCompat.Type.systemBars() or WindowInsetsCompat.Type.displayCutout())
            v.updatePadding(left = bars.left, top = bars.top, right = bars.right, bottom = bars.bottom)
            WindowInsetsCompat.CONSUMED
        }
        val toolbar: Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.title = "Settings"
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        toolbar.navigationIcon?.setTint(AppThemes.current(this).labelColor)
        toolbar.setNavigationOnClickListener { onBackPressedDispatcher.onBackPressed() }

        // androidx PreferenceFragmentCompat uses supportFragmentManager
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, PreferenceFragment())
            .commit()
    }

    fun applyThemeColors() {
        val theme = AppThemes.current(this)
        val toolbar: Toolbar = findViewById(R.id.toolbar)
        toolbar.setBackgroundColor(theme.backgroundColor)
        toolbar.setTitleTextColor(theme.labelColor)
        findViewById<android.view.View>(android.R.id.content).setBackgroundColor(theme.backgroundColor)
        ThemeApplier.applySystemBars(window, theme)
    }
}
