package com.yeamin.decisionwidget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews

class DecisionWidget : AppWidgetProvider() {

    companion object {
        private const val ACTION_DECIDE =
            "com.yeamin.decisionwidget.ACTION_DECIDE"

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
            updateWidget(context, appWidgetManager, widgetId, "Ready?")
        }
    }

    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)

        if (intent.action == ACTION_DECIDE) {
            val widgetId = intent.getIntExtra(
                AppWidgetManager.EXTRA_APPWIDGET_ID,
                AppWidgetManager.INVALID_APPWIDGET_ID
            )

            if (widgetId != AppWidgetManager.INVALID_APPWIDGET_ID) {
                val answer = answers.random()

                updateWidget(
                    context,
                    AppWidgetManager.getInstance(context),
                    widgetId,
                    answer
                )
            }
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
            R.layout.widget_decision
        )

        views.setTextViewText(R.id.result, result)

        val intent = Intent(context, DecisionWidget::class.java).apply {
            action = ACTION_DECIDE
            putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, widgetId)
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            widgetId,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        views.setOnClickPendingIntent(
            R.id.decide_button,
            pendingIntent
        )

        manager.updateAppWidget(widgetId, views)
    }
}
