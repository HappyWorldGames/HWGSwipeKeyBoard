package com.happyworldgames.keyboard

import android.annotation.SuppressLint
import android.content.Context
import android.inputmethodservice.InputMethodService
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.widget.FrameLayout
import androidx.core.content.ContextCompat
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
        private fun fullHintHashMap() {
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
            saveFile.writeText(hintArrayList.joinToString("\n") { it.joinToString() })
        }

        fun convertDpToPixel(context: Context, dp: Float): Float {
            return dp * (context.resources.displayMetrics.densityDpi.toFloat() / android.util.DisplayMetrics.DENSITY_DEFAULT)
        }
    }

    private var keyBoardLayoutNow = 0

    private var mode: Int = 0 // 0 = обычный, 1 = перемещение, -1 = запретить перемещение
    private var lastX = 0f
    var lastY = 0f
    var longClickTime = System.currentTimeMillis()

    var firstId = -1
    var lastId = -1

    private lateinit var keyboardBinding: KeyboardBinding
    private lateinit var hintKeyboardBinding: HintKeyboardBinding
    private lateinit var container: FrameLayout

    @SuppressLint("ClickableViewAccessibility")
    val onTouchListener = View.OnTouchListener { v, event ->
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
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
                    }else if (kotlin.math.abs(lastX - event.rawX) > 20 || kotlin.math.abs(lastY - event.rawY) > 20) {
                        mode = -1
                    }
                }
                if(mode != 1) hintPosition(posToNumberPos(event.x.toInt(), event.y.toInt()))
            }
        }
        true
    }

    override fun onCreateInputView(): View {
        // Создаем основной контейнер
        container = FrameLayout(this)
        container.layoutParams = FrameLayout.LayoutParams(
            FrameLayout.LayoutParams.MATCH_PARENT,
            FrameLayout.LayoutParams.MATCH_PARENT
        )

        // Контейнер для клавиатуры теперь размещается вверху
        val topContainer = FrameLayout(this)
        val topParams = FrameLayout.LayoutParams(
            FrameLayout.LayoutParams.WRAP_CONTENT,
            FrameLayout.LayoutParams.WRAP_CONTENT
        )
        // Меняем gravity на TOP: клавиатура прижимается к верху экрана
        topParams.gravity = Gravity.TOP or Gravity.CENTER_HORIZONTAL
        // Добавляем отступ сверху (опционально, для эстетики)
        topParams.topMargin = convertDpToPixel(container.context, 40f).toInt()
        topContainer.layoutParams = topParams

        // Создаем основную клавиатуру
        keyboardBinding = KeyboardBinding.inflate(LayoutInflater.from(this))
        keyboardBinding.root.layoutParams = FrameLayout.LayoutParams(
            FrameLayout.LayoutParams.WRAP_CONTENT,
            FrameLayout.LayoutParams.WRAP_CONTENT
        )

        // Создаем клавиатуру подсказок
        hintKeyboardBinding = HintKeyboardBinding.inflate(LayoutInflater.from(this))
        val hintParams = FrameLayout.LayoutParams(
            FrameLayout.LayoutParams.WRAP_CONTENT,
            FrameLayout.LayoutParams.WRAP_CONTENT
        )
        // Подсказки будут показываться под основной клавиатурой
        hintParams.gravity = Gravity.BOTTOM or Gravity.CENTER_HORIZONTAL
        hintKeyboardBinding.root.layoutParams = hintParams
        hintKeyboardBinding.root.visibility = View.GONE

        // Собираем иерархию
        topContainer.addView(keyboardBinding.root)
        topContainer.addView(hintKeyboardBinding.root)
        container.addView(topContainer)

        keyboardBinding.root.setOnTouchListener(onTouchListener)
        saveFile = File(filesDir, "saveKeyBoardLayout.txt")
        loadHintArrayList()

        return container
    }

    override fun onStartInputView(info: android.view.inputmethod.EditorInfo?, restarting: Boolean) {
        super.onStartInputView(info, restarting)
        // Обновляем положение подсказок при показе клавиатуры
        hintKeyboardBinding.root.visibility = View.GONE
    }

    private fun numberPosToOriginalNumber(firstPos: Int, lastPos: Int): Int {
        return if(firstPos < lastPos) (firstPos.toString()+lastPos).toInt() else (lastPos.toString()+firstPos).toInt()
    }

    private fun posToNumberPos(posX: Int, posY: Int): Int {
        val view = keyboardBinding.root
        if(posX < 0 || posX > view.width) return -1
        if(posY < 0 || posY > view.height) return -1

        val boxWidth = view.width / 3
        val x = when {
            posX in 0 until boxWidth -> 1
            posX >= boxWidth && posX < boxWidth * 2 -> 2
            else -> 3
        }

        val boxHeight = view.height / 3
        val y = when {
            posY in 0 until boxHeight -> 0
            posY >= boxHeight && posY < boxHeight * 2 -> 3
            else -> 6
        }

        return x + y
    }

    private fun gestureDo(){
        if(firstId == lastId) return
        val result = hintHashMap[numberPosToOriginalNumber(firstId, lastId)] ?: return
        val ic = currentInputConnection

        when(result){
            "⌫" -> ic?.deleteSurroundingText(1, 0)
            "˽" -> ic?.commitText(" ", 1)
            "⏎" -> ic?.sendKeyEvent(android.view.KeyEvent(android.view.KeyEvent.ACTION_DOWN, android.view.KeyEvent.KEYCODE_ENTER))
            "⤇" -> replaceKeyBoardLayoutForward()
            "⤆" -> replaceKeyBoardLayoutBack()
            else -> ic?.commitText(result, result.length)
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

        // Показываем подсказки над клавиатурой
        hintKeyboardBinding.root.visibility = View.VISIBLE

        // Позиционируем подсказки над клавиатурой
        val hintParams = hintKeyboardBinding.root.layoutParams as FrameLayout.LayoutParams
        hintParams.gravity = Gravity.BOTTOM or Gravity.CENTER_HORIZONTAL

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
        hintKeyboardBinding.root.visibility = View.GONE

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
        hintKeyboardBinding.viewPos1.background = if(pos == 1) ContextCompat.getDrawable(this, R.drawable.custom_border) else null
        hintKeyboardBinding.viewPos2.background = if(pos == 2) ContextCompat.getDrawable(this, R.drawable.custom_border) else null
        hintKeyboardBinding.viewPos3.background = if(pos == 3) ContextCompat.getDrawable(this, R.drawable.custom_border) else null
        hintKeyboardBinding.viewPos4.background = if(pos == 4) ContextCompat.getDrawable(this, R.drawable.custom_border) else null
        hintKeyboardBinding.viewPos5.background = if(pos == 5) ContextCompat.getDrawable(this, R.drawable.custom_border) else null
        hintKeyboardBinding.viewPos6.background = if(pos == 6) ContextCompat.getDrawable(this, R.drawable.custom_border) else null
        hintKeyboardBinding.viewPos7.background = if(pos == 7) ContextCompat.getDrawable(this, R.drawable.custom_border) else null
        hintKeyboardBinding.viewPos8.background = if(pos == 8) ContextCompat.getDrawable(this, R.drawable.custom_border) else null
        hintKeyboardBinding.viewPos9.background = if(pos == 9) ContextCompat.getDrawable(this, R.drawable.custom_border) else null
    }

    private fun loadHintArrayList(){
        if(saveFile.exists()){
            val lines = saveFile.readLines()
            hintArrayList.clear()
            for(item in lines){
                hintArrayList.add(ArrayList(item.splitToSequence(", ").filter { it.isNotEmpty() }.toList()))
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
}