package com.example.totalfootball2d

import android.content.Context
import android.content.SharedPreferences
import android.graphics.Color
import android.os.Bundle
import android.view.Gravity
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import androidx.activity.ComponentActivity

class MainActivity : ComponentActivity() {

    private lateinit var prefs: SharedPreferences
    private var budget = 5000000 // ميزانية النادي الافتتاحية (5 ملايين)
    private var teamRating = 75  // تقييم الفريق

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        prefs = getSharedPreferences("TotalFootballPrefs", Context.MODE_PRIVATE)
        budget = prefs.getInt("CLUB_BUDGET", 5000000)
        
        showMainMenu()
    }

    // عرض القائمة الرئيسية لإدارة النادي واللعب
    private fun showMainMenu() {
        val layout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER
            setBackgroundColor(Color.parseColor("#1B5E20"))
            setPadding(50, 50, 50, 50)
        }

        // عنوان اللعبة
        val title = TextView(this).apply {
            text = "Total Football 2D"
            textSize = 32f
            setTextColor(Color.WHITE)
            gravity = Gravity.CENTER
        }
        layout.addView(title)

        // معلومات النادي (الميزانية والتقييم)
        val infoText = TextView(this).apply {
            text = "\nميزانية النادي: $budget $\nتقييم الفريق: $teamRating\n"
            textSize = 20f
            setTextColor(Color.YELLOW)
            gravity = Gravity.CENTER
        }
        layout.addView(infoText)

        // زر بدء المباراة
        val playBtn = Button(this).apply {
            text = "بدء المباراة"
            textSize = 18f
            setOnClickListener {
                // الانتقال إلى شاشة اللعب (GameView)
                setContentView(GameView(this@MainActivity))
            }
        }
        layout.addView(playBtn)

        // زر سوق الانتقالات
        val transferBtn = Button(this).apply {
            text = "سوق الانتقالات وإدارة الفريق"
            textSize = 18f
            setOnClickListener {
                showTransferMarket()
            }
        }
        layout.addView(transferBtn)

        setContentView(layout)
    }

    // شاشة سوق الانتقالات البسيطة
    private fun showTransferMarket() {
        val layout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER
            setBackgroundColor(Color.parseColor("#0D47A1"))
            setPadding(50, 50, 50, 50)
        }

        val title = TextView(this).apply {
            text = "سوق الانتقالات"
            textSize = 28f
            setTextColor(Color.WHITE)
            gravity = Gravity.CENTER
        }
        layout.addView(title)

        val marketInfo = TextView(this).apply {
            text = "\nاختر لاعباً لتعزيز صفوف فريقك:\n\n1. مهاجم سوبر (السعر: 2,000,000 $)\n"
            textSize = 18f
            setTextColor(Color.WHITE)
        }
        layout.addView(marketInfo)

        // زر شراء اللاعب
        val buyBtn = Button(this).apply {
            text = "شراء المهاجم السوبر"
            setOnClickListener {
                if (budget >= 2000000) {
                    budget -= 2000000
                    teamRating += 5
                    prefs.edit().putInt("CLUB_BUDGET", budget).apply()
                    marketInfo.text = "\nتم شراء اللاعب بنجاح! 🎉\nالميزانية الحالية: $budget $\nتقييم الفريق: $teamRating\n"
                } else {
                    marketInfo.text = "\nعذراً، الميزانية غير كافية للشراء!\n"
                }
            }
        }
        layout.addView(buyBtn)

        // زر العودة للقائمة الرئيسية
        val backBtn = Button(this).apply {
            text = "العودة للقائمة الرئيسية"
            setOnClickListener {
                showMainMenu()
            }
        }
        layout.addView(backBtn)

        setContentView(layout)
    }
}
