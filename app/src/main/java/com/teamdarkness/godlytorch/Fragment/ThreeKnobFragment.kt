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

/** Dual-LED knob: master + per-LED crollers for devices with separate white/yellow torches. */
class ThreeKnobFragment : Fragment(), OnFragmentBackPressListener {

    private var doubleBackToExitPressedOnce = false
    private var whiteSingleTap = false
    private var yellowSingleTap = false
    private var masterSingleTap = false

    private var whiteOn = false
    private var yellowOn = false
    private var doubleTapEnabled = false

    private var yellowValue = 0
    private var whiteValue = 0
    private var yellowValueOld = 0
    private var whiteValueOld = 0

    private var whiteLedFileLocation = ""
    private var yellowLedFileLocation = ""
    private var toggleFileLocation = ""
    private var brightnessMax = 0

    private var yellowProgress = 1
    private var whiteProgress = 1
    private var masterProgress = 1

    private var themeCrollers: List<Croller>? = null
    private var themeSettingsBtn: ImageButton? = null
    private var themeStatusView: TextView? = null

    private var doubleBackJob: Job? = null
    private var whiteSingleTapJob: Job? = null
    private var yellowSingleTapJob: Job? = null
    private var masterSingleTapJob: Job? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(R.layout.fragment_three_knob, container, false)

        val masterCroller: Croller = view.findViewById(R.id.bothCroller)
        val whiteCroller: Croller = view.findViewById(R.id.whiteCroller)
        val yellowCroller: Croller = view.findViewById(R.id.yellowCroller)
        val settingsButton: ImageButton = view.findViewById(R.id.settingsButton)
        val masterStatus: TextView = view.findViewById(R.id.masterStatus)
        themeCrollers = listOf(masterCroller, whiteCroller, yellowCroller)
        themeSettingsBtn = settingsButton
        themeStatusView = masterStatus
        applyTheme(view, themeCrollers!!, settingsButton, masterStatus)

        context?.let { ctx ->
            val prefs = PreferenceManager.getDefaultSharedPreferences(ctx)
            whiteLedFileLocation = prefs.getString(Prefs.WHITE_FILE_LOCATION, null) ?: ""
            yellowLedFileLocation = prefs.getString(Prefs.YELLOW_FILE_LOCATION, null) ?: ""
            toggleFileLocation = prefs.getString(Prefs.TOGGLE_FILE_LOCATION, null) ?: ""
            doubleTapEnabled = prefs.getBoolean(Prefs.DOUBLE_TONE_ENABLED, true)
            brightnessMax = prefs.getInt(Prefs.BRIGHTNESS_MAX, 0)
        }

        settingsButton.setOnClickListener {
            startActivity(Intent(context, SettingsActivity::class.java))
        }

        masterCroller.setOnCrollerChangeListener(object : OnCrollerChangeListener {
            override fun onProgressChanged(croller: Croller?, progress: Int) {
                if (progress == masterProgress) return
                if (progress == 1) {
                    whiteCroller.isEnabled = true
                    yellowCroller.isEnabled = true
                    whiteValue = 0; yellowValue = 0
                    whiteOn = false; yellowOn = false
                } else {
                    whiteCroller.isEnabled = false
                    yellowCroller.isEnabled = false
                    whiteOn = true; yellowOn = true
                    val v = ((brightnessMax / 20) * (progress - 1)).coerceAtMost(brightnessMax)
                    whiteValue = v
                    yellowValue = v
                }
                masterProgress = progress
                pushStatus(masterStatus, progressLabel("Master", progress))
            }

            override fun onTap(croller: Croller?) {
                if (!doubleTapEnabled) return
                if (masterSingleTap) {
                    masterCroller.progress = if (masterProgress > 1) 1 else 20
                }
                masterSingleTap = true
                masterSingleTapJob?.cancel()
                masterSingleTapJob = viewLifecycleOwner.lifecycleScope.launch {
                    delay(300); masterSingleTap = false
                }
            }

            override fun onStartTrackingTouch(croller: Croller?) {
                pushStatus(masterStatus, "Adjusting master level…")
            }

            override fun onStopTrackingTouch(croller: Croller?) {
                KnobTheming.pulse(croller)
                if (whiteValue == whiteValueOld && yellowValue == yellowValueOld) {
                    pushStatus(masterStatus, progressLabel("Master", masterProgress))
                    return
                }
                pushStatus(masterStatus, "Applying brightness…")
                val ok = controlLed(whiteValue, yellowValue, yellowOn)
                if (ok) {
                    whiteValueOld = whiteValue
                    yellowValueOld = yellowValue
                    pushStatus(masterStatus, progressLabel("Master", masterProgress), KnobTheming.StatusMode.EMPHASIZE)
                } else {
                    pushStatus(masterStatus, "Write failed · verify root access", KnobTheming.StatusMode.ERROR)
                }
            }
        })

