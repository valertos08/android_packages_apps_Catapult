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
import android.os.Handler
import android.os.IBinder
import android.os.Looper
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
import androidx.preference.PreferenceManager
import com.android.internal.policy.IShortcutService
import org.lineageos.tv.launcher.ext.appCardSize
import org.lineageos.tv.launcher.ext.recentAppsEnabled
import org.lineageos.tv.launcher.model.Launchable
import org.lineageos.tv.launcher.model.LeanbackAppInfo
import org.lineageos.tv.launcher.view.AppCard

class GlobalHotkeyService : Service() {

    companion object {
        private const val TAG = "GlobalHotkeyService"
        
        private const val HOTKEY_KEYCODE_HOME = 3
        private const val HOTKEY_META_HOME = 0
        private const val HOTKEY_KEYCODE_TAB = 61
        private const val HOTKEY_META_ALT = 2
        
        private const val DOUBLE_HOME_TIMEOUT = 500L
        private var instance: GlobalHotkeyService? = null

        @Volatile
        var overlayShown = false
            private set

        @Volatile
        var isDoubleTapPending = false
            private set

        fun isOverlayShown() = overlayShown
        
        fun setOverlayShown(value: Boolean) {
            overlayShown = value
            isDoubleTapPending = false
        }
        
        fun clearOverlayFlag() {
            overlayShown = false
            isDoubleTapPending = false
        }
    }

    private var shortcutCallback: IShortcutService? = null
    private var lastHomePressTime = 0L
    private var alreadyStartedActivity = false
    private val handler = Handler(Looper.getMainLooper())

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

                    val expectedHome = HOTKEY_KEYCODE_HOME.toLong() or (HOTKEY_META_HOME.toLong() shl 32)
                    val expectedAltTab = HOTKEY_KEYCODE_TAB.toLong() or (HOTKEY_META_ALT.toLong() shl 32)

                    when (shortcutCode) {
                        expectedHome -> handleHomeKey()
                        expectedAltTab -> handleAltTabKey()
                        else -> Log.w(TAG, "Unknown shortcut code: $shortcutCode")
                    }
                }

                private fun handleHomeKey() {
                    val prefs = PreferenceManager.getDefaultSharedPreferences(this@GlobalHotkeyService)
                    if (!prefs.recentAppsEnabled) {
                        return
                    }
                    
                    val currentTime = System.currentTimeMillis()
                    val timeSinceLastPress = currentTime - lastHomePressTime
                    
                    if (timeSinceLastPress in 1..DOUBLE_HOME_TIMEOUT) {
                        Log.d(TAG, "Double HOME detected!")
                        overlayShown = true
                    }
                    
                    lastHomePressTime = currentTime
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

            val shortcutCodeHome = HOTKEY_KEYCODE_HOME.toLong() or (HOTKEY_META_HOME.toLong() shl 32)
            val shortcutCodeAltTab = HOTKEY_KEYCODE_TAB.toLong() or (HOTKEY_META_ALT.toLong() shl 32)
            
            registerMethod.invoke(wm, shortcutCodeHome, shortcutCallback)
            registerMethod.invoke(wm, shortcutCodeAltTab, shortcutCallback)
            Log.d(TAG, "Hotkeys registered: HOME, Alt+Tab")

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

            val unregisterMethod = wm.javaClass.methods.find {
                it.name == "unregisterShortcutKey" && it.parameterTypes.size == 2
            }

            if (unregisterMethod != null && shortcutCallback != null) {
                val shortcutCodeHome = HOTKEY_KEYCODE_HOME.toLong() or (HOTKEY_META_HOME.toLong() shl 32)
                val shortcutCodeAltTab = HOTKEY_KEYCODE_TAB.toLong() or (HOTKEY_META_ALT.toLong() shl 32)
                
                unregisterMethod.invoke(wm, shortcutCodeHome, shortcutCallback)
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