package com.happyworldgames.keyboard

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.provider.Settings
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.view.animation.AccelerateDecelerateInterpolator
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.GridLayout
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.graphics.toColorInt
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import com.google.android.material.button.MaterialButton

class LearningActivity : AppCompatActivity() {

    private lateinit var instructionText: TextView
    private lateinit var editText: EditText
    private lateinit var nextButton: MaterialButton
    private lateinit var progressBar: ProgressBar
    private lateinit var gestureArrow: ImageView
    private lateinit var visualizerGrid: GridLayout

    private var currentStep = 0
    private var isPracticeMode = false // Режим практики начинается после всех демо

    private val demoSteps = listOf(
        LearningStep(R.string.learn_welcome),
        LearningStep(R.string.learn_intro),
        LearningStep(R.string.learn_step_a, startZone = 1, endZone = 2),
        LearningStep(R.string.learn_step_space, startZone = 5, endZone = 8),
        LearningStep(R.string.learn_step_backspace, startZone = 5, endZone = 4, showReverse = true),
        LearningStep(R.string.learn_step_shift, startZone = 5, endZone = 2, showReverse = true),
        LearningStep(R.string.learn_step_layout, startZone = 5, endZone = 9),
        LearningStep(R.string.learn_step_side_buttons)
    )

    private val practiceSteps = listOf(
        LearningStep(R.string.learn_now_try, expectedChars = listOf("a", "а"), startZone = 1, endZone = 2),
        LearningStep(R.string.learn_step_space, expectedChars = listOf(" "), startZone = 5, endZone = 8),
        LearningStep(R.string.learn_step_backspace, isBackspace = true, startZone = 5, endZone = 4),
        LearningStep(R.string.learn_step_finished)
    )

    private var lastTextLength = 0
    private var animationSet: AnimatorSet? = null

    data class LearningStep(
        val textResId: Int,
        val expectedChars: List<String>? = null,
        val isBackspace: Boolean = false,
        val startZone: Int? = null,
        val endZone: Int? = null,
        val showReverse: Boolean = false
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, false)
        setContentView(R.layout.learning)

        val rootView = findViewById<View>(android.R.id.content)
        val controller = WindowInsetsControllerCompat(window, rootView)
        controller.isAppearanceLightStatusBars = true

