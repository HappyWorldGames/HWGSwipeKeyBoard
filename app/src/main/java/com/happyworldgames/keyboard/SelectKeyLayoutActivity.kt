package com.happyworldgames.keyboard

import android.content.Intent
import android.content.res.Resources
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.tabs.TabLayoutMediator
import com.happyworldgames.keyboard.databinding.SelectKeyLayoutBinding
import com.happyworldgames.keyboard.databinding.SelectKeyLayoutItemBinding
import com.happyworldgames.keyboard.databinding.SettingKeyboardBinding

class SelectKeyLayoutActivity : AppCompatActivity() {
    companion object {
        private val allSymbolsName = arrayListOf(
            "Symbols", "English", "Russia"
        )
        private val allSymbols = arrayListOf(
            arrayListOf("⌫", "⏎", "˽", "⤆", "⤇", "1", "2", "3", "4", "5", "6", "7", "8", "9", "0", ".", ",", ";", ":"),
            arrayListOf("a", "b", "c", "d", "e", "f", "g", "h", "i", "j", "k", "l", "m", "n", "o", "p", "q", "r", "s", "t", "u", "v", "w", "x", "y", "z"),
            arrayListOf("а", "б", "в", "г", "д", "е", "ё", "ж", "з", "и", "й", "к", "л", "м", "н", "о", "п", "р", "с", "т", "у", "ф", "х", "ц", "ч", "ш", "щ", "ъ", "ы", "ь", "э", "ю", "я"),
        )
    }

    private val selectKeyLayoutBinding by lazy { SelectKeyLayoutBinding.inflate(layoutInflater) }

    private val positionArray by lazy { intent.getIntExtra("array", -1) }
    private val positionItem by lazy { intent.getIntExtra("position", -1) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, false)
        setContentView(selectKeyLayoutBinding.root)

        ViewCompat.setOnApplyWindowInsetsListener(selectKeyLayoutBinding.root) { view, windowInsets ->
            val insets = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars())
            view.setPadding(insets.left, insets.top, insets.right, insets.bottom)
            windowInsets
        }

        if(positionItem == -1 || positionArray == -1) return
        selectKeyLayoutBinding.editText.setText(SimpleIME.hintArrayList[positionArray][positionItem])

        onBackPressedDispatcher.addCallback(this, object: OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                SimpleIME.hintArrayList[positionArray][positionItem] = selectKeyLayoutBinding.editText.text.toString()
                SimpleIME.replaceKeyBoardLayout(positionArray)
                SimpleIME.saveHintArrayListAsync(this@SelectKeyLayoutActivity)
                startActivity(Intent(this@SelectKeyLayoutActivity, SettingKeyBoardLayout::class.java).apply {
                    putExtra("itemPos", positionItem)
                    putExtra("itemArray", positionArray)
                })
            }
        })

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
            val spanCount = Resources.getSystem().displayMetrics.widthPixels / SimpleIME.convertDpToPixel(applicationContext, 70f).toInt()
            holder.main.symbolsRecyclerView.layoutManager = GridLayoutManager(holder.main.root.context, spanCount)
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