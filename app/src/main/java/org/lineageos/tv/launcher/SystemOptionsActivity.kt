/*
 * SPDX-FileCopyrightText: 2024 The LineageOS Project
 * SPDX-License-Identifier: Apache-2.0
 */

package org.lineageos.tv.launcher

import android.app.ActivityOptions
import android.app.PendingIntent
import android.bluetooth.BluetoothManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.icu.text.DateFormat
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import android.net.wifi.WifiInfo
import android.net.wifi.WifiManager
import android.os.Build
import android.os.Bundle
import android.os.IBinder
import android.provider.Settings
import android.service.notification.StatusBarNotification
import android.text.Spannable
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import android.util.Log
import android.view.KeyEvent
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.content.ContextCompat
import androidx.leanback.widget.VerticalGridView
import com.google.android.material.button.MaterialButton
import org.lineageos.tv.launcher.notification.NotificationAdapter
import org.lineageos.tv.launcher.notification.NotificationUtils
import org.lineageos.tv.launcher.notification.TvNotificationListener
import org.lineageos.tv.launcher.view.NotificationItemView
import java.util.Calendar


class SystemOptionsActivity : ModalActivity(R.layout.activity_system_options),
    NotificationAdapter.OnItemActionListener {
    // Views
    private val dateTextView by lazy { findViewById<TextView>(R.id.date) }
    private val sleepButton by lazy { findViewById<MaterialButton>(R.id.sleep_button) }
    private val settingsButton by lazy { findViewById<MaterialButton>(R.id.settings_button) }
    private val powerButton by lazy { findViewById<MaterialButton>(R.id.power_button) }
    private val networkButton by lazy { findViewById<MaterialButton>(R.id.network_button) }
    private val bluetoothButton by lazy { findViewById<MaterialButton>(R.id.bluetooth_button) }
    private val notificationList by lazy { findViewById<VerticalGridView>(R.id.notification_list) }
    private val noNotifications by lazy { findViewById<TextView>(R.id.no_notifications) }
    private val noNotificationAccess by lazy { findViewById<LinearLayout>(R.id.no_notification_access) }
    private val allowNotificationAccess by lazy { findViewById<MaterialButton>(R.id.allow_notification_access) }

    private val colorStateList by lazy {
        ContextCompat.getColorStateList(
            this,
            R.color.system_options_button_content_secondary_tint
        )
    }

    private var notificationListener: TvNotificationListener? = null
    private val notificationAdapter: NotificationAdapter by lazy { NotificationAdapter(this, this) }
    private var notificationUpdateListener: TvNotificationListener.NotificationUpdateListener? =
        null

    private val connectivityManager by lazy { getSystemService(ConnectivityManager::class.java) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Animate
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            overrideActivityTransition(
                OVERRIDE_TRANSITION_OPEN,
                R.anim.slide_in_right,
                R.anim.slide_out_right
            )
            overrideActivityTransition(
                OVERRIDE_TRANSITION_CLOSE,
                R.anim.slide_in_right,
                R.anim.slide_out_right
            )
        }

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

        allowNotificationAccess.setOnClickListener {
            startActivity(NOTIFICATION_SETTINGS)
        }

        notificationList.adapter = notificationAdapter

        // WIFI callbacks
        val request = NetworkRequest.Builder()
            .addTransportType(NetworkCapabilities.TRANSPORT_WIFI)
            .build()
        connectivityManager.registerNetworkCallback(request, connCallback)
    }

    override fun onStart() {
        super.onStart()
        if (!NotificationUtils.notificationPermissionGranted(this)) {
            noNotificationAccess.visibility = View.VISIBLE
            noNotifications.visibility = View.GONE
            notificationList.visibility = View.GONE
            return
        }

        val intent = Intent(this, TvNotificationListener::class.java)
        intent.setAction(TvNotificationListener.ACTION_LOCAL_BINDING)
        bindService(intent, notificationListenerConnectionListener, Context.BIND_AUTO_CREATE)
    }

    override fun onResume() {
        super.onResume()
        if (NotificationUtils.notificationPermissionGranted(this)) {
            noNotificationAccess.visibility = View.GONE
        } else {
            noNotificationAccess.visibility = View.VISIBLE
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        notificationListener?.removeNotificationUpdateListener(notificationUpdateListener)
        connectivityManager.unregisterNetworkCallback(connCallback)
    }

    private fun setNetworkButton(wifiInfo: WifiInfo? = null) {
        var networkString = resources.getString(R.string.unknown)
        val capabilities =
            connectivityManager.getNetworkCapabilities(connectivityManager.activeNetwork)

        if (capabilities == null
            || !capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
            || !capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)
        ) {
            // No internet connection
            networkButton.icon =
                AppCompatResources.getDrawable(this, R.drawable.ic_wifi_not_connected)
            networkString = resources.getString(R.string.not_connected)
        } else {
            if (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET)) {
                // Ethernet connection
                networkButton.icon = AppCompatResources.getDrawable(this, R.drawable.ic_ethernet)
            }

            if (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)) {
                // WIFI connection
                networkString = resources.getString(R.string.connected)
                if (wifiInfo != null) {
                    val wifiManager = getSystemService(WifiManager::class.java)
                    val wifiStrength = (wifiManager.calculateSignalLevel(wifiInfo.rssi)
                        .toFloat() / wifiManager.maxSignalLevel * wifiManager.maxSignalLevel).toInt()
                    networkButton.icon =
                        AppCompatResources.getDrawable(this, wifiIcons[wifiStrength])
                } else {
                    networkButton.icon = AppCompatResources.getDrawable(this, wifiIcons[3])
                }
            }
        }

        val networkSpan =
            SpannableString(resources.getString(R.string.network_status, networkString))
        networkButton.text = updateButton(networkSpan, false)

        networkButton.onFocusChangeListener = View.OnFocusChangeListener { _, hasFocus ->
            networkButton.text = updateButton(networkSpan, hasFocus)
        }

        networkButton.setOnClickListener {
            startActivity(WIFI_SETTINGS)
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

    private fun loadNotifications() {
        val notifications = notificationListener?.getNotifications()
        val ranking = notificationListener?.currentRanking
        if (!notifications.isNullOrEmpty() && ranking != null) {
            val statusBarNotifications = ArrayList<StatusBarNotification>()
            for (key in ranking.orderedKeys) {
                val sbn: StatusBarNotification? = notifications[key]
                if (sbn != null) {
                    statusBarNotifications.add(sbn)
                }
            }
            notificationAdapter.submitList(statusBarNotifications)
            noNotifications.visibility = View.GONE
            notificationList.visibility = View.VISIBLE
        } else {
            noNotifications.visibility = View.VISIBLE
            notificationList.visibility = View.GONE
        }
    }

    private val notificationListenerConnectionListener: ServiceConnection =
        object : ServiceConnection {
            override fun onServiceConnected(className: ComponentName, binder: IBinder) {
                notificationListener = (binder as TvNotificationListener.LocalBinder).getService()
                loadNotifications()

                notificationUpdateListener =
                    object : TvNotificationListener.NotificationUpdateListener {
                        override fun onNotificationsChanged() {
                            loadNotifications()
                        }
                    }
                notificationListener?.addNotificationUpdateListener(notificationUpdateListener)
            }

            override fun onServiceDisconnected(className: ComponentName) {
                notificationListener = null
            }
        }

    override fun onItemClick(view: NotificationItemView) {
        val notification = view.statusBarNotification?.notification ?: return
        try {
            if (notification.contentIntent != null) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
                    val activityOptions = ActivityOptions.makeBasic()
                    activityOptions.setPendingIntentBackgroundActivityStartMode(
                        ActivityOptions.MODE_BACKGROUND_ACTIVITY_START_ALLOWED
                    )
                    notification.contentIntent?.send(activityOptions.toBundle())
                } else {
                    notification.contentIntent?.send()
                }
            }

            view.statusBarNotification?.let { cancelNotification(it) }
        } catch (e: PendingIntent.CanceledException) {
            Log.d(
                "SystemOptionsActivity",
                "Pending intent canceled for : ${notification.contentIntent}"
            )
        }
    }

    override fun onKey(view: NotificationItemView, keyCode: Int, event: KeyEvent): Boolean {
        if (event.action != KeyEvent.ACTION_DOWN) {
            return false
        }

        when (keyCode) {
            KeyEvent.KEYCODE_DPAD_LEFT -> {
                if (view.swipeStatus == NotificationItemView.SwipeStatus.LEFT) {
                    view.resetState()
                    view.statusBarNotification?.let { cancelNotification(it) }
                } else {
                    view.animateDismissLeft()
                }
                return true
            }

            KeyEvent.KEYCODE_DPAD_RIGHT -> {
                if (view.swipeStatus == NotificationItemView.SwipeStatus.RIGHT) {
                    view.resetState()
                    view.statusBarNotification?.let { cancelNotification(it) }
                } else {
                    view.animateDismissRight()
                }
                return true
            }

            KeyEvent.KEYCODE_DPAD_CENTER -> {
                if (view.swipeStatus != NotificationItemView.SwipeStatus.NONE) {
                    view.resetState()
                    view.statusBarNotification?.let { cancelNotification(it) }
                }
                return true
            }

            else -> {
                if (view.swipeStatus != NotificationItemView.SwipeStatus.NONE) {
                    view.animateCloseDismiss()
                }
                return false
            }
        }
    }

    private fun cancelNotification(sbn: StatusBarNotification) {
        if (NotificationUtils.shouldAutoCancel(sbn.notification)) {
            notificationListener?.cancelNotification(sbn.key)
        }
    }

    private val connCallback = object : ConnectivityManager.NetworkCallback() {
        override fun onLost(network: Network) {
            super.onLost(network)
            runOnUiThread {
                setNetworkButton()
            }
        }

        override fun onAvailable(network: Network) {
            super.onAvailable(network)
            runOnUiThread {
                setNetworkButton()
            }
        }

        override fun onCapabilitiesChanged(
            network: Network,
            networkCapabilities: NetworkCapabilities
        ) {
            super.onCapabilitiesChanged(network, networkCapabilities)
            val wifiInfo = networkCapabilities.transportInfo as WifiInfo
            runOnUiThread {
                setNetworkButton(wifiInfo)
            }
        }
    }

    companion object {
        val SETTINGS: Intent = Intent(Settings.ACTION_SETTINGS)
        val WIFI_SETTINGS: Intent = Intent(Settings.ACTION_WIFI_SETTINGS)
        val BLUETOOTH_SETTINGS: Intent = Intent(Settings.ACTION_BLUETOOTH_SETTINGS)
        val NOTIFICATION_SETTINGS: Intent = Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS)

        val wifiIcons = intArrayOf(
            R.drawable.ic_wifi_signal_0,
            R.drawable.ic_wifi_signal_1,
            R.drawable.ic_wifi_signal_2,
            R.drawable.ic_wifi_signal_3,
            R.drawable.ic_wifi_signal_4
        )
    }
}
