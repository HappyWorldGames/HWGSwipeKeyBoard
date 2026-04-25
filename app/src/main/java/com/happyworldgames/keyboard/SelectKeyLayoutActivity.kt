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
import androidx.core.content.edit
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
        private const val TYPE_HEADER = 0
        private const val TYPE_SYMBOL = 1

        // Базовый набор символов с категориями
        private fun getSymbolsWithHeaders(resources: Resources) = arrayListOf(
            SymbolItem.Header(resources.getString(R.string.category_management)),
            SymbolItem.Symbol("⚙️"), SymbolItem.Symbol("❌"), SymbolItem.Symbol("⌫"), 
            SymbolItem.Symbol("⏎"), SymbolItem.Symbol("˽"), SymbolItem.Symbol("⤆"), 
            SymbolItem.Symbol("⤇"), SymbolItem.Symbol("⇧"),
            
            SymbolItem.Header(resources.getString(R.string.category_digits)),
            SymbolItem.Symbol("1"), SymbolItem.Symbol("2"), SymbolItem.Symbol("3"), 
            SymbolItem.Symbol("4"), SymbolItem.Symbol("5"), SymbolItem.Symbol("6"), 
            SymbolItem.Symbol("7"), SymbolItem.Symbol("8"), SymbolItem.Symbol("9"), 
            SymbolItem.Symbol("0"),
            
            SymbolItem.Header(resources.getString(R.string.category_punctuation)),
            SymbolItem.Symbol("."), SymbolItem.Symbol(","), SymbolItem.Symbol("!"), 
            SymbolItem.Symbol("?"), SymbolItem.Symbol(";"), SymbolItem.Symbol(":"), 
            SymbolItem.Symbol("\""), SymbolItem.Symbol("'"), SymbolItem.Symbol("("), 
            SymbolItem.Symbol(")"), SymbolItem.Symbol("["), SymbolItem.Symbol("]"), 
            SymbolItem.Symbol("{"), SymbolItem.Symbol("}"), SymbolItem.Symbol("<"), 
            SymbolItem.Symbol(">"), SymbolItem.Symbol("«"), SymbolItem.Symbol("»"),
            
            SymbolItem.Header(resources.getString(R.string.category_symbols_math)),
            SymbolItem.Symbol("@"), SymbolItem.Symbol("#"), SymbolItem.Symbol("$"), 
            SymbolItem.Symbol("€"), SymbolItem.Symbol("%"), SymbolItem.Symbol("&"), 
            SymbolItem.Symbol("*"), SymbolItem.Symbol("-"), SymbolItem.Symbol("+"), 
            SymbolItem.Symbol("="), SymbolItem.Symbol("/"), SymbolItem.Symbol("\\"), 
            SymbolItem.Symbol("|"), SymbolItem.Symbol("~"), SymbolItem.Symbol("±"), 
            SymbolItem.Symbol("×"), SymbolItem.Symbol("÷"), SymbolItem.Symbol("°")
        )

        // Все доступные языки
        private val allLanguages = arrayListOf(
            "English" to arrayListOf("a", "b", "c", "d", "e", "f", "g", "h", "i", "j", "k", "l", "m", "n", "o", "p", "q", "r", "s", "t", "u", "v", "w", "x", "y", "z"),
            "Русский" to arrayListOf("а", "б", "в", "г", "д", "е", "ё", "ж", "з", "и", "й", "к", "л", "м", "н", "о", "п", "р", "с", "т", "у", "ф", "х", "ц", "ч", "ш", "щ", "ъ", "ы", "ь", "э", "ю", "я"),
            "Українська" to arrayListOf("а", "б", "в", "г", "ґ", "д", "е", "є", "ж", "з", "и", "і", "ї", "й", "к", "л", "м", "н", "о", "п", "р", "с", "т", "у", "ф", "х", "ц", "ч", "ш", "щ", "ь", "ю", "я"),
            "Deutsch" to arrayListOf("a", "b", "c", "d", "e", "f", "g", "h", "i", "j", "k", "l", "m", "n", "o", "p", "q", "r", "s", "t", "u", "v", "w", "x", "y", "z", "ä", "ö", "ü", "ß"),
            "Español" to arrayListOf("a", "b", "c", "d", "e", "f", "g", "h", "i", "j", "k", "l", "m", "n", "ñ", "o", "p", "q", "r", "s", "t", "u", "v", "w", "x", "y", "z"),
            "Français" to arrayListOf("a", "b", "c", "d", "e", "f", "g", "h", "i", "j", "k", "l", "m", "n", "o", "p", "q", "r", "s", "t", "u", "v", "w", "x", "y", "z", "à", "â", "æ", "ç", "é", "è", "ê", "ë", "î", "ï", "ô", "œ", "ù", "û", "ü", "ÿ"),
            "Italiano" to arrayListOf("a", "b", "c", "d", "e", "f", "g", "h", "i", "j", "k", "l", "m", "n", "o", "p", "q", "r", "s", "t", "u", "v", "w", "x", "y", "z", "à", "è", "é", "ì", "í", "î", "ò", "ó", "ù", "ú"),
            "Português" to arrayListOf("a", "b", "c", "d", "e", "f", "g", "h", "i", "j", "k", "l", "m", "n", "o", "p", "q", "r", "s", "t", "u", "v", "w", "x", "y", "z", "á", "â", "ã", "à", "ç", "é", "ê", "í", "ó", "ô", "õ", "ú"),
            "Polski" to arrayListOf("a", "ą", "b", "c", "ć", "d", "e", "ę", "f", "g", "h", "i", "j", "k", "l", "ł", "m", "n", "ń", "o", "ó", "p", "q", "r", "s", "ś", "t", "u", "v", "w", "x", "y", "z", "ź", "ż"),
            "Қазақша" to arrayListOf("а", "ә", "б", "в", "г", "ғ", "д", "е", "ё", "ж", "з", "и", "й", "к", "қ", "л", "м", "н", "ң", "о", "ө", "п", "р", "с", "т", "у", "ұ", "ү", "ф", "х", "һ", "ц", "ч", "ш", "щ", "ъ", "ы", "і", "ь", "э", "ю", "я"),
            "Беларуская" to arrayListOf("а", "б", "в", "г", "д", "е", "ё", "ж", "з", "і", "й", "к", "л", "м", "н", "о", "п", "р", "с", "т", "у", "ў", "ф", "х", "ц", "ч", "ш", "ы", "ь", "э", "ю", "я"),
            "Türkçe" to arrayListOf("a", "b", "c", "ç", "d", "e", "f", "g", "ğ", "h", "ı", "i", "j", "k", "l", "m", "n", "o", "ö", "p", "r", "s", "ş", "t", "u", "ü", "v", "y", "z")
        )
    }

    private val activeLanguages = arrayListOf<Pair<String, ArrayList<String>>>()
    private val symbolsWithHeaders by lazy { getSymbolsWithHeaders(resources) }

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

        loadActiveLanguages()

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
            tab.text = if (position == 0) getString(R.string.symbols) else activeLanguages[position - 1].first
        }.attach()

        selectKeyLayoutBinding.manageLanguages.setOnClickListener {
            showLanguageSelectionDialog()
        }

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

    private fun loadActiveLanguages() {
        val prefs = getSharedPreferences("keyboard_prefs", MODE_PRIVATE)
        val saved = prefs.getStringSet("active_languages", setOf("English", "Русский")) ?: setOf("English", "Русский")
        activeLanguages.clear()
        activeLanguages.addAll(allLanguages.filter { it.first in saved })
    }

    private fun saveActiveLanguages() {
        val prefs = getSharedPreferences("keyboard_prefs", MODE_PRIVATE)
        prefs.edit {
            putStringSet("active_languages", activeLanguages.map { it.first }.toSet())
        }
    }

    private fun showLanguageSelectionDialog() {
        val languageNames = allLanguages.map { it.first }.toTypedArray()
        val checkedItems = languageNames.map { name -> activeLanguages.any { it.first == name } }.toBooleanArray()

        androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle(R.string.manage_languages)
            .setMultiChoiceItems(languageNames, checkedItems) { _, which, isChecked ->
                checkedItems[which] = isChecked
            }
            .setPositiveButton(android.R.string.ok) { _, _ ->
                activeLanguages.clear()
                for (i in languageNames.indices) {
                    if (checkedItems[i]) {
                        allLanguages.find { it.first == languageNames[i] }?.let { activeLanguages.add(it) }
                    }
                }
                saveActiveLanguages()
                selectKeyLayoutBinding.viewpager.adapter?.notifyDataSetChanged()
                // Перепривязываем TabLayoutMediator
                TabLayoutMediator(selectKeyLayoutBinding.tabLayout, selectKeyLayoutBinding.viewpager) { tab, position ->
                    tab.text = if (position == 0) getString(R.string.symbols) else activeLanguages[position - 1].first
                }.attach()
            }
            .setNegativeButton(R.string.cancel, null)
            .show()
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
                val langSymbols = activeLanguages[currentPos - 1].second.map { SymbolItem.Symbol(it) }
                holder.main.symbolsRecyclerView.adapter = SymbolsWithHeadersAdapter(langSymbols)
            }
        }

        override fun getItemCount(): Int = 1 + activeLanguages.size
    }

    inner class SymbolsWithHeadersAdapter(private val items: List<SymbolItem>) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

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
