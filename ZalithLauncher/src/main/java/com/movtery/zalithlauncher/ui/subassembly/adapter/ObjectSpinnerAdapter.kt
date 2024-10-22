package com.movtery.zalithlauncher.ui.subassembly.adapter

import android.annotation.SuppressLint
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.skydoves.powerspinner.OnSpinnerItemSelectedListener
import com.skydoves.powerspinner.PowerSpinnerInterface
import com.skydoves.powerspinner.PowerSpinnerView
import com.skydoves.powerspinner.databinding.PowerspinnerItemDefaultPowerBinding

/**
 * 改自 [com.skydoves.powerspinner.DefaultSpinnerAdapter]
 */
@SuppressLint("NotifyDataSetChanged")
class ObjectSpinnerAdapter<T>(
    powerSpinnerView: PowerSpinnerView,
    private val itemNameProvider: (T) -> String //提供函数来获取字符串
) : RecyclerView.Adapter<ObjectSpinnerAdapter.ViewHolder<T>>(),
    PowerSpinnerInterface<T> {
    companion object {
        private const val NO_INT_VALUE = Int.MIN_VALUE
        private const val NO_SELECTED_INDEX = -1
    }

    override var index: Int = powerSpinnerView.selectedIndex
    override val spinnerView: PowerSpinnerView = powerSpinnerView
    override var onSpinnerItemSelectedListener: OnSpinnerItemSelectedListener<T>? = null

    private val spinnerItems: MutableList<T> = arrayListOf()

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ViewHolder<T> {
        val binding = PowerspinnerItemDefaultPowerBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ViewHolder(binding, itemNameProvider).apply {
            binding.root.setOnClickListener {
                val position = bindingAdapterPosition.takeIf { it != RecyclerView.NO_POSITION }
                    ?: return@setOnClickListener
                notifyItemSelected(position)
            }
        }
    }

    override fun onBindViewHolder(holder: ViewHolder<T>, position: Int) {
        holder.bind(spinnerView, spinnerItems[position], index == position)
    }

    override fun setItems(itemList: List<T>) {
        this.spinnerItems.clear()
        this.spinnerItems.addAll(itemList)
        notifyDataSetChanged()
    }

    override fun notifyItemSelected(index: Int) {
        if (index == NO_SELECTED_INDEX) return
        val oldIndex = this.index
        this.index = index
        this.spinnerView.notifyItemSelected(
            index,
            itemNameProvider(spinnerItems[index])
        )
        notifyDataSetChanged()
        this.onSpinnerItemSelectedListener?.onItemSelected(
            oldIndex = oldIndex,
            oldItem = oldIndex.takeIf { it != NO_SELECTED_INDEX }?.let { spinnerItems[oldIndex] },
            newIndex = index,
            newItem = spinnerItems[index]
        )
    }

    override fun getItemCount(): Int = spinnerItems.size

    class ViewHolder<T>(
        private val binding: PowerspinnerItemDefaultPowerBinding,
        private val itemNameProvider: (T) -> String
    ) : RecyclerView.ViewHolder(binding.root) {

        internal fun bind(spinnerView: PowerSpinnerView, item: T, isSelectedItem: Boolean) {
            binding.itemDefaultText.apply {
                text = itemNameProvider(item)
                typeface = spinnerView.typeface
                gravity = spinnerView.gravity
                setTextSize(TypedValue.COMPLEX_UNIT_PX, spinnerView.textSize)
                setTextColor(spinnerView.currentTextColor)
            }
            binding.root.setPadding(
                spinnerView.paddingLeft,
                spinnerView.paddingTop,
                spinnerView.paddingRight,
                spinnerView.paddingBottom
            )
            if (spinnerView.spinnerItemHeight != NO_INT_VALUE) {
                binding.root.height = spinnerView.spinnerItemHeight
            }
            if (spinnerView.spinnerSelectedItemBackground != null && isSelectedItem) {
                binding.root.background = spinnerView.spinnerSelectedItemBackground
            } else {
                binding.root.background = null
            }
        }
    }
}
