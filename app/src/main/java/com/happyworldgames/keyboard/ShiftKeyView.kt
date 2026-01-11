package com.happyworldgames.keyboard

import android.content.Context
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatImageView

class ShiftKeyView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : AppCompatImageView(context, attrs, defStyleAttr) {

    private var isShifted = false
    private var isCapsLocked = false

    companion object {
        private val STATE_SHIFTED = intArrayOf(R.attr.state_shifted)
        private val STATE_CAPSLOCK = intArrayOf(R.attr.state_capslock)
    }

    fun setShifted(shifted: Boolean) {
        if (isShifted != shifted) {
            isShifted = shifted
            refreshDrawableState()
        }
    }

    fun setCapsLocked(capsLocked: Boolean) {
        if (isCapsLocked != capsLocked) {
            isCapsLocked = capsLocked
            refreshDrawableState()
        }
    }

    override fun onCreateDrawableState(extraSpace: Int): IntArray {
        val drawableState = super.onCreateDrawableState(extraSpace + 2)
        if (isShifted) {
            mergeDrawableStates(drawableState, STATE_SHIFTED)
        }
        if (isCapsLocked) {
            mergeDrawableStates(drawableState, STATE_CAPSLOCK)
        }
        return drawableState
    }
}