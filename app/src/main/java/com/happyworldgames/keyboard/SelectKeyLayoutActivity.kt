package com.happyworldgames.keyboard

import android.content.Intent
import android.content.res.Resources
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
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
    
    // Класс для элементов списка (либо заголовок, либо символ)
    sealed class SymbolItem {
        data class Header(val title: String) : SymbolItem()
        data class Symbol(val value: String) : SymbolItem()
    }

    companion object {
        // Базовый набор символов с категориями
        private val symbolsWithHeaders = arrayListOf(
            SymbolItem.Header("Управление"),
            SymbolItem.Symbol("⚙️"), SymbolItem.Symbol("❌"), SymbolItem.Symbol("⌫"), 
            SymbolItem.Symbol("⏎"), SymbolItem.Symbol("˽"), SymbolItem.Symbol("⤆"), 
            SymbolItem.Symbol("⤇"), SymbolItem.Symbol("⇧"),
            
            SymbolItem.Header("Цифры"),
            SymbolItem.Symbol("1"), SymbolItem.Symbol("2"), SymbolItem.Symbol("3"), 
            SymbolItem.Symbol("4"), SymbolItem.Symbol("5"), SymbolItem.Symbol("6"), 
            SymbolItem.Symbol("7"), SymbolItem.Symbol("8"), SymbolItem.Symbol("9"), 
            SymbolItem.Symbol("0"),
            
            SymbolItem.Header("Пунктуация"),
            SymbolItem.Symbol("."), SymbolItem.Symbol(","), SymbolItem.Symbol("!"), 
            SymbolItem.Symbol("?"), SymbolItem.Symbol(";"), SymbolItem.Symbol(":"), 
            SymbolItem.Symbol("\""), SymbolItem.Symbol("'"), SymbolItem.Symbol("("), 
            SymbolItem.Symbol(")"), SymbolItem.Symbol("["), SymbolItem.Symbol("]"), 
            SymbolItem.Symbol("{"), SymbolItem.Symbol("}"), SymbolItem.Symbol("<"), 
            SymbolItem.Symbol(">"), SymbolItem.Symbol("«"), SymbolItem.Symbol("»"),
            
            SymbolItem.Header("Символы и Математика"),
            SymbolItem.Symbol("@"), SymbolItem.Symbol("#"), SymbolItem.Symbol("$"), 
            SymbolItem.Symbol("€"), SymbolItem.Symbol("%"), SymbolItem.Symbol("&"), 
            SymbolItem.Symbol("*"), SymbolItem.Symbol("-"), SymbolItem.Symbol("+"), 
            SymbolItem.Symbol("="), SymbolItem.Symbol("/"), SymbolItem.Symbol("\\"), 
            SymbolItem.Symbol("|"), SymbolItem.Symbol("~"), SymbolItem.Symbol("±"), 
            SymbolItem.Symbol("×"), SymbolItem.Symbol("÷"), SymbolItem.Symbol("°")
        )

        // Языки (в будущем их можно фильтровать на основе настроек)
        private val languages = arrayListOf(
            "English" to arrayListOf("a", "b", "c", "d", "e", "f", "g", "h", "i", "j", "k", "l", "m", "n", "o", "p", "q", "r", "s", "t", "u", "v", "w", "x", "y", "z"),
            "Russia" to arrayListOf("а", "б", "в", "г", "д", "е", "ё", "ж", "з", "и", "й", "к", "л", "м", "н", "о", "п", "р", "с", "т", "у", "ф", "х", "ц", "ч", "ш", "щ", "ъ", "ы", "ь", "э", "ю", "я")
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
        selectKeyLayoutBinding.editText.requestFocus()
        selectKeyLayoutBinding.editText.setSelection(selectKeyLayoutBinding.editText.text.length)

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
            tab.text = if (position == 0) "Symbols" else languages[position - 1].first
        }.attach()

        selectKeyLayoutBinding.backspace.setOnClickListener {
            val cursorPosition: Int = selectKeyLayoutBinding.editText.selectionStart
            if (cursorPosition > 0) {
                selectKeyLayoutBinding.editText.text = selectKeyLayoutBinding.editText.text.delete(cursorPosition - 1, cursorPosition)
                selectKeyLayoutBinding.editText.setSelection(cursorPosition - 1)
            } else if (cursorPosition == -1 && selectKeyLayoutBinding.editText.text.isNotEmpty()) {
                // Если фокуса нет, стираем с конца
                val length = selectKeyLayoutBinding.editText.text.length
                selectKeyLayoutBinding.editText.text = selectKeyLayoutBinding.editText.text.delete(length - 1, length)
            }
        }
    }

    inner class CustomViewPagerRecyclerAdapter : RecyclerView.Adapter<CustomViewPagerRecyclerAdapter.MyViewHolder>() {
        inner class MyViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            val main = SettingKeyboardBinding.bind(itemView)
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
            return MyViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.setting_keyboard, parent, false))
        }

        override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
            val currentPos = holder.bindingAdapterPosition
            val context = holder.main.root.context
            val spanCount = Resources.getSystem().displayMetrics.widthPixels / SimpleIME.convertDpToPixel(context, 60f).toInt()
            
            val layoutManager = GridLayoutManager(context, spanCount)
            
            if (currentPos == 0) {
                // Для вкладки Symbols настраиваем заголовки на всю ширину
                layoutManager.spanSizeLookup = object : GridLayoutManager.SpanSizeLookup() {
                    override fun getSpanSize(pos: Int): Int {
                        return if (symbolsWithHeaders[pos] is SymbolItem.Header) spanCount else 1
                    }
                }
                holder.main.symbolsRecyclerView.layoutManager = layoutManager
                holder.main.symbolsRecyclerView.adapter = SymbolsWithHeadersAdapter(symbolsWithHeaders)
            } else {
                holder.main.symbolsRecyclerView.layoutManager = layoutManager
                val langSymbols = languages[currentPos - 1].second.map { SymbolItem.Symbol(it) }
                holder.main.symbolsRecyclerView.adapter = SymbolsWithHeadersAdapter(langSymbols)
            }
        }

        override fun getItemCount(): Int = 1 + languages.size
    }

    inner class SymbolsWithHeadersAdapter(private val items: List<SymbolItem>) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
        private val TYPE_HEADER = 0
        private val TYPE_SYMBOL = 1

        override fun getItemViewType(position: Int): Int = when (items[position]) {
            is SymbolItem.Header -> TYPE_HEADER
            is SymbolItem.Symbol -> TYPE_SYMBOL
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
            return if (viewType == TYPE_HEADER) {
                val view = TextView(parent.context).apply {
                    layoutParams = ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
                    setPadding(32, 24, 16, 8)
                    textSize = 14f
                    setTextColor(0xFF757575.toInt()) // Серый цвет
                    typeface = android.graphics.Typeface.DEFAULT_BOLD
                }
                HeaderViewHolder(view)
            } else {
                val binding = SelectKeyLayoutItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
                SymbolViewHolder(binding)
            }
        }

        override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
            val item = items[position]
            if (holder is HeaderViewHolder && item is SymbolItem.Header) {
                holder.textView.text = item.title
            } else if (holder is SymbolViewHolder && item is SymbolItem.Symbol) {
                holder.binding.value.text = item.value
                holder.binding.root.setOnClickListener {
                    selectKeyLayoutBinding.editText.append(item.value)
                }
            }
        }

        override fun getItemCount(): Int = items.size

        inner class HeaderViewHolder(val textView: TextView) : RecyclerView.ViewHolder(textView)
        inner class SymbolViewHolder(val binding: SelectKeyLayoutItemBinding) : RecyclerView.ViewHolder(binding.root)
    }
}
