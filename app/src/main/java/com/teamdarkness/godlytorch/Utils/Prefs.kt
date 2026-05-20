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

/**
 * SharedPreferences keys. Values must stay byte-stable across releases to
 * preserve user settings across upgrades — do not rename the string literals.
 */
object Prefs {
    // Device profile
    const val SELECTED_DEVICE = "selectedDevice"
    const val IS_DUAL_TONE = "deviceDualTone"
    const val SINGLE_FILE_LOCATION = "singleLedFileLocation"
    const val WHITE_FILE_LOCATION = "whiteLedFileLocation"
    const val YELLOW_FILE_LOCATION = "yellowLedFileLocation"
    const val TOGGLE_FILE_LOCATION = "toggleFileLocation"
    const val BRIGHTNESS_MAX = "brightnessMax"

    // Tile state (persisted so labels survive process death)
    const val TILE_WHITE_ON = "tileWhiteIsOn"
    const val TILE_YELLOW_ON = "tileYellowIsOn"
    const val TILE_MASTER_ON = "tileMasterIsOn"
    const val TILE_WHITE_NAME = "tileWhiteName"
    const val TILE_YELLOW_NAME = "tileYellowName"
    const val TILE_MASTER_NAME = "tileMasterName"
    const val TILE_WHITE_STATE = "tileWhiteState"
    const val TILE_YELLOW_STATE = "tileYellowState"
    const val TILE_MASTER_STATE = "tileMasterState"

    // Behaviour
    const val DOUBLE_TONE_ENABLED = "doubleTapKnob"
    const val USE_INTERNAL_BUSYBOX = "useInternalBusybox"
    const val APP_THEME = "appTheme"
    const val TILE_BEHAVIOUR = "tileBehaviour"
    const val TOGGLE_INTENSITY = "toggleIntensity"
    const val INTENSITY_STEPS = "intensitySteps"
}
