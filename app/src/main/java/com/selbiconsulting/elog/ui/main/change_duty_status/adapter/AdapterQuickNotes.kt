package com.selbiconsulting.elog.ui.main.change_duty_status.adapter

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.selbiconsulting.elog.data.model.dto.DtoNote
import com.selbiconsulting.elog.databinding.ItemSelectableDateBinding


class AdapterQuickNotes(
    private val context: Context,
    private val listener: NotesItemListener,
    private val checkedNotes: List<DtoNote>? = emptyList()
) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    var notes: List<DtoNote> = listOf()
        set(value) {
            field = value
            notifyDataSetChanged()
        }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(context)
        val item = ItemSelectableDateBinding.inflate(inflater, parent, false)
        return HolderNotes(item)
    }

    override fun getItemCount() = notes.size

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        (holder as HolderNotes).bind(notes[position])
    }

    inner class HolderNotes(val b: ItemSelectableDateBinding) : RecyclerView.ViewHolder(b.root) {
        fun bind(note: DtoNote) {
            b.checkboxDate.isChecked = (checkedNotes?.contains(note) == true)
            b.checkboxDate.text = note.name

            if (b.checkboxDate.isChecked) listener.onNotesItemCheckedChanged(true, note)

            b.checkboxDate.setOnCheckedChangeListener { _, isChecked ->
                listener.onNotesItemCheckedChanged(isChecked, note)
            }
        }
    }
}

interface NotesItemListener {
    fun onNotesItemCheckedChanged(isChecked: Boolean, note: DtoNote)
}

