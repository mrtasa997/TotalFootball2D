package com.example.totalfootball2d

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.view.MotionEvent
import android.view.SurfaceHolder
import android.view.SurfaceView
import kotlin.math.hypot

// كلاس اللاعب مع إحداثيات ومواقع متحركة
class Player(var x: Float, var y: Float, val startX: Float, val startY: Float, val isUserTeam: Boolean, val name: String) {
    fun reset() {
        x = startX
        y = startY
    }
}

class GameView(context: Context) : SurfaceView(context), Runnable, SurfaceHolder.Callback {

    private var gameThread: Thread? = null
    private var isPlaying = false
    private val paint = Paint()

    private var ballX = 0f
    private var ballY = 0f
    private var ballSpeedX = 0f
    private var ballSpeedY = 0f
    private val friction = 0.97f // احتكاك واقعي للكرة

    private var isDragging = false
    private var touchStartX = 0f
    private var touchStartY = 0f

    private var scoreTeam1 = 0
    private var scoreTeam2 = 0
    private var goalMessage = ""
    private var goalMessageTimer = 0

    private val players = mutableListOf<Player>()
    private var isInitialized = false

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
        if (!isInitialized && width > 0 && height > 0) {
            setupPositions(width, height)
            isInitialized = true
        }
    }

    private fun setupPositions(width: Int, height: Int) {
        ballX = width / 2f
        ballY = height / 2f
        ballSpeedX = 0f
        ballSpeedY = 0f

        players.clear()
        // فريق المستخدم (في الأسفل - أزرق)
        players.add(Player(width / 2f, height - 350f, width / 2f, height - 350f, true, "مهاجم"))
        players.add(Player(width / 3f, height - 600f, width / 3f, height - 600f, true, "وسط أيسر"))
        players.add(Player(width * 2 / 3f, height - 600f, width * 2 / 3f, height - 600f, true, "وسط أيمن"))
        players.add(Player(width / 2f, height - 120f, width / 2f, height - 120f, true, "حارس"))

        // فريق الخصم (في الأعلى - أحمر - يتحرك بذكاء اصطناعي)
        players.add(Player(width / 2f, 350f, width / 2f, 350f, false, "مهاجم خصم"))
        players.add(Player(width / 3f, 600f, width / 3f, 600f, false, "مدافع أيسر"))
        players.add(Player(width * 2 / 3f, 600f, width * 2 / 3f, 600f, false, "مدافع أيمن"))
        players.add(Player(width / 2f, 120f, width / 2f, 120f, false, "حارس خصم"))
    }

    private fun resetMatch(width: Int, height: Int) {
        ballX = width / 2f
        ballY = height / 2f
        ballSpeedX = 0f
        ballSpeedY = 0f
        for (p in players) {
            p.reset()
        }
    }

    override fun surfaceDestroyed(holder: SurfaceHolder) {
        isPlaying = false
        var retry = true
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
                updateGame(width, height)
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

    private fun updateGame(width: Int, height: Int) {
        // حركة الكرة والفيزياء
        ballX += ballSpeedX
        ballY += ballSpeedY
        ballSpeedX *= friction
        ballSpeedY *= friction

        // حركة اللاعبين (الذكاء الاصطناعي للخصم والفريق)
        for (p in players) {
            if (!p.isUserTeam) {
                // الخصم يتحرك ببطء باتجاه الكرة ليجعل المباراة حية وواقعية
                val dx = ballX - p.x
                val dy = ballY - p.y
                val dist = hypot(dx.toDouble(), dy.toDouble()).toFloat()
                if (dist > 5f) {
                    p.x += (dx / dist) * 2.2f // سرعة حركة الخصم
                    p.y += (dy / dist) * 2.2f
                }
            }

            // فحص الاصطدام بين اللاعب والكرة (لكي يدفع اللاعب الكرة عند الاقتراب)
            val pdx = ballX - p.x
            val pdy = ballY - p.y
            val pDist = hypot(pdx.toDouble(), pdy.toDouble()).toFloat()
            if (pDist < 50f) {
                ballSpeedX += (pdx / pDist) * 4f
                ballSpeedY += (pdy / pDist) * 4f
            }
        }

        // حدود الملعب والأهداف
        val margin = 50f
        val goalWidth = 300f
        val goalLeft = (width - goalWidth) / 2f
        val goalRight = (width + goalWidth) / 2f

        if (ballY < margin) {
            if (ballX in goalLeft..goalRight) {
                scoreTeam1++
                goalMessage = "هدف لصالحك! 🎉"
                goalMessageTimer = 80
                resetMatch(width, height)
            } else {
                ballY = margin
                ballSpeedY *= -0.7f
            }
        } else if (ballY > height - margin) {
            if (ballX in goalLeft..goalRight) {
                scoreTeam2++
                goalMessage = "هدف للخصم! ❌"
                goalMessageTimer = 80
                resetMatch(width, height)
            } else {
                ballY = height - margin
                ballSpeedY *= -0.7f
            }
        }

        if (ballX < margin) {
            ballX = margin
            ballSpeedX *= -0.7f
        } else if (ballX > width - margin) {
            ballX = width - margin
            ballSpeedX *= -0.7f
        }

        if (goalMessageTimer > 0) {
            goalMessageTimer--
        }
    }

    private fun drawGame(canvas: Canvas) {
        // رسم الملعب
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

        // رسم اللاعبين
        for (p in players) {
            paint.style = Paint.Style.FILL
            paint.color = if (p.isUserTeam) Color.BLUE else Color.RED
            canvas.drawCircle(p.x, p.y, 45f, paint)

            paint.style = Paint.Style.STROKE
            paint.color = Color.WHITE
            paint.strokeWidth = 3f
            canvas.drawCircle(p.x, p.y, 45f, paint)

            paint.textSize = 22f
            paint.color = Color.WHITE
            canvas.drawText(p.name, p.x - 40f, p.y - 55f, paint)
        }

        // رسم الكرة
        paint.style = Paint.Style.FILL
        paint.color = Color.WHITE
        canvas.drawCircle(ballX, ballY, 30f, paint)

        paint.style = Paint.Style.STROKE
        paint.color = Color.BLACK
        paint.strokeWidth = 3f
        canvas.drawCircle(ballX, ballY, 30f, paint)

        // النتيجة والرسائل
        paint.textSize = 50f
        paint.style = Paint.Style.FILL
        paint.color = Color.YELLOW
        canvas.drawText("فريقك: $scoreTeam1  |  الخصم: $scoreTeam2", 60f, 120f, paint)

        if (goalMessageTimer > 0) {
            paint.textSize = 80f
            paint.color = Color.YELLOW
            canvas.drawText(goalMessage, width / 4f, height / 2f, paint)
        }

        // خط السحب للتسديد
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
                if (hypot(dx.toDouble(), dy.toDouble()) < 100f) {
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
                    val forceMultiplier = 0.18f
                    ballSpeedX = (ballX - event.x) * forceMultiplier
                    ballSpeedY = (ballY - event.y) * forceMultiplier
                    isDragging = false
                }
            }
        }
        return true
    }
}
