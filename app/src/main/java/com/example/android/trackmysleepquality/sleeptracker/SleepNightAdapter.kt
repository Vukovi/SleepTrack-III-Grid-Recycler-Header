/*
Ovde ima nekoliko situacija i refaktorisanja
1.  Osnovna pre klase ViewHolder
2.  Prvobitna sa ViewHolderom
3.  A-REFACTOR nekoliko koraka refaktorisanja onBindViewHolder metode
4.  B-REFACTOR nekoliko koraka refaktorisanja onCreateViewHolder metode
5.  DIFF-UTIL REFACTOR
6.  DATA BINDING REFACTOR
7.  BINDING ADAPTER - BINDING UTILS
 */

package com.example.android.trackmysleepquality.sleeptracker

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.android.trackmysleepquality.R
import com.example.android.trackmysleepquality.database.SleepNight
import com.example.android.trackmysleepquality.databinding.ListItemSleepNightBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.lang.ClassCastException

// REFACTOR HEADER LIST
//class SleepNightAdapter(val clickListener: SleepNightListener): ListAdapter<SleepNight, SleepNightAdapter.ViewHolder>(SleepNightDiffCallback()) {
class SleepNightAdapter(val clickListener: SleepNightListener): ListAdapter<DataItem, RecyclerView.ViewHolder>(SleepNightDiffCallback()) {

    private val adapterScope = CoroutineScope(Dispatchers.Default)

    fun addHeaderAndSubmitList(list: List<SleepNight>?){
        adapterScope.launch {
            val items = when (list) {
                null -> listOf(DataItem.Header)
                else -> listOf(DataItem.Header) + list.map { DataItem.SleepNightItem(it) }
            }

            withContext(Dispatchers.Main){
                submitList(items)
            }
        }
    }

    // REFACTOR HEADER LIST - nisam imao pre, zbog header-a dodajem
    override fun getItemViewType(position: Int): Int {
        return when (getItem(position)) {
            is DataItem.Header -> ITEM_VIEW_TYPE_HEADER
            is DataItem.SleepNightItem -> ITEM_VIEW_TYPE_ITEM
        }
    }

//    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
//        val item = getItem(position)
//        holder.bind(item, clickListener)
//    }
    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder) {
             is ViewHolder -> {
                val item = getItem(position) as DataItem.SleepNightItem
                holder.bind(item.sleepNight, clickListener)
             }
         }

    }

    // REFACTOR HEADER LIST
//    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
//        return ViewHolder.from(parent)
//    }
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            ITEM_VIEW_TYPE_HEADER -> TextViewHolder.from(parent)
            ITEM_VIEW_TYPE_ITEM -> ViewHolder.from(parent)
            else -> throw ClassCastException("Nepoznat view type: ${viewType}")
        }
    }

    class ViewHolder private constructor (val binding: ListItemSleepNightBinding) : RecyclerView.ViewHolder(binding.root) {

        fun bind(item: SleepNight, clickListener: SleepNightListener) {
            binding.sleep = item
            binding.clickListener = clickListener!!
            binding.executePendingBindings()

        }

        companion object {
             fun from(parent: ViewGroup): ViewHolder {
                 val layoutInflater = LayoutInflater.from(parent.context)
                 val binding = ListItemSleepNightBinding.inflate(layoutInflater, parent, false)
                return ViewHolder(binding)
            }
        }
    }


    // Za header klasa
    class TextViewHolder(view:View): RecyclerView.ViewHolder(view){
        companion object {
            fun from(parent: ViewGroup): TextViewHolder {
                val layoutInflater = LayoutInflater.from(parent.context)
                val view = layoutInflater.inflate(R.layout.header, parent, false)
                return TextViewHolder(view)
            }
        }
    }

}

// azurirnaje tabele ili collectiona, tj recyclera
//class SleepNightDiffCallback: DiffUtil.ItemCallback<SleepNight>() {
//    override fun areItemsTheSame(oldItem: SleepNight, newItem: SleepNight): Boolean {
//        return oldItem.nightId == newItem.nightId
//    }
//
//    override fun areContentsTheSame(oldItem: SleepNight, newItem: SleepNight): Boolean {
//        return oldItem == newItem
//    }
//
//}
// REFACTOR HEADER LIST
class SleepNightDiffCallback: DiffUtil.ItemCallback<DataItem>() {
    override fun areItemsTheSame(oldItem: DataItem, newItem: DataItem): Boolean {
        return oldItem.id == newItem.id
    }

    override fun areContentsTheSame(oldItem: DataItem, newItem: DataItem): Boolean {
        return oldItem == newItem
    }

}

// Did select cell is Swifta, ovde u vidu paterna
class SleepNightListener(val clickListener: (sleepId: Long) -> Unit) {
    fun onCLick(night: SleepNight) = clickListener(night.nightId)
}

//Dodaavanje header-a listi
sealed class DataItem {
    abstract val id: Long
    data class SleepNightItem(val sleepNight: SleepNight): DataItem(){
        override val id: Long = sleepNight.nightId
    }
    object Header: DataItem(){
        override val id: Long = Long.MIN_VALUE
    }
}

private const val ITEM_VIEW_TYPE_HEADER = 0
private const val ITEM_VIEW_TYPE_ITEM = 1