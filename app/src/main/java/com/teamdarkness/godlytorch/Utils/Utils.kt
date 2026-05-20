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

import android.content.Context
import android.os.Build
import android.text.Html
import android.text.Spanned
import android.util.Log
import androidx.preference.PreferenceManager

import com.topjohnwu.superuser.Shell
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream
import java.util.Locale

object Utils {

    /** Cached root shell handle (initialized lazily, reused across writes). */
    fun askRoot(): Boolean = Shell.getShell().isRoot

    /** Fire-and-forget root command. Use only when result not needed. */
    fun runCommand(cmd: String) {
        Shell.cmd(cmd).submit()
    }

    /** Synchronous root command returning stdout lines. */
    fun runCommandSync(cmd: String): List<String> = Shell.cmd(cmd).exec().out

    private fun prefs(context: Context) = PreferenceManager.getDefaultSharedPreferences(context)

    private fun readMaxBrightness(ledFile: String, context: Context?): String {
        if (context == null || ledFile.isBlank()) return ""
        val maxFile = ledFile.replace("brightness", "max_brightness")
        val out = runCommandSync("cat /sys/class/leds/$maxFile")
        out.firstOrNull { it.isNotBlank() }?.let { return it }

        // Fallback to bundled busybox
        copyBusyBox(context)
        prefs(context).edit().putBoolean(Prefs.USE_INTERNAL_BUSYBOX, true).apply()
        val bb = "${context.filesDir?.absolutePath}/busybox"
        val out2 = runCommandSync("$bb cat /sys/class/leds/$maxFile")
        return out2.firstOrNull { it.isNotBlank() } ?: ""
    }

    /** Match the running device against [DeviceList] by deviceId or aliases. */
    fun checkSupport(context: Context?): Boolean {
        if (context == null) return false
        val buildDev = Build.DEVICE.lowercase(Locale.ROOT)
        val match = DeviceList.getDevices().firstOrNull {
            val id = it.deviceId.lowercase(Locale.ROOT)
            id == buildDev || id.contains(buildDev) || buildDev.contains(id) ||
                    it.aliases.any { a -> a.lowercase(Locale.ROOT) == buildDev }
        } ?: return false
        selectDevice(context, match)
        return true
    }

    fun readDevice(context: Context): Device? {
        val p = prefs(context)
        if (p.getString(Prefs.SELECTED_DEVICE, null) == null) return null
        return Device().apply {
            deviceId = p.getString(Prefs.SELECTED_DEVICE, "") ?: ""
            isDualTone = p.getBoolean(Prefs.IS_DUAL_TONE, false)
            whiteLedFileLocation = p.getString(Prefs.WHITE_FILE_LOCATION, "") ?: ""
            yellowLedFileLocation = p.getString(Prefs.YELLOW_FILE_LOCATION, "") ?: ""
            toggleFileLocation = p.getString(Prefs.TOGGLE_FILE_LOCATION, "") ?: ""
            singleLedFileLocation = p.getString(Prefs.SINGLE_FILE_LOCATION, "") ?: ""
        }
    }

    fun selectDevice(context: Context?, device: Device?): Boolean {
        if (context == null || device == null) return false
        return runCatching {
            prefs(context).edit().apply {
                putString(Prefs.SELECTED_DEVICE, device.deviceId)
                putBoolean(Prefs.IS_DUAL_TONE, device.isDualTone)
                if (device.isDualTone) {
                    putString(Prefs.WHITE_FILE_LOCATION, device.whiteLedFileLocation)
                    putString(Prefs.YELLOW_FILE_LOCATION, device.yellowLedFileLocation)
                    putString(Prefs.TOGGLE_FILE_LOCATION, device.toggleFileLocation)
                    putInt(Prefs.BRIGHTNESS_MAX, readMaxBrightness(device.whiteLedFileLocation, context).toIntOrNull() ?: 0)
                } else {
                    putString(Prefs.SINGLE_FILE_LOCATION, device.singleLedFileLocation)
                    putInt(Prefs.BRIGHTNESS_MAX, readMaxBrightness(device.singleLedFileLocation, context).toIntOrNull() ?: 0)
                    putString(Prefs.WHITE_FILE_LOCATION, "")
                    putString(Prefs.YELLOW_FILE_LOCATION, "")
                    putString(Prefs.TOGGLE_FILE_LOCATION, "")
                }
                apply()
            }
        }.isSuccess
    }

    fun getDeviceId(): String = Build.DEVICE
    fun getDeviceName(): String = Build.MODEL

    fun fromHtml(source: String): Spanned = Html.fromHtml(source, Html.FROM_HTML_MODE_LEGACY)

    fun getSingleFileLocationById(deviceId: String): String? =
        DeviceList.getDevices().firstOrNull { it.deviceId == deviceId }?.singleLedFileLocation

    fun getWhiteLedFileLocationById(deviceId: String): String? =
        DeviceList.getDevices().firstOrNull { it.deviceId == deviceId }?.whiteLedFileLocation

    fun getYellowLedFileLocationById(deviceId: String): String? =
        DeviceList.getDevices().firstOrNull { it.deviceId == deviceId }?.yellowLedFileLocation

    fun getToggleFileLocationById(deviceId: String): String? =
        DeviceList.getDevices().firstOrNull { it.deviceId == deviceId }?.toggleFileLocation

    fun getDeviceNameById(deviceId: String?): String =
        DeviceList.getDevices().firstOrNull { it.deviceId == deviceId }?.deviceName ?: ""

    fun getDevicePositionById(deviceId: String?): Int =
        DeviceList.getDevices().indexOfFirst { it.deviceId == deviceId }.coerceAtLeast(0)

    fun getDeviceById(deviceId: String): Device? =
        DeviceList.getDevices().firstOrNull { it.deviceId == deviceId }

    fun copyBusyBox(context: Context?) {
        context ?: return
        try {
            val outFile = File(context.filesDir?.absolutePath, "busybox")
            context.assets.open("binary/busybox").use { input ->
                FileOutputStream(outFile).use { output -> copyFile(input, output, context) }
            }
        } catch (e: Exception) {
            Log.e("Utils", "Failed to copy busybox", e)
        }
    }

    @Throws(IOException::class)
    private fun copyFile(inStream: InputStream, out: OutputStream, context: Context?) {
        val buffer = ByteArray(8192)
        var read = inStream.read(buffer)
        while (read != -1) {
            out.write(buffer, 0, read)
            read = inStream.read(buffer)
        }
        out.flush()
        File(context?.filesDir?.absolutePath + "/busybox").setExecutable(true)
    }
}
