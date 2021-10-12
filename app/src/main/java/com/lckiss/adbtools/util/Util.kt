package com.lckiss.adbtools.util

import android.animation.Animator
import android.content.res.Resources
import android.os.Build
import android.util.TypedValue
import android.view.View
import android.view.ViewAnimationUtils
import android.view.animation.LinearInterpolator
import androidx.core.animation.addListener


val Float.dp: Float
    get() = TypedValue.applyDimension(
        TypedValue.COMPLEX_UNIT_DIP, this, Resources.getSystem().displayMetrics
    )

val Int.dp: Int
    get() = TypedValue.applyDimension(
        TypedValue.COMPLEX_UNIT_DIP, this.toFloat(), Resources.getSystem().displayMetrics
    ).toInt()

inline fun View.createCircularReveal(
    centerX: Int,
    centerY: Int,
    startRadius: Float,
    endRadius: Float,
    crossinline onEnd: (animator: Animator) -> Unit = {}
): Animator? {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
        visibility = View.VISIBLE
        val anim = ViewAnimationUtils.createCircularReveal(this, centerX, centerY, startRadius, endRadius)
        anim.duration = 500
        anim.interpolator = LinearInterpolator()
        anim.addListener(onEnd = onEnd)
        anim.start()
        anim
    } else {
        visibility = View.VISIBLE
        null
    }
}