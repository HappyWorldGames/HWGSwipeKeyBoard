package com.happyworldgames.keyboard

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.happyworldgames.keyboard.databinding.SettingKeyboardBinding

class SettingKeyBoardLayout : AppCompatActivity() {

    private val settingKeyBoardBinding by lazy { SettingKeyboardBinding.inflate(layoutInflater) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(settingKeyBoardBinding.root)

        settingKeyBoardBinding.symbolsRecyclerView.layoutManager = GridLayoutManager(this, 3) //LinearLayoutManager(this)
        settingKeyBoardBinding.symbolsRecyclerView.adapter = CustomRecyclerAdapter()
    }

    override fun onBackPressed() {
        SimpleIME.fullHintHashMap()
        super.onBackPressed()
    }

    class CustomRecyclerAdapter : RecyclerView.Adapter<CustomRecyclerAdapter.MyViewHolder>() {

        private var mainLayout: LinearLayout? = null
        private lateinit var editText: EditText
        private lateinit var editAlertDialog: AlertDialog

        class MyViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            val mainLayout: ConstraintLayout = itemView.findViewById(R.id.main)
            val combinationTextView: TextView = itemView.findViewById(R.id.combinate)
            val combinationValueTextView: TextView = itemView.findViewById(R.id.combinate_value)
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
            val itemView = LayoutInflater.from(parent.context)
                .inflate(R.layout.setting_keyboard_item, parent, false)
            return MyViewHolder(itemView)
        }

        override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
            val backgroundColor = when(position){
                in 0..7 -> Color.parseColor("#F4511E") // 1
                in 8..14 -> Color.parseColor("#FB8C00") // 2
                in 14..20 -> Color.parseColor("#E53935") // 3
                in 21..25 -> Color.parseColor("#43A047") // 4
                in 26..29 -> Color.parseColor("#00897B") // 5
                in 30..32 -> Color.parseColor("#1E88E5") // 6
                in 33..34 -> Color.parseColor("#3949AB") // 7
                else -> Color.parseColor("#8E24AA") // 8
            }

            holder.mainLayout.setBackgroundColor(backgroundColor)
            holder.combinationTextView.text = combinationToString(SimpleIME.hintArrayNumber[position])
            holder.combinationValueTextView.text = SimpleIME.hintArray[position]

            holder.itemView.setOnClickListener {
                val context = holder.itemView.context

                val intent = Intent(context, SelectKeyLayoutActivity::class.java)
                intent.putExtra("position", position)
                context.startActivity(intent)
/*
                initAlertDialog(context)
                editText.setText(SimpleIME.hintArray[position])

                editAlertDialog.setButton(AlertDialog.BUTTON_POSITIVE, "OK"){ _, _ ->
                    SimpleIME.hintArray[position] = editText.text.toString()
                }
                editAlertDialog.show()*/
            }
        }

        override fun getItemCount() = SimpleIME.hintArrayNumber.size

        private fun combinationToString(number: Int): String {
            val first = number.toString()[0]
            val last = number.toString()[1]

            return "($first➜$last)\n($last➜$first)"
        }
        private fun initAlertDialog(context: Context){
            if(mainLayout != null) return

            mainLayout = LinearLayout(context)
            mainLayout!!.orientation = LinearLayout.VERTICAL

            editText = EditText(context)
            mainLayout!!.addView(editText)

            val buttons = LinearLayout(context)
            buttons.layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT)
            buttons.orientation = LinearLayout.HORIZONTAL
            mainLayout!!.addView(buttons)

            val backspaceButton = Button(context)
            backspaceButton.layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
            backspaceButton.text = "⌫"
            backspaceButton.setOnClickListener {
                editText.append("⌫")
            }
            buttons.addView(backspaceButton)

            val enterButton = Button(context)
            enterButton.layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
            enterButton.text = "⏎"
            enterButton.setOnClickListener {
                editText.append("⏎")
            }
            buttons.addView(enterButton)

            val spaceButton = Button(context)
            spaceButton.layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
            spaceButton.text = "˽"
            spaceButton.setOnClickListener {
                editText.append("˽")
            }
            buttons.addView(spaceButton)

            editAlertDialog = AlertDialog.Builder(context)
                .setView(mainLayout)
                .setNeutralButton("Cancel", null)
                .create()
        }

    }

}