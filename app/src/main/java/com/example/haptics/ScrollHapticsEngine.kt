package com.example.haptics

import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.view.HapticFeedbackConstants
import android.view.View
import com.example.model.HapticFeel

/**
 * Tactile haptics engine tailored for infinite feed scrolling, story scrubbing,
 * and double-tap form appreciation. Combines modern VibrationEffect primitives
 * with View.performHapticFeedback fallback for broad device compatibility.
 */
class ScrollHapticsEngine(context: Context) {

    private val vibrator: Vibrator? = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        val manager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
        manager?.defaultVibrator
    } else {
        @Suppress("DEPRECATION")
        context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
    }

    private var lastMicroTickMs: Long = 0L
    private var lastDetentMs: Long = 0L

    /**
     * Triggered when a new form card settles or passes the focal viewport boundary.
     */
    fun triggerScrollDetent(feel: HapticFeel, view: View? = null) {
        if (feel == HapticFeel.OFF) return
        val now = System.currentTimeMillis()
        if (now - lastDetentMs < 48L) return
        lastDetentMs = now

        val vib = vibrator
        if (vib == null || !vib.hasVibrator()) {
            view?.performHapticFeedback(HapticFeedbackConstants.CLOCK_TICK)
            return
        }

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                val effectId = when (feel) {
                    HapticFeel.CRISP_DETENT -> VibrationEffect.EFFECT_CLICK
                    HapticFeel.SOFT_TICK -> VibrationEffect.EFFECT_TICK
                    HapticFeel.HEAVY_CLICK -> VibrationEffect.EFFECT_HEAVY_CLICK
                    HapticFeel.OFF -> return
                }
                vib.vibrate(VibrationEffect.createPredefined(effectId))
            } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val (duration, amplitude) = when (feel) {
                    HapticFeel.CRISP_DETENT -> 14L to 120
                    HapticFeel.SOFT_TICK -> 8L to 65
                    HapticFeel.HEAVY_CLICK -> 24L to 200
                    HapticFeel.OFF -> return
                }
                vib.vibrate(VibrationEffect.createOneShot(duration, amplitude))
            } else {
                @Suppress("DEPRECATION")
                vib.vibrate(if (feel == HapticFeel.HEAVY_CLICK) 20L else 10L)
            }
        } catch (_: SecurityException) {
            view?.performHapticFeedback(HapticFeedbackConstants.CLOCK_TICK)
        }
    }

    /**
     * Subtle mechanical watch-crown tick fired periodically during continuous scroll drag.
     */
    fun triggerMicroScrollTick(feel: HapticFeel, view: View? = null) {
        if (feel == HapticFeel.OFF) return
        val now = System.currentTimeMillis()
        if (now - lastMicroTickMs < 38L) return
        lastMicroTickMs = now

        val vib = vibrator
        if (vib == null || !vib.hasVibrator()) {
            view?.performHapticFeedback(HapticFeedbackConstants.CLOCK_TICK)
            return
        }

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val (duration, amplitude) = when (feel) {
                    HapticFeel.CRISP_DETENT -> 6L to 55
                    HapticFeel.SOFT_TICK -> 4L to 35
                    HapticFeel.HEAVY_CLICK -> 10L to 110
                    HapticFeel.OFF -> return
                }
                vib.vibrate(VibrationEffect.createOneShot(duration, amplitude))
            } else {
                @Suppress("DEPRECATION")
                vib.vibrate(6L)
            }
        } catch (_: SecurityException) {
            view?.performHapticFeedback(HapticFeedbackConstants.CLOCK_TICK)
        }
    }

    /**
     * Distinctive double-beat heartbeat pulse for double-tapping a form to appreciate it.
     */
    fun triggerHeartBeat(feel: HapticFeel, view: View? = null) {
        if (feel == HapticFeel.OFF) return
        val vib = vibrator
        if (vib == null || !vib.hasVibrator()) {
            view?.performHapticFeedback(HapticFeedbackConstants.CONFIRM)
            return
        }

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val peak = when (feel) {
                    HapticFeel.CRISP_DETENT -> 150
                    HapticFeel.SOFT_TICK -> 85
                    HapticFeel.HEAVY_CLICK -> 230
                    HapticFeel.OFF -> return
                }
                val timings = longArrayOf(0L, 18L, 48L, 26L)
                val amplitudes = intArrayOf(0, (peak * 0.65f).toInt(), 0, peak)
                vib.vibrate(VibrationEffect.createWaveform(timings, amplitudes, -1))
            } else {
                @Suppress("DEPRECATION")
                vib.vibrate(longArrayOf(0L, 16L, 45L, 24L), -1)
            }
        } catch (_: SecurityException) {
            view?.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS)
        }
    }

    /**
     * Crisp selection click for buttons, bookmarks, tabs, and studio pickers.
     */
    fun triggerActionClick(feel: HapticFeel, view: View? = null) {
        if (feel == HapticFeel.OFF) return
        val vib = vibrator
        if (vib == null || !vib.hasVibrator()) {
            view?.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY)
            return
        }

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                val effectId = when (feel) {
                    HapticFeel.SOFT_TICK -> VibrationEffect.EFFECT_TICK
                    HapticFeel.CRISP_DETENT -> VibrationEffect.EFFECT_CLICK
                    HapticFeel.HEAVY_CLICK -> VibrationEffect.EFFECT_HEAVY_CLICK
                    HapticFeel.OFF -> return
                }
                vib.vibrate(VibrationEffect.createPredefined(effectId))
            } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vib.vibrate(VibrationEffect.createOneShot(10L, 95))
            } else {
                @Suppress("DEPRECATION")
                vib.vibrate(10L)
            }
        } catch (_: SecurityException) {
            view?.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY)
        }
    }
}
