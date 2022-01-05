package com.happyworldgames.keyboard

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.tabs.TabLayoutMediator
import com.happyworldgames.keyboard.databinding.SelectKeyLayoutBinding
import com.happyworldgames.keyboard.databinding.SelectKeyLayoutItemBinding
import com.happyworldgames.keyboard.databinding.SettingKeyboardBinding

class SelectKeyLayoutActivity : AppCompatActivity() {
    companion object {
        private val allSymbolsName = arrayListOf(
            "Symbols", "English"
        )
        private val allSymbols = arrayListOf(
            arrayListOf("0", "1", "2", "3", "4", "5", "6", "7", "8", "9", ".", ",", ";", ":"),
            arrayListOf("A", "B", "C", "D", "E", "F", "G", "H", "I", "J", "K", "L", "M", "N", "O", "P", "Q", "R", "S", "T", "U", "V", "W", "X", "Y", "Z")
        )
    }

    private val selectKeyLayoutBinding by lazy { SelectKeyLayoutBinding.inflate(layoutInflater) }

    private val positionItem by lazy { intent.getIntExtra("position", -1) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(selectKeyLayoutBinding.root)

        if(positionItem == -1) return
        selectKeyLayoutBinding.editText.setText(SimpleIME.hintArray[positionItem])

        selectKeyLayoutBinding.viewpager.adapter = CustomViewPagerRecyclerAdapter()
        TabLayoutMediator(selectKeyLayoutBinding.tabLayout, selectKeyLayoutBinding.viewpager) { tab, position ->
            tab.text = allSymbolsName[position]
        }.attach()

        selectKeyLayoutBinding.backspace.setOnClickListener {
            val cursorPosition: Int = selectKeyLayoutBinding.editText.selectionStart
            val length = selectKeyLayoutBinding.editText.text.length
            if (cursorPosition > 0) {
                selectKeyLayoutBinding.editText.text = selectKeyLayoutBinding.editText.text.delete(cursorPosition - 1, cursorPosition)
                selectKeyLayoutBinding.editText.setSelection(cursorPosition - 1)
            }else if(length > 0){
                selectKeyLayoutBinding.editText.text = selectKeyLayoutBinding.editText.text.delete(length -1, length)
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

    inner class CustomViewPagerRecyclerAdapter : RecyclerView.Adapter<CustomViewPagerRecyclerAdapter.MyViewHolder>() {

        inner class MyViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            val main = SettingKeyboardBinding.bind(itemView)
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
            val itemView = LayoutInflater.from(parent.context).inflate(R.layout.setting_keyboard, parent, false)
            return MyViewHolder(itemView)
        }

        override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
            holder.main.symbolsRecyclerView.layoutManager = GridLayoutManager(holder.main.root.context, 4)
            holder.main.symbolsRecyclerView.adapter = CustomRecyclerAdapter(allSymbols[position])
        }

        override fun getItemCount(): Int = allSymbols.size
    }

    inner class CustomRecyclerAdapter(private val symbols: ArrayList<String>) : RecyclerView.Adapter<CustomRecyclerAdapter.MyViewHolder>() {

        inner class MyViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            val mainLayout = SelectKeyLayoutItemBinding.bind(itemView)
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
            val itemView = LayoutInflater.from(parent.context).inflate(R.layout.select_key_layout_item, parent, false)
            return MyViewHolder(itemView)
        }

        override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
            holder.mainLayout.value.text = symbols[position]
            holder.mainLayout.root.setOnClickListener {
                selectKeyLayoutBinding.editText.append(symbols[position])
            }
        }

        override fun getItemCount(): Int = symbols.size
    }
}