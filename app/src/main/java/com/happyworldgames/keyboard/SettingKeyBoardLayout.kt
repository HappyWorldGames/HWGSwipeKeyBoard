package com.happyworldgames.keyboard

import android.content.Intent
import android.content.res.Resources
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.graphics.toColorInt
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.tabs.TabLayoutMediator
import com.happyworldgames.keyboard.databinding.SettingKeyboardBinding
import com.happyworldgames.keyboard.databinding.SettingKeyboardViewPagerBinding
import java.io.File


class SettingKeyBoardLayout : AppCompatActivity() {

    private val settingKeyBoardViewPagerBinding by lazy { SettingKeyboardViewPagerBinding.inflate(layoutInflater) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(settingKeyBoardViewPagerBinding.root)

        val controller = WindowInsetsControllerCompat(window, settingKeyBoardViewPagerBinding.root)
        controller.isAppearanceLightStatusBars = true

        ViewCompat.setOnApplyWindowInsetsListener(settingKeyBoardViewPagerBinding.root) { view, windowInsets ->
            val insets = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars())
            view.setPadding(insets.left, insets.top, insets.right, insets.bottom)
            windowInsets
        }

        onBackPressedDispatcher.addCallback(this, object: OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                SimpleIME.replaceKeyBoardLayout(0)
                SimpleIME.saveHintArrayListAsync(this@SettingKeyBoardLayout)
                startActivity(Intent(this@SettingKeyBoardLayout, MainActivity::class.java))
            }
        })

        settingKeyBoardViewPagerBinding.keyLayoutView.adapter = CustomViewPagerRecyclerAdapter()
        val mediator = TabLayoutMediator(settingKeyBoardViewPagerBinding.keyLayouts, settingKeyBoardViewPagerBinding.keyLayoutView) { tab, position ->
            tab.text = "№$position"
        }
        mediator.attach()

        settingKeyBoardViewPagerBinding.addLayout.setOnClickListener {
            val newLayout = ArrayList(SimpleIME.hintArrayList.lastOrNull() ?: SimpleIME.hintArrayList[0])
            SimpleIME.hintArrayList.add(newLayout)
            settingKeyBoardViewPagerBinding.keyLayoutView.adapter?.notifyItemInserted(SimpleIME.hintArrayList.size - 1)
            settingKeyBoardViewPagerBinding.keyLayoutView.currentItem = SimpleIME.hintArrayList.size - 1
            SimpleIME.saveHintArrayListAsync(this)
        }

        settingKeyBoardViewPagerBinding.deleteLayout.setOnClickListener {
            if (SimpleIME.hintArrayList.size > 1) {
                val currentItem = settingKeyBoardViewPagerBinding.keyLayoutView.currentItem
                SimpleIME.hintArrayList.removeAt(currentItem)
                settingKeyBoardViewPagerBinding.keyLayoutView.adapter?.notifyItemRemoved(currentItem)
                SimpleIME.saveHintArrayListAsync(this)
                // Re-attaching mediator to refresh tab names.
                mediator.detach()
                mediator.attach()
            }
        }

        SimpleIME.loadHintArrayListAsync(this@SettingKeyBoardLayout) {
            settingKeyBoardViewPagerBinding.keyLayoutView.adapter?.notifyDataSetChanged()
            mediator.detach()
            mediator.attach()
        }
    }

    override fun onResume() {
        super.onResume()
//        TODO()
//        if (intent.extras != null) {
//            settingKeyBoardViewPagerBinding.keyLayoutView.adapter?.notifyItemChanged(
//                intent.extras!!.getInt("itemPos")
//            )
//        }
        settingKeyBoardViewPagerBinding.keyLayoutView.adapter?.notifyDataSetChanged()
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
                in 0..7 -> "#F4511E".toColorInt() // 1
                in 8..14 -> "#FB8C00".toColorInt() // 2
                in 14..20 -> "#E53935".toColorInt() // 3
                in 21..25 -> "#43A047".toColorInt() // 4
                in 26..29 -> "#00897B".toColorInt() // 5
                in 30..32 -> "#1E88E5".toColorInt() // 6
                in 33..34 -> "#3949AB".toColorInt() // 7
                else -> "#8E24AA".toColorInt() // 8
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