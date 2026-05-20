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


import android.app.AlertDialog
import android.content.ActivityNotFoundException
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.os.Build
import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast

import com.teamdarkness.godlytorch.R
import com.teamdarkness.godlytorch.Utils.OnFragmentBackPressListener
import com.teamdarkness.godlytorch.Utils.Utils.fromHtml
import com.teamdarkness.godlytorch.Utils.Utils.getDeviceId
import com.teamdarkness.godlytorch.Utils.Utils.getDeviceName
import android.content.Intent
import android.net.Uri
import androidx.constraintlayout.widget.ConstraintLayout
import com.google.android.material.snackbar.Snackbar
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.teamdarkness.godlytorch.Settings.DeviceListAdapter
import com.teamdarkness.godlytorch.Utils.Prefs
import com.teamdarkness.godlytorch.Utils.Utils
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import com.teamdarkness.godlytorch.Utils.Utils.getDeviceNameById
import com.teamdarkness.godlytorch.Utils.AppThemes
import androidx.preference.PreferenceManager


class IncompatibleFragment : Fragment(), OnFragmentBackPressListener {

    private var doubleBackToExitPressedOnce = false

    private lateinit var root: ConstraintLayout

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_incompatible, container, false)

        val incompatibleText: TextView = view.findViewById(R.id.incompatibleText)
        val contactButton: Button = view.findViewById(R.id.contactButton)
        val btnSelectDevice: Button = view.findViewById(R.id.btnSelectDevice)
        val btnApplyDevice: Button = view.findViewById(R.id.btnApplyDevice)
        val deviceName: TextView = view.findViewById(R.id.deviceName)
        root = view.findViewById(R.id.root)

        val prefs = context?.let { PreferenceManager.getDefaultSharedPreferences(it) }
        val theme = AppThemes.current(view.context)
        root.setBackgroundColor(theme.backgroundColor)
        incompatibleText.setTextColor(theme.labelColor)
        deviceName.setTextColor(theme.labelColor)
        btnSelectDevice.setTextColor(theme.labelColor)
        btnApplyDevice.setTextColor(theme.labelColor)
        contactButton.setTextColor(theme.labelColor)

        contactButton.setOnClickListener {
            val options = arrayOf<CharSequence>("Send device info", "Open Telegram")

            val builder = AlertDialog.Builder(context)
            builder.setTitle(R.string.contact_us)
            builder.setItems(options) { dialog, which ->
                when (which) {
                    0 -> showDeviceInfoPreview()
                    1 -> openLink("https://t.me/Spix3D")
                }
                dialog.dismiss()
            }
            builder.show()
        }

        btnSelectDevice.setOnClickListener {
            val builder = AlertDialog.Builder(context)
            builder.setTitle("Select device profile")
            val deviceView = RecyclerView(it.context)
            deviceView.layoutManager = LinearLayoutManager(context)
            deviceView.adapter = DeviceListAdapter(context)
            builder.setView(deviceView)
            builder.setNegativeButton("Cancel") { dialogInterface, _ ->
                dialogInterface.dismiss()
            }
            builder.setPositiveButton("Select") { dialogInterface, _ ->
                dialogInterface.dismiss()
                val selectedDevice = prefs?.getString(Prefs.SELECTED_DEVICE, "")
                deviceName.text = getDeviceNameById(selectedDevice)
                btnApplyDevice.isEnabled = !selectedDevice.isNullOrEmpty()
            }
            builder.show()
        }

        btnApplyDevice.setOnClickListener {
            val selectedDevice = prefs?.getString(Prefs.SELECTED_DEVICE, "")

            selectedDevice?.let {
                if (selectedDevice.isNotEmpty())
                    launchKnobs()
                else {
                    Snackbar.make(root, "Select a device profile first", Snackbar.LENGTH_SHORT).show()
                    if (Utils.checkSupport(context)) {
                        launchKnobs()
                    } else {
                        val fragTransaction = activity?.supportFragmentManager?.beginTransaction()
                        fragTransaction?.let {
                            fragTransaction.replace(R.id.mainFrame, IncompatibleFragment())
                            fragTransaction.commit()
                        }
                    }
                }
            }
        }

        incompatibleText.text = fromHtml(String.format(getString(R.string.incompatible_message), "${getDeviceName()} (${getDeviceId()})"))
        btnApplyDevice.isEnabled = !(prefs?.getString(Prefs.SELECTED_DEVICE, "").isNullOrEmpty())

        return view
    }

    fun refreshTheme() {
        val theme = AppThemes.current(requireContext())
        root.setBackgroundColor(theme.backgroundColor)
        root.findViewById<TextView>(R.id.incompatibleText)?.setTextColor(theme.labelColor)
        root.findViewById<TextView>(R.id.deviceName)?.setTextColor(theme.labelColor)
        root.findViewById<Button>(R.id.btnSelectDevice)?.setTextColor(theme.labelColor)
        root.findViewById<Button>(R.id.btnApplyDevice)?.setTextColor(theme.labelColor)
        root.findViewById<Button>(R.id.contactButton)?.setTextColor(theme.labelColor)
    }

    override fun onBackPressed() {
        if (doubleBackToExitPressedOnce) {
            activity?.finishAffinity()
            return
        }

        this.doubleBackToExitPressedOnce = true
        Toast.makeText(context, "Press BACK again to exit", Toast.LENGTH_SHORT).show()

        viewLifecycleOwner.lifecycleScope.launch {
            delay(2000)
            doubleBackToExitPressedOnce = false
        }
    }

    private fun buildDeviceInfo(): String {
        return """Device Request for GodlyTorch
|
|Model: ${Build.MODEL}
|Codename: ${Build.DEVICE}
|Manufacturer: ${Build.MANUFACTURER}
|Product: ${Build.PRODUCT}
|Board: ${Build.BOARD}
|Android: ${Build.VERSION.RELEASE} (API ${Build.VERSION.SDK_INT})
""".trimMargin("|")
    }

    private fun showDeviceInfoPreview() {
        val info = buildDeviceInfo()
        val preview = TextView(context).apply {
            text = info
            textSize = 14f
            setPadding(48, 32, 48, 32)
            setTextIsSelectable(true)
        }
        AlertDialog.Builder(context)
            .setTitle("Review device info")
            .setView(preview)
            .setPositiveButton("Copy & open Telegram") { _, _ ->
                copyToClipboard(info)
                openLink("https://t.me/Spix3D")
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun copyToClipboard(text: String) {
        val cm = context?.getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager
        cm?.setPrimaryClip(ClipData.newPlainText("Device info", text))
        Toast.makeText(context, "Device info copied — paste it in Telegram", Toast.LENGTH_LONG).show()
    }

    private fun openLink(link: String) {
        val intent = Intent()
        intent.action = Intent.ACTION_VIEW
        intent.data = Uri.parse(link)
        try {
            startActivity(intent)
        } catch (e: ActivityNotFoundException) {
            Snackbar.make(root, "Please install a browser and try again", Snackbar.LENGTH_SHORT).show()
        }
    }

    private fun launchKnobs() {

        val prefs = context?.let { PreferenceManager.getDefaultSharedPreferences(it) }
        val isDualTone = prefs?.getBoolean(Prefs.IS_DUAL_TONE, true)

        isDualTone?.let {
            if (isDualTone) {
                val fragTransaction = activity?.supportFragmentManager?.beginTransaction()
                fragTransaction?.let {
                    fragTransaction.replace(R.id.mainFrame, ThreeKnobFragment())
                    fragTransaction.commit()
                }
            } else {
                val fragTransaction = activity?.supportFragmentManager?.beginTransaction()
                fragTransaction?.let {
                    fragTransaction.replace(R.id.mainFrame, SingleKnobFragment())
                    fragTransaction.commit()
                }
            }
        }
    }

}// Required empty public constructor
