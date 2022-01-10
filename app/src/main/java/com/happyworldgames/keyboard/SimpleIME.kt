package com.happyworldgames.keyboard

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.PixelFormat
import android.inputmethodservice.InputMethodService
import android.os.Build
import android.util.DisplayMetrics
import android.util.Log
import android.view.*
import com.happyworldgames.keyboard.databinding.HintKeyboardBinding
import com.happyworldgames.keyboard.databinding.KeyboardBinding
import java.io.File

class SimpleIME : InputMethodService() {
    companion object {
        val hintArrayList = arrayListOf<ArrayList<String>>(
            arrayListOf("Q", "W", "E", "R", "T", "Y", "U", "I", "O", "P", "A", "S", "D", "F", "G", "H", "J", "K", "L", "Z", "X", "C", "V", "B", "N", "M", "1", "2", "3", "4", "5", "⏎", "˽", "⤆", "⤇", "⌫"),
            arrayListOf("А", "Б", "В", "Г", "Д", "Е", "Ё", "Ж", "З", "И", "Й", "К", "Л", "М", "Н", "О", "П", "Р", "С", "Т", "У", "Ф", "Х", "Ц", "Ч", "Ш", "Щ", "Ъ", "Ы", "Ь", "Э", "Ю", "Я", "⤆", "⤇", "⌫"),
            arrayListOf("1", "2", "3", "4", "5", "6", "7", "8", "9", "0", ",", ".", "?", "!", ":", ";", "П", "Р", "С", "Т", "У", "Ф", "Х", "Ц", "Ч", "Ш", "Щ", "Ъ", "Ы", "Ь", "Э", "Ю", "Я", "⤆", "⤇", "⌫"),
        )
        private lateinit var saveFile: File

        val hintArrayNumber = arrayOf(12, 13, 14, 15, 16, 17, 18, 19, 23, 24, 25, 26, 27, 28, 29, 34, 35, 36, 37, 38, 39, 45, 46, 47, 48, 49, 56, 57, 58, 59, 67, 68, 69, 78, 79, 89)
        val hintArray = arrayListOf("Q", "W", "E", "R", "T", "Y", "U", "I", "O", "P", "A", "S", "D", "F", "G", "H", "J", "K", "L", "Z", "X", "C", "V", "B", "N", "M", "1", "2", "3", "4", "5", "6", "7", "⏎", "˽", "⌫")
        private val hintHashMap = hashMapOf<Int, String>()
        private fun fullHintHashMap(){
            for(i in hintArrayNumber.indices)
                hintHashMap[hintArrayNumber[i]] = hintArray[i]
            hintArray.add(" ")
        }
        fun replaceKeyBoardLayout(index: Int){
            if(index > hintArrayList.size - 1) return
            hintArray.clear()
            hintArray.addAll(hintArrayList[index])
            fullHintHashMap()
        }

        fun saveHintArrayList(){
            if(!saveFile.exists()) saveFile.createNewFile()
            val saveText = StringBuilder("")
            for(item in hintArrayList){
                saveText.appendLine(item.joinToString())
            }
            saveFile.writeText(saveText.toString())
        }

        fun convertDpToPixel(context: Context, dp: Float): Float {
            return dp * (context.resources.displayMetrics.densityDpi.toFloat() / DisplayMetrics.DENSITY_DEFAULT)
        }
    }

    private var showKeyboard = false
    private var keyBoardLayoutNow = 0

    private var mode: Int = 0 // 0 = обычный, 1 = перемещение, -1 = запретить перемещение
    private var lastX = 0f
    var lastY = 0f
    var longClickTime = System.currentTimeMillis()

    var firstId = -1
    var lastId = -1

    private val keyboardBinding by lazy { KeyboardBinding.inflate(layoutInflater) }
    private val hintKeyboardBinding by lazy { HintKeyboardBinding.inflate(layoutInflater) }

    private val manager by lazy { getSystemService(WINDOW_SERVICE) as WindowManager }

    private val mainParams by lazy { WindowManager.LayoutParams(
        convertDpToPixel(120.toFloat()).toInt(),
        convertDpToPixel(120.toFloat()).toInt(),
        if(Build.VERSION.SDK_INT < 26) WindowManager.LayoutParams.TYPE_PHONE else WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
        WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
        PixelFormat.TRANSLUCENT
    ) }
    private val hintParams by lazy { WindowManager.LayoutParams(
        convertDpToPixel(120.toFloat()).toInt(),
        convertDpToPixel(120.toFloat()).toInt(),
        if(Build.VERSION.SDK_INT < 26) WindowManager.LayoutParams.TYPE_PHONE else WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
        WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
        PixelFormat.TRANSLUCENT
    ) }

