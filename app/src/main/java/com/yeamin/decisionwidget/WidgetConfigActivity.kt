package com.yeamin.decisionwidget

import android.app.Activity
import android.appwidget.AppWidgetManager
import android.content.Intent
import android.graphics.Color
import android.graphics.Typeface
import android.os.Bundle
import android.view.Gravity
import android.view.ViewGroup
import android.widget.*

class WidgetConfigActivity : Activity() {

    private var widgetId = AppWidgetManager.INVALID_APPWIDGET_ID

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setResult(RESULT_CANCELED)

        widgetId = intent?.getIntExtra(
            AppWidgetManager.EXTRA_APPWIDGET_ID,
            AppWidgetManager.INVALID_APPWIDGET_ID
        ) ?: AppWidgetManager.INVALID_APPWIDGET_ID

        if (widgetId == AppWidgetManager.INVALID_APPWIDGET_ID) {
            finish()
            return
        }

        val prefs = getSharedPreferences("widget_settings", MODE_PRIVATE)
        val density = resources.displayMetrics.density
        fun dp(value: Int) = (value * density).toInt()

        fun sectionTitle(value: String) = TextView(this).apply {
            text = value
            textSize = 11f
            letterSpacing = 0.12f
            setTextColor(Color.rgb(150, 150, 165))
            setTypeface(typeface, Typeface.BOLD)
            setPadding(0, dp(22), 0, dp(8))
        }

        fun radio(label: String, value: String) = RadioButton(this).apply {
            id = ViewGroup.generateViewId()
            tag = value
            text = label
            textSize = 15f
            setTextColor(Color.WHITE)
            setPadding(dp(4), dp(5), dp(4), dp(5))
        }

        fun addOptions(
            root: LinearLayout,
            title: String,
            options: List<Pair<String, String>>,
            savedValue: String
        ): RadioGroup {
            root.addView(sectionTitle(title))

            return RadioGroup(this).also { group ->
                options.forEach { option ->
                    val item = radio(option.first, option.second)
                    group.addView(item)

                    if (option.second == savedValue) {
                        item.isChecked = true
                    }
                }

                root.addView(group)
            }
        }

        fun addOpacityControl(
            root: LinearLayout,
            title: String,
            savedValue: Int
        ): SeekBar {
            root.addView(sectionTitle(title))

            val safeValue = savedValue.coerceIn(10, 100)

            val valueText = TextView(this).apply {
                text = "$safeValue%"
                textSize = 18f
                gravity = Gravity.END
                setTextColor(Color.WHITE)
                setTypeface(typeface, Typeface.BOLD)
            }

            root.addView(valueText)

            val slider = SeekBar(this).apply {
                max = 9
                progress = safeValue / 10 - 1

                setOnSeekBarChangeListener(
                    object : SeekBar.OnSeekBarChangeListener {
                        override fun onProgressChanged(
                            seekBar: SeekBar?,
                            progress: Int,
                            fromUser: Boolean
                        ) {
                            valueText.text = "${(progress + 1) * 10}%"
                        }

                        override fun onStartTrackingTouch(seekBar: SeekBar?) {}
                        override fun onStopTrackingTouch(seekBar: SeekBar?) {}
                    }
                )
            }

            root.addView(slider)

            val range = LinearLayout(this).apply {
                orientation = LinearLayout.HORIZONTAL
            }

            range.addView(
                TextView(this).apply {
                    text = "10%"
                    setTextColor(Color.rgb(150, 150, 165))
                },
                LinearLayout.LayoutParams(
                    0,
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    1f
                )
            )

            range.addView(
                TextView(this).apply {
                    text = "100%"
                    gravity = Gravity.END
                    setTextColor(Color.rgb(150, 150, 165))
                },
                LinearLayout.LayoutParams(
                    0,
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    1f
                )
            )

            root.addView(range)
            return slider
        }

        fun selected(group: RadioGroup, fallback: String): String {
            val selectedId = group.checkedRadioButtonId
            if (selectedId == -1) return fallback

            return group.findViewById<RadioButton>(selectedId)
                ?.tag
                ?.toString()
                ?: fallback
        }

