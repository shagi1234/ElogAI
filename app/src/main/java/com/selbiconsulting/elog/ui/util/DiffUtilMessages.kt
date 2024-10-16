package com.selbiconsulting.elog.ui.util

import androidx.recyclerview.widget.DiffUtil
import com.selbiconsulting.elog.data.model.entity.ChatItem
import com.selbiconsulting.elog.data.model.entity.EntityMessage

class DiffUtilMessages(
    private val oldList: List<EntityMessage>,
    private val newList: List<EntityMessage>,
) : DiffUtil.Callback() {

    override fun getOldListSize(): Int {
        return oldList.size
    }

    override fun getNewListSize(): Int {
        return newList.size
    }

    override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        val oldItem = oldList[oldItemPosition]
        val newItem = newList[newItemPosition]
        return (oldItem.localId == newItem.localId || oldItem.id == newItem.id)
    }

    override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        val oldItem = oldList[oldItemPosition]
        val newItem = newList[newItemPosition]
        return oldItem == newItem
    }
}