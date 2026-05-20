/*
 * This file is part of Godly Torch.
 */

package com.teamdarkness.godlytorch.Settings

import android.content.Context
import android.util.AttributeSet
import androidx.appcompat.app.AlertDialog
import androidx.preference.Preference
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.teamdarkness.godlytorch.Utils.DeviceList
import com.teamdarkness.godlytorch.Utils.Utils.getDevicePositionById

class SelectDevicePreference(context: Context, attrs: AttributeSet?) : Preference(context, attrs) {

    init {
        // Force the click action to show our custom dialog
        setOnPreferenceClickListener { showDeviceDialog(); true }
    }

    private fun showDeviceDialog() {
        val recycler = RecyclerView(context).apply {
            layoutManager = LinearLayoutManager(context)
            adapter = DeviceListAdapter(context)
            setPadding(24, 24, 24, 24)
        }
        val dialog = AlertDialog.Builder(context)
            .setTitle("Change Device")
            .setView(recycler)
            .setNegativeButton(android.R.string.cancel, null)
            .create()
        getPersistedString(null)?.let {
            recycler.smoothScrollToPosition(getDevicePositionById(it))
        }
        // Refresh adapter list (devices are static but we may need it)
        DeviceList.getDevices()
        dialog.show()
    }
}
