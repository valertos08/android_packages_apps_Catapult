/*
 * SPDX-FileCopyrightText: 2024 The LineageOS Project
 * SPDX-License-Identifier: Apache-2.0
 */

package org.lineageos.tv.launcher

import android.bluetooth.BluetoothManager
import android.content.Intent
import android.icu.text.DateFormat
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Bundle
import android.provider.Settings
import android.text.Spannable
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import android.view.View
import android.widget.TextView
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.content.ContextCompat
import com.google.android.material.button.MaterialButton
import java.util.Calendar


class SystemOptionsActivity : ModalActivity(R.layout.activity_system_options) {
    // Views
    private val dateTextView by lazy { findViewById<TextView>(R.id.date) }
    private val sleepButton by lazy { findViewById<MaterialButton>(R.id.sleep_button) }
    private val settingsButton by lazy { findViewById<MaterialButton>(R.id.settings_button) }
    private val powerButton by lazy { findViewById<MaterialButton>(R.id.power_button) }
    private val networkButton by lazy { findViewById<MaterialButton>(R.id.network_button) }
    private val bluetoothButton by lazy { findViewById<MaterialButton>(R.id.bluetooth_button) }

    private val colorStateList by lazy {
        ContextCompat.getColorStateList(
            this,
            R.color.system_options_button_content_secondary_tint
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Date
        val currentDate = Calendar.getInstance().time
        dateTextView.text = DateFormat.getPatternInstance(DateFormat.YEAR_ABBR_MONTH_WEEKDAY_DAY)
            .format(currentDate)

        // Wifi & Bluetooth
        setNetworkButton()
        setBluetoothButton()

        settingsButton.setOnClickListener {
            startActivity(SETTINGS)
        }
    }

    private fun setNetworkButton() {
        var networkString = resources.getString(R.string.unknown)
        val connectivityManager = getSystemService(CONNECTIVITY_SERVICE) as ConnectivityManager
        val capabilities =
            connectivityManager.getNetworkCapabilities(connectivityManager.activeNetwork)

        if (capabilities == null
            || !capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
            || !capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)
        ) {
            networkString = resources.getString(R.string.not_connected)
        } else {
            if (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET)) {
                networkButton.icon = AppCompatResources.getDrawable(this, R.drawable.ic_ethernet)
            }

            if (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)) {
                networkString = resources.getString(R.string.enabled)
            }
        }

        val networkSpan =
            SpannableString(resources.getString(R.string.network_status, networkString))
        networkButton.text = updateButton(networkSpan, false)

        networkButton.onFocusChangeListener = View.OnFocusChangeListener { _, hasFocus ->
            networkButton.text = updateButton(networkSpan, hasFocus)
        }

        networkButton.setOnLongClickListener {
            startActivity(WIFI_SETTINGS)
            true
        }
    }

    private fun setBluetoothButton() {
        var btString = resources.getString(R.string.disabled)
        val bluetoothAdapter = getSystemService(BluetoothManager::class.java).adapter
        if (bluetoothAdapter != null && bluetoothAdapter.isEnabled) {
            btString = resources.getString(R.string.enabled)
        }

        val btSpan = SpannableString(resources.getString(R.string.bluetooth_status, btString))
        bluetoothButton.text = updateButton(btSpan, false)

        bluetoothButton.onFocusChangeListener = View.OnFocusChangeListener { _, hasFocus ->
            bluetoothButton.text = updateButton(btSpan, hasFocus)
        }

        bluetoothButton.setOnClickListener {
            startActivity(BLUETOOTH_SETTINGS)
        }
    }

    private fun updateButton(content: SpannableString, hasFocus: Boolean): SpannableString {
        val color = colorStateList?.getColorForState(
            if (hasFocus) intArrayOf(android.R.attr.state_focused) else intArrayOf(),
            colorStateList!!.defaultColor
        ) ?: colorStateList?.defaultColor ?: ContextCompat.getColor(this, R.color.white_disabled)

        content.setSpan(
            ForegroundColorSpan(color),
            content.indexOf("\n"),
            content.length,
            Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
        )

        return content
    }

    companion object {
        val SETTINGS: Intent = Intent(Settings.ACTION_SETTINGS)
        val WIFI_SETTINGS: Intent = Intent(Settings.ACTION_WIFI_SETTINGS)
        val BLUETOOTH_SETTINGS: Intent = Intent(Settings.ACTION_BLUETOOTH_SETTINGS)
    }
}
