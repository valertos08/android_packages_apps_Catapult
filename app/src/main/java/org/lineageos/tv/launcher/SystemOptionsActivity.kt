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
import androidx.lifecycle.lifecycleScope
import com.google.android.material.button.MaterialButton
import kotlinx.coroutines.launch
import org.lineageos.tv.launcher.ext.NetworkState
import org.lineageos.tv.launcher.ext.networkCallbackFlow
import org.lineageos.tv.launcher.notification.NotificationAdapter
import org.lineageos.tv.launcher.notification.NotificationUtils
import org.lineageos.tv.launcher.notification.TvNotificationListener
import org.lineageos.tv.launcher.view.NotificationItemView
import java.util.Calendar


class SystemOptionsActivity : ModalActivity(R.layout.activity_system_options),
    NotificationAdapter.OnItemActionListener {
    // Views
    private val allowNotificationAccessMaterialButton by lazy { findViewById<MaterialButton>(R.id.allowNotificationAccessMaterialButton) }
    private val bluetoothMaterialButton by lazy { findViewById<MaterialButton>(R.id.bluetoothMaterialButton) }
    private val dateTextView by lazy { findViewById<TextView>(R.id.dateTextView) }
    private val networkMaterialButton by lazy { findViewById<MaterialButton>(R.id.networkMaterialButton) }
    private val noNotificationAccessLinearLayout by lazy { findViewById<LinearLayout>(R.id.noNotificationAccessLinearLayout) }
    private val noNotificationsTextView by lazy { findViewById<TextView>(R.id.noNotificationsTextView) }
    private val notificationsVerticalGridView by lazy { findViewById<VerticalGridView>(R.id.notificationsVerticalGridView) }
    private val powerMaterialButton by lazy { findViewById<MaterialButton>(R.id.powerMaterialButton) }
    private val settingsButton by lazy { findViewById<MaterialButton>(R.id.settingsMaterialButton) }
    private val sleepMaterialButton by lazy { findViewById<MaterialButton>(R.id.sleepMaterialButton) }

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

        allowNotificationAccessMaterialButton.setOnClickListener {
            startActivity(NOTIFICATION_SETTINGS)
        }

        notificationsVerticalGridView.adapter = notificationAdapter

        // WIFI callbacks
        val request = NetworkRequest.Builder()
            .addTransportType(NetworkCapabilities.TRANSPORT_WIFI)
            .build()

        lifecycleScope.launch {
            connectivityManager.networkCallbackFlow(request).collect {
                when (it) {
                    is NetworkState.Available -> setNetworkButton(
                        capabilities = connectivityManager.getNetworkCapabilities(it.network)
                    )

                    is NetworkState.Lost -> setNetworkButton(
                        capabilities = connectivityManager.getNetworkCapabilities(it.network)
                    )

                    is NetworkState.CapabilitiesChanged ->
                        setNetworkButton(
                            wifiInfo = it.networkCapabilities.transportInfo as WifiInfo,
                            capabilities = it.networkCapabilities
                        )
                }
            }
        }
    }

    override fun onStart() {
        super.onStart()
        if (!NotificationUtils.notificationPermissionGranted(this)) {
            noNotificationAccessLinearLayout.visibility = View.VISIBLE
            noNotificationsTextView.visibility = View.GONE
            notificationsVerticalGridView.visibility = View.GONE
            return
        }

        val intent = Intent(this, TvNotificationListener::class.java)
        intent.setAction(TvNotificationListener.ACTION_LOCAL_BINDING)
        bindService(intent, notificationListenerConnectionListener, Context.BIND_AUTO_CREATE)
    }

    override fun onResume() {
        super.onResume()
        if (NotificationUtils.notificationPermissionGranted(this)) {
            noNotificationAccessLinearLayout.visibility = View.GONE
        } else {
            noNotificationAccessLinearLayout.visibility = View.VISIBLE
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        notificationListener?.removeNotificationUpdateListener(notificationUpdateListener)
    }

    private fun setNetworkButton(
        wifiInfo: WifiInfo? = null,
        capabilities: NetworkCapabilities? = connectivityManager.getNetworkCapabilities(
            connectivityManager.activeNetwork
        )
    ) {
        var networkString = resources.getString(R.string.unknown)

        if (capabilities == null
            || !capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
            || !capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)
        ) {
            // No internet connection
            networkMaterialButton.icon =
                AppCompatResources.getDrawable(this, R.drawable.ic_wifi_not_connected)
            networkString = resources.getString(R.string.not_connected)
        } else {
            if (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET)) {
                // Ethernet connection
                networkMaterialButton.icon =
                    AppCompatResources.getDrawable(this, R.drawable.ic_ethernet)
            }

            if (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)) {
                // WIFI connection
                networkString = resources.getString(R.string.connected)
                if (wifiInfo != null) {
                    val wifiManager = getSystemService(WifiManager::class.java)
                    val wifiStrength = (wifiManager.calculateSignalLevel(wifiInfo.rssi)
                        .toFloat() / wifiManager.maxSignalLevel * wifiManager.maxSignalLevel).toInt()
                    networkMaterialButton.icon =
                        AppCompatResources.getDrawable(this, wifiIcons[wifiStrength])
                } else {
                    networkMaterialButton.icon = AppCompatResources.getDrawable(this, wifiIcons[3])
                }
            }
        }

        val networkSpan =
            SpannableString(resources.getString(R.string.network_status, networkString))
        networkMaterialButton.text = updateButton(networkSpan, false)

        networkMaterialButton.onFocusChangeListener = View.OnFocusChangeListener { _, hasFocus ->
            networkMaterialButton.text = updateButton(networkSpan, hasFocus)
        }

        networkMaterialButton.setOnClickListener {
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
        bluetoothMaterialButton.text = updateButton(btSpan, false)

        bluetoothMaterialButton.onFocusChangeListener = View.OnFocusChangeListener { _, hasFocus ->
            bluetoothMaterialButton.text = updateButton(btSpan, hasFocus)
        }

        bluetoothMaterialButton.setOnClickListener {
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
            noNotificationsTextView.visibility = View.GONE
            notificationsVerticalGridView.visibility = View.VISIBLE
        } else {
            noNotificationsTextView.visibility = View.VISIBLE
            notificationsVerticalGridView.visibility = View.GONE
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
