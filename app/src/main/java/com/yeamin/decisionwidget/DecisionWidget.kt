package com.yeamin.decisionwidget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.content.res.ColorStateList
import android.content.res.Configuration
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.widget.RemoteViews

class DecisionWidget : AppWidgetProvider() {

    data class Personality(
        val caption: String,
        val firstButton: String,
        val repeatButton: String
    )

    data class ShapeResources(
        val widgetDrawable: Int,
        val buttonDrawable: Int
    )

    companion object {

        private const val ACTION_DECIDE =
            "com.yeamin.decisionwidget.ACTION_DECIDE"

        private const val PREFS_NAME =
            "widget_settings"

        private val answers = arrayOf(
            "DO IT!",
            "GO FOR IT!",
            "WAIT",
            "NOT NOW",
            "THINK AGAIN",
            "ABSOLUTELY!"
        )
    }

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        appWidgetIds.forEach { widgetId ->
            refreshWidget(
                context,
                appWidgetManager,
                widgetId
            )
        }
    }

    override fun onAppWidgetOptionsChanged(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetId: Int,
        newOptions: Bundle
    ) {
        super.onAppWidgetOptionsChanged(
            context,
            appWidgetManager,
            appWidgetId,
            newOptions
        )

        refreshWidget(
            context,
            appWidgetManager,
            appWidgetId
        )
    }

    override fun onReceive(
        context: Context,
        intent: Intent
    ) {
        super.onReceive(context, intent)

        if (intent.action == ACTION_DECIDE) {

            // Short tactile click
            performHaptic(context)

            val widgetId = intent.getIntExtra(
                AppWidgetManager.EXTRA_APPWIDGET_ID,
                AppWidgetManager.INVALID_APPWIDGET_ID
            )

            if (
                widgetId !=
                AppWidgetManager.INVALID_APPWIDGET_ID
            ) {

                val answer = answers.random()

                context.getSharedPreferences(
                    PREFS_NAME,
                    Context.MODE_PRIVATE
                ).edit()
                    .putString(
                        "result_$widgetId",
                        answer
                    )
                    .apply()

                updateWidget(
                    context,
                    AppWidgetManager.getInstance(context),
                    widgetId,
                    answer
                )
            }
        }
    }

    override fun onDeleted(
        context: Context,
        appWidgetIds: IntArray
    ) {
        super.onDeleted(context, appWidgetIds)

        val editor = context.getSharedPreferences(
            PREFS_NAME,
            Context.MODE_PRIVATE
        ).edit()

        appWidgetIds.forEach { widgetId ->
            editor.remove("personality_$widgetId")
            editor.remove("theme_$widgetId")
            editor.remove("shape_$widgetId")
            editor.remove("opacity_$widgetId")
            editor.remove("accent_$widgetId")
            editor.remove("accent_opacity_$widgetId")
            editor.remove("result_$widgetId")
        }

        editor.apply()
    }

    private fun refreshWidget(
        context: Context,
        manager: AppWidgetManager,
        widgetId: Int
    ) {

        val result = context.getSharedPreferences(
            PREFS_NAME,
            Context.MODE_PRIVATE
        ).getString(
            "result_$widgetId",
            "Ready?"
        ) ?: "Ready?"

        updateWidget(
            context,
            manager,
            widgetId,
            result
        )
    }

    private fun getPersonality(
        context: Context,
        widgetId: Int
    ): Personality {

        val mode = context.getSharedPreferences(
            PREFS_NAME,
            Context.MODE_PRIVATE
        ).getString(
            "personality_$widgetId",
            "classic"
        )

        return when (mode) {

            "sarcastic" -> Personality(
                "CAN'T DECIDE, HUH?",
                "FINE, TELL ME",
                "REGRETS?"
            )

            "playful" -> Personality(
                "ASKING FATE NOW?",
                "HIT ME",
                "TRY YOUR LUCK"
            )

            "gentle" -> Personality(
                "TAKE A MOMENT",
                "GUIDE ME",
                "THINK AGAIN"
            )

            else -> Personality(
                "NEED A SIGN?",
                "REVEAL",
                "RETHINK"
            )
        }
    }

    private fun chooseLayout(
        manager: AppWidgetManager,
        widgetId: Int
    ): Int {

        val options =
            manager.getAppWidgetOptions(widgetId)

        val width = options.getInt(
            AppWidgetManager.OPTION_APPWIDGET_MIN_WIDTH,
            250
        )

        val height = options.getInt(
            AppWidgetManager.OPTION_APPWIDGET_MIN_HEIGHT,
            110
        )

        return when {

            height < 100 ->
                R.layout.widget_compact

            width < 250 ->
                R.layout.widget_minimal

            else ->
                R.layout.widget_decision
        }
    }

    private fun getShapeResources(
        shape: String
    ): ShapeResources {

        return when (shape) {

            "soft" -> ShapeResources(
                R.drawable.widget_shape_soft,
                R.drawable.button_shape_soft
            )

            "pill" -> ShapeResources(
                R.drawable.widget_shape_pill,
                R.drawable.button_shape_pill
            )

            "minimal" -> ShapeResources(
                R.drawable.widget_shape_minimal,
                R.drawable.button_shape_minimal
            )

            else -> ShapeResources(
                R.drawable.widget_shape_oneui,
                R.drawable.button_shape_oneui
            )
        }
    }

    private fun updateWidget(
        context: Context,
        manager: AppWidgetManager,
        widgetId: Int,
        result: String
    ) {

        val views = RemoteViews(
            context.packageName,
            chooseLayout(
                manager,
                widgetId
            )
        )

        val prefs = context.getSharedPreferences(
            PREFS_NAME,
            Context.MODE_PRIVATE
        )

        val personality =
            getPersonality(
                context,
                widgetId
            )

        val theme = prefs.getString(
            "theme_$widgetId",
            "system"
        ) ?: "system"

        val shape = prefs.getString(
            "shape_$widgetId",
            "oneui"
        ) ?: "oneui"

        val accent = prefs.getString(
            "accent_$widgetId",
            "system"
        ) ?: "system"

        val backgroundOpacity =
            prefs.getInt(
                "opacity_$widgetId",
                90
            ).coerceIn(
                10,
                100
            )

        val accentOpacity =
            prefs.getInt(
                "accent_opacity_$widgetId",
                100
            ).coerceIn(
                10,
                100
            )

        /*
         * Theme
         */

        val systemDark =
            context.resources.configuration.uiMode and
                Configuration.UI_MODE_NIGHT_MASK ==
                Configuration.UI_MODE_NIGHT_YES

        val resolvedTheme =
            if (theme == "system") {

                if (systemDark) {
                    "dark"
                } else {
                    "light"
                }

            } else {
                theme
            }

        val baseBackground =
            when (resolvedTheme) {

                "amoled" ->
                    Color.rgb(0, 0, 0)

                "light" ->
                    Color.rgb(
                        245,
                        245,
                        248
                    )

                else ->
                    Color.rgb(
                        20,
                        20,
                        24
                    )
            }

        val backgroundColor =
            withAlpha(
                baseBackground,
                backgroundOpacity
            )

        /*
         * Text colors
         */

        val primaryText =
            if (resolvedTheme == "light") {

                Color.rgb(
                    25,
                    25,
                    30
                )

            } else {

                Color.WHITE
            }

        val secondaryText =
            if (resolvedTheme == "light") {

                Color.rgb(
                    95,
                    95,
                    105
                )

            } else {

                Color.rgb(
                    185,
                    185,
                    195
                )
            }

        /*
         * User accent
         */

        val baseAccent =
            when (accent) {

                "purple" ->
                    Color.rgb(
                        108,
                        99,
                        255
                    )

                "blue" ->
                    Color.rgb(
                        20,
                        100,
                        244
                    )

                "emerald" ->
                    Color.rgb(
                        34,
                        160,
                        105
                    )

                "teal" ->
                    Color.rgb(
                        0,
                        150,
                        155
                    )

                "amber" ->
                    Color.rgb(
                        225,
                        145,
                        25
                    )

                "coral" ->
                    Color.rgb(
                        238,
                        100,
                        82
                    )

                "rose" ->
                    Color.rgb(
                        220,
                        75,
                        125
                    )

                "graphite" ->
                    Color.rgb(
                        80,
                        82,
                        92
                    )

                else -> {

                    if (
                        Build.VERSION.SDK_INT >=
                        Build.VERSION_CODES.S
                    ) {

                        context.getColor(
                            android.R.color
                                .system_accent1_500
                        )

                    } else {

                        Color.rgb(
                            108,
                            99,
                            255
                        )
                    }
                }
            }

        /*
         * Result mood color
         */

        val moodBaseColor =
            when (result) {

                "DO IT!",
                "GO FOR IT!",
                "ABSOLUTELY!" ->
                    Color.rgb(
                        45,
                        170,
                        105
                    )

                "WAIT",
                "NOT NOW" ->
                    Color.rgb(
                        225,
                        155,
                        45
                    )

                "THINK AGAIN" ->
                    Color.rgb(
                        65,
                        125,
                        235
                    )

                else ->
                    baseAccent
            }

        val moodAccentColor =
            withAlpha(
                moodBaseColor,
                accentOpacity
            )

        /*
         * Widget text
         */

        views.setTextViewText(
            R.id.title,
            personality.caption
        )

        views.setTextViewText(
            R.id.result,
            result
        )

        val actionText =
            if (result == "Ready?") {

                personality.firstButton

            } else {

                personality.repeatButton
            }

        views.setTextViewText(
            R.id.decide_button,
            actionText
        )

        views.setTextColor(
            R.id.title,
            secondaryText
        )

        views.setTextColor(
            R.id.result,
            primaryText
        )

        views.setTextColor(
            R.id.decide_button,
            Color.WHITE
        )

        /*
         * Widget and button shape
         */

        val shapeResources =
            getShapeResources(
                shape
            )

        views.setInt(
            R.id.widget_root,
            "setBackgroundResource",
            shapeResources.widgetDrawable
        )

        views.setInt(
            R.id.decide_button,
            "setBackgroundResource",
            shapeResources.buttonDrawable
        )

        /*
         * Background + accent opacity
         */

        if (
            Build.VERSION.SDK_INT >=
            Build.VERSION_CODES.S
        ) {

            views.setColorStateList(
                R.id.widget_root,
                "setBackgroundTintList",
                ColorStateList.valueOf(
                    backgroundColor
                )
            )

            views.setColorStateList(
                R.id.decide_button,
                "setBackgroundTintList",
                ColorStateList.valueOf(
                    moodAccentColor
                )
            )

        } else {

            views.setInt(
                R.id.widget_root,
                "setBackgroundColor",
                backgroundColor
            )

            views.setInt(
                R.id.decide_button,
                "setBackgroundColor",
                moodAccentColor
            )
        }

        /*
         * Button action
         */

        val decideIntent = Intent(
            context,
            DecisionWidget::class.java
        ).apply {

            action =
                ACTION_DECIDE

            putExtra(
                AppWidgetManager.EXTRA_APPWIDGET_ID,
                widgetId
            )
        }

        val pendingIntent =
            PendingIntent.getBroadcast(
                context,
                widgetId,
                decideIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or
                    PendingIntent.FLAG_IMMUTABLE
            )

        views.setOnClickPendingIntent(
            R.id.decide_button,
            pendingIntent
        )

        manager.updateAppWidget(
            widgetId,
            views
        )
    }

    /*
     * Short, crisp haptic click
     */

    private fun performHaptic(
        context: Context
    ) {

        try {

            val vibrator: Vibrator =
                if (
                    Build.VERSION.SDK_INT >=
                    Build.VERSION_CODES.S
                ) {

                    val vibratorManager =
                        context.getSystemService(
                            VibratorManager::class.java
                        )

                    vibratorManager.defaultVibrator

                } else {

                    @Suppress("DEPRECATION")
                    context.getSystemService(
                        Context.VIBRATOR_SERVICE
                    ) as Vibrator
                }

            if (!vibrator.hasVibrator()) {
                return
            }

            if (
                Build.VERSION.SDK_INT >=
                Build.VERSION_CODES.Q
            ) {

                vibrator.vibrate(
                    VibrationEffect.createPredefined(
                        VibrationEffect.EFFECT_CLICK
                    )
                )

            } else {

                vibrator.vibrate(
                    VibrationEffect.createOneShot(
                        20L,
                        VibrationEffect.DEFAULT_AMPLITUDE
                    )
                )
            }

        } catch (_: Exception) {

            // Widget continues working even if
            // haptic feedback is unavailable.

        }
    }

    private fun withAlpha(
        color: Int,
        opacity: Int
    ): Int {

        return Color.argb(
            (255 * opacity / 100f).toInt(),
            Color.red(color),
            Color.green(color),
            Color.blue(color)
        )
    }
}