        ViewCompat.setOnApplyWindowInsetsListener(rootView) { view, windowInsets ->
            val insets = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars())
            view.setPadding(insets.left, insets.top, insets.right, insets.bottom)
            windowInsets
        }

        instructionText = findViewById(R.id.instructionText)
        editText = findViewById(R.id.editText)
        nextButton = findViewById(R.id.nextButton)
        progressBar = findViewById(R.id.learningProgress)
        gestureArrow = findViewById(R.id.gestureArrow)
        visualizerGrid = findViewById(R.id.visualizerGrid)

        progressBar.max = demoSteps.size + practiceSteps.size
        
        updateStep()

        nextButton.setOnClickListener {
            if (!isKeyboardEnabled()) {
                startActivity(Intent(Settings.ACTION_INPUT_METHOD_SETTINGS))
            } else if (!isKeyboardSelected()) {
                val imeManager = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
                imeManager.showInputMethodPicker()
            } else {
                moveToNext()
            }
        }

        editText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                lastTextLength = s?.length ?: 0
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                if (!isPracticeMode) return

                val step = practiceSteps[currentStep]
                val currentText = s?.toString() ?: ""
                
                if (step.expectedChars != null) {
                    val lastChar = if (currentText.isNotEmpty()) currentText.takeLast(1) else ""
                    if (step.expectedChars.any { it.equals(lastChar, ignoreCase = true) }) {
                        completeStep()
                    }
                } else if (step.isBackspace) {
                    if (currentText.length < lastTextLength) {
                        completeStep()
                    }
                }
            }

            override fun afterTextChanged(s: Editable?) {}
        })
    }

    private fun moveToNext() {
        if (!isPracticeMode) {
            if (currentStep < demoSteps.size - 1) {
                currentStep++
            } else {
                isPracticeMode = true
                currentStep = 0
                // При переходе к практике фокусируемся на поле ввода
                editText.requestFocus()
                val imm = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
                imm.showSoftInput(editText, 0)
            }
        } else {
            if (currentStep < practiceSteps.size - 1) {
                currentStep++
            } else {
                finish()
                return
            }
        }
        updateStep()
    }

    override fun onResume() {
        super.onResume()
        updateStep()
    }

    private fun isKeyboardEnabled(): Boolean {
        val imm = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
        return imm.enabledInputMethodList.any { it.packageName == packageName }
    }

    private fun isKeyboardSelected(): Boolean {
        val currentIme = Settings.Secure.getString(contentResolver, Settings.Secure.DEFAULT_INPUT_METHOD)
        return currentIme?.contains(packageName) == true
    }

    private fun updateStep() {
        if (!isKeyboardEnabled()) {
            instructionText.text = getString(R.string.learn_keyboard_not_enabled)
            nextButton.text = getString(R.string.turn_on_keyboard)
            nextButton.visibility = View.VISIBLE
            gestureArrow.visibility = View.GONE
            return
        }

        if (!isKeyboardSelected()) {
            instructionText.text = getString(R.string.learn_keyboard_not_selected)
            nextButton.text = getString(R.string.select_keyboard)
            nextButton.visibility = View.VISIBLE
            gestureArrow.visibility = View.GONE
            return
        }

        val step = if (isPracticeMode) practiceSteps[currentStep] else demoSteps[currentStep]
        
        instructionText.text = getString(step.textResId)
        
        if (isPracticeMode) {
            progressBar.progress = demoSteps.size + currentStep + 1
            // В режиме практики кнопка "Далее" может служить пропуском
            nextButton.text = getString(if (currentStep == practiceSteps.size - 1) android.R.string.ok else R.string.next)
            nextButton.visibility = View.VISIBLE 
            
            // Если это финальный шаг практики, скрываем анимацию
            if (step.expectedChars == null && !step.isBackspace && currentStep == practiceSteps.size - 1) {
                startGestureAnimation(null, null)
            } else {
                startGestureAnimation(step.startZone, step.endZone, step.showReverse)
            }
        } else {
            progressBar.progress = currentStep + 1
            nextButton.text = getString(R.string.next)
            nextButton.visibility = View.VISIBLE
            startGestureAnimation(step.startZone, step.endZone, step.showReverse)
        }
    }

    private fun completeStep() {
        editText.setBackgroundColor("#2000FF00".toColorInt())
        editText.postDelayed({
            editText.setBackgroundColor(Color.TRANSPARENT)
            moveToNext()
        }, 300)
    }

    private fun startGestureAnimation(startZone: Int?, endZone: Int?, showReverse: Boolean = false) {
        // 1. Останавливаем и полностью очищаем старую анимацию
        animationSet?.removeAllListeners()
        animationSet?.cancel()
        animationSet = null
        
        gestureArrow.visibility = View.GONE
        gestureArrow.alpha = 0f
        
        // Сброс подсветки ячеек
        for (i in 0 until visualizerGrid.childCount) {
            val child = visualizerGrid.getChildAt(i)
            if (child.id == R.id.vPos5) {
                 child.setBackgroundResource(R.drawable.circle_center)
            } else {
                 child.setBackgroundColor("#20000000".toColorInt())
            }
        }

        if (startZone == null || endZone == null) return

        val startView = getZoneView(startZone)
        val endView = getZoneView(endZone)

        // Подсвечиваем активные зоны
        startView.setBackgroundColor("#4000FF00".toColorInt())
        endView.setBackgroundColor("#4000FF00".toColorInt())

        gestureArrow.visibility = View.VISIBLE
        
        visualizerGrid.post {
            val startX = startView.x + startView.width / 2f - gestureArrow.width / 2f
            val startY = startView.y + startView.height / 2f - gestureArrow.height / 2f
            val endX = endView.x + endView.width / 2f - gestureArrow.width / 2f
            val endY = endView.y + endView.height / 2f - gestureArrow.height / 2f

            val angleForward = Math.toDegrees(Math.atan2((endY - startY).toDouble(), (endX - startX).toDouble())).toFloat()
            val angleBackward = angleForward + 180f

            // Подготовка объектов анимации
            val fadeIn = ObjectAnimator.ofFloat(gestureArrow, "alpha", 0f, 1f).setDuration(200)
            val fadeOut = ObjectAnimator.ofFloat(gestureArrow, "alpha", 1f, 0f).setDuration(200)
            
            val moveForwardX = ObjectAnimator.ofFloat(gestureArrow, "x", startX, endX).setDuration(800)
            val moveForwardY = ObjectAnimator.ofFloat(gestureArrow, "y", startY, endY).setDuration(800)
            
            val moveBackwardX = ObjectAnimator.ofFloat(gestureArrow, "x", endX, startX).setDuration(800)
            val moveBackwardY = ObjectAnimator.ofFloat(gestureArrow, "y", endY, startY).setDuration(800)

            val pause = ObjectAnimator.ofFloat(gestureArrow, "alpha", 1f, 1f).setDuration(300)

            val newSet = AnimatorSet()
            if (showReverse) {
                // Сложная цепочка: Появление -> Вперед -> Пауза(с разворотом) -> Назад -> Исчезновение
                val rotateBackward = ObjectAnimator.ofFloat(gestureArrow, "rotation", angleForward, angleBackward).setDuration(0)
                
                newSet.playSequentially(
                    fadeIn,
                    AnimatorSet().apply { playTogether(moveForwardX, moveForwardY) },
                    pause,
                    rotateBackward,
                    AnimatorSet().apply { playTogether(moveBackwardX, moveBackwardY) },
                    fadeOut
                )
            } else {
                // Простая цепочка: Появление -> Вперед -> Исчезновение
                newSet.playSequentially(
                    fadeIn,
                    AnimatorSet().apply { playTogether(moveForwardX, moveForwardY) },
                    fadeOut
                )
            }

            newSet.interpolator = AccelerateDecelerateInterpolator()
            newSet.addListener(object : android.animation.AnimatorListenerAdapter() {
                private var cancelled = false

                override fun onAnimationCancel(animation: android.animation.Animator) {
                    cancelled = true
                }

                override fun onAnimationStart(animation: android.animation.Animator) {
                    gestureArrow.x = startX
                    gestureArrow.y = startY
                    gestureArrow.rotation = angleForward
                }

                override fun onAnimationEnd(animation: android.animation.Animator) {
                    if (!cancelled) {
                        // Рекурсивный перезапуск этого же жеста через небольшую паузу
                        gestureArrow.postDelayed({
                            if (!cancelled) newSet.start()
                        }, 500)
                    }
                }
            })

            animationSet = newSet
            newSet.start()
        }
    }

    private fun getZoneView(zone: Int): View {
        return when (zone) {
            1 -> findViewById(R.id.vPos1)
            2 -> findViewById(R.id.vPos2)
            3 -> findViewById(R.id.vPos3)
            4 -> findViewById(R.id.vPos4)
            5 -> findViewById(R.id.vPos5)
            6 -> findViewById(R.id.vPos6)
            7 -> findViewById(R.id.vPos7)
            8 -> findViewById(R.id.vPos8)
            9 -> findViewById(R.id.vPos9)
            else -> findViewById(R.id.vPos5)
        }
    }
}