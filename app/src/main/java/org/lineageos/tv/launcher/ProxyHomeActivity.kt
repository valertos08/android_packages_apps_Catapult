/*
 * SPDX-FileCopyrightText: 2025 The LineageOS Project
 * SPDX-License-Identifier: Apache-2.0
 */

package org.lineageos.tv.launcher

import android.app.Activity
import android.app.ActivityManager
import android.app.ActivityOptions
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.PixelFormat
import android.graphics.drawable.BitmapDrawable
import android.os.Bundle
import android.util.TypedValue
import android.view.ContextThemeWrapper
import android.view.Gravity
import android.view.KeyEvent
import android.view.animation.AlphaAnimation
import android.view.animation.Animation
import android.view.animation.AnimationSet
import android.view.animation.RotateAnimation
import android.view.animation.ScaleAnimation
import android.view.animation.TranslateAnimation
import android.view.View
import android.view.ViewTreeObserver
import android.view.WindowManager
import android.view.animation.DecelerateInterpolator
import android.widget.HorizontalScrollView
import android.widget.LinearLayout
import android.widget.TextView
import org.lineageos.tv.launcher.model.LeanbackAppInfo
import org.lineageos.tv.launcher.view.AppCard

class ProxyHomeActivity : Activity() {

    companion object {
        private const val TAG = "ProxyHomeActivity"
    }

    private var windowManager: WindowManager? = null
    private var overlayView: View? = null
    private var cardsContainer: LinearLayout? = null
    private var scrollView: HorizontalScrollView? = null
    private val taskSnapshots = mutableMapOf<Int, Bitmap>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        windowManager = getSystemService(Context.WINDOW_SERVICE) as WindowManager
        
