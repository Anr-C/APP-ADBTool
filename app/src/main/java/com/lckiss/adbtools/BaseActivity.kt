package com.lckiss.adbtools

import android.animation.Animator
import android.content.Intent
import android.view.View
import android.view.ViewTreeObserver
import androidx.appcompat.app.AppCompatActivity
import com.lckiss.adbtools.util.createCircularReveal
import kotlin.math.sqrt

abstract class BaseActivity : AppCompatActivity() {

    private var isInAnimation = false
    private var onGlobalLayout: ViewTreeObserver.OnGlobalLayoutListener? = null
    private var animator: Animator? = null

    fun doInitAnim(v: View) {
        v.visibility = View.INVISIBLE
        onGlobalLayout = ViewTreeObserver.OnGlobalLayoutListener {
            //此时既是开始揭露动画的最佳时机
            if (isInAnimation) return@OnGlobalLayoutListener
            isInAnimation = true
            val measuredRadio = sqrt(v.width * v.width * 1.0 + v.height * v.height)
            animator = v.createCircularReveal(
                0,
                v.height,
                0f,
                measuredRadio.toFloat(),
                onEnd = {
                    v.viewTreeObserver.removeOnGlobalLayoutListener(onGlobalLayout)
                    isInAnimation = false
                }
            )
        }
        v.viewTreeObserver.addOnGlobalLayoutListener(onGlobalLayout)
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        this.intent = intent
    }

    override fun onDestroy() {
        animator?.removeAllListeners()
        animator?.cancel()
        super.onDestroy()
    }
}
