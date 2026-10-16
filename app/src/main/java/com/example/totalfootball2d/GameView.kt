package com.example.totalfootball2d

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.view.MotionEvent
import android.view.SurfaceHolder
import android.view.SurfaceView

// كلاس بسيط لتمثيل اللاعب
class Player(var x: Float, var y: Float, val isUserTeam: Boolean, val name: String)

class GameView(context: Context) : SurfaceView(context), Runnable, SurfaceHolder.Callback {

    private var gameThread: Thread? = null
    private var isPlaying = false
    private val paint = Paint()

    // الكرة
    private var ballX = 0f
    private var ballY = 0f
    private var ballSpeedX = 0f
    private var ballSpeedY = 0f
    private val friction = 0.98f

    // السحب والتحكم
    private var isDragging = false
    private var touchStartX = 0f
    private var touchStartY = 0f

    // عداد الأهداف
    private var scoreTeam1 = 0
    private var scoreTeam2 = 0
    private var goalMessage = ""
    private var goalMessageTimer = 0

    // قائمة اللاعبين (فريقك وفريق الخصم)
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
            resetPositions(width, height)
            isInitialized = true
        }
    }

    private fun resetPositions(width: Int, height: Int) {
        ballX = width / 2f
        ballY = height / 2f
        ballSpeedX = 0f
        ballSpeedY = 0f

        players.clear()
        // فريق المستخدم (في الأسفل - أزرق)
        players.add(Player(width / 2f, height - 300f, true, "مهاجم"))
        players.add(Player(width / 3f, height - 500f, true, "وسط أيسر"))
        players.add(Player(width * 2 / 3f, height - 500f, true, "وسط أيمن"))
        players.add(Player(width / 2f, height - 150f, true, "حارس"))

        // فريق الخصم (في الأعلى - أحمر)
        players.add(Player(width / 2f, 300f, false, "مهاجم خصم"))
        players.add(Player(width / 3f, 500f, false, "مدافع أيسر"))
        players.add(Player(width * 2 / 3f, 500f, false, "مدافع أيمن"))
        players.add(Player(width / 2f, 150f, false, "حارس خصم"))
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
                updatePhysics(width, height)
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

    private fun updatePhysics(width: Int, height: Int) {
        ballX += ballSpeedX
        ballY += ballSpeedY

        ballSpeedX *= friction
        ballSpeedY *= friction

        val margin = 50f
        val goalWidth = 300f
        val goalLeft = (width - goalWidth) / 2f
        val goalRight = (width + goalWidth) / 2f

        // فحص الأهداف
        if (ballY < margin) {
            if (ballX in goalLeft..goalRight) {
                scoreTeam1++
                goalMessage = "هدف لك! 🎉"
                goalMessageTimer = 60
                resetPositions(width, height)
            } else {
                ballY = margin
                ballSpeedY *= -0.7f
            }
        } else if (ballY > height - margin) {
            if (ballX in goalLeft..goalRight) {
                scoreTeam2++
                goalMessage = "هدف للخصم! ❌"
                goalMessageTimer = 60
                resetPositions(width, height)
            } else {
                ballY = height - margin
                ballSpeedY *= -0.7f
            }
        }

        // الحواف الجانبية
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
        // خلفية الملعب
        canvas.drawColor(Color.parseColor("#2E7D32"))

        paint.color = Color.WHITE
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = 6f

        val margin = 50f
        // حدود الملعب والمنتصف
        canvas.drawRect(margin, margin, width - margin, height - margin, paint)
        canvas.drawLine(margin, height / 2f, width - margin, height / 2f, paint)
        canvas.drawCircle(width / 2f, height / 2f, 180f, paint)

        paint.style = Paint.Style.FILL
        canvas.drawCircle(width / 2f, height / 2f, 12f, paint)

        // المرميان
        val goalWidth = 300f
        canvas.drawRect((width - goalWidth) / 2f, margin - 40f, (width + goalWidth) / 2f, margin, paint)
        canvas.drawRect((width - goalWidth) / 2f, height - margin, (width + goalWidth) / 2f, height - margin + 40f, paint)

        // رسم اللاعبين داخل الملعب
        for (p in players) {
            paint.style = Paint.Style.FILL
            paint.color = if (p.isUserTeam) Color.BLUE else Color.RED
            canvas.drawCircle(p.x, p.y, 40f, paint)

            paint.style = Paint.Style.STROKE
            paint.color = Color.WHITE
            paint.strokeWidth = 3f
            canvas.drawCircle(p.x, p.y, 40f, paint)

            // كتابة اسم مركز اللاعب فوقه
            paint.textSize = 24f
            paint.color = Color.WHITE
            canvas.drawText(p.name, p.x - 35f, p.y - 50f, paint)
        }

        // رسم الكرة
        paint.style = Paint.Style.FILL
        paint.color = Color.WHITE
        canvas.drawCircle(ballX, ballY, 30f, paint)

        paint.style = Paint.Style.STROKE
        paint.color = Color.BLACK
        paint.strokeWidth = 3f
        canvas.drawCircle(ballX, ballY, 30f, paint)

        // رسم النتيجة في الأعلى
        paint.textSize = 50f
        paint.style = Paint.Style.FILL
        paint.color = Color.YELLOW
        canvas.drawText("فريقك: $scoreTeam1  |  الخصم: $scoreTeam2", 60f, 120f, paint)

        // رسالة الهدف
        if (goalMessageTimer > 0) {
            paint.textSize = 80f
            paint.color = Color.YELLOW
            canvas.drawText(goalMessage, width / 4f, height / 2f, paint)
        }

        // خط التوجيه عند سحب الكرة
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
