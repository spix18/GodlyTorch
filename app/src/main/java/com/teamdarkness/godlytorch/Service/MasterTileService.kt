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

/** Master torch — lights both LEDs equally on dual-tone, or the single LED on single-tone. */
class MasterTileService : TorchTileBase() {
    override val tileStatusKey = "masterTileStatus"
    override val tileOnKey = Prefs.TILE_MASTER_ON
    override val tileNameKey = Prefs.TILE_MASTER_NAME
    override val tileStateKey = Prefs.TILE_MASTER_STATE
    override val defaultLabel = "Torch"
    override val supportsSingleTone = true
    override val mutexes = listOf(
        Mutex(Prefs.TILE_WHITE_ON, "White Torch tile is active. You can use one tile at a time only."),
        Mutex(Prefs.TILE_YELLOW_ON, "Yellow Control tile is active. You can use one tile at a time only.")
    )

    override fun buildToggleOn(device: Device, toggleIntensityPct: Int, brightnessMax: Int): String {
        return if (device.isDualTone) {
            val torchVal = (brightnessMax * toggleIntensityPct) / 100
            LedController.buildOnDualReset(
                white = device.whiteLedFileLocation,
                yellow = device.yellowLedFileLocation,
                toggle = device.toggleFileLocation,
                whiteVal = torchVal,
                yellowVal = torchVal,
                masterVal = brightnessMax
            )
        } else {
            // Preserved historical formula. Likely intended `/ 100` but the legacy code
            // divides by brightnessMax — keep it byte-stable until separately reviewed.
            val torchVal = if (brightnessMax > 0) (brightnessMax * toggleIntensityPct) / brightnessMax else 0
            LedController.buildOnSingleReset(device.singleLedFileLocation, torchVal)
        }
    }

    override fun buildCycleOn(device: Device, percentage: Int, brightnessMax: Int): String {
        return if (device.isDualTone) {
            LedController.buildOnDualReset(
                white = device.whiteLedFileLocation,
                yellow = device.yellowLedFileLocation,
                toggle = device.toggleFileLocation,
                whiteVal = percentage,
                yellowVal = percentage,
                masterVal = brightnessMax
            )
        } else {
            LedController.buildOnSingleReset(device.singleLedFileLocation, percentage)
        }
    }
}
