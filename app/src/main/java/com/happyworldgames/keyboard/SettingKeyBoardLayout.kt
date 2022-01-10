package com.happyworldgames.keyboard

import android.content.Intent
import android.content.res.Resources
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.tabs.TabLayoutMediator
import com.happyworldgames.keyboard.databinding.SettingKeyboardBinding
import com.happyworldgames.keyboard.databinding.SettingKeyboardViewPagerBinding

class SettingKeyBoardLayout : AppCompatActivity() {

    private val settingKeyBoardViewPagerBinding by lazy { SettingKeyboardViewPagerBinding.inflate(layoutInflater) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(settingKeyBoardViewPagerBinding.root)

        settingKeyBoardViewPagerBinding.keyLayoutView.adapter = CustomViewPagerRecyclerAdapter()
        TabLayoutMediator(settingKeyBoardViewPagerBinding.keyLayouts, settingKeyBoardViewPagerBinding.keyLayoutView) { tab, position ->
            tab.text = "№$position"
        }.attach()
    }

    override fun onBackPressed() {
        SimpleIME.replaceKeyBoardLayout(0)
        SimpleIME.saveHintArrayList()
        startActivity(Intent(this, MainActivity::class.java))
    }

    inner class CustomViewPagerRecyclerAdapter : RecyclerView.Adapter<CustomViewPagerRecyclerAdapter.MyViewHolder>() {

        inner class MyViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            val main = SettingKeyboardBinding.bind(itemView)
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
            val itemView = LayoutInflater.from(parent.context).inflate(R.layout.setting_keyboard, parent, false)
            return MyViewHolder(itemView)
        }

        override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
            val spanCount = Resources.getSystem().displayMetrics.widthPixels / SimpleIME.convertDpToPixel(applicationContext, 120f).toInt()
            holder.main.symbolsRecyclerView.layoutManager = GridLayoutManager(holder.main.root.context, spanCount)
            holder.main.symbolsRecyclerView.adapter = CustomRecyclerAdapter(position)
        }

        override fun getItemCount(): Int = SimpleIME.hintArrayList.size
    }

    class CustomRecyclerAdapter(private val dataIndex: Int) : RecyclerView.Adapter<CustomRecyclerAdapter.MyViewHolder>() {

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
            holder.combinationValueTextView.text = SimpleIME.hintArrayList[dataIndex][position]

            holder.itemView.setOnClickListener {
                val context = holder.itemView.context

                val intent = Intent(context, SelectKeyLayoutActivity::class.java)
                intent.putExtra("position", position)
                intent.putExtra("array", dataIndex)
                context.startActivity(intent)
            }
        }

        override fun getItemCount() = SimpleIME.hintArrayNumber.size

        private fun combinationToString(number: Int): String {
            val first = number.toString()[0]
            val last = number.toString()[1]

            return "($first➜$last)\n($last➜$first)"
        }

    }

}