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

package com.teamdarkness.godlytorch.Service

import android.annotation.TargetApi
import android.os.Build
import android.service.quicksettings.Tile
import android.service.quicksettings.TileService
import androidx.preference.PreferenceManager
import com.teamdarkness.godlytorch.Dialog.TileDialog
import com.teamdarkness.godlytorch.Utils.Device
import com.teamdarkness.godlytorch.Utils.LedController
import com.teamdarkness.godlytorch.Utils.Prefs
import com.teamdarkness.godlytorch.Utils.Utils

/**
 * Shared scaffolding for the three QS tiles (White / Yellow / Master). Encodes the
 * device-selected / mutex / root checks and the two tile behaviours (toggle, N-step
 * cycle) once; subclasses describe their identity and how to build the per-tile
 * on/off shell command.
 *
 * Behaviour preserved verbatim from the original 400-line-per-file copies — including
 * the irregular "clamp pct>90 to 100" that only fires at cycle steps 3 and 6, the
 * cycle off-trigger `tileStatus == intensitySteps + 1` that's checked only at cases
 * 2..5, and the unconditional off at case 6.
 */
@TargetApi(Build.VERSION_CODES.N)
abstract class TorchTileBase : TileService() {

    // ---------- Per-tile identity (subclasses fill in) ----------

    protected abstract val tileStatusKey: String
    protected abstract val tileOnKey: String
    protected abstract val tileNameKey: String
    protected abstract val tileStateKey: String
    protected abstract val defaultLabel: String
    /** Other tiles' ON prefs to check before activating this one. */
    protected abstract val mutexes: List<Mutex>
    /** White and Yellow are dual-tone only; Master accepts both. */
    protected abstract val supportsSingleTone: Boolean

    // ---------- Subclass-supplied command builders ----------

    /**
     * Command for the toggle behaviour ("on" only — "off" always uses [LedController.buildOff]).
     * @param toggleIntensityPct 0..100 from the user preference.
     */
    protected abstract fun buildToggleOn(device: Device, toggleIntensityPct: Int, brightnessMax: Int): String

    /**
     * Command for the cycle behaviour ("on" only). [percentage] is the raw cycle
     * percentage that gets written directly as the LED value (not scaled by max).
     */
    protected abstract fun buildCycleOn(device: Device, percentage: Int, brightnessMax: Int): String

    data class Mutex(val onKey: String, val message: String)

    // ---------- Tile lifecycle ----------

    final override fun onStartListening() {
        super.onStartListening()
        val prefs = PreferenceManager.getDefaultSharedPreferences(applicationContext)
        val device = prefs.getString(Prefs.SELECTED_DEVICE, "") ?: ""
        val isDual = prefs.getBoolean(Prefs.IS_DUAL_TONE, true)

        if (device.isEmpty() || (!supportsSingleTone && !isDual)) {
            qsTile.label = "$defaultLabel Unsupported"
            qsTile.state = Tile.STATE_UNAVAILABLE
        } else {
            qsTile.label = prefs.getString(tileNameKey, defaultLabel) ?: defaultLabel
            qsTile.state = prefs.getInt(tileStateKey, Tile.STATE_INACTIVE)
        }
        qsTile.updateTile()
    }

    final override fun onClick() {
        super.onClick()
        updateTile()
    }

    private fun updateTile() {
        val prefs = PreferenceManager.getDefaultSharedPreferences(applicationContext)
        val selectedDevice = prefs.getString(Prefs.SELECTED_DEVICE, "") ?: ""

        if (selectedDevice.isEmpty()) {
            showDialog(TileDialog.getDialog(
                this, "Godly Torch",
                "Your device is not set or supported yet. Open the app to request support."
            ))
            return
        }

        for (m in mutexes) {
            if (prefs.getBoolean(m.onKey, false)) {
                showDialog(TileDialog.getDialog(this, "Godly Torch", m.message))
                return
            }
        }

        val tile = qsTile
        tile.state = Tile.STATE_UNAVAILABLE
        tile.updateTile()

        if (!Utils.askRoot()) {
            if (!isLocked) {
                showDialog(TileDialog.getDialog(
                    this, "Godly Torch",
                    "Root access is required to run this app. " +
                            "Make sure your device is rooted and root access is enabled."
                ))
            }
            tile.state = Tile.STATE_INACTIVE
            tile.updateTile()
            return
        }

        val device = Utils.readDevice(baseContext) ?: return
        if (!supportsSingleTone && !device.isDualTone) return

        val brightnessMax = prefs.getInt(Prefs.BRIGHTNESS_MAX, 0)
        val tileStatus = prefs.getInt(tileStatusKey, 0)

        when (prefs.getString(Prefs.TILE_BEHAVIOUR, "1")) {
            "2" -> handleCycle(prefs, device, brightnessMax, tileStatus, tile)
            else -> handleToggle(prefs, device, brightnessMax, tileStatus, tile)
        }
    }

    // ---------- Behaviours ----------

    private fun handleToggle(
        prefs: android.content.SharedPreferences,
        device: Device,
        brightnessMax: Int,
        tileStatus: Int,
        tile: Tile
    ) {
        if (tileStatus > 0) {
            LedController.fire(LedController.buildOff(device))
            persist(prefs, status = 0, on = false, name = defaultLabel, state = Tile.STATE_INACTIVE)
            applyTile(tile, defaultLabel, Tile.STATE_INACTIVE)
            return
        }
        val toggleIntensity = prefs.getString(Prefs.TOGGLE_INTENSITY, "100")?.toIntOrNull() ?: 100
        LedController.fire(buildToggleOn(device, toggleIntensity, brightnessMax))
        persist(prefs, status = 1, on = true, name = defaultLabel, state = Tile.STATE_ACTIVE)
        applyTile(tile, defaultLabel, Tile.STATE_ACTIVE)
    }

    private fun handleCycle(
        prefs: android.content.SharedPreferences,
        device: Device,
        brightnessMax: Int,
        tileStatus: Int,
        tile: Tile
    ) {
        val steps = prefs.getString(Prefs.INTENSITY_STEPS, "5")?.toIntOrNull() ?: 5

        // Off triggers (preserved exactly from original case statements):
        //  - tileStatus 6 is the always-off sentinel
        //  - tileStatus 2..5 turns off when it matches the configured "intensitySteps + 1"
        val shouldOff = tileStatus == 6 ||
                (tileStatus in 2..5 && tileStatus == steps + 1)

        if (shouldOff) {
            LedController.fire(LedController.buildOff(device))
            persist(prefs, status = 0, on = false, name = defaultLabel, state = Tile.STATE_INACTIVE)
            applyTile(tile, defaultLabel, Tile.STATE_INACTIVE)
            return
        }

        val nextStep = tileStatus + 1
        val stepSize = 100 / (steps + 1)
        var pct = stepSize * nextStep
        // Preserved historical clamp: only fires on cycle cases 2 and 5 in the
        // original (which compute step 3 and step 6 respectively).
        if ((nextStep == 3 || nextStep == 6) && pct > 90) pct = 100

        LedController.fire(buildCycleOn(device, pct, brightnessMax))
        persist(prefs, status = nextStep, on = true, name = "$pct%", state = Tile.STATE_ACTIVE)
        applyTile(tile, "$pct%", Tile.STATE_ACTIVE)
    }

    // ---------- State helpers ----------

    private fun persist(
        prefs: android.content.SharedPreferences,
        status: Int,
        on: Boolean,
        name: String,
        state: Int
    ) {
        prefs.edit().apply {
            putInt(tileStatusKey, status)
            putBoolean(tileOnKey, on)
            putString(tileNameKey, name)
            putInt(tileStateKey, state)
            apply()
        }
    }

    private fun applyTile(tile: Tile, label: String, state: Int) {
        tile.label = label
        tile.state = state
        tile.updateTile()
    }
}
