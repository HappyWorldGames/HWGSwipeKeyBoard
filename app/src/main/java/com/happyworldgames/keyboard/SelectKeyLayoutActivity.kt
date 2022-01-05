package com.happyworldgames.keyboard

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.happyworldgames.keyboard.databinding.SelectKeyLayoutBinding

class SelectKeyLayoutActivity : AppCompatActivity() {

    private val selectKeyLayoutBinding by lazy { SelectKeyLayoutBinding.inflate(layoutInflater) }

    private val positionItem by lazy { intent.getIntExtra("position", -1) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(selectKeyLayoutBinding.root)

        if(positionItem == -1) return

        selectKeyLayoutBinding.backspace.setOnClickListener {
            val cursorPosition: Int = selectKeyLayoutBinding.editText.selectionStart
            if (cursorPosition > 0) {
                selectKeyLayoutBinding.editText.text = selectKeyLayoutBinding.editText.text.delete(cursorPosition - 1, cursorPosition)
                selectKeyLayoutBinding.editText.setSelection(cursorPosition - 1)
            }
        }
        selectKeyLayoutBinding.backspaceButton.setOnClickListener {
            selectKeyLayoutBinding.editText.append("⌫")
        }
        selectKeyLayoutBinding.enterButton.setOnClickListener {
            selectKeyLayoutBinding.editText.append("⏎")
        }
        selectKeyLayoutBinding.spaceButton.setOnClickListener {
            selectKeyLayoutBinding.editText.append("˽")
        }
    }

    override fun onBackPressed() {
        SimpleIME.hintArray[positionItem] = selectKeyLayoutBinding.editText.text.toString()
        super.onBackPressed()
    }
}