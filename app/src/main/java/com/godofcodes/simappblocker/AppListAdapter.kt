package com.godofcodes.simappblocker

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.godofcodes.simappblocker.databinding.ItemAppBinding

class AppListAdapter(
    private val onClick: (AppItem) -> Unit
) : ListAdapter<AppItem, AppListAdapter.ViewHolder>(DIFF_CALLBACK) {

    companion object {
        private val DIFF_CALLBACK = object : DiffUtil.ItemCallback<AppItem>() {
            override fun areItemsTheSame(old: AppItem, new: AppItem) =
                old.packageName == new.packageName

            override fun areContentsTheSame(old: AppItem, new: AppItem) =
                old.packageName == new.packageName && old.isHidden == new.isHidden
        }
    }

    inner class ViewHolder(private val binding: ItemAppBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(item: AppItem) {
            val ctx = binding.root.context
            binding.appIcon.setImageDrawable(item.icon)
            binding.appName.text = item.label
            binding.appPackage.text = item.packageName
            binding.statusText.text = ctx.getString(
                if (item.isHidden) R.string.status_hidden else R.string.status_visible
            )
            binding.statusText.setTextColor(
                ctx.getColor(if (item.isHidden) R.color.hidden_badge_text else R.color.visible_badge_text)
            )
            binding.statusText.setBackgroundColor(
                ctx.getColor(if (item.isHidden) R.color.hidden_badge else R.color.visible_badge)
            )
            binding.root.setOnClickListener { onClick(item) }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemAppBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }
}