    @SuppressLint("ClickableViewAccessibility")
    val onTouchListener = View.OnTouchListener { v, event ->
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                event.xPrecision
                lastX = event.rawX
                lastY = event.rawY

                firstId = posToNumberPos(event.x.toInt(), event.y.toInt())
                hintDo()

                Log.e("HWG", "DOWN ID:$firstId")

                if(firstId == 5) {
                    mode = 0
                    longClickTime = System.currentTimeMillis()
                }
            }
            MotionEvent.ACTION_UP -> {
                lastId = posToNumberPos(event.x.toInt(), event.y.toInt())
                Log.e("HWG", "UP ID:$lastId")
                hintReturn()
                gestureDo()
            }
            MotionEvent.ACTION_MOVE -> {
                if(firstId == 5) {
                    if (longClickTime + 1000 < System.currentTimeMillis() && mode == 0){
                        mode = 1
                        hintReturn()
                    }else if (kotlin.math.abs(lastX - event.rawX) > convertDpToPixel(20f) || kotlin.math.abs(
                            lastY - event.rawY
                        ) > convertDpToPixel(20f)
                    ) mode = -1
                    if (mode == 1) {
                        mainParams.x -= (lastX - event.rawX).toInt()
                        mainParams.y += (lastY - event.rawY).toInt()
                        manager.updateViewLayout(keyboardBinding.root, mainParams)
                        lastX = event.rawX
                        lastY = event.rawY
                    }
                }
                if(mode != 1) hintPosition(posToNumberPos(event.x.toInt(), event.y.toInt()))
            }
        }
        true
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onCreate() {
        super.onCreate()

        saveFile = File(filesDir, "saveKeyBoardLayout.txt")
        loadHintArrayList()

        mainParams.gravity = Gravity.BOTTOM or Gravity.CENTER
        mainParams.x = 0
        mainParams.y = convertDpToPixel(40.toFloat()).toInt()

        hintParams.gravity = Gravity.TOP or Gravity.CENTER
        hintParams.x = 0
        hintParams.y = convertDpToPixel(40.toFloat()).toInt()

        keyboardBinding.root.setOnTouchListener(onTouchListener)
    }

    private fun numberPosToOriginalNumber(firstPos: Int, lastPos: Int): Int {
        return if(firstPos < lastPos) (firstPos.toString()+lastPos).toInt() else (lastPos.toString()+firstPos).toInt()
    }
    private fun posToNumberPos(posX: Int, posY: Int): Int {
        if(posX < 0 || posX > keyboardBinding.root.width) return -1
        if(posY < 0 || posY > keyboardBinding.root.height) return -1

        val boxWidth = keyboardBinding.root.width / 3
        val x = if (posX in 0 until boxWidth) 1
        else if (posX >= boxWidth && posX < boxWidth * 2) 2
        else 3

        val boxHeight = keyboardBinding.root.height / 3
        val y = if (posY in 0 until boxHeight) 0
        else if (posY >= boxHeight && posY < boxHeight * 2) 3
        else 6

        return x + y
    }

    private fun gestureDo(){
        if(firstId == lastId) return
        val result = hintHashMap[numberPosToOriginalNumber(firstId, lastId)] ?: return
        when(result){
            "⌫" -> currentInputConnection.deleteSurroundingText(1, 0)
            "˽" -> currentInputConnection.commitText(" ", 1)
            "⏎" -> currentInputConnection.sendKeyEvent(KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_ENTER))
            "⤇" -> replaceKeyBoardLayoutForward()
            "⤆" -> replaceKeyBoardLayoutBack()
            else -> currentInputConnection.commitText(result, result.length)
        }
    }

    private fun hintDo(){
        val tempArray = arrayListOf<Int>()
        for(i in hintArrayNumber.indices){
            if(tempArray.size == firstId - 1) tempArray.add(hintArray.size - 1)
            val temp = hintArrayNumber[i].toString()
            val first = temp.substring(0, 1).toInt()
            val last = temp.substring(1, 2).toInt()
            when(firstId){
                first, last -> tempArray.add(i)
            }
        }
        if(tempArray.size == firstId - 1) tempArray.add(hintArray.size - 1)

        hintKeyboardBinding.viewPos1.text = hintArray[tempArray[0]]
        hintKeyboardBinding.viewPos2.text = hintArray[tempArray[1]]
        hintKeyboardBinding.viewPos3.text = hintArray[tempArray[2]]
        hintKeyboardBinding.viewPos4.text = hintArray[tempArray[3]]
        hintKeyboardBinding.viewPos5.text = hintArray[tempArray[4]]
        hintKeyboardBinding.viewPos6.text = hintArray[tempArray[5]]
        hintKeyboardBinding.viewPos7.text = hintArray[tempArray[6]]
        hintKeyboardBinding.viewPos8.text = hintArray[tempArray[7]]
        hintKeyboardBinding.viewPos9.text = hintArray[tempArray[8]]
    }
    private fun hintReturn(){
        hintKeyboardBinding.viewPos1.text = "1"
        hintKeyboardBinding.viewPos2.text = "2"
        hintKeyboardBinding.viewPos3.text = "3"
        hintKeyboardBinding.viewPos4.text = "4"
        hintKeyboardBinding.viewPos5.text = "5"
        hintKeyboardBinding.viewPos6.text = "6"
        hintKeyboardBinding.viewPos7.text = "7"
        hintKeyboardBinding.viewPos8.text = "8"
        hintKeyboardBinding.viewPos9.text = "9"

        hintPosition(-1)
    }
    @SuppressLint("UseCompatLoadingForDrawables")
    private fun hintPosition(pos: Int){
        hintKeyboardBinding.viewPos1.background = if(pos == 1) getDrawable(R.drawable.custom_border) else null
        hintKeyboardBinding.viewPos2.background = if(pos == 2) getDrawable(R.drawable.custom_border) else null
        hintKeyboardBinding.viewPos3.background = if(pos == 3) getDrawable(R.drawable.custom_border) else null
        hintKeyboardBinding.viewPos4.background = if(pos == 4) getDrawable(R.drawable.custom_border) else null
        hintKeyboardBinding.viewPos5.background = if(pos == 5) getDrawable(R.drawable.custom_border) else null
        hintKeyboardBinding.viewPos6.background = if(pos == 6) getDrawable(R.drawable.custom_border) else null
        hintKeyboardBinding.viewPos7.background = if(pos == 7) getDrawable(R.drawable.custom_border) else null
        hintKeyboardBinding.viewPos8.background = if(pos == 8) getDrawable(R.drawable.custom_border) else null
        hintKeyboardBinding.viewPos9.background = if(pos == 9) getDrawable(R.drawable.custom_border) else null
    }

    override fun onWindowShown() {
        super.onWindowShown()
        show()
    }

    override fun onWindowHidden() {
        super.onWindowHidden()
        hide()
    }

    private fun show(){
        if(showKeyboard) return
        manager.addView(hintKeyboardBinding.root, hintParams)
        manager.addView(keyboardBinding.root, mainParams)
        showKeyboard = true
    }
    private fun hide(){
        if(!showKeyboard) return
        manager.removeView(hintKeyboardBinding.root)
        manager.removeView(keyboardBinding.root)
        showKeyboard = false
    }

    private fun loadHintArrayList(){
        if(saveFile.exists()){
            val lines = saveFile.readLines()
            hintArrayList.clear()
            for(item in lines){
                hintArrayList.add(item.splitToSequence(", ").filter { it.isNotEmpty() }.toList() as ArrayList<String>)
            }
        }else saveHintArrayList()
        replaceKeyBoardLayout(keyBoardLayoutNow)
    }
    private fun replaceKeyBoardLayoutForward(){
        keyBoardLayoutNow++
        if(keyBoardLayoutNow > hintArrayList.size - 1) keyBoardLayoutNow = 0
        replaceKeyBoardLayout(keyBoardLayoutNow)
    }
    private fun replaceKeyBoardLayoutBack(){
        keyBoardLayoutNow--
        if(keyBoardLayoutNow < 0) keyBoardLayoutNow = hintArrayList.size - 1
        replaceKeyBoardLayout(keyBoardLayoutNow)
    }

    private fun convertDpToPixel(dp: Float): Float {
        return Companion.convertDpToPixel(applicationContext, dp)
    }
}