        val scroll = ScrollView(this)

        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(24), dp(28), dp(24), dp(30))
            setBackgroundColor(Color.rgb(20, 20, 24))
        }

        root.addView(TextView(this).apply {
            text = "Decision Maker"
            textSize = 28f
            setTextColor(Color.WHITE)
            setTypeface(typeface, Typeface.BOLD)
        })

        root.addView(TextView(this).apply {
            text = "Make this widget yours."
            textSize = 14f
            setTextColor(Color.rgb(175, 175, 188))
            setPadding(0, dp(5), 0, dp(4))
        })

        val personalityGroup = addOptions(
            root,
            "PERSONALITY",
            listOf(
                "Classic  ·  Need a sign?" to "classic",
                "Sarcastic  ·  Can't decide, huh?" to "sarcastic",
                "Playful  ·  Asking fate now?" to "playful",
                "Gentle  ·  Take a moment" to "gentle"
            ),
            prefs.getString(
                "personality_$widgetId",
                "classic"
            ) ?: "classic"
        )

        val themeGroup = addOptions(
            root,
            "THEME",
            listOf(
                "System" to "system",
                "Dark" to "dark",
                "AMOLED" to "amoled",
                "Light" to "light"
            ),
            prefs.getString(
                "theme_$widgetId",
                "system"
            ) ?: "system"
        )

        val shapeGroup = addOptions(
            root,
            "SHAPE",
            listOf(
                "One UI  ·  Smooth and rounded" to "oneui",
                "Soft  ·  Moderately rounded" to "soft",
                "Pill  ·  Extra rounded" to "pill",
                "Minimal  ·  Sharper corners" to "minimal"
            ),
            prefs.getString(
                "shape_$widgetId",
                "oneui"
            ) ?: "oneui"
        )

        val backgroundOpacitySlider = addOpacityControl(
            root,
            "BACKGROUND OPACITY",
            prefs.getInt("opacity_$widgetId", 90)
        )

        val accentGroup = addOptions(
            root,
            "ACCENT COLOR",
            listOf(
                "Dynamic / System" to "system",
                "Purple" to "purple",
                "Samsung Blue" to "blue",
                "Emerald" to "emerald",
                "Teal" to "teal",
                "Amber" to "amber",
                "Coral" to "coral",
                "Rose" to "rose",
                "Graphite" to "graphite"
            ),
            prefs.getString(
                "accent_$widgetId",
                "system"
            ) ?: "system"
        )

        val accentOpacitySlider = addOpacityControl(
            root,
            "ACCENT OPACITY",
            prefs.getInt("accent_opacity_$widgetId", 100)
        )

        val applyButton = Button(this).apply {
            text = "APPLY"
            textSize = 14f
            setTextColor(Color.WHITE)
            setTypeface(typeface, Typeface.BOLD)
            setBackgroundColor(Color.rgb(108, 99, 255))

            setOnClickListener {
                prefs.edit()
                    .putString(
                        "personality_$widgetId",
                        selected(personalityGroup, "classic")
                    )
                    .putString(
                        "theme_$widgetId",
                        selected(themeGroup, "system")
                    )
                    .putString(
                        "shape_$widgetId",
                        selected(shapeGroup, "oneui")
                    )
                    .putInt(
                        "opacity_$widgetId",
                        (backgroundOpacitySlider.progress + 1) * 10
                    )
                    .putString(
                        "accent_$widgetId",
                        selected(accentGroup, "system")
                    )
                    .putInt(
                        "accent_opacity_$widgetId",
                        (accentOpacitySlider.progress + 1) * 10
                    )
                    .apply()

                val updateIntent = Intent(
                    this@WidgetConfigActivity,
                    DecisionWidget::class.java
                ).apply {
                    action = AppWidgetManager.ACTION_APPWIDGET_UPDATE
                    putExtra(
                        AppWidgetManager.EXTRA_APPWIDGET_IDS,
                        intArrayOf(widgetId)
                    )
                }

                sendBroadcast(updateIntent)

                setResult(
                    RESULT_OK,
                    Intent().apply {
                        putExtra(
                            AppWidgetManager.EXTRA_APPWIDGET_ID,
                            widgetId
                        )
                    }
                )

                finish()
            }
        }

        root.addView(
            applyButton,
            LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                dp(54)
            ).apply {
                topMargin = dp(28)
            }
        )

        scroll.addView(root)
        setContentView(scroll)
    }
}