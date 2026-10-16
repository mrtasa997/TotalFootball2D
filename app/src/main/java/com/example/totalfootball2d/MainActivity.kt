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
    private var budget = 5000000
    private var teamRating = 75
    private var points = 0
    private var matchesPlayed = 0
    private var currentOpponentIndex = 0

    private val teamsList = listOf("نادي الصقور", "نادي الزوراء", "نادي الشرطة", "نادي القوة", "نادي التضامن")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        prefs = getSharedPreferences("TotalFootballPrefs", Context.MODE_PRIVATE)
        budget = prefs.getInt("CLUB_BUDGET", 5000000)
        points = prefs.getInt("CLUB_POINTS", 0)
        matchesPlayed = prefs.getInt("MATCHES_PLAYED", 0)
        currentOpponentIndex = prefs.getInt("OPPONENT_INDEX", 0)
        
        showMainMenu()
    }

    // القائمة الرئيسية لإدارة النادي
    private fun showMainMenu() {
        val layout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER
            setBackgroundColor(Color.parseColor("#1B5E20"))
            setPadding(50, 50, 50, 50)
        }

        val title = TextView(this).apply {
            text = "Total Football 2D - مهنة المدرب"
            textSize = 26f
            setTextColor(Color.WHITE)
            gravity = Gravity.CENTER
        }
        layout.addView(title)

        val infoText = TextView(this).apply {
            text = "\nميزانية النادي: $budget $\nتقييم الفريق: $teamRating\nنقاط الدوري: $points | المباريات الملعوبة: $matchesPlayed\n"
            textSize = 18f
            setTextColor(Color.YELLOW)
            gravity = Gravity.CENTER
        }
        layout.addView(infoText)

        // زر خوض المباراة القادمة
        val nextOpponent = if (currentOpponentIndex < teamsList.size) teamsList[currentOpponentIndex] else "نادي نهائي البطولة"
        val playBtn = Button(this).apply {
            text = "لعب المباراة ضد: $nextOpponent"
            textSize = 16f
            setOnClickListener {
                setContentView(GameView(this@MainActivity))
            }
        }
        layout.addView(playBtn)

        // زر جدول الدوري
        val leagueBtn = Button(this).apply {
            text = "جدول ترتيب الدوري"
            textSize = 16f
            setOnClickListener {
                showLeagueTable()
            }
        }
        layout.addView(leagueBtn)

        // زر سوق الانتقالات
        val transferBtn = Button(this).apply {
            text = "سوق الانتقالات"
            textSize = 16f
            setOnClickListener {
                showTransferMarket()
            }
        }
        layout.addView(transferBtn)

        setContentView(layout)
    }

    // شاشة جدول الدوري
    private fun showLeagueTable() {
        val layout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER
            setBackgroundColor(Color.parseColor("#004D40"))
            setPadding(50, 50, 50, 50)
        }

        val title = TextView(this).apply {
            text = "جدول ترتيب الدوري"
            textSize = 26f
            setTextColor(Color.WHITE)
            gravity = Gravity.CENTER
        }
        layout.addView(title)

        val tableContent = TextView(this).apply {
            text = "\n1. فريقك (نادينا) - النقاط: $points\n2. ${teamsList[0]} - النقاط: ${points + 2}\n3. ${teamsList[1]} - النقاط: ${points - 1}\n4. ${teamsList[2]} - النقاط: ${points - 3}\n\n"
            textSize = 18f
            setTextColor(Color.WHITE)
        }
        layout.addView(tableContent)

        val backBtn = Button(this).apply {
            text = "العودة للقائمة الرئيسية"
            setOnClickListener {
                showMainMenu()
            }
        }
        layout.addView(backBtn)

        setContentView(layout)
    }

    // شاشة سوق الانتقالات
    private fun showTransferMarket() {
        val layout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER
            setBackgroundColor(Color.parseColor("#0D47A1"))
            setPadding(50, 50, 50, 50)
        }

        val title = TextView(this).apply {
            text = "سوق الانتقالات"
            textSize = 26f
            setTextColor(Color.WHITE)
            gravity = Gravity.CENTER
        }
        layout.addView(title)

        val marketInfo = TextView(this).apply {
            text = "\nعزز صفوف فريقك لمواجهة الأندية القوية:\n\n1. مهاجم سوبر (السعر: 2,000,000 $)\n"
            textSize = 18f
            setTextColor(Color.WHITE)
        }
        layout.addView(marketInfo)

        val buyBtn = Button(this).apply {
            text = "شراء المهاجم السوبر"
            setOnClickListener {
                if (budget >= 2000000) {
                    budget -= 2000000
                    teamRating += 5
                    prefs.edit().putInt("CLUB_BUDGET", budget).apply()
                    marketInfo.text = "\nتم شراء اللاعب بنجاح! 🎉\nالميزانية الحالية: $budget $\nتقييم الفريق: $teamRating\n"
                } else {
                    marketInfo.text = "\nعذراً، الميزانية غير كافية!\n"
                }
            }
        }
        layout.addView(buyBtn)

        val backBtn = Button(this).apply {
            text = "العودة"
            setOnClickListener {
                showMainMenu()
            }
        }
        layout.addView(backBtn)

        setContentView(layout)
    }
}
