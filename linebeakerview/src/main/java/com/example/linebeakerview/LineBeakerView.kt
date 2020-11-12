package com.example.linebeakerview

import android.view.View
import android.view.MotionEvent
import android.graphics.Paint
import android.graphics.Color
import android.graphics.Canvas
import android.graphics.Path
import android.graphics.RectF

val parts : Int = 5
val strokeFactor : Float = 90f
val sizeFactor : Float = 2.9f
val wSizeFactor : Float = 7.8f
val delay : Long = 20
val rot : Float = 90f
val scGap : Float = 0.02f / parts
val backColor : Int = Color.parseColor("#BDBDBD")
val colors : Array<Int> = arrayOf(
    "#F44336",
    "#009688",
    "#3F51B5",
    "#4CAF50",
    "#FF5722"
).map {
    Color.parseColor(it)
}.toTypedArray()

fun Int.inverse() : Float = 1f / this
fun Float.maxScale(i : Int, n : Int) : Float = Math.max(0f, this - i * n.inverse())
fun Float.divideScale(i : Int, n : Int) : Float = Math.min(n.inverse(), maxScale(i, n)) * n
fun Float.sinify() : Float = Math.sin(this * Math.PI).toFloat()


fun Canvas.drawLineBeakerPath(sf : Float, size : Float, wSize : Float, paint : Paint) {
    val xEnd : Float = size + wSize
    val x : Float = xEnd - (2 * size + wSize) * sf.divideScale(parts - 1, parts)
    paint.style = Paint.Style.FILL
    save()
    val path : Path = Path()
    path.moveTo(-size, -wSize)
    path.lineTo(size, wSize)
    path.arcTo(RectF(size - wSize, -wSize, size + wSize, wSize), -90f, 180f)
    path.lineTo(-size, wSize)
    clipPath(path)
    drawRect(RectF(x, -wSize, xEnd, wSize), paint)
    restore()
}

fun Canvas.drawLineBeaker(scale : Float, w : Float, h : Float, paint : Paint) {
    val size : Float = Math.min(w, h) / sizeFactor
    val wSize : Float = Math.min(w, h) / wSizeFactor
    val sf : Float = scale.sinify()
    save()
    translate(w / 2, h / 2)
    rotate(rot * sf.divideScale(3, parts))
    paint.style = Paint.Style.STROKE
    for (j in 0..1) {
        drawLine(-size, -wSize + 2 * wSize * j, -size + 2 * size * sf.divideScale(j * 2, parts), -wSize + 2 * wSize * j, paint)
    }
    drawArc(RectF(size - wSize, -wSize, size + wSize, wSize), -90f, 180f * sf.divideScale(1, parts), false, paint)
    drawLineBeakerPath(sf, size, wSize, paint)
    restore()
}

fun Canvas.drawLBNode(i : Int, scale : Float, paint : Paint) {
    val w : Float = width.toFloat()
    val h : Float = height.toFloat()
    paint.color = colors[i]
    paint.strokeCap = Paint.Cap.ROUND
    paint.strokeWidth = Math.min(w, h) / strokeFactor
    drawLineBeaker(scale, w, h, paint)
}
