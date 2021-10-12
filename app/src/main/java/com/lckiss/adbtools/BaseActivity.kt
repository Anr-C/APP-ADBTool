package com.lckiss.adbtools

import android.animation.Animator
import android.content.Intent
import android.view.View
import android.view.ViewTreeObserver
import androidx.appcompat.app.AppCompatActivity
import com.lckiss.adbtools.util.createCircularReveal

abstract class BaseActivity : AppCompatActivity() {

    private var isInAnimation = false
    private var onGlobalLayout: ViewTreeObserver.OnGlobalLayoutListener? = null
    private var animator: Animator? = null

    fun doInitAnim(v: View) {
        v.visibility = View.INVISIBLE
        onGlobalLayout = ViewTreeObserver.OnGlobalLayoutListener { //此时既是开始揭露动画的最佳时机
            if (isInAnimation) return@OnGlobalLayoutListener
            isInAnimation = true
            val measuredRadio = v.width
            animator = v.createCircularReveal(
                0,
                measuredRadio,
                0f,
                measuredRadio * 1f,
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
