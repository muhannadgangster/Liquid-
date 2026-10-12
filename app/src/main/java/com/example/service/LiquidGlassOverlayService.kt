package com.example.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.graphics.PixelFormat
import android.os.Build
import android.os.IBinder
import android.provider.Settings
import android.util.TypedValue
import android.view.Gravity
import android.view.WindowManager
import androidx.core.app.NotificationCompat
import com.example.MainActivity
import com.example.R
import com.example.data.GlassMaterialPreset
import com.example.data.GlassPreferences
import com.example.data.InstalledAppsRepository
import com.example.data.PreferencesRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

class LiquidGlassOverlayService : Service() {

    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    private lateinit var windowManager: WindowManager
    private lateinit var prefsRepo: PreferencesRepository
    private lateinit var appsRepo: InstalledAppsRepository

    private var bubbleView: LiquidGlassBubbleView? = null
    private var bubbleLayoutParams: WindowManager.LayoutParams? = null

    private var expandedPanelView: LiquidGlassExpandedPanelView? = null
    private var expandedLayoutParams: WindowManager.LayoutParams? = null

    private var isExpanded = false
    private var currentPreferences = GlassPreferences()
    private var foregroundDetector: ForegroundAppDetector? = null

    companion object {
        const val CHANNEL_ID = "liquid_glass_overlay_channel"
        const val NOTIFICATION_ID = 1001

        const val ACTION_START = "com.example.liquidglass.ACTION_START"
        const val ACTION_EMERGENCY_STOP = "com.example.liquidglass.ACTION_EMERGENCY_STOP"
        const val ACTION_TOGGLE_PANEL = "com.example.liquidglass.ACTION_TOGGLE_PANEL"

        fun start(context: Context) {
            val intent = Intent(context, LiquidGlassOverlayService::class.java).apply {
                action = ACTION_START
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(intent)
            } else {
                context.startService(intent)
            }
        }

        fun stop(context: Context) {
            val intent = Intent(context, LiquidGlassOverlayService::class.java).apply {
                action = ACTION_EMERGENCY_STOP
            }
            context.startService(intent)
        }
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        windowManager = getSystemService(Context.WINDOW_SERVICE) as WindowManager
        prefsRepo = PreferencesRepository(this)
        appsRepo = InstalledAppsRepository(this)

        createNotificationChannel()
        startForeground(NOTIFICATION_ID, buildNotification())

        if (!Settings.canDrawOverlays(this)) {
            stopSelf()
            return
        }

        initOverlayBubble()
        observePreferences()
        initForegroundDetector()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_EMERGENCY_STOP -> {
                emergencyStop()
                return START_NOT_STICKY
            }
            ACTION_TOGGLE_PANEL -> {
                toggleExpandedPanel()
            }
        }
        return START_STICKY
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                getString(R.string.overlay_notification_title),
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = getString(R.string.overlay_notification_text)
                setShowBadge(false)
            }
            val manager = getSystemService(NotificationManager::class.java)
            manager?.createNotificationChannel(channel)
        }
    }

    private fun buildNotification(): Notification {
        val openAppIntent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP
        }
        val openPendingIntent = PendingIntent.getActivity(
            this, 0, openAppIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val emergencyStopIntent = Intent(this, LiquidGlassOverlayService::class.java).apply {
            action = ACTION_EMERGENCY_STOP
        }
        val emergencyPendingIntent = PendingIntent.getService(
            this, 1, emergencyStopIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_liquid_glass_1791827191031)
            .setContentTitle(getString(R.string.overlay_notification_title))
            .setContentText(getString(R.string.overlay_notification_text))
            .setContentIntent(openPendingIntent)
            .setOngoing(true)
            .addAction(
                android.R.drawable.ic_menu_close_clear_cancel,
                getString(R.string.action_emergency_stop),
                emergencyPendingIntent
            )
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build()
    }

    private fun initOverlayBubble() {
        val bubbleSizePx = dpToPx(currentPreferences.bubbleSizeDp)

        val layoutType = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
        } else {
            @Suppress("DEPRECATION")
            WindowManager.LayoutParams.TYPE_PHONE
        }

        bubbleLayoutParams = WindowManager.LayoutParams(
            bubbleSizePx,
            bubbleSizePx,
            layoutType,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                    WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.TOP or Gravity.START
            x = 24
            y = dpToPx(160)
        }

        bubbleView = LiquidGlassBubbleView(
            context = this,
            windowManager = windowManager,
            layoutParams = bubbleLayoutParams!!,
            onBubbleClicked = { toggleExpandedPanel() }
        ).apply {
            updatePreferences(currentPreferences)
        }

        try {
            windowManager.addView(bubbleView, bubbleLayoutParams)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun toggleExpandedPanel() {
        if (isExpanded) {
            collapsePanel()
        } else {
            expandPanel()
        }
    }

    private fun expandPanel() {
        if (isExpanded) return
        isExpanded = true

        val layoutType = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
        } else {
            @Suppress("DEPRECATION")
            WindowManager.LayoutParams.TYPE_PHONE
        }

        expandedLayoutParams = WindowManager.LayoutParams(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.MATCH_PARENT,
            layoutType,
            WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL or
                    WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH,
            PixelFormat.TRANSLUCENT
        )

        expandedPanelView = LiquidGlassExpandedPanelView(
            context = this,
            onClose = { collapsePanel() },
            onEmergencyStop = { emergencyStop() },
            onPresetSelected = { preset ->
                serviceScope.launch {
                    prefsRepo.updatePreset(preset)
                }
            },
            onTransparencyChanged = { trans ->
                serviceScope.launch {
                    prefsRepo.updateTransparency(trans)
                }
            }
        ).apply {
            updatePreferences(currentPreferences)
        }

        try {
            windowManager.addView(expandedPanelView, expandedLayoutParams)
            bubbleView?.visibility = android.view.View.GONE
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun collapsePanel() {
        if (!isExpanded) return
        isExpanded = false
        try {
            expandedPanelView?.let { windowManager.removeView(it) }
        } catch (e: Exception) {
            // Ignore
        }
        expandedPanelView = null
        bubbleView?.visibility = android.view.View.VISIBLE
    }

    private fun emergencyStop() {
        collapsePanel()
        removeBubbleView()
        serviceScope.launch {
            prefsRepo.updateOverlayEnabled(false)
        }
        stopForeground(STOP_FOREGROUND_REMOVE)
        stopSelf()
    }

    private fun removeBubbleView() {
        try {
            bubbleView?.let { windowManager.removeView(it) }
        } catch (e: Exception) {
            // Ignore
        }
        bubbleView = null
    }

    private fun observePreferences() {
        prefsRepo.preferencesFlow.onEach { prefs ->
            currentPreferences = prefs
            if (!prefs.masterEnabled || !prefs.overlayEnabled) {
                emergencyStop()
                return@onEach
            }

            bubbleView?.updatePreferences(prefs)
            expandedPanelView?.updatePreferences(prefs)

            // Resize bubble if dimension changed
            val targetSize = dpToPx(prefs.bubbleSizeDp)
            bubbleLayoutParams?.let { lp ->
                if (lp.width != targetSize || lp.height != targetSize) {
                    lp.width = targetSize
                    lp.height = targetSize
                    try {
                        bubbleView?.let { windowManager.updateViewLayout(it, lp) }
                    } catch (e: Exception) {
                        // Ignore
                    }
                }
            }
        }.launchIn(serviceScope)
    }

    private fun initForegroundDetector() {
        foregroundDetector = ForegroundAppDetector(this, serviceScope) { pkgName ->
            if (currentPreferences.perAppProfilesEnabled) {
                val (isEnabled, preset) = appsRepo.getAppProfile(pkgName)
                if (!isEnabled) {
                    // Temporarily hide bubble over disabled app
                    bubbleView?.visibility = android.view.View.GONE
                } else {
                    bubbleView?.visibility = if (isExpanded) android.view.View.GONE else android.view.View.VISIBLE
                    val updatedPrefs = currentPreferences.copy(preset = preset)
                    bubbleView?.updatePreferences(updatedPrefs)
                    expandedPanelView?.updatePreferences(updatedPrefs)
                }
            }
        }
        foregroundDetector?.startPolling()
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        // Adjust coordinates within bounds after rotation
        val metrics = resources.displayMetrics
        bubbleLayoutParams?.let { lp ->
            lp.x = lp.x.coerceIn(0, metrics.widthPixels - lp.width)
            lp.y = lp.y.coerceIn(0, metrics.heightPixels - lp.height)
            try {
                bubbleView?.let { windowManager.updateViewLayout(it, lp) }
            } catch (e: Exception) {
                // Ignore
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        foregroundDetector?.stopPolling()
        collapsePanel()
        removeBubbleView()
        serviceScope.cancel()
    }

    private fun dpToPx(dp: Int): Int {
        return TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP,
            dp.toFloat(),
            resources.displayMetrics
        ).toInt()
    }
}
