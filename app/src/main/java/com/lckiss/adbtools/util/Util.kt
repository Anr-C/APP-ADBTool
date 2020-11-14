package com.lckiss.adbtools.util

import android.content.res.Resources
import android.os.Build
import android.util.TypedValue
import android.view.View
import android.view.ViewAnimationUtils


val Float.dp: Float
    get() = TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP, this, Resources.getSystem().displayMetrics
    )

val Int.dp: Int
    get() = TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP, this.toFloat(), Resources.getSystem().displayMetrics
    ).toInt()

fun View.createCircularReveal(centerX: Int, centerY: Int, startRadius: Float, endRadius: Float) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            visibility = View.GONE
            val anim = ViewAnimationUtils.createCircularReveal(this, centerX, centerY, startRadius, endRadius)
            anim.duration = 500
            visibility = View.VISIBLE
            anim.start()
    } else {
        visibility = View.VISIBLE
    }
}
