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
