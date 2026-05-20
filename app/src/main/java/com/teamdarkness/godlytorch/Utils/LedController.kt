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

import com.topjohnwu.superuser.Shell

/**
 * Single source of truth for building and dispatching the `echo N > /sys/class/leds/...`
 * commands that drive the torch hardware. Centralised so fragments, tile services and
 * any future surface share the same wire format.
 *
 * Two layers:
 *  - low-level [echo] / [SLEEP] primitives for call sites that need full control over
 *    the exact command sequence (tile services preserve subtle pre-reset quirks);
 *  - high-level [buildOff] / [knobApply] / [cycleApply] for fragments that just want
 *    "turn this device off" or "set this brightness".
 *
 * Dispatched in one `Shell.cmd(...)` call so the kernel sees writes in order without
 * re-forking the root shell.
 */
object LedController {

    private const val ECHO = "echo %1\$s > /sys/class/leds/%2\$s;"
    const val SLEEP = "sleep 0.01;"

    /** Low-level primitive: one `echo VALUE > /sys/class/leds/FILE;` line. */
    fun echo(value: Any, file: String): String = String.format(ECHO, value.toString(), file)

    // ---------- Composable command builders ----------

    /** Zero all three sysfs nodes that define a dual-tone torch. */
    fun buildOffDual(white: String, yellow: String, toggle: String): String =
        echo(0, white) + echo(0, yellow) + echo(0, toggle)

    /** Zero the single sysfs node that defines a single-LED torch. */
    fun buildOffSingle(single: String): String = echo(0, single)

    /** Off command, dispatched by [Device.isDualTone]. */
    fun buildOff(device: Device): String =
        if (device.isDualTone) buildOffDual(device.whiteLedFileLocation, device.yellowLedFileLocation, device.toggleFileLocation)
        else buildOffSingle(device.singleLedFileLocation)

    /**
     * Reset-then-set sequence for a dual-tone torch:
     * zero the toggle, brief sleep so the controller registers a clean edge, then
     * write the LED values and finally raise the toggle.
     */
    fun buildOnDualReset(
        white: String, yellow: String, toggle: String,
        whiteVal: Int, yellowVal: Int, masterVal: Int
    ): String = echo(0, toggle) + SLEEP +
            echo(whiteVal, white) +
            echo(yellowVal, yellow) +
            echo(masterVal, toggle)

    /** Reset-then-set sequence for a single-LED torch. */
    fun buildOnSingleReset(single: String, value: Int): String =
        echo(0, single) + SLEEP + echo(value, single)

    /**
     * Drop straight to a value without resetting. Used by the white/yellow toggle
     * tile paths where the original code intentionally skipped the reset prefix.
     */
    fun buildOnDualDirect(
        white: String, yellow: String, toggle: String,
        whiteVal: Int, yellowVal: Int, masterVal: Int
    ): String = echo(whiteVal, white) + echo(yellowVal, yellow) + echo(masterVal, toggle)

    // ---------- Dispatch ----------

    /** Fire-and-forget. Use from tile services or any surface that doesn't need a result. */
    fun fire(command: String) {
        Shell.cmd(command).submit()
    }

    /** Blocking dispatch. Returns true on shell exit code 0. */
    fun run(command: String): Boolean =
        runCatching { Shell.cmd(command).exec().isSuccess }.getOrDefault(false)
}
