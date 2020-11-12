package com.example.linebeakerview

import android.view.View
import android.view.MotionEvent
import android.graphics.Paint
import android.graphics.Color
import android.graphics.Canvas
import android.graphics.Path
import android.graphics.RectF
import android.content.Context
import android.app.Activity

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

class LineBeakerView(ctx : Context) : View(ctx) {

    private val renderer : Renderer = Renderer(this)

    override fun onDraw(canvas : Canvas) {
        renderer.render(canvas)
    }

    override fun onTouchEvent(event : MotionEvent) : Boolean {
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                renderer.handleTap()
            }
        }
        return true
    }

    data class State(var scale : Float = 0f, var dir : Float = 0f, var prevScale : Float = 0f) {

        fun update(cb : (Float) -> Unit) {
            scale += scGap * dir
            if (Math.abs(scale - prevScale) > 1) {
                scale = prevScale + dir
                dir = 0f
                prevScale = scale
                cb(prevScale)
            }
        }

        fun startUpdating(cb : () -> Unit) {
            if (dir == 0f) {
                dir = 1f - 2 * prevScale
                cb()
            }
        }
    }

    data class Animator(var view : View, var animated : Boolean = false) {

        fun animate(cb : () -> Unit) {
            if (animated) {
                cb()
                try {
                    Thread.sleep(delay)
                    view.invalidate()
                } catch(ex : Exception) {

                }
            }
        }

        fun start() {
            if (!animated) {
                animated = true
                view.postInvalidate()
            }
        }

        fun stop() {
            if (animated) {
                animated = false
            }
        }
    }

    data class LBNode(var i : Int, val state : State = State()) {

        private var next : LBNode? = null
        private var prev : LBNode? = null

        init {
            addNeighbor()
        }

        fun addNeighbor() {
            if (i < colors.size - 1) {
                next = LBNode(i + 1)
                next?.prev = this
            }
        }

        fun draw(canvas : Canvas, paint : Paint) {
            canvas.drawLBNode(i, state.scale, paint)
        }

        fun update(cb : (Float) -> Unit) {
            state.update(cb)
        }

        fun startUpdating(cb : () -> Unit) {
            state.startUpdating(cb)
        }

        fun getNext(dir : Int, cb : () -> Unit) : LBNode {
            var curr : LBNode? = prev
            if (dir == 1) {
                curr = next
            }
            if (curr != null) {
                return curr
            }
            cb()
            return this
        }
    }

    data class LineBeaker(var i : Int) {

        private var curr : LBNode = LBNode(0)
        private var dir : Int = 1

        fun draw(canvas : Canvas, paint : Paint) {
            curr.draw(canvas, paint)
        }

        fun update(cb : (Float) -> Unit) {
            curr.update {
                curr = curr.getNext(dir) {
                    dir *= -1
                }
                cb(it)
            }
        }

        fun startUpdating(cb : () -> Unit) {
            curr.startUpdating(cb)
        }
    }

    data class Renderer(var view : LineBeakerView) {

        private val animator : Animator = Animator(view)
        private val lb : LineBeaker = LineBeaker(0)
        private val paint : Paint = Paint(Paint.ANTI_ALIAS_FLAG)

        fun render(canvas : Canvas) {
            canvas.drawColor(backColor)
            lb.draw(canvas, paint)
            animator.animate {
                lb.update {
                    animator.stop()
                }
            }
        }

        fun handleTap() {
            lb.startUpdating {
                animator.start()
            }
        }
    }
}