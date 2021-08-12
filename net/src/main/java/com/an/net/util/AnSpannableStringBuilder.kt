package com.an.net.util

import android.graphics.Color
import android.text.SpannableStringBuilder
import android.text.Spanned
import android.text.TextPaint
import android.text.method.LinkMovementMethod
import android.text.style.*
import android.view.View
import android.widget.TextView
import androidx.annotation.ColorInt

class AnSpannableStringBuilder {

    private val list = mutableListOf<AnSpan>()

    fun span(anSpan: AnSpan): AnSpannableStringBuilder {
        list.add(anSpan)
        return this
    }

    fun bindTextView(textView: TextView) {
        val builder = SpannableStringBuilder()
        list.forEach { anSpan ->
            val start = builder.length
            builder.append(anSpan.text)
            val end = builder.length

            anSpan.click?.let {
                val span = object : ClickableSpan() {
                    override fun onClick(widget: View) {
                        it()
                    }
                    override fun updateDrawState(ds: TextPaint) {
                        super.updateDrawState(ds)
                        ds.color = Color.TRANSPARENT
                        ds.isUnderlineText = false
                        ds.clearShadowLayer()
                    }
                }
                textView.highlightColor=Color.TRANSPARENT
                textView.movementMethod = LinkMovementMethod.getInstance()
                builder.setSpan(
                    span, start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
                )
            }

            anSpan.textSize?.let {
                val span = AbsoluteSizeSpan(it, true)
                builder.setSpan(
                    span, start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
                )
            }
            anSpan.textColor?.let {
                val span = ForegroundColorSpan(it)
                builder.setSpan(
                    span, start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
                )
            }
            anSpan.backgroundColor?.let {
                val span = BackgroundColorSpan(it)
                builder.setSpan(
                    span, start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
                )
            }

        }
        textView.text = builder
    }
}

data class AnSpan(
    val text: String,
    val textSize: Int? = null,
    @ColorInt val textColor: Int? = null,
    @ColorInt val backgroundColor: Int? = null,
    val click: TextClick? = null
)

typealias TextClick = () -> Unit
