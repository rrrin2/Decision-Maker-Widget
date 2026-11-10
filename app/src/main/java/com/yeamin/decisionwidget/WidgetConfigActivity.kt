package com.yeamin.decisionwidget

import android.app.Activity
import android.appwidget.AppWidgetManager
import android.content.Intent
import android.os.Bundle
import android.graphics.Color
import android.graphics.Typeface
import android.view.Gravity
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.TextView

class WidgetConfigActivity : Activity() {

    private var widgetId = AppWidgetManager.INVALID_APPWIDGET_ID

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setResult(RESULT_CANCELED)

        widgetId = intent?.extras?.getInt(
            AppWidgetManager.EXTRA_APPWIDGET_ID,
            AppWidgetManager.INVALID_APPWIDGET_ID
        ) ?: AppWidgetManager.INVALID_APPWIDGET_ID

        if (widgetId == AppWidgetManager.INVALID_APPWIDGET_ID) {
            finish()
            return
        }

        val density = resources.displayMetrics.density
        fun dp(value: Int) = (value * density).toInt()

        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER_HORIZONTAL
            setPadding(dp(24), dp(32), dp(24), dp(24))
            setBackgroundColor(Color.rgb(20, 20, 24))
        }

        val heading = TextView(this).apply {
            text = "Choose a personality"
            textSize = 26f
            setTextColor(Color.WHITE)
            setTypeface(typeface, Typeface.BOLD)
        }

        val description = TextView(this).apply {
            text = "Each widget can have its own voice."
            textSize = 14f
            setTextColor(Color.rgb(180, 180, 190))
            setPadding(0, dp(8), 0, dp(24))
        }

        val group = RadioGroup(this).apply {
            orientation = RadioGroup.VERTICAL
        }

        val options = listOf(
            Triple("classic", "Classic", "Need a sign?"),
            Triple("sarcastic", "Sarcastic", "Can't decide, huh?"),
            Triple("playful", "Playful", "Asking fate now?"),
            Triple("gentle", "Gentle", "Take a moment")
        )

        options.forEachIndexed { index, option ->
            val button = RadioButton(this).apply {
                id = ViewGroup.generateViewId()
                tag = option.first
                text = "${option.second}\n${option.third}"
                textSize = 16f
                setTextColor(Color.WHITE)
                setPadding(dp(8), dp(10), dp(8), dp(10))

                if (index == 0) {
                    isChecked = true
                }
            }

            group.addView(button)
        }

        val saveButton = Button(this).apply {
            text = "APPLY"
            textSize = 14f
            setTextColor(Color.WHITE)
            setBackgroundColor(Color.rgb(108, 99, 255))

            setOnClickListener {
                val selectedButton =
                    group.findViewById<RadioButton>(group.checkedRadioButtonId)

                val selectedMode =
                    selectedButton?.tag?.toString() ?: "classic"

                getSharedPreferences(
                    "widget_settings",
                    MODE_PRIVATE
                ).edit()
                    .putString("personality_$widgetId", selectedMode)
                    .apply()

                val updateIntent =
                    Intent(this@WidgetConfigActivity, DecisionWidget::class.java).apply {
                        action = AppWidgetManager.ACTION_APPWIDGET_UPDATE
                        putExtra(
                            AppWidgetManager.EXTRA_APPWIDGET_IDS,
                            intArrayOf(widgetId)
                        )
                    }

                sendBroadcast(updateIntent)

                val resultIntent = Intent().apply {
                    putExtra(
                        AppWidgetManager.EXTRA_APPWIDGET_ID,
                        widgetId
                    )
                }

                setResult(RESULT_OK, resultIntent)
                finish()
            }
        }

        root.addView(heading)
        root.addView(description)

        root.addView(
            group,
            LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                0,
                1f
            )
        )

        root.addView(
            saveButton,
            LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                dp(52)
            )
        )

        setContentView(root)
    }
}
