package com.selbiconsulting.elog.ui.main.common

import android.content.Context
import android.graphics.DashPathEffect
import android.graphics.Paint
import android.widget.FrameLayout
import androidx.core.content.ContextCompat
import com.jjoe64.graphview.GraphView
import com.jjoe64.graphview.helper.StaticLabelsFormatter
import com.jjoe64.graphview.series.DataPoint
import com.jjoe64.graphview.series.LineGraphSeries
import com.selbiconsulting.elog.R
import com.selbiconsulting.elog.ui.util.UiHelper

class GraphViewHelper(
    private val context: Context,
    private val graphView: GraphView,
) {

    init {
        val labels = arrayOf("OFF", "SB", "DR", "ON", "")
        val reversedLabels = labels.reversedArray()

        val labelsH = arrayOf(
            "M",
            "1",
            "2",
            "3",
            "4",
            "5",
            "6",
            "7",
            "8",
            "9",
            "10",
            "11",
            "N",
            "1",
            "2",
            "3",
            "4",
            "5",
            "6",
            "7",
            "8",
            "9",
            "10",
            "11",
            "M"
        )

        val staticLabelsFormatter = StaticLabelsFormatter(graphView)
        staticLabelsFormatter.setVerticalLabels(reversedLabels)
        staticLabelsFormatter.setHorizontalLabels(labelsH)
        graphView.gridLabelRenderer.labelFormatter = staticLabelsFormatter
        graphView.gridLabelRenderer.labelsSpace = 0
        graphView.gridLabelRenderer.padding = 0


        // Set text size in dp
        val textSizeInPx = UiHelper(context).dpToPxFloat(7)
        graphView.gridLabelRenderer.textSize = textSizeInPx

        // Set label color
        graphView.gridLabelRenderer.verticalLabelsColor =
            ContextCompat.getColor(context, R.color.text_primary)
        graphView.gridLabelRenderer.horizontalLabelsColor =
            ContextCompat.getColor(context, R.color.text_primary)

        // Hide labels
        graphView.gridLabelRenderer.isVerticalLabelsVisible = false
        graphView.gridLabelRenderer.isHorizontalLabelsVisible = false

        // Set axis color
        graphView.gridLabelRenderer.gridColor =
            ContextCompat.getColor(context, R.color.transparent)

        graphView.viewport.setMinX(0.0)
        graphView.viewport.setMaxX(24.0)
        graphView.gridLabelRenderer.isHighlightZeroLines = false;

        graphView.viewport.setMinY(0.0)
        graphView.viewport.setMaxY(4.0)

        graphView.viewport.isXAxisBoundsManual = true
        graphView.viewport.isYAxisBoundsManual = true

        graphView.gridLabelRenderer.numHorizontalLabels = 25
        graphView.gridLabelRenderer.setHumanRounding(false)
    }

    fun setSeriesSolid(series: LineGraphSeries<DataPoint>) {
        series.color = ContextCompat.getColor(context, R.color.primary_brand)
        graphView.addSeries(series)
    }

    fun removeAllSeries() {
        graphView.removeAllSeries()
    }

    fun setSeriesDashed(series: LineGraphSeries<DataPoint>) {
        series.color = ContextCompat.getColor(context, R.color.primary_brand)

        val paint = Paint()
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = 5f // Customize the stroke width as needed
        paint.color = series.color
        paint.pathEffect = DashPathEffect(floatArrayOf(10f, 10f), 0f)

        series.setCustomPaint(paint)
        graphView.addSeries(series)
    }

    fun setSeriesWithColor(series: LineGraphSeries<DataPoint>, colorResId: Int) {
        series.color = ContextCompat.getColor(context, colorResId)
        graphView.addSeries(series)
    }
}
