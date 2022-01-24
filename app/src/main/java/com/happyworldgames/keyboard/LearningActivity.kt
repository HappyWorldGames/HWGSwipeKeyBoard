package com.happyworldgames.keyboard

import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.floatingactionbutton.FloatingActionButton

/*
    Имеется область сверху, это подсказка, так же есть область снизу, это управление, она же активная зона.
    Активная зона имеет квадрат 3x3, вокруг круга. Всего 9 частей по которым можно проводить пальцем,
    нажатие на каждую часть выводит в подсказке возможные комбинации
    Свайп из нажатой части в другую вызовет действие.

    0. Показать стрелками на поле подсказки и активную зону
    1. Активная зона имеет квадрат вокруг круга 3x3(виртуально разделено), при нажатии на любую область активной зоны
    2. В области подсказок выводятся возможные комбинации
    3. Свайп из нажатой области в другую область активной зоны приведет к действию из области подсказок
    4. Возможные действия: напечатать, переключить раскладку, backspace(стереть), enter(ввод), space(пробел)
    5. Зажатие круга даст возможность перемежать его по экрану
 */

class LearningActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.learning)

        var learnImgPos = 0
        val learnImgId = arrayOf(
            R.drawable.learn_1,
            R.drawable.learn_1_hand,
            R.drawable.learn_2_hand,
            R.drawable.learn_3_hand,
            R.drawable.learn_4
        )

        findViewById<FloatingActionButton>(R.id.next_learn).setOnClickListener {
            learnImgPos++
            if(learnImgPos >= learnImgId.size) learnImgPos = 0
            setImage(learnImgId[learnImgPos])
            setText(learnImgPos)
        }
        findViewById<FloatingActionButton>(R.id.previous_learn).setOnClickListener {
            learnImgPos--
            if(learnImgPos < 0) learnImgPos = learnImgId.size - 1
            setImage(learnImgId[learnImgPos])
            setText(learnImgPos)
        }
    }

    private fun setText(pos: Int) {
        findViewById<TextView>(R.id.textView2).text = "$pos"
    }
    private fun setImage(id: Int) {
        findViewById<ImageView>(R.id.imageView).setImageResource(id)
    }
}