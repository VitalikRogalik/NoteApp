package com.example.notesapp.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.navigation.Navigation
import androidx.navigation.fragment.FragmentNavigatorExtras
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.notesapp.R
import com.example.notesapp.databinding.NoteItemLayoutBinding
import com.example.notesapp.fragments.NoteFragmentDirections
import com.example.notesapp.models.Note
import com.example.notesapp.utils.hideKeyboard

class RvNotesAdapter: ListAdapter<Note, RvNotesAdapter.NotesViewHolder>(DiffUtilCallback()) {

    class NotesViewHolder(private val binding: NoteItemLayoutBinding): RecyclerView.ViewHolder(binding.root) {

        fun bind(item: Note){
            binding.note = item
            itemView.setOnClickListener{
                //создаем имя перехода, такое же имя должно быть в разметке конечного отпрпавления
                val noteDetailTransitionName = itemView.context.getString(R.string.note_detail_transition_name)
                //создаем extras clickedView-transitionName, Необходимо передать view чтобы знало откуда начать анимацию
                val extras = FragmentNavigatorExtras(itemView to noteDetailTransitionName)

                val direction = NoteFragmentDirections.actionNoteFragmentToSaveOrUpdateFragment(item)
                Navigation.findNavController(itemView).navigate(direction, extras)
            }
            itemView.hideKeyboard()
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NotesViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        val binding = NoteItemLayoutBinding.inflate(layoutInflater, parent, false)
        return NotesViewHolder(binding)
    }

    override fun onBindViewHolder(holder: NotesViewHolder, position: Int) {
        val item = getItem(position)
        holder.bind(item)
    }
}