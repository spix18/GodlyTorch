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

import com.teamdarkness.godlytorch.Utils.Device
import com.teamdarkness.godlytorch.Utils.LedController
import com.teamdarkness.godlytorch.Utils.Prefs

/** Yellow-only torch (dual-tone devices). */
class YellowTileService : TorchTileBase() {
    override val tileStatusKey = "yellowTileStatus"
    override val tileOnKey = Prefs.TILE_YELLOW_ON
    override val tileNameKey = Prefs.TILE_YELLOW_NAME
    override val tileStateKey = Prefs.TILE_YELLOW_STATE
    override val defaultLabel = "Yellow Torch"
    override val supportsSingleTone = false
    override val mutexes = listOf(
        Mutex(Prefs.TILE_WHITE_ON, "White Torch tile is active. You can use one tile at a time only."),
        Mutex(Prefs.TILE_MASTER_ON, "Master Control tile is active. You can use one tile at a time only.")
    )

    override fun buildToggleOn(device: Device, toggleIntensityPct: Int, brightnessMax: Int): String {
        val torchVal = (brightnessMax * toggleIntensityPct) / 100
        // Historical: toggle path skips the toggle-reset prefix that the cycle path uses.
        return LedController.buildOnDualDirect(
            white = device.whiteLedFileLocation,
            yellow = device.yellowLedFileLocation,
            toggle = device.toggleFileLocation,
            whiteVal = 0,
            yellowVal = torchVal,
            masterVal = brightnessMax
        )
    }

    override fun buildCycleOn(device: Device, percentage: Int, brightnessMax: Int): String =
        LedController.buildOnDualReset(
            white = device.whiteLedFileLocation,
            yellow = device.yellowLedFileLocation,
            toggle = device.toggleFileLocation,
            whiteVal = 0,
            yellowVal = percentage,
            masterVal = brightnessMax
        )
}
