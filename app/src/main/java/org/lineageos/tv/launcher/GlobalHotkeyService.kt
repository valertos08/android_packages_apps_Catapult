/*
 * SPDX-FileCopyrightText: 2025 The LineageOS Project
 * SPDX-License-Identifier: Apache-2.0
 */

package org.lineageos.tv.launcher

import android.app.ActivityManager
import android.app.Service
import android.app.usage.UsageStatsManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.pm.ResolveInfo
import android.graphics.PixelFormat
import android.os.IBinder
import android.os.ServiceManager
import android.util.Log
import android.util.TypedValue
import android.view.ContextThemeWrapper
import android.view.Gravity
import android.view.KeyEvent
import android.view.View
import android.view.ViewTreeObserver
import android.view.WindowManager
import android.widget.FrameLayout
import android.widget.HorizontalScrollView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.view.isVisible
import com.android.internal.policy.IShortcutService
import org.lineageos.tv.launcher.ext.appCardSize
import org.lineageos.tv.launcher.ext.recentAppsEnabled
import org.lineageos.tv.launcher.model.Launchable
import org.lineageos.tv.launcher.model.LeanbackAppInfo
import org.lineageos.tv.launcher.view.AppCard

class GlobalHotkeyService : Service() {

    companion object {
        private const val TAG = "GlobalHotkeyService"

        // HOME is deliberately NOT registered here. The platform only dispatches
        // shortcut services for key combos that carry a modifier: in
        // AppLaunchShortcutManager.handleShortcutService() the lookup is skipped
        // when (metaState & META_CTRL_ON|META_ALT_ON|META_SHIFT_ON|META_META_ON)
        // is zero, so a bare HOME press can never reach a subscriber. On top of
        // that PhoneWindowManager consumes HOME in
        // DisplayHomeButtonHandler.handleHomeButton() before the shortcut service
        // stage is reached. Double-tap HOME is detected in ProxyHomeActivity
        // instead, which is the registered HOME activity.
        private const val HOTKEY_KEYCODE_TAB = 61
        private const val HOTKEY_META_ALT = 2

        private var instance: GlobalHotkeyService? = null

        @Volatile
        var overlayShown = false
            private set

        fun isOverlayShown() = overlayShown

        fun setOverlayShown(value: Boolean) {
            overlayShown = value
        }

        fun clearOverlayFlag() {
            overlayShown = false
        }
    }

    private var shortcutCallback: IShortcutService? = null
    private var alreadyStartedActivity = false

    override fun onCreate() {
        super.onCreate()
        instance = this
        Log.d(TAG, "Service created")
        registerHotkey()
    }

    override fun onDestroy() {
        super.onDestroy()
        instance = null
        Log.d(TAG, "Service destroyed")
        unregisterHotkey()
    }

    override fun onBind(intent: Intent?) = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int) = START_STICKY

    private fun registerHotkey() {
        try {
            val wmBinder = ServiceManager.getService(Context.WINDOW_SERVICE) ?: return

            val iWindowManagerStub = Class.forName("android.view.IWindowManager\$Stub")
            val asInterfaceMethod = iWindowManagerStub.getMethod("asInterface", android.os.IBinder::class.java)
            val wm = asInterfaceMethod.invoke(null, wmBinder) ?: return

            val registerMethod = wm.javaClass.methods.find {
                it.name == "registerShortcutKey" && it.parameterTypes.size == 2 &&
                it.parameterTypes[0] == Long::class.java
            } ?: return

            shortcutCallback = object : IShortcutService.Stub() {
                override fun notifyShortcutKeyPressed(shortcutCode: Long) {
                    Log.d(TAG, "Shortcut pressed: code=$shortcutCode")

                    val expectedAltTab = HOTKEY_KEYCODE_TAB.toLong() or (HOTKEY_META_ALT.toLong() shl 32)

                    when (shortcutCode) {
                        expectedAltTab -> handleAltTabKey()
                        else -> Log.w(TAG, "Unknown shortcut code: $shortcutCode")
                    }
                }

                private fun handleAltTabKey() {
                    Log.d(TAG, "Alt+Tab pressed!")

                    if (overlayShown) {
                        overlayShown = false
                    } else {
                        overlayShown = true
                        val intent = Intent(this@GlobalHotkeyService, ProxyHomeActivity::class.java).apply {
                            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        }
                        startActivity(intent)
                    }
                }
            }

            val shortcutCodeAltTab = HOTKEY_KEYCODE_TAB.toLong() or (HOTKEY_META_ALT.toLong() shl 32)
            
            registerMethod.invoke(wm, shortcutCodeAltTab, shortcutCallback)
            Log.d(TAG, "Hotkeys registered: Alt+Tab")

        } catch (e: Exception) {
            Log.e(TAG, "Failed to register hotkey", e)
        }
    }

    private fun unregisterHotkey() {
        try {
            val wmBinder = ServiceManager.getService(Context.WINDOW_SERVICE) ?: return
            val iWindowManagerStub = Class.forName("android.view.IWindowManager\$Stub")
            val asInterfaceMethod = iWindowManagerStub.getMethod("asInterface", android.os.IBinder::class.java)
            val wm = asInterfaceMethod.invoke(null, wmBinder) ?: return

            // unregisterShortcutKey is no longer part of IWindowManager.aidl, so
            // this lookup normally returns null and the registration goes away
            // with the process. Nothing else to do.
            val unregisterMethod = wm.javaClass.methods.find {
                it.name == "unregisterShortcutKey" && it.parameterTypes.size == 2
            }

            if (unregisterMethod != null && shortcutCallback != null) {
                val shortcutCodeAltTab = HOTKEY_KEYCODE_TAB.toLong() or (HOTKEY_META_ALT.toLong() shl 32)
                
                unregisterMethod.invoke(wm, shortcutCodeAltTab, shortcutCallback)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to unregister hotkey", e)
        }
    }

    private fun showOverlay() {
        overlayShown = true
    }

    private fun hideOverlay() {
        overlayShown = false
    }

    private fun getRecentApps(): List<String> {
        return emptyList()
    }
}