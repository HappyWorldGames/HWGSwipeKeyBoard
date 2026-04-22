package com.happyworldgames.keyboard

import android.annotation.SuppressLint
import android.content.Context
import android.inputmethodservice.InputMethodService
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.os.VibrationEffect
import android.os.Vibrator
import android.util.Log
import android.view.GestureDetector
import android.view.Gravity
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.widget.FrameLayout
import androidx.core.content.ContextCompat
import com.happyworldgames.keyboard.databinding.HintKeyboardBinding
import com.happyworldgames.keyboard.databinding.KeyboardBinding
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import androidx.core.view.isVisible

class SimpleIME : InputMethodService() {
    companion object {
        private const val TAG = "SimpleIME"
        private const val SAVE_FILENAME = "keyboard_layouts.json"

        val hintArrayList = arrayListOf<ArrayList<String>>(
            arrayListOf("Q", "W", "E", "R", "T", "Y", "U", "I", "O", "P", "A", "S", "D", "F", "G", "H", "J", "K", "L", "Z", "X", "C", "V", "B", "N", "M", "1", "2", "3", "4", "5", "⏎", "˽", "⤆", "⤇", "⌫"),
            arrayListOf("А", "Б", "В", "Г", "Д", "Е", "Ё", "Ж", "З", "И", "Й", "К", "Л", "М", "Н", "О", "П", "Р", "С", "Т", "У", "Ф", "Х", "Ц", "Ч", "Ш", "Щ", "Ъ", "Ы", "Ь", "Э", "Ю", "Я", "⤆", "⤇", "⌫"),
            arrayListOf("1", "2", "3", "4", "5", "6", "7", "8", "9", "0", ",", ".", "?", "!", ":", ";", "П", "Р", "С", "Т", "У", "Ф", "Х", "Ц", "Ч", "Ш", "Щ", "Ъ", "Ы", "Ь", "Э", "Ю", "Я", "⤆", "⤇", "⌫"),
        )

        lateinit var saveFile: File
        private val mainHandler = Handler(Looper.getMainLooper())

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

        // Асинхронное сохранение в JSON формате
        fun saveHintArrayListAsync(context: Context) {
            Thread {
                try {
                    saveHintArrayListSync(context)
                } catch (e: Exception) {
                    Log.e(TAG, "Error saving keyboard layouts", e)
                }
            }.start()
        }

        // Синхронное сохранение (вызывать только в фоновом потоке)
        private fun saveHintArrayListSync(context: Context) {
            ensureSaveFileInitialized(context)

            val json = JSONObject()
            val layoutsArray = JSONArray()

            for (layout in hintArrayList) {
                val layoutArray = JSONArray()
                for (key in layout) {
                    layoutArray.put(key)
                }
                layoutsArray.put(layoutArray)
            }

            json.put("layouts", layoutsArray)
            json.put("version", 1) // Для будущих миграций

            saveFile.writeText(json.toString(2)) // 2 - отступ для читаемости
            Log.d(TAG, "Keyboard layouts saved successfully")
        }

        // Асинхронная загрузка из JSON
        fun loadHintArrayListAsync(context: Context, onLoaded: () -> Unit = {}) {
            Thread {
                try {
                    ensureSaveFileInitialized(context)
                    loadHintArrayListSync(context)

                    // Обновление UI должно происходить в основном потоке
                    mainHandler.post {
                        onLoaded()
                        Log.d(TAG, "Keyboard layouts loaded and UI updated")
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Error loading keyboard layouts", e)
                    // В случае ошибки используем значения по умолчанию
                    mainHandler.post {
                        onLoaded()
                    }
                }
            }.start()
        }

        // Синхронная загрузка (вызывать только в фоновом потоке)
        private fun loadHintArrayListSync(context: Context) {
            if (!saveFile.exists()) {
                Log.d(TAG, "Save file not found, using default layouts")
                return
            }

            try {
                val jsonString = saveFile.readText()
                if (jsonString.isEmpty()) {
                    Log.w(TAG, "Save file is empty")
                    return
                }

                val json = JSONObject(jsonString)
                val version = json.optInt("version", 0)

                if (json.has("layouts") && json.get("layouts") is JSONArray) {
                    val layoutsArray = json.getJSONArray("layouts")
                    hintArrayList.clear()

                    for (i in 0 until layoutsArray.length()) {
                        val layoutArray = layoutsArray.getJSONArray(i)
                        val layout = ArrayList<String>()

                        for (j in 0 until layoutArray.length()) {
                            layout.add(layoutArray.getString(j))
                        }

                        if (layout.isNotEmpty()) {
                            hintArrayList.add(layout)
                        }
                    }

                    Log.d(TAG, "Loaded ${hintArrayList.size} layouts from JSON")
                } else {
                    Log.w(TAG, "Invalid JSON format - no layouts array found")
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error parsing JSON file", e)
                // В случае ошибки парсинга - оставляем текущие значения
            }
        }

        // Инициализация файла сохранения
        private fun ensureSaveFileInitialized(context: Context) {
            if (!::saveFile.isInitialized) {
                saveFile = File(context.filesDir, SAVE_FILENAME)
            }
        }

        fun convertDpToPixel(context: Context, dp: Float): Float {
            return dp * (context.resources.displayMetrics.densityDpi.toFloat() / android.util.DisplayMetrics.DENSITY_DEFAULT)
        }
    }

    private var keyBoardLayoutNow = 0
    private var isShifted = false
    private var isCapsLock = false

    private val backspaceHandler = Handler(Looper.getMainLooper())
    private var backspaceRunnable: Runnable? = null

    private var mode: Int = 0 // 0 = обычный, 1 = перемещение, -1 = запретить перемещение
    private var lastX = 0f
    var lastY = 0f
    var longClickTime = System.currentTimeMillis()

    var firstId = -1
    private var lastId = -1

    private var lastVibratedPos = -1

    private val vibrator: Vibrator by lazy {
        @Suppress("DEPRECATION")
        getSystemService(VIBRATOR_SERVICE) as Vibrator
    }

    private fun performVibration(pos: Int, isCommit: Boolean = false, isBackAction: Boolean = false) {
        val sharedPreferences = getSharedPreferences("keyboard_settings", MODE_PRIVATE)
        if (!sharedPreferences.getBoolean("enable_vibration", true)) return

        if (!isCommit && pos == lastVibratedPos) return
        if (!isCommit) lastVibratedPos = pos

        val baseStrength = sharedPreferences.getInt("vibration_strength", 100)
        val baseDuration = sharedPreferences.getInt("vibration_duration", 20).toLong()
        val differentVibration = sharedPreferences.getBoolean("vibration_different", false)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val effect = when {
                isBackAction -> VibrationEffect.createOneShot(baseDuration, baseStrength)
                isCommit -> VibrationEffect.createOneShot(baseDuration + 10, baseStrength)
                differentVibration && pos == 5 -> {
                    VibrationEffect.createWaveform(
                        longArrayOf(0, baseDuration, 40, baseDuration),
                        intArrayOf(0, baseStrength, 0, baseStrength),
                        -1
                    )
                }
                differentVibration && pos != -1 -> {
                    // Рассчитываем расстояние от центра (зона 5)
                    // Зоны: 1 2 3
                    //       4 5 6
                    //       7 8 9
                    val row = (pos - 1) / 3
                    val col = (pos - 1) % 3
                    val dist = kotlin.math.sqrt(((row - 1) * (row - 1) + (col - 1) * (col - 1)).toDouble())
                    
                    val zoneStrength = (baseStrength + (dist * 20)).toInt().coerceAtMost(255)
                    val zoneDuration = baseDuration + (dist * 5).toLong()
                    VibrationEffect.createOneShot(zoneDuration, zoneStrength)
                }
                else -> VibrationEffect.createOneShot(baseDuration, baseStrength)
            }
            vibrator.vibrate(effect)
        } else {
            @Suppress("DEPRECATION")
            vibrator.vibrate(if (isCommit && !isBackAction) baseDuration + 10 else baseDuration)
        }
    }

    private lateinit var keyboardBinding: KeyboardBinding
    private lateinit var hintKeyboardBinding: HintKeyboardBinding
    private lateinit var container: FrameLayout

    @SuppressLint("ClickableViewAccessibility")
    val onTouchListener = View.OnTouchListener { _, event ->
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                lastX = event.rawX
                lastY = event.rawY

                firstId = posToNumberPos(event.x.toInt(), event.y.toInt())
                performVibration(firstId)
                hintDo()

                if(firstId == 5) {
                    mode = 0
                    longClickTime = System.currentTimeMillis()
                }
            }
            MotionEvent.ACTION_UP -> {
                lastId = posToNumberPos(event.x.toInt(), event.y.toInt())
                hintReturn()
                gestureDo()
                lastVibratedPos = -1
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

    private fun updateShiftKeyState() {
        keyboardBinding.shiftButton.setShifted(isShifted)
        keyboardBinding.shiftButton.setCapsLocked(isCapsLock)
        if (hintKeyboardBinding.root.isVisible) {
            hintDo()
        }
        val sharedPreferences = getSharedPreferences("keyboard_settings", MODE_PRIVATE)
        if (sharedPreferences.getBoolean("show_symbols_on_keys", true)) {
            updateSymbolsOnKeys()
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onCreateInputView(): View {
        // ... view creation ...
        container = FrameLayout(this)
        container.layoutParams = FrameLayout.LayoutParams(
            FrameLayout.LayoutParams.MATCH_PARENT,
            FrameLayout.LayoutParams.MATCH_PARENT
        )

        val topContainer = FrameLayout(this)
        val topParams = FrameLayout.LayoutParams(
            FrameLayout.LayoutParams.MATCH_PARENT,
            FrameLayout.LayoutParams.WRAP_CONTENT
        )
        topParams.gravity = Gravity.BOTTOM or Gravity.CENTER_HORIZONTAL

        val bottomMarginDp = 40f
        topParams.bottomMargin = convertDpToPixel(container.context, bottomMarginDp).toInt()

        topContainer.layoutParams = topParams

        keyboardBinding = KeyboardBinding.inflate(LayoutInflater.from(this))

        hintKeyboardBinding = HintKeyboardBinding.inflate(LayoutInflater.from(this))
        val hintParams = FrameLayout.LayoutParams(
            FrameLayout.LayoutParams.WRAP_CONTENT,
            FrameLayout.LayoutParams.WRAP_CONTENT
        )
        hintParams.gravity = Gravity.BOTTOM or Gravity.CENTER_HORIZONTAL
        hintKeyboardBinding.root.layoutParams = hintParams
        hintKeyboardBinding.root.visibility = View.GONE

        topContainer.addView(keyboardBinding.root)
        topContainer.addView(hintKeyboardBinding.root)
        container.addView(topContainer)

        keyboardBinding.control.setOnTouchListener(onTouchListener)
        keyboardBinding.backspaceButton.setOnTouchListener { _, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    performVibration(-1, isCommit = true, isBackAction = true)
                    currentInputConnection?.deleteSurroundingText(1, 0)

                    backspaceRunnable = object : Runnable {
                        override fun run() {
                            performVibration(-1, isCommit = true, isBackAction = true)
                            currentInputConnection?.deleteSurroundingText(1, 0)
                            backspaceHandler.postDelayed(this, 50) // Повторять каждые 50 мс
                        }
                    }
                    backspaceHandler.postDelayed(backspaceRunnable!!, 500) // Начальная задержка 500 мс
                    true
                }
                MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                    backspaceRunnable?.let { backspaceHandler.removeCallbacks(it) }
                    true
                }
                else -> false
            }
        }

        val gestureDetector = GestureDetector(this, object : GestureDetector.SimpleOnGestureListener() {
            override fun onDoubleTap(e: MotionEvent): Boolean {
                isCapsLock = !isCapsLock
                isShifted = isCapsLock
                updateShiftKeyState()
                return true
            }

            override fun onSingleTapConfirmed(e: MotionEvent): Boolean {
                if (isCapsLock) {
                    isCapsLock = false
                    isShifted = false
                } else {
                    isShifted = !isShifted
                }
                updateShiftKeyState()
                return true
            }
        })

        keyboardBinding.shiftButton.setOnTouchListener { _, event ->
            if (event.action == MotionEvent.ACTION_DOWN) {
                performVibration(-1, isCommit = true)
            }
            gestureDetector.onTouchEvent(event)
            true
        }

        keyboardBinding.spaceButton.setOnClickListener {
            performVibration(-1, isCommit = true)
            currentInputConnection?.commitText(" ", 1)
        }

        keyboardBinding.layoutSwitchButton.setOnClickListener {
            performVibration(-1, isCommit = true)
            replaceKeyBoardLayoutForward()
        }

        applySettings()

        loadHintArrayListAsync(this) {
            replaceKeyBoardLayout(keyBoardLayoutNow)
            updateLayoutUI()
        }

        return container
    }

    private fun applySettings() {
        val sharedPreferences = getSharedPreferences("keyboard_settings", MODE_PRIVATE)
        keyboardBinding.backspaceButton.visibility = if (sharedPreferences.getBoolean("show_backspace", true)) View.VISIBLE else View.GONE
        keyboardBinding.spaceButton.visibility = if (sharedPreferences.getBoolean("show_space", true)) View.VISIBLE else View.GONE
        keyboardBinding.layoutSwitchButton.visibility = if (sharedPreferences.getBoolean("show_layout_switch", true)) View.VISIBLE else View.GONE
        keyboardBinding.shiftButton.visibility = if (sharedPreferences.getBoolean("show_shift", true)) View.VISIBLE else View.GONE

        updateSymbolsOnKeysVisibility()
    }

    private fun updateSymbolsOnKeysVisibility() {
        val sharedPreferences = getSharedPreferences("keyboard_settings", MODE_PRIVATE)
        val showSymbols = sharedPreferences.getBoolean("show_symbols_on_keys", true)
        val visibility = if (showSymbols) View.VISIBLE else View.GONE

        keyboardBinding.textPos1.visibility = visibility
        keyboardBinding.textPos2.visibility = visibility
        keyboardBinding.textPos3.visibility = visibility
        keyboardBinding.textPos4.visibility = visibility
        keyboardBinding.textPos5.visibility = visibility
        keyboardBinding.textPos6.visibility = visibility
        keyboardBinding.textPos7.visibility = visibility
        keyboardBinding.textPos8.visibility = visibility
        keyboardBinding.textPos9.visibility = visibility

        if (showSymbols) {
            updateSymbolsOnKeys()
        }
    }

    private fun updateSymbolsOnKeys() {
        val isCaseUp = isShifted || isCapsLock
        fun formatText(text: String): String {
            return if (text.length == 1 && text[0].isLetter()) {
                if (isCaseUp) text.uppercase() else text.lowercase()
            } else if (text.length > 1) {
                text.substring(0, 1) // Берем только первый символ для компактности
            } else text
        }

        val textViews = arrayOf(
            keyboardBinding.textPos1, keyboardBinding.textPos2, keyboardBinding.textPos3,
            keyboardBinding.textPos4, keyboardBinding.textPos5, keyboardBinding.textPos6,
            keyboardBinding.textPos7, keyboardBinding.textPos8, keyboardBinding.textPos9
        )

        for (i in 0..8) {
            val keyId = i + 1
            val tempArray = arrayListOf<String>()
            val placeholder = hintArray.lastOrNull() ?: " "

            // Логика подбора символов как в hintDo
            for (num in hintArrayNumber) {
                if (tempArray.size == keyId - 1) tempArray.add(placeholder)
                val sNum = num.toString()
                val f = sNum.substring(0, 1).toInt()
                val l = sNum.substring(1, 2).toInt()
                if (f == keyId || l == keyId) {
                    tempArray.add(hintHashMap[num] ?: "")
                }
            }
            if (tempArray.size == keyId - 1) tempArray.add(placeholder)

            // Формируем сетку 3x3 текстом
            val sb = StringBuilder()
            for (row in 0..2) {
                for (col in 0..2) {
                    val idx = row * 3 + col
                    if (idx < tempArray.size) {
                        val sym = formatText(tempArray[idx])
                        sb.append(if (sym.isEmpty() || sym == " ") "·" else sym) // Точка для пустых мест
                    }
                    if (col < 2) sb.append(" ")
                }
                if (row < 2) sb.append("\n")
            }
            textViews[i].text = sb.toString()
            textViews[i].typeface = android.graphics.Typeface.MONOSPACE
        }
    }

    override fun onStartInputView(info: android.view.inputmethod.EditorInfo?, restarting: Boolean) {
        super.onStartInputView(info, restarting)
        applySettings()
        hintKeyboardBinding.root.visibility = View.GONE
    }

    override fun onDestroy() {
        super.onDestroy()
        saveHintArrayListAsync(this)
    }

    private fun numberPosToOriginalNumber(firstPos: Int, lastPos: Int): Int {
        return if(firstPos < lastPos) (firstPos.toString()+lastPos).toInt() else (lastPos.toString()+firstPos).toInt()
    }

    private fun posToNumberPos(posX: Int, posY: Int): Int {
        val view = keyboardBinding.control
        if(posX < 0 || posX > view.width) return -1
        if(posY < 0 || posY > view.height) return -1

        val boxWidth = view.width / 3
        val x = when {
            posX in 0 until boxWidth -> 1
            posX < boxWidth * 2 -> 2
            else -> 3
        }

        val boxHeight = view.height / 3
        val y = when {
            posY in 0 until boxHeight -> 0
            posY < boxHeight * 2 -> 3
            else -> 6
        }

        return x + y
    }

    private fun gestureDo(){
        if(firstId == lastId) return
        val result = hintHashMap[numberPosToOriginalNumber(firstId, lastId)] ?: return
        val ic = currentInputConnection

        val isBackAction = result == "⌫" || result == "⤆"
        performVibration(lastId, isCommit = true, isBackAction = isBackAction)

        when(result){
            "⌫" -> ic?.deleteSurroundingText(1, 0)
            "˽" -> ic?.commitText(" ", 1)
            "⏎" -> ic?.sendKeyEvent(android.view.KeyEvent(android.view.KeyEvent.ACTION_DOWN, android.view.KeyEvent.KEYCODE_ENTER))
            "⤇" -> replaceKeyBoardLayoutForward()
            "⤆" -> replaceKeyBoardLayoutBack()
            "⇧" -> {
                isShifted = !isShifted
                updateShiftKeyState()
            }
            else -> {
                val textToCommit = if (isShifted || isCapsLock) result.uppercase() else result.lowercase()
                ic?.commitText(textToCommit, 1)

                if (isShifted && !isCapsLock) {
                    isShifted = false
                    updateShiftKeyState()
                }
            }
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

        hintKeyboardBinding.root.visibility = View.VISIBLE

        val hintParams = hintKeyboardBinding.root.layoutParams as FrameLayout.LayoutParams
        hintParams.gravity = Gravity.BOTTOM or Gravity.CENTER_HORIZONTAL

        val isCaseUp = isShifted || isCapsLock

        fun getHintText(index: Int): CharSequence {
            val text = hintArray[tempArray[index]]
            return if (text.length == 1 && text[0].isLetter()) {
                if (isCaseUp) text.uppercase() else text.lowercase()
            } else {
                text
            }
        }

        hintKeyboardBinding.viewPos1.text = getHintText(0)
        hintKeyboardBinding.viewPos2.text = getHintText(1)
        hintKeyboardBinding.viewPos3.text = getHintText(2)
        hintKeyboardBinding.viewPos4.text = getHintText(3)
        hintKeyboardBinding.viewPos5.text = getHintText(4)
        hintKeyboardBinding.viewPos6.text = getHintText(5)
        hintKeyboardBinding.viewPos7.text = getHintText(6)
        hintKeyboardBinding.viewPos8.text = getHintText(7)
        hintKeyboardBinding.viewPos9.text = getHintText(8)
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
        performVibration(pos)
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

    @SuppressLint("SetTextI18n")
    private fun updateLayoutUI() {
        keyboardBinding.layoutSwitchButton.text = (keyBoardLayoutNow + 1).toString()
        val sharedPreferences = getSharedPreferences("keyboard_settings", MODE_PRIVATE)
        if (sharedPreferences.getBoolean("show_symbols_on_keys", true)) {
            updateSymbolsOnKeys()
        }
    }

    private fun replaceKeyBoardLayoutForward(){
        keyBoardLayoutNow++
        if(keyBoardLayoutNow > hintArrayList.size - 1) keyBoardLayoutNow = 0
        replaceKeyBoardLayout(keyBoardLayoutNow)
        updateLayoutUI()
        // Сохраняем изменения сразу
        saveHintArrayListAsync(this)
    }

    private fun replaceKeyBoardLayoutBack(){
        keyBoardLayoutNow--
        if(keyBoardLayoutNow < 0) keyBoardLayoutNow = hintArrayList.size - 1
        replaceKeyBoardLayout(keyBoardLayoutNow)
        updateLayoutUI()
        // Сохраняем изменения сразу
        saveHintArrayListAsync(this)
    }
}