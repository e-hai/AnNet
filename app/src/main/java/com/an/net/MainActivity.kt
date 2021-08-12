package com.an.net

import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.TextView
import com.an.net.util.AnSpan
import com.an.net.util.AnSpannableStringBuilder

class MainActivity : AppCompatActivity() {


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val viewModel = MainViewModel(application)
        val titleView = findViewById<TextView>(R.id.titleView)
        val anspan = AnSpannableStringBuilder()
        anspan.span(AnSpan("哈哈，"))
        anspan.span(AnSpan("666", textColor = Color.BLUE))
        anspan.span(AnSpan("哈哈，"))
        anspan.span(AnSpan("888", backgroundColor = Color.YELLOW))
        anspan.span(AnSpan("哈哈，"))
        anspan.span(AnSpan("999", textSize = 16))
        anspan.span(AnSpan("哈哈，"))
        anspan.span(AnSpan("111",textColor = Color.CYAN) {
            Log.d("Main", "111111")
        })
        anspan.bindTextView(titleView)
    }
}