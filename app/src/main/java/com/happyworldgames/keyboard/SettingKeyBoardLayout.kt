package com.happyworldgames.keyboard

import android.content.Context
import android.content.Intent
import android.content.res.Resources
import android.os.Bundle
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
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.tabs.TabLayoutMediator
import com.happyworldgames.keyboard.databinding.SettingKeyboardBinding
import com.happyworldgames.keyboard.databinding.SettingKeyboardViewPagerBinding


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

        // Check if tutorial should be shown
        val prefs = getSharedPreferences("keyboard_prefs", Context.MODE_PRIVATE)
        if (!prefs.getBoolean("layout_tutorial_shown", false)) {
            settingKeyBoardViewPagerBinding.tutorialOverlay.visibility = View.VISIBLE
        }

        settingKeyBoardViewPagerBinding.btnGotIt.setOnClickListener {
            settingKeyBoardViewPagerBinding.tutorialOverlay.visibility = View.GONE
            prefs.edit().putBoolean("layout_tutorial_shown", true).apply()
        }

        settingKeyBoardViewPagerBinding.btnSkipTutorial.setOnClickListener {
            settingKeyBoardViewPagerBinding.tutorialOverlay.visibility = View.GONE
            prefs.edit().putBoolean("layout_tutorial_shown", true).apply()
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
            showAddLayoutDialog()
        }

        settingKeyBoardViewPagerBinding.manageLayouts.setOnClickListener {
            showManageLayoutsDialog()
        }

        SimpleIME.loadHintArrayListAsync(this@SettingKeyBoardLayout) {
            settingKeyBoardViewPagerBinding.keyLayoutView.adapter?.notifyDataSetChanged()
            mediator.detach()
            mediator.attach()
        }
    }

    private fun showAddLayoutDialog() {
        val options = arrayOf(
            getString(R.string.add_layout_copy),
            getString(R.string.add_layout_predefined)
        )

        androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle(R.string.add_layout_title)
            .setItems(options) { _, which ->
                when (which) {
                    0 -> addCopyLayout()
                    1 -> showPredefinedLanguagesDialog()
                }
            }
            .show()
    }

    private fun addCopyLayout() {
        val currentLayout = SimpleIME.hintArrayList.getOrNull(settingKeyBoardViewPagerBinding.keyLayoutView.currentItem)
            ?: SimpleIME.hintArrayList[0]
        val newLayout = ArrayList(currentLayout)
        addNewLayoutToArrayList(newLayout)
    }

    private fun showPredefinedLanguagesDialog() {
        val languages = SimpleIME.predefinedLayouts.keys.toTypedArray()

        androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle(R.string.select_language)
            .setItems(languages) { _, which ->
                val selectedLang = languages[which]
                val predefined = SimpleIME.predefinedLayouts[selectedLang]
                if (predefined != null) {
                    addNewLayoutToArrayList(ArrayList(predefined))
                }
            }
            .show()
    }

    private fun addNewLayoutToArrayList(newLayout: ArrayList<String>) {
        SimpleIME.hintArrayList.add(newLayout)
        settingKeyBoardViewPagerBinding.keyLayoutView.adapter?.notifyItemInserted(SimpleIME.hintArrayList.size - 1)
        settingKeyBoardViewPagerBinding.keyLayoutView.currentItem = SimpleIME.hintArrayList.size - 1
        SimpleIME.saveHintArrayListAsync(this)
        refreshTabs()
    }

    private fun refreshTabs() {
        // Re-attaching mediator to refresh tab names.
        val mediator = TabLayoutMediator(settingKeyBoardViewPagerBinding.keyLayouts, settingKeyBoardViewPagerBinding.keyLayoutView) { tab, position ->
            tab.text = "№$position"
        }
        mediator.attach()
    }

    private fun showManageLayoutsDialog() {
        val container = android.widget.LinearLayout(this)
        container.orientation = android.widget.LinearLayout.VERTICAL
        container.setPadding(32, 32, 32, 0)

        val hint = TextView(this)
        hint.text = getString(R.string.manage_layouts_hint)
        hint.textSize = 14f
        hint.alpha = 0.7f
        hint.setPadding(16, 0, 16, 16)
        container.addView(hint)

        val recyclerView = RecyclerView(this)
        recyclerView.layoutManager = LinearLayoutManager(this)
        val adapter = ManageLayoutsAdapter(SimpleIME.hintArrayList) {
            refreshTabs()
            settingKeyBoardViewPagerBinding.keyLayoutView.adapter?.notifyDataSetChanged()
            SimpleIME.saveHintArrayListAsync(this)
        }
        recyclerView.adapter = adapter
        container.addView(recyclerView)

        val touchHelper = ItemTouchHelper(object : ItemTouchHelper.SimpleCallback(ItemTouchHelper.UP or ItemTouchHelper.DOWN, ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT) {
            override fun onMove(rv: RecyclerView, vh: RecyclerView.ViewHolder, target: RecyclerView.ViewHolder): Boolean {
                val fromPos = vh.adapterPosition
                val toPos = target.adapterPosition
                val item = SimpleIME.hintArrayList.removeAt(fromPos)
                SimpleIME.hintArrayList.add(toPos, item)
                adapter.notifyItemMoved(fromPos, toPos)
                return true
            }

            override fun onSwiped(vh: RecyclerView.ViewHolder, direction: Int) {
                val pos = vh.adapterPosition
                confirmAndDelete(pos, adapter)
            }
        })
        touchHelper.attachToRecyclerView(recyclerView)

        androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle(R.string.manage_layouts)
            .setView(container)
            .setPositiveButton(android.R.string.ok) { _, _ ->
                refreshTabs()
                settingKeyBoardViewPagerBinding.keyLayoutView.adapter?.notifyDataSetChanged()
                SimpleIME.saveHintArrayListAsync(this)
            }
            .show()
    }

    private fun confirmAndDelete(position: Int, adapter: ManageLayoutsAdapter) {
        if (SimpleIME.hintArrayList.size <= 1) {
            adapter.notifyItemChanged(position)
            return
        }

        androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle(R.string.delete_confirm_title)
            .setMessage(R.string.delete_confirm_msg)
            .setPositiveButton(R.string.delete) { _, _ ->
                SimpleIME.hintArrayList.removeAt(position)
                adapter.notifyItemRemoved(position)
                adapter.onLayoutsChanged()
            }
            .setNegativeButton(R.string.cancel) { _, _ ->
                adapter.notifyItemChanged(position)
            }
            .setOnCancelListener {
                adapter.notifyItemChanged(position)
            }
            .show()
    }

    inner class ManageLayoutsAdapter(
        private val layouts: ArrayList<ArrayList<String>>,
        val onLayoutsChanged: () -> Unit
    ) : RecyclerView.Adapter<ManageLayoutsAdapter.ViewHolder>() {

        inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
            val title: TextView = view.findViewById(R.id.layout_title)
            val delete: View = view.findViewById(R.id.delete_button)
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.item_manage_layout, parent, false)
            return ViewHolder(view)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            holder.title.text = getString(R.string.layout_n, position)
            holder.delete.setOnClickListener {
                confirmAndDelete(holder.adapterPosition, this)
            }
        }

        override fun getItemCount() = layouts.size
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