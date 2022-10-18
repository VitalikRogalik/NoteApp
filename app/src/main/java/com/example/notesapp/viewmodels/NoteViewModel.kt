package com.example.notesapp.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.notesapp.models.ImageModel
import com.example.notesapp.models.Note
import com.example.notesapp.repository.NoteRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class NoteViewModel(private val repository: NoteRepository): ViewModel() {

    private var _noteListLiveData = MutableLiveData<List<Note>>()
    val noteListLiveData: LiveData<List<Note>> = _noteListLiveData

    var dataList = mutableListOf<ImageModel>()

    var editTextPosition: Int = 0

    fun saveNote(newNote: Note){
        viewModelScope.launch(Dispatchers.IO) {
            repository.createNote(note = newNote)
        }
    }

    fun updateNote(existingNote: Note){
        viewModelScope.launch(Dispatchers.IO) {
            repository.updateNote(note = existingNote)
        }
    }

    fun deleteNote(existingNote: Note){
        viewModelScope.launch(Dispatchers.IO) {
            repository.deleteNote(note = existingNote)
        }
    }

    fun searchNote(query: String): LiveData<List<Note>>{
        return repository.searchNote(query = query)
    }

    fun getAllNote(){
        viewModelScope.launch(Dispatchers.IO) {
            val noteList = repository.getAllNote()
            _noteListLiveData.postValue(noteList)
        }
    }
}