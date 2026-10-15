package com.example.totalfootball2d

import android.os.Bundle
import androidx.activity.ComponentActivity

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // تشغيل كلاس GameView كواجهة أساسية للعبة
        setContentView(GameView(this))
    }
}