        if (GlobalHotkeyService.isOverlayShown()) {
            GlobalHotkeyService.clearOverlayFlag()
            showOverlay()
        } else {
            launchMainActivity()
        }
    }

    private fun launchMainActivity() {
        val intent = Intent(this, MainActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_NO_ANIMATION)
        startActivity(intent)
        finish()
        overridePendingTransition(0, 0)
    }

    private fun showOverlay() {
        val recentTasks = getRecentTasks()
        val cardMargin = (20 * resources.displayMetrics.density).toInt()

        val root = object : LinearLayout(this) {
            override fun dispatchKeyEvent(event: KeyEvent): Boolean {
                if (event.action == KeyEvent.ACTION_DOWN) {
                    when (event.keyCode) {
                        KeyEvent.KEYCODE_BACK, KeyEvent.KEYCODE_ESCAPE -> {
                            hideOverlayAndFinish()
                            return true
                        }
                    }
                }
                return super.dispatchKeyEvent(event)
            }
        }.apply {
            orientation = LinearLayout.VERTICAL
            setBackgroundColor(0xF5000000.toInt())
            isFocusable = true
            isFocusableInTouchMode = true
        }

        val headerContainer = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            setPadding(
                (32 * resources.displayMetrics.density).toInt(),
                (24 * resources.displayMetrics.density).toInt(),
                (32 * resources.displayMetrics.density).toInt(),
                (16 * resources.displayMetrics.density).toInt()
            )
        }

        val titleText = TextView(this).apply {
            text = "Recent Tasks (${recentTasks.size})"
            setTextColor(0xFFFFFFFF.toInt())
            setTextSize(TypedValue.COMPLEX_UNIT_SP, 28f)
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
        }
        headerContainer.addView(titleText)
        root.addView(headerContainer)

        val themedContext = ContextThemeWrapper(this, R.style.Theme_Catapult)

        scrollView = HorizontalScrollView(themedContext).apply {
            isHorizontalScrollBarEnabled = false
            isFocusable = false
            clipChildren = false
            clipToPadding = false
        }

        cardsContainer = LinearLayout(themedContext).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            setPadding(
                (32 * resources.displayMetrics.density).toInt(),
                (80 * resources.displayMetrics.density).toInt(),
                (32 * resources.displayMetrics.density).toInt(),
                (80 * resources.displayMetrics.density).toInt()
            )
            clipChildren = false
            clipToPadding = false
        }

        for (task in recentTasks) {
            try {
                val packageName = task.packageName
                val taskId = task.taskId
                val displayName = task.displayName
                val taskLabel = task.taskLabel
                val customIcon = task.customIcon
                val thumbnail = task.thumbnail
                val baseIntent = task.baseIntent

                // Try to get app info for icon and label
                val appInfo = try {
                    packageManager.getApplicationInfo(packageName, 0)
                } catch (e: Exception) {
                    null
                }

                // Create app info for the card
                val appInfoForCard: LeanbackAppInfo? = if (appInfo != null) {
                    try {
                        val intent = packageManager.getLaunchIntentForPackage(packageName) 
                            ?: baseIntent
                        if (intent != null) {
                            val resolveInfo = packageManager.resolveActivity(
                                intent, 
                                PackageManager.MATCH_DEFAULT_ONLY
                            )
                            if (resolveInfo != null) {
                                LeanbackAppInfo(resolveInfo, themedContext)
                            } else null
                        } else null
                    } catch (e: Exception) {
                        null
                    }
                } else null

                val card = AppCard(themedContext).apply {
                    isFocusable = true
                    isClickable = true
                    isFocusableInTouchMode = true
                    
                    // Set card info first (establishes default icon/banner)
                    appInfoForCard?.let { setCardInfo(it) }
                    
                    // Override icon with custom icon (hides banner, shows custom icon)
                    if (customIcon != null) {
                        setCustomIcon(customIcon)
                    }
                    
                    // Override title with task-specific displayName
                    if (!displayName.isNullOrEmpty()) {
                        setCustomTitle(displayName)
                    }
                    
                    clipChildren = false
                    clipToPadding = false
                    setPadding(
                        (8 * resources.displayMetrics.density).toInt(),
                        (16 * resources.displayMetrics.density).toInt(),
                        (8 * resources.displayMetrics.density).toInt(),
                        (16 * resources.displayMetrics.density).toInt()
                    )
                    layoutParams = LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.WRAP_CONTENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                    ).apply {
                        marginEnd = cardMargin
                    }

                    tag = taskId

                    if (thumbnail != null) {
                        background = BitmapDrawable(resources, thumbnail)
                    }

                    // Set content description using task label or display name
                    val descText = if (!taskLabel.isNullOrEmpty()) taskLabel else displayName
                    if (descText.isNotEmpty()) {
                        contentDescription = descText
                    }

                    setOnClickListener {
                        try {
                            val activityManager = getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
                            val options = ActivityOptions.makeBasic()
                            options.setPendingIntentBackgroundActivityStartMode(
                                ActivityOptions.MODE_BACKGROUND_ACTIVITY_START_ALLOWED
                            )
                            activityManager.moveTaskToFront(
                                taskId,
                                ActivityManager.MOVE_TASK_WITH_HOME,
                                options.toBundle()
                            )
                            root.postDelayed({
                                hideOverlayAndFinish()
                            }, 200)
                        } catch (e: Exception) {
                            android.util.Log.e(TAG, "Failed to move task to front", e)
                            try {
                                baseIntent?.let { intent ->
                                    intent.addFlags(
                                        Intent.FLAG_ACTIVITY_NEW_TASK or
                                        Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED or
                                        Intent.FLAG_ACTIVITY_LAUNCHED_FROM_HISTORY
                                    )
                                    startActivity(intent)
                                }
                            } catch (e2: Exception) {
                                android.util.Log.e(TAG, "Failed to start activity", e2)
                            }
                            hideOverlayAndFinish()
                        }
                    }

                    setOnKeyListener { v, keyCode, event ->
                        if (keyCode == KeyEvent.KEYCODE_DPAD_UP && event.action == KeyEvent.ACTION_DOWN) {
                            removeTaskWithAnimation(v, taskId)
                            true
                        } else {
                            false
                        }
                    }
                }

                cardsContainer?.addView(card)

            } catch (e: Exception) {
                android.util.Log.e(TAG, "Failed to load task: ${task.packageName}", e)
            }
        }

        val sv = scrollView!!
        val cc = cardsContainer!!
        
        sv.addView(cc)
        root.addView(sv)

        // Add "Close All" button only if there are tasks
        var closeAllButton: android.widget.Button? = null
        if (recentTasks.isNotEmpty()) {
            closeAllButton = android.widget.Button(this).apply {
                text = "Close All"
                textSize = 18f
                setTextColor(0xFFFFFFFF.toInt())
                setBackgroundResource(android.R.drawable.btn_default)
                setPadding(
                    (24 * resources.displayMetrics.density).toInt(),
                    (12 * resources.displayMetrics.density).toInt(),
                    (24 * resources.displayMetrics.density).toInt(),
                    (12 * resources.displayMetrics.density).toInt()
                )
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                ).apply {
                    topMargin = (16 * resources.displayMetrics.density).toInt()
                    gravity = Gravity.CENTER_HORIZONTAL
                }
                setOnClickListener {
                    closeAllTasksWithAnimation()
                }
            }
            root.addView(closeAllButton)
        }

        val windowParams = WindowManager.LayoutParams(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
            WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL or
            WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN or
            WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS or
            WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH,
            PixelFormat.TRANSLUCENT
        ).apply {
            title = "RecentAppsOverlay"
        }

        windowManager?.addView(root, windowParams)
        overlayView = root

        root.viewTreeObserver.addOnWindowAttachListener(object : ViewTreeObserver.OnWindowAttachListener {
            override fun onWindowAttached() {
                root.post {
                    root.isFocusable = true
                    root.isFocusableInTouchMode = true
                    root.requestFocus()
                    if (cardsContainer?.childCount ?: 0 > 0) {
                        cardsContainer?.getChildAt(0)?.requestFocus()
                    }
                }
            }

            override fun onWindowDetached() {
                root.viewTreeObserver.removeOnWindowAttachListener(this)
                overlayView = null
            }
        })
    }

    private fun removeTaskWithAnimation(card: View, taskId: Int) {
        val density = resources.displayMetrics.density
        
        var removed = false
        
        // Try ActivityTaskManager.removeTask via reflection
        try {
            val atmClass = Class.forName("android.app.ActivityTaskManager")
            val method = atmClass.getMethod("removeTask", Int::class.javaPrimitiveType)
            method.invoke(null, taskId)
            android.util.Log.d(TAG, "Removed task $taskId via ActivityTaskManager.removeTask")
            removed = true
        } catch (e: Exception) {
            android.util.Log.d(TAG, "ActivityTaskManager.removeTask failed: ${e.message}")
        }
        
        // Try IActivityTaskManager via ServiceManager
        if (!removed) {
            try {
                val serviceManagerClass = Class.forName("android.os.ServiceManager")
                val getServiceMethod = serviceManagerClass.getMethod("getService", String::class.java)
                val service = getServiceMethod.invoke(null, "activity_task")
                
                val stubClass = Class.forName("android.app.IActivityTaskManager\$Stub")
                val asInterfaceMethod = stubClass.getMethod("asInterface", Class.forName("android.os.IBinder"))
                val iatm = asInterfaceMethod.invoke(null, service)
                
                // Try removeTask method
                val removeMethod = iatm.javaClass.getMethod("removeTask", Int::class.javaPrimitiveType)
                removeMethod.invoke(iatm, taskId)
                android.util.Log.d(TAG, "Removed task $taskId via IActivityTaskManager.removeTask")
                removed = true
            } catch (e: Exception) {
                android.util.Log.d(TAG, "IATM removeTask failed: ${e.message}")
            }
        }
        
        if (!removed) {
            // Try to use the underlying activity task manager directly
            try {
                val atm = android.app.ActivityTaskManager.getInstance()
                val method = atm.javaClass.getMethod("removeTask", Int::class.javaPrimitiveType)
                method.invoke(atm, taskId)
                android.util.Log.d(TAG, "Removed task $taskId via ActivityTaskManager.getInstance().removeTask")
                removed = true
            } catch (e: Exception) {
                android.util.Log.e(TAG, "All removeTask methods failed", e)
            }
        }
        
        // Always animate and remove card from UI regardless of actual task removal
        card.animate()
            .translationY(-500f * density)
            .alpha(0f)
            .setDuration(300)
            .setInterpolator(DecelerateInterpolator())
            .withEndAction {
                cardsContainer?.removeView(card)
                
                if (cardsContainer?.childCount == 0) {
                    hideOverlayAndFinish()
                } else {
                    val nextFocusable = cardsContainer?.focusedChild
                    if (nextFocusable != null) {
                        nextFocusable.requestFocus()
                    } else if (cardsContainer?.childCount ?: 0 > 0) {
                        cardsContainer?.getChildAt(0)?.requestFocus()
                    }
                }
            }
    }

    private fun hideOverlayAndFinish() {
        try {
            overlayView?.let {
                windowManager?.removeViewImmediate(it)
                overlayView = null
            }
        } catch (e: Exception) {
            android.util.Log.e(TAG, "Failed to hide overlay", e)
            overlayView = null
        }
        
        GlobalHotkeyService.clearOverlayFlag()
        moveTaskToBack(true)
    }

    private fun closeAllTasksWithAnimation() {
        val displayMetrics = resources.displayMetrics
        val screenWidth = displayMetrics.widthPixels
        val screenHeight = displayMetrics.heightPixels
        val centerX = screenWidth / 2f
        val centerY = screenHeight / 2f
        
        val cc = cardsContainer!!
        val cardCount = cc.childCount
        
        if (cardCount == 0) {
            hideOverlayAndFinish()
            return
        }
        
        // Get all task IDs and remove them via ActivityTaskManager
        val taskIds = mutableListOf<Int>()
        for (i in 0 until cc.childCount) {
            val card = cc.getChildAt(i) as? View
            card?.tag?.let { taskIds.add(it as Int) }
        }
        
        for (taskId in taskIds) {
            try {
                val atm = android.app.ActivityTaskManager.getInstance()
                try {
                    val method = atm.javaClass.getMethod("removeTask", Int::class.javaPrimitiveType)
                    method.invoke(atm, taskId)
                } catch (e: Exception) {
                    try {
                        val removeMethod = atm.javaClass.getMethod("removeTask", Int::class.javaPrimitiveType, Int::class.javaPrimitiveType)
                        removeMethod.invoke(atm, taskId, 0)
                    } catch (e2: Exception) {
                        android.util.Log.w(TAG, "Failed to remove task $taskId", e2)
                    }
                }
            } catch (e: Exception) {
                android.util.Log.w(TAG, "Failed to remove task $taskId", e)
            }
        }
        
        // Phase 1: Move ALL to center together (no delay)
        for (i in 0 until cardCount) {
            val card = cc.getChildAt(i) as? View ?: continue
            
            val location = IntArray(2)
            card.getLocationOnScreen(location)
            val cardCenterX = location[0] + card.width / 2f
            val cardCenterY = location[1] + card.height / 2f
            
            val deltaX = centerX - cardCenterX
            val deltaY = centerY - cardCenterY
            
            val translationX = android.animation.ObjectAnimator.ofFloat(card, View.TRANSLATION_X, card.translationX, deltaX)
            val translationY = android.animation.ObjectAnimator.ofFloat(card, View.TRANSLATION_Y, card.translationY, deltaY)
            val scaleX = android.animation.ObjectAnimator.ofFloat(card, View.SCALE_X, 1f, 0.7f)
            val scaleY = android.animation.ObjectAnimator.ofFloat(card, View.SCALE_Y, 1f, 0.7f)
            
            translationX.duration = 300
            translationY.duration = 300
            scaleX.duration = 300
            scaleY.duration = 300
            
            // NO delay - all move together!
            
            translationX.start()
            translationY.start()
            scaleX.start()
            scaleY.start()
        }
        
        // Phase 2: Fly away together as ONE stack (all at same time, after phase 1)
        android.os.Handler(android.os.Looper.getMainLooper()).postDelayed({
            for (i in 0 until cardCount) {
                val card = cc.getChildAt(i) as? View ?: continue
                
                val location = IntArray(2)
                card.getLocationOnScreen(location)
                val cardCenterX = location[0] + card.width / 2f
                val cardCenterY = location[1] + card.height / 2f
                
                val centerTransX = centerX - cardCenterX
                val centerTransY = centerY - cardCenterY
                
                // All stacked on same position
                card.translationX = centerTransX
                card.translationY = centerTransY
                card.scaleX = 0.7f
                card.scaleY = 0.7f
                
                // All fly together to top-right corner (same trajectory)
                val rotation = android.animation.ObjectAnimator.ofFloat(card, View.ROTATION, 0f, 360f)
                val flyX = android.animation.ObjectAnimator.ofFloat(card, View.TRANSLATION_X, centerTransX, screenWidth + 100f)
                val flyY = android.animation.ObjectAnimator.ofFloat(card, View.TRANSLATION_Y, centerTransY, -screenHeight * 0.25f)
                val finalScaleX = android.animation.ObjectAnimator.ofFloat(card, View.SCALE_X, 0.7f, 0.02f)
                val finalScaleY = android.animation.ObjectAnimator.ofFloat(card, View.SCALE_Y, 0.7f, 0.02f)
                val alpha = android.animation.ObjectAnimator.ofFloat(card, View.ALPHA, 1f, 0f)
                
                rotation.duration = 400
                flyX.duration = 400
                flyY.duration = 400
                finalScaleX.duration = 400
                finalScaleY.duration = 400
                alpha.duration = 400
                
                val interpolator = android.view.animation.AccelerateInterpolator(1.5f)
                rotation.interpolator = interpolator
                flyX.interpolator = interpolator
                flyY.interpolator = interpolator
                
                rotation.start()
                flyX.start()
                flyY.start()
                finalScaleX.start()
                finalScaleY.start()
                alpha.start()
            }
        }, 450)
        
        // Close overlay after animation completes
        android.os.Handler(android.os.Looper.getMainLooper()).postDelayed({
            hideOverlayAndFinish()
        }, 700)
    }

    private data class RecentTask(
        val packageName: String,
        val taskId: Int,
        val displayName: String,
        val taskLabel: String? = null,
        val customIcon: android.graphics.drawable.Drawable? = null,
        val thumbnail: Bitmap? = null,
        val baseIntent: Intent? = null
    )

    private fun getRecentTasks(): List<RecentTask> {
        val tasks = LinkedHashMap<Int, RecentTask>()
        val myPackage = packageName
        taskSnapshots.clear()
        
        android.util.Log.d(TAG, "Getting recent tasks...")
        
        // 1. Try IActivityTaskManager via Reflection (PRIMARY)
        try {
            android.util.Log.d(TAG, "Trying IActivityTaskManager via Reflection...")
            
            val serviceManagerClass = Class.forName("android.os.ServiceManager")
            val getServiceMethod = serviceManagerClass.getMethod("getService", String::class.java)
            val service = getServiceMethod.invoke(null, "activity_task")
            
            val stubClass = Class.forName("android.app.IActivityTaskManager\$Stub")
            val asInterfaceMethod = stubClass.getMethod("asInterface", Class.forName("android.os.IBinder"))
            val iatm = asInterfaceMethod.invoke(null, service)
            
            android.util.Log.d(TAG, "Got IActivityTaskManager: $iatm")
            
            // API 34 signature: getTasks(int maxNum, boolean filterOnlyVisibleRecents, boolean keepIntentExtra, int displayId)
            try {
                val getTasksMethod = iatm.javaClass.getMethod(
                    "getTasks",
                    Int::class.javaPrimitiveType,
                    Boolean::class.javaPrimitiveType,
                    Boolean::class.javaPrimitiveType,
                    Int::class.javaPrimitiveType
                )
                
                @Suppress("UNCHECKED_CAST")
                val result = getTasksMethod.invoke(iatm, 30, false, true, 0) as? List<*>
                
                if (result != null && result.isNotEmpty()) {
                    android.util.Log.d(TAG, "Reflection getTasks returned ${result.size} tasks")
                    processTaskListReflection(tasks, result, myPackage)
                }
            } catch (e: Exception) {
                android.util.Log.d(TAG, "API 34 getTasks signature failed: ${e.message}")
                
                // Fallback: try generic getTasks
                val methods = iatm.javaClass.methods.filter { it.name == "getTasks" }
                for (method in methods) {
                    try {
                        val params = method.parameterTypes.map { paramType ->
                            when {
                                paramType == Int::class.javaPrimitiveType -> 30
                                paramType == Boolean::class.javaPrimitiveType -> false
                                else -> null
                            }
                        }.toTypedArray()
                        
                        @Suppress("UNCHECKED_CAST")
                        val result = method.invoke(iatm, *params) as? List<*>
                        
                        if (result != null && result.isNotEmpty()) {
                            android.util.Log.d(TAG, "Fallback getTasks returned ${result.size} tasks")
                            processTaskListReflection(tasks, result, myPackage)
                            break
                        }
                    } catch (e2: Exception) {
                        android.util.Log.d(TAG, "${method.name} failed: ${e2.message}")
                    }
                }
            }
        } catch (e: Exception) {
            android.util.Log.e(TAG, "Reflection approach failed", e)
        }
        
        // 2. Fallback: getRunningTasks
        if (tasks.isEmpty()) {
            try {
                android.util.Log.d(TAG, "Trying getRunningTasks as fallback...")
                val activityManager = getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
                @Suppress("DEPRECATION")
                val runningTasks = activityManager.getRunningTasks(30)
                android.util.Log.d(TAG, "getRunningTasks: count=${runningTasks.size}")
                
                for (taskInfo in runningTasks) {
                    try {
                        val pkgName = taskInfo.baseActivity?.packageName ?: continue
                        if (pkgName == myPackage) continue
                        
                        val taskId = taskInfo.id
                        if (taskId <= 0) continue
                        
                        val topActivity = taskInfo.topActivity
                        if (topActivity == null) {
                            android.util.Log.d(TAG, "Skipping dead task (no topActivity): $pkgName")
                            continue
                        }
                        
                        android.util.Log.d(TAG, "Running task: $pkgName, id=$taskId")
                        
                        val displayName = try {
                            val appInfo = packageManager.getApplicationInfo(pkgName, 0)
                            packageManager.getApplicationLabel(appInfo).toString()
                        } catch (e: Exception) {
                            pkgName
                        }
                        
                        tasks[taskId] = RecentTask(pkgName, taskId, displayName)
                    } catch (e: Exception) {
                        android.util.Log.w(TAG, "Failed to process running task", e)
                    }
                }
            } catch (e: Exception) {
                android.util.Log.e(TAG, "getRunningTasks failed", e)
            }
        }
        
        android.util.Log.d(TAG, "getRecentTasks returning ${tasks.size} tasks")
        return tasks.values.toList()
    }
    
    private fun processTaskListReflection(tasks: LinkedHashMap<Int, RecentTask>, taskList: List<*>, myPackage: String) {
        for (taskInfo in taskList) {
            try {
                if (taskInfo == null) continue
                
                val taskClass = taskInfo.javaClass
                android.util.Log.d(TAG, "Task class: ${taskClass.name}")
                
                // Log all available fields (including inherited from TaskInfo)
                val allFields = mutableListOf<String>()
                var currentClass: Class<*>? = taskClass
                while (currentClass != null && currentClass != Any::class.java) {
                    val className = currentClass.simpleName
                    for (field in currentClass.declaredFields) {
                        allFields.add("$className.${field.name}")
                    }
                    currentClass = currentClass.superclass
                }
                android.util.Log.d(TAG, "All fields (including inherited): $allFields")
                
                val pkgName: String?
                val taskId: Int
                val baseIntent: Intent?
                
                // Get taskId - Android 14 uses taskId instead of id
                taskId = try {
                    taskClass.getField("taskId").getInt(taskInfo)
                } catch (e: Exception) {
                    try {
                        taskClass.getField("persistentId").getInt(taskInfo)
                    } catch (e2: Exception) {
                        try {
                            taskClass.getField("id").getInt(taskInfo)
                        } catch (e3: Exception) {
                            -1
                        }
                    }
                }
                
                // Get baseIntent (through inheritance from TaskInfo)
                baseIntent = try {
                    taskClass.getField("baseIntent").get(taskInfo) as? Intent
                } catch (e: Exception) {
                    android.util.Log.d(TAG, "No baseIntent field: ${e.message}")
                    null
                }
                
                // Try to get baseActivity directly if available
                val baseActivity = try {
                    taskClass.getField("baseActivity").get(taskInfo) as? android.content.ComponentName
                } catch (e: Exception) {
                    android.util.Log.d(TAG, "No baseActivity field: ${e.message}")
                    null
                }
                
                // Try to get topActivity (for filtering dead tasks)
                val topActivity = try {
                    taskClass.getField("topActivity").get(taskInfo) as? android.content.ComponentName
                } catch (e: Exception) {
                    android.util.Log.d(TAG, "No topActivity field: ${e.message}")
                    null
                }
                
                // Determine package name from available sources
                pkgName = baseIntent?.component?.packageName
                    ?: baseActivity?.packageName
                    ?: topActivity?.packageName
                
                // Try to get TaskDescription for custom task label and icon
                var taskLabel: String? = null
                var customIcon: android.graphics.drawable.Drawable? = null
                try {
                    val taskDescField = taskClass.getField("taskDescription")
                    val taskDescription = taskDescField.get(taskInfo)
                    if (taskDescription != null) {
                        val getLabelMethod = taskDescription.javaClass.getMethod("getLabel")
                        val labelResult = getLabelMethod.invoke(taskDescription)
                        taskLabel = labelResult?.toString()
                        android.util.Log.d(TAG, "TaskDescription label: $taskLabel")
                        
                        // Try to get custom icon from TaskDescription
                        try {
                            val getIconMethod = taskDescription.javaClass.getMethod("getInMemoryIcon")
                            val iconObj = getIconMethod.invoke(taskDescription)
                            if (iconObj != null && iconObj is android.graphics.drawable.Drawable) {
                                customIcon = iconObj
                                android.util.Log.d(TAG, "Got custom icon from TaskDescription")
                            }
                        } catch (e: Exception) {
                            android.util.Log.d(TAG, "No custom icon in TaskDescription: ${e.message}")
                        }
                    }
                } catch (e: Exception) {
                    android.util.Log.d(TAG, "No taskDescription or label: ${e.message}")
                }
                
                // Try to get activity-specific icon from ActivityInfo
                val component = baseIntent?.component
                if (customIcon == null && component != null && pkgName != null) {
                    try {
                        // Get ActivityInfo to check icon resource ID
                        val activityInfo = packageManager.getActivityInfo(component, 0)
                        val appInfo = packageManager.getApplicationInfo(pkgName, 0)
                        
                        // Compare icon resource IDs
                        val activityIconRes = activityInfo.icon
                        val appIconRes = appInfo.icon
                        
                        android.util.Log.d(TAG, "Icon comparison: activityIconRes=$activityIconRes, appIconRes=$appIconRes")
                        
                        // If activity has different icon resource, use it
                        if (activityIconRes != 0 && activityIconRes != appIconRes) {
                            val drawable = packageManager.getDrawable(pkgName, activityIconRes, appInfo)
                            android.util.Log.d(TAG, "getDrawable result: ${drawable != null}")
                            customIcon = drawable
                            android.util.Log.d(TAG, "Activity has different icon resource: $component (res=$activityIconRes)")
                        } else {
                            android.util.Log.d(TAG, "Activity uses same icon as app: $component - will use banner")
                        }
                    } catch (e: Exception) {
                        android.util.Log.d(TAG, "Icon comparison failed: ${e.message}")
                    }
                }
                
                android.util.Log.d(TAG, "Resolved: pkgName=$pkgName, taskId=$taskId, baseIntent=$baseIntent, baseActivity=$baseActivity, topActivity=$topActivity")
                
                if (pkgName == null || pkgName == myPackage) continue
                if (taskId <= 0) continue
                
                // Use three-layer fallback to resolve task title
                val displayName = resolveTaskTitle(taskInfo, pkgName)
                
                android.util.Log.d(TAG, "Adding (refl): $pkgName, taskId=$taskId, displayName=$displayName (taskLabel=$taskLabel, hasCustomIcon=${customIcon != null})")
                tasks[taskId] = RecentTask(pkgName, taskId, displayName, taskLabel, customIcon, null, baseIntent)
            } catch (e: Exception) {
                android.util.Log.w(TAG, "Failed to process task via reflection: ${e.message}", e)
            }
        }
    }
    
    private fun debugDumpFields(obj: Any) {
        android.util.Log.d("DEBUG_PARSER", "=== Dumping Fields for ${obj.javaClass.name} ===")
        val fields = obj.javaClass.fields
        for (field in fields) {
            try {
                val value = field.get(obj)
                android.util.Log.d("DEBUG_PARSER", "Field: ${field.name} | Type: ${field.type.simpleName} | Value: $value")
            } catch (e: Exception) {
                android.util.Log.d("DEBUG_PARSER", "Field: ${field.name} | Error reading value")
            }
        }
        android.util.Log.d("DEBUG_PARSER", "Superclass: ${obj.javaClass.superclass?.name}")
    }
    
    private fun resolveTaskTitle(taskObj: Any, packageName: String): String {
        // 1. Попытка через TaskDescription (динамическое имя)
        try {
            val tdField = taskObj.javaClass.getField("taskDescription")
            val td = tdField.get(taskObj)
            if (td != null) {
                val label = td.javaClass.getMethod("getLabel").invoke(td)?.toString()
                if (!label.isNullOrEmpty()) {
                    android.util.Log.d(TAG, "Layer 1 (TaskDescription): $label")
                    return label
                }
            }
        } catch (e: Exception) {
            android.util.Log.d(TAG, "Layer 1 failed: ${e.message}")
        }
        
        // 2. Попытка получить Activity Label через ComponentName
        try {
            val baseIntentField = taskObj.javaClass.getField("baseIntent")
            val baseIntent = baseIntentField.get(taskObj) as? Intent
            val componentName = baseIntent?.component
            
            if (componentName != null) {
                val className = componentName.className
                android.util.Log.d(TAG, "Activity className: $className")
                
                // Проверяем nonLocalizedLabel и loadLabel
                try {
                    val activityInfo = packageManager.getActivityInfo(componentName, 0)
                    val nonLocalizedLabel = activityInfo.nonLocalizedLabel?.toString()
                    if (!nonLocalizedLabel.isNullOrEmpty() && nonLocalizedLabel != packageName) {
                        android.util.Log.d(TAG, "Layer 2a (nonLocalizedLabel): $nonLocalizedLabel")
                        return nonLocalizedLabel
                    }
                    
                    val activityLabel = activityInfo.loadLabel(packageManager).toString()
                    if (activityLabel.isNotEmpty() && activityLabel != packageName) {
                        android.util.Log.d(TAG, "Layer 2b (loadLabel): $activityLabel")
                        return activityLabel
                    }
                } catch (e: Exception) {
                    android.util.Log.d(TAG, "ActivityInfo lookup failed: ${e.message}")
                }
                
                // 3. Попытка извлечь имя из className (heuristic)
                val derivedName = deriveNameFromClassName(className, packageName)
                if (derivedName != null) {
                    android.util.Log.d(TAG, "Layer 3 (derived): $derivedName")
                    return derivedName
                }
            }
        } catch (e: Exception) {
            android.util.Log.d(TAG, "Layer 2 failed: ${e.message}")
        }
        
        // 4. Финальный Fallback: Имя приложения
        return try {
            val appInfo = packageManager.getApplicationInfo(packageName, 0)
            val appLabel = packageManager.getApplicationLabel(appInfo).toString()
            android.util.Log.d(TAG, "Layer 4 (AppInfo): $appLabel")
            appLabel
        } catch (e: Exception) {
            android.util.Log.d(TAG, "Layer 4 failed: ${e.message}")
            packageName
        }
    }
    
    private fun deriveNameFromClassName(className: String, packageName: String): String? {
        // Извлекаем короткое имя класса (без пакета и суффикса Activity)
        val simpleName = className.substringAfterLast('.')
            .replace("Activity", "")
            .replace("activity", "")
        
        if (simpleName.isEmpty()) return null
        
        // Пытаемся перевести или использовать понятные имена
        val appName = try {
            val appInfo = packageManager.getApplicationInfo(packageName, 0)
            packageManager.getApplicationLabel(appInfo).toString()
        } catch (e: Exception) {
            null
        }
        
        // Определяем тип activity по имени
        return when {
            simpleName.contains("Editor", ignoreCase = true) ||
            simpleName.contains("TextEdit", ignoreCase = true) ||
            simpleName.contains("Writer", ignoreCase = true) ||
            simpleName.contains("Note", ignoreCase = true) -> "$appName блокнот"
            
            simpleName.contains("Explorer", ignoreCase = true) ||
            simpleName.contains("Files", ignoreCase = true) ||
            simpleName.contains("FileMan", ignoreCase = true) ||
            simpleName.contains("Browser", ignoreCase = true) -> "$appName файлы"
            
            simpleName.contains("Player", ignoreCase = true) ||
            simpleName.contains("Video", ignoreCase = true) ||
            simpleName.contains("Media", ignoreCase = true) -> "$appName плеер"
            
            simpleName.contains("Settings", ignoreCase = true) -> "$appName настройки"
            
            simpleName.contains("Main", ignoreCase = true) -> appName
            
            else -> {
                // Используем простое имя activity если оно информативное
                val cleanName = simpleName.replaceFirstChar { it.uppercaseChar() }
                if (cleanName.length > 3 && cleanName != packageName.substringAfterLast('.')) {
                    "$appName $cleanName"
                } else null
            }
        }
    }
    
    private fun getTaskSnapshot(taskId: Int): Bitmap? {
        try {
            val activityManager = getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
            val method = activityManager.javaClass.getMethod("getTaskSnapshot", Int::class.javaPrimitiveType)
            val snapshot = method.invoke(activityManager, taskId)
            
            if (snapshot != null) {
                val snapshotClass = snapshot.javaClass
                val getBitmapMethod = snapshotClass.getMethod("getBitmap")
                val bitmap = getBitmapMethod.invoke(snapshot) as? Bitmap
                android.util.Log.d(TAG, "Got snapshot for task $taskId: ${bitmap != null}")
                return bitmap
            }
        } catch (e: Exception) {
            android.util.Log.w(TAG, "getTaskSnapshot failed for task $taskId", e)
        }
        return null
    }
}