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

package com.teamdarkness.godlytorch.Utils

import android.app.Activity
import android.content.Context
import android.graphics.Color
import android.os.Build
import androidx.preference.PreferenceManager
import com.teamdarkness.godlytorch.R

data class AppTheme(
    val id: String,
    val name: String,
    val hueFamily: String,
    val backgroundColor: Int,
    val labelColor: Int,
    val footerColor: Int,
    val accentColor: Int,
    val indicatorColor: Int,
    val progressColor: Int,
    val progressSecondaryColor: Int,
    val mainCircleColor: Int,
    val backCircleColor: Int,
    val surfaceColor: Int,
    val mutedColor: Int,
    val successColor: Int,
    val warningColor: Int,
    val lightSystemBars: Boolean
)

object AppThemes {
    val cardinal = AppTheme(
        id = "cardinal",
        name = "Cardinal",
        hueFamily = "Red",
        backgroundColor = Color.parseColor("#E2381E"),
        labelColor = Color.WHITE,
        footerColor = Color.parseColor("#C8FFFFFF"),
        accentColor = Color.parseColor("#FFE240"),
        indicatorColor = Color.parseColor("#0B3C49"),
        progressColor = Color.parseColor("#C2E2EA"),
        progressSecondaryColor = Color.parseColor("#64FFFFFF"),
        mainCircleColor = Color.WHITE,
        backCircleColor = Color.parseColor("#EDEDED"),
        surfaceColor = Color.parseColor("#1AFFFFFF"),
        mutedColor = Color.parseColor("#A8FFFFFF"),
        successColor = Color.parseColor("#B9F4FF"),
        warningColor = Color.parseColor("#FFE240"),
        lightSystemBars = false
    )

    private val themes = listOf(
        cardinal,
        cardinal.copy(
            id = "obsidian",
            name = "Obsidian",
            hueFamily = "Neutral-Dark",
            backgroundColor = Color.parseColor("#151211"),
            labelColor = Color.parseColor("#FFE3D1"),
            footerColor = Color.parseColor("#99FFE3D1"),
            accentColor = Color.parseColor("#FF8A3C"),
            indicatorColor = Color.parseColor("#FF8A3C"),
            progressColor = Color.parseColor("#FFD166"),
            progressSecondaryColor = Color.parseColor("#3DFFFFFF"),
            surfaceColor = Color.parseColor("#14FFFFFF"),
            mutedColor = Color.parseColor("#B08F7F75"),
            successColor = Color.parseColor("#FFD166"),
            warningColor = Color.parseColor("#FF8A3C")
        ),
        cardinal.copy(
            id = "ember",
            name = "Ember",
            hueFamily = "Warm-Brown",
            backgroundColor = Color.parseColor("#3A1B12"),
            labelColor = Color.parseColor("#FFF1D6"),
            footerColor = Color.parseColor("#A8FFF1D6"),
            accentColor = Color.parseColor("#FFB000"),
            indicatorColor = Color.parseColor("#FFC85A"),
            progressColor = Color.parseColor("#FFE0A3"),
            progressSecondaryColor = Color.parseColor("#3DFFFFFF"),
            surfaceColor = Color.parseColor("#18FFFFFF"),
            mutedColor = Color.parseColor("#B8E2BBA8"),
            successColor = Color.parseColor("#FFE0A3"),
            warningColor = Color.parseColor("#FFB000")
        ),
        cardinal.copy(
            id = "polar",
            name = "Polar",
            hueFamily = "Cool-Light",
            backgroundColor = Color.parseColor("#EEF7F8"),
            labelColor = Color.parseColor("#12353B"),
            footerColor = Color.parseColor("#9912353B"),
            accentColor = Color.parseColor("#1E9BB2"),
            indicatorColor = Color.parseColor("#0D5261"),
            progressColor = Color.parseColor("#9FE7F1"),
            progressSecondaryColor = Color.parseColor("#4D0D5261"),
            backCircleColor = Color.parseColor("#DDECEE"),
            surfaceColor = Color.parseColor("#66FFFFFF"),
            mutedColor = Color.parseColor("#7A12353B"),
            successColor = Color.parseColor("#1E9BB2"),
            warningColor = Color.parseColor("#D9A900"),
            lightSystemBars = true
        )
    )

    fun current(context: Context): AppTheme {
        val prefs = PreferenceManager.getDefaultSharedPreferences(context)
        val id = prefs.getString(Prefs.APP_THEME, cardinal.id)
        return themes.firstOrNull { it.id == id } ?: cardinal
    }

    fun nameFor(id: String?) = themes.firstOrNull { it.id == id }?.let { "${it.name}  ·  ${it.hueFamily}" } ?: cardinal.name

    fun activityThemeRes(context: Context): Int = when (current(context).id) {
        "obsidian" -> R.style.AppTheme_Obsidian
        "ember" -> R.style.AppTheme_Ember
        "polar" -> R.style.AppTheme_Polar
        else -> R.style.AppTheme_Cardinal
    }

    /**
     * Persists the chosen theme as the splash screen theme for the NEXT cold start.
     * No-op below API 31. Safe to call from any Activity on every theme change and
     * every Activity.onCreate to keep the persisted splash in sync.
     */
    fun applySplashTheme(activity: Activity) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S) return
        try {
            activity.splashScreen.setSplashScreenTheme(activityThemeRes(activity))
        } catch (_: Throwable) {
        }
    }
}
