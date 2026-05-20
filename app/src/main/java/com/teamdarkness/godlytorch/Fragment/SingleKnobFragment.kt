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

package com.teamdarkness.godlytorch.Fragment

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.preference.PreferenceManager
import com.mikepenz.iconics.view.IconicsTextView
import com.sdsmdg.harjot.crollerTest.Croller
import com.sdsmdg.harjot.crollerTest.OnCrollerChangeListener
import com.teamdarkness.godlytorch.R
import com.teamdarkness.godlytorch.Settings.SettingsActivity
import com.teamdarkness.godlytorch.Utils.AppThemes
import com.teamdarkness.godlytorch.Utils.LedController
import com.teamdarkness.godlytorch.Utils.OnFragmentBackPressListener
import com.teamdarkness.godlytorch.Utils.Prefs
import com.teamdarkness.godlytorch.Utils.Utils
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/** Single-LED knob: one master croller controlling a torch with one brightness file. */
class SingleKnobFragment : Fragment(), OnFragmentBackPressListener {

    private var doubleBackToExitPressedOnce = false
    private var masterSingleTap = false
    private var doubleTapEnabled = false
    private var doubleBackJob: Job? = null
    private var singleTapJob: Job? = null

    private var masterValue = 0
    private var masterValueOld = 0
    private var singleLedFileLocation = ""
    private var brightnessMax = 0
    private var masterProgress = 1

    private var themeCroller: Croller? = null
    private var themeSettingsBtn: ImageButton? = null
    private var themeStatusView: TextView? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(R.layout.fragment_single_knob, container, false)

        val masterCroller: Croller = view.findViewById(R.id.masterCroller)
        val settingsButton: ImageButton = view.findViewById(R.id.settingsButton)
        val masterStatus: TextView = view.findViewById(R.id.masterStatus)
        themeCroller = masterCroller
        themeSettingsBtn = settingsButton
        themeStatusView = masterStatus
        applyTheme(view, masterCroller, settingsButton, masterStatus)

        context?.let { ctx ->
            val prefs = PreferenceManager.getDefaultSharedPreferences(ctx)
            singleLedFileLocation = prefs.getString(Prefs.SINGLE_FILE_LOCATION, null) ?: ""
            doubleTapEnabled = prefs.getBoolean(Prefs.DOUBLE_TONE_ENABLED, true)
            brightnessMax = prefs.getInt(Prefs.BRIGHTNESS_MAX, 0)
        }

        settingsButton.setOnClickListener {
            startActivity(Intent(context, SettingsActivity::class.java))
        }

        masterCroller.setOnCrollerChangeListener(object : OnCrollerChangeListener {
            override fun onProgressChanged(croller: Croller?, progress: Int) {
                if (progress == masterProgress) return
                masterValue = if (progress == 1) 0 else
                    ((brightnessMax / 20) * (progress - 1)).coerceAtMost(brightnessMax)
                masterProgress = progress
                pushStatus(masterStatus, progressLabel(progress))
            }

            override fun onTap(croller: Croller?) {
                if (!doubleTapEnabled) return
                if (masterSingleTap) {
                    masterCroller.progress = if (masterProgress > 1) 1 else 20
                }
                masterSingleTap = true
                singleTapJob?.cancel()
                singleTapJob = viewLifecycleOwner.lifecycleScope.launch {
                    delay(300)
                    masterSingleTap = false
                }
            }

            override fun onStartTrackingTouch(croller: Croller?) {
                pushStatus(masterStatus, "Adjusting master level…")
            }

            override fun onStopTrackingTouch(croller: Croller?) {
                KnobTheming.pulse(croller)
                if (masterValue == masterValueOld) {
                    pushStatus(masterStatus, progressLabel(masterProgress))
                    return
                }
                pushStatus(masterStatus, "Applying brightness…")
                if (controlLed(masterValue)) {
                    masterValueOld = masterValue
                    pushStatus(masterStatus, progressLabel(masterProgress), KnobTheming.StatusMode.EMPHASIZE)
                } else {
                    pushStatus(masterStatus, "Write failed · verify root access", KnobTheming.StatusMode.ERROR)
                }
            }
        })

        return view
    }

    fun refreshTheme() {
        val v = view ?: return
        val croller = themeCroller ?: return
        val settingsBtn = themeSettingsBtn ?: return
        val statusView = themeStatusView ?: return
        applyTheme(v, croller, settingsBtn, statusView)
    }

    override fun onBackPressed() {
        if (doubleBackToExitPressedOnce) {
            val dialog = AlertDialog.Builder(requireContext())
                .setTitle("Quit")
                .setMessage("Please wait...")
                .setView(ProgressBar(requireContext()))
                .setCancelable(false)
                .create()
            dialog.show()
            controlLed(0)
            viewLifecycleOwner.lifecycleScope.launch {
                delay(300)
                dialog.dismiss()
                activity?.finishAffinity()
            }
            return
        }
        doubleBackToExitPressedOnce = true
        Toast.makeText(context, "Press BACK again to exit", Toast.LENGTH_SHORT).show()
        doubleBackJob?.cancel()
        doubleBackJob = viewLifecycleOwner.lifecycleScope.launch {
            delay(2000)
            doubleBackToExitPressedOnce = false
        }
    }

    // ---------- Helpers ----------

    private fun applyTheme(view: View, croller: Croller, settingsButton: ImageButton, masterStatus: TextView) {
        val theme = AppThemes.current(view.context)
        view.setBackgroundColor(theme.backgroundColor)
        view.findViewById<IconicsTextView>(R.id.textView)?.apply {
            setTextColor(theme.footerColor)
            text = footerStatus()
        }
        settingsButton.setColorFilter(theme.labelColor)
        KnobTheming.applyStatusPill(masterStatus, theme)
        KnobTheming.applyCroller(croller, theme)
        pushStatus(masterStatus, progressLabel(masterProgress))
    }

    private fun pushStatus(view: TextView, text: String, mode: KnobTheming.StatusMode = KnobTheming.StatusMode.NORMAL) {
        KnobTheming.setStatus(view, text, AppThemes.current(view.context), mode)
    }

    private fun percentFromProgress(progress: Int): Int =
        if (progress <= 1) 0 else ((progress - 1) * 100) / 19

    private fun progressLabel(progress: Int): String {
        val percent = percentFromProgress(progress)
        return if (percent == 0) "Root ready · Master off" else "Root ready · Master $percent%"
    }

    private fun footerStatus(): String {
        val device = Utils.readDevice(requireContext())
        val name = Utils.getDeviceNameById(device?.deviceId).ifBlank { "Device profile needed" }
        val led = if (device?.isDualTone == true) "Dual LED" else "Single LED"
        return "$name · $led · Root ready"
    }

    private fun controlLed(value: Int): Boolean {
        if (singleLedFileLocation.isEmpty()) return false
        if (!Utils.askRoot()) return false
        return LedController.run(LedController.buildOnSingleReset(singleLedFileLocation, value))
    }
}
