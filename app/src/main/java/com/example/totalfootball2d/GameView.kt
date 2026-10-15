package com.example.totalfootball2d

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.view.MotionEvent
import android.view.SurfaceHolder
import android.view.SurfaceView

class GameView(context: Context) : SurfaceView(context), Runnable, SurfaceHolder.Callback {

    private var gameThread: Thread? = null
    private var isPlaying = false
    private val paint = Paint()

    private var ballX = 500f
    private var ballY = 900f
    private var ballSpeedX = 0f
    private var ballSpeedY = 0f
    private val friction = 0.98f

    private var isDragging = false
    private var touchStartX = 0f
    private var touchStartY = 0f

    init {
        holder.addCallback(this)
        isFocusable = true
    }

    override fun surfaceCreated(holder: SurfaceHolder) {
        isPlaying = true
        gameThread = Thread(this)
        gameThread?.start()
    }

    override fun surfaceChanged(holder: SurfaceHolder, format: Int, width: Int, height: Int) {
        if (ballX == 500f && ballY == 900f) {
            ballX = width / 2f
            ballY = height / 2f
        }
    }

    override fun surfaceDestroyed(holder: SurfaceHolder) {
        var retry = true
        isPlaying = false
        while (retry) {
            try {
                gameThread?.join()
                retry = false
            } catch (e: InterruptedException) {
                e.printStackTrace()
            }
        }
    }

    override fun run() {
        while (isPlaying) {
            if (holder.surface.isValid) {
                val canvas = holder.lockCanvas()
                updatePhysics()
                drawGame(canvas)
                holder.unlockCanvasAndPost(canvas)
            }
            try {
                Thread.sleep(16)
            } catch (e: InterruptedException) {
                e.printStackTrace()
            }
        }
    }

    private fun updatePhysics() {
        ballX += ballSpeedX
        ballY += ballSpeedY

        ballSpeedX *= friction
        ballSpeedY *= friction

        val margin = 50f
        if (ballX < margin) {
            ballX = margin
            ballSpeedX *= -0.7f
        } else if (ballX > width - margin) {
            ballX = width - margin
            ballSpeedX *= -0.7f
        }

        if (ballY < margin) {
            ballY = margin
            ballSpeedY *= -0.7f
        } else if (ballY > height - margin) {
            ballY = height - margin
            ballSpeedY *= -0.7f
        }
    }

    private fun drawGame(canvas: Canvas) {
        canvas.drawColor(Color.parseColor("#2E7D32"))

        paint.color = Color.WHITE
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = 6f

        val margin = 50f
        canvas.drawRect(margin, margin, width - margin, height - margin, paint)
        canvas.drawLine(margin, height / 2f, width - margin, height / 2f, paint)
        canvas.drawCircle(width / 2f, height / 2f, 180f, paint)

        paint.style = Paint.Style.FILL
        canvas.drawCircle(width / 2f, height / 2f, 12f, paint)

        val goalWidth = 300f
        canvas.drawRect((width - goalWidth) / 2f, margin - 40f, (width + goalWidth) / 2f, margin, paint)
        canvas.drawRect((width - goalWidth) / 2f, height - margin, (width + goalWidth) / 2f, height - margin + 40f, paint)

        paint.style = Paint.Style.FILL
        paint.color = Color.WHITE
        canvas.drawCircle(ballX, ballY, 30f, paint)

        paint.style = Paint.Style.STROKE
        paint.color = Color.BLACK
        paint.strokeWidth = 3f
        canvas.drawCircle(ballX, ballY, 30f, paint)

        if (isDragging) {
            paint.color = Color.YELLOW
            paint.strokeWidth = 8f
            canvas.drawLine(ballX, ballY, touchStartX, touchStartY, paint)
        }
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                val dx = event.x - ballX
                val dy = event.y - ballY
                if (Math.hypot(dx.toDouble(), dy.toDouble()) < 100f) {
                    isDragging = true
                    touchStartX = event.x
                    touchStartY = event.y
                }
            }
            MotionEvent.ACTION_MOVE -> {
                if (isDragging) {
                    touchStartX = event.x
                    touchStartY = event.y
                }
            }
            MotionEvent.ACTION_UP -> {
                if (isDragging) {
                    val forceMultiplier = 0.15f
                    ballSpeedX = (ballX - event.x) * forceMultiplier
                    ballSpeedY = (ballY - event.y) * forceMultiplier
                    isDragging = false
                }
            }
        }
        return true
    }
}