        whiteCroller.setOnCrollerChangeListener(object : OnCrollerChangeListener {
            override fun onProgressChanged(croller: Croller?, progress: Int) {
                if (progress == whiteProgress) return
                if (progress == 1) {
                    if (yellowCroller.progress == 1) masterCroller.isEnabled = true
                    whiteValue = 0; whiteOn = false
                } else {
                    masterCroller.isEnabled = false
                    whiteOn = true
                    // Preserved: legacy code clamps to 225 (not 255) for per-LED values
                    whiteValue = ((255 / 20) * (progress - 1)).coerceAtMost(225)
                }
                whiteProgress = progress
                pushStatus(masterStatus, progressLabel("White", progress))
            }

            override fun onTap(croller: Croller?) {
                if (!doubleTapEnabled) return
                if (whiteSingleTap) {
                    whiteCroller.progress = if (whiteOn) 1 else 20
                }
                whiteSingleTap = true
                whiteSingleTapJob?.cancel()
                whiteSingleTapJob = viewLifecycleOwner.lifecycleScope.launch {
                    delay(300); whiteSingleTap = false
                }
            }

            override fun onStartTrackingTouch(croller: Croller?) {
                pushStatus(masterStatus, "Adjusting white level…")
            }

            override fun onStopTrackingTouch(croller: Croller?) {
                KnobTheming.pulse(croller)
                if (whiteValue == whiteValueOld) {
                    pushStatus(masterStatus, progressLabel("White", whiteProgress))
                    return
                }
                pushStatus(masterStatus, "Applying brightness…")
                val ok = controlLed(whiteValue, yellowValue, whiteOn || yellowOn)
                if (ok) {
                    whiteValueOld = whiteValue
                    pushStatus(masterStatus, progressLabel("White", whiteProgress), KnobTheming.StatusMode.EMPHASIZE)
                } else {
                    pushStatus(masterStatus, "Write failed · verify root access", KnobTheming.StatusMode.ERROR)
                }
            }
        })

        yellowCroller.setOnCrollerChangeListener(object : OnCrollerChangeListener {
            override fun onProgressChanged(croller: Croller?, progress: Int) {
                if (progress == yellowProgress) return
                if (progress == 1) {
                    if (whiteCroller.progress == 1) masterCroller.isEnabled = true
                    yellowValue = 0; yellowOn = false
                } else {
                    masterCroller.isEnabled = false
                    yellowOn = true
                    yellowValue = ((255 / 20) * (progress - 1)).coerceAtMost(225)
                }
                yellowProgress = progress
                pushStatus(masterStatus, progressLabel("Yellow", progress))
            }

            override fun onTap(croller: Croller?) {
                if (!doubleTapEnabled) return
                if (yellowSingleTap) {
                    yellowCroller.progress = if (yellowOn) 1 else 20
                }
                yellowSingleTap = true
                yellowSingleTapJob?.cancel()
                yellowSingleTapJob = viewLifecycleOwner.lifecycleScope.launch {
                    delay(300); yellowSingleTap = false
                }
            }

            override fun onStartTrackingTouch(croller: Croller?) {
                pushStatus(masterStatus, "Adjusting yellow level…")
            }

            override fun onStopTrackingTouch(croller: Croller?) {
                KnobTheming.pulse(croller)
                if (yellowValue == yellowValueOld) {
                    pushStatus(masterStatus, progressLabel("Yellow", yellowProgress))
                    return
                }
                pushStatus(masterStatus, "Applying brightness…")
                val ok = controlLed(whiteValue, yellowValue, whiteOn || yellowOn)
                if (ok) {
                    yellowValueOld = yellowValue
                    pushStatus(masterStatus, progressLabel("Yellow", yellowProgress), KnobTheming.StatusMode.EMPHASIZE)
                } else {
                    pushStatus(masterStatus, "Write failed · verify root access", KnobTheming.StatusMode.ERROR)
                }
            }
        })

        return view
    }

    fun refreshTheme() {
        val v = view ?: return
        val crollers = themeCrollers ?: return
        val settingsBtn = themeSettingsBtn ?: return
        val statusView = themeStatusView ?: return
        applyTheme(v, crollers, settingsBtn, statusView)
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
            controlLed(0, 0, false)
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

    private fun applyTheme(
        view: View,
        crollers: List<Croller>,
        settingsButton: ImageButton,
        masterStatus: TextView
    ) {
        val theme = AppThemes.current(view.context)
        view.setBackgroundColor(theme.backgroundColor)
        view.findViewById<IconicsTextView>(R.id.textView)?.apply {
            setTextColor(theme.footerColor)
            text = footerStatus()
        }
        settingsButton.setColorFilter(theme.labelColor)
        KnobTheming.applyStatusPill(masterStatus, theme)
        crollers.forEach { KnobTheming.applyCroller(it, theme) }
        pushStatus(masterStatus, progressLabel("Master", masterProgress))
    }

    private fun pushStatus(view: TextView, text: String, mode: KnobTheming.StatusMode = KnobTheming.StatusMode.NORMAL) {
        KnobTheming.setStatus(view, text, AppThemes.current(view.context), mode)
    }

    private fun masterPercent(progress: Int): Int =
        if (progress <= 1) 0 else ((progress - 1) * 100) / 19

    private fun progressLabel(name: String, progress: Int): String {
        val percent = masterPercent(progress)
        return if (percent == 0) "Root ready · $name off" else "Root ready · $name $percent%"
    }

    private fun footerStatus(): String {
        val device = Utils.readDevice(requireContext())
        val name = Utils.getDeviceNameById(device?.deviceId).ifBlank { "Device profile needed" }
        val led = if (device?.isDualTone == true) "Dual LED" else "Single LED"
        return "$name · $led · Root ready"
    }

    private fun controlLed(whiteLed: Int, yellowLed: Int, torchState: Boolean): Boolean {
        if (whiteLedFileLocation.isEmpty() || yellowLedFileLocation.isEmpty() || toggleFileLocation.isEmpty())
            return false
        if (!Utils.askRoot()) return false
        val torch = if (torchState) brightnessMax else 0
        return LedController.run(
            LedController.buildOnDualReset(
                white = whiteLedFileLocation,
                yellow = yellowLedFileLocation,
                toggle = toggleFileLocation,
                whiteVal = whiteLed,
                yellowVal = yellowLed,
                masterVal = torch
            )
        )
    }
}
