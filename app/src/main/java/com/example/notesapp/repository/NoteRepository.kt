package com.example.notesapp.repository

import com.example.notesapp.db.NoteDatabase
import com.example.notesapp.models.Note

class NoteRepository(private val db: NoteDatabase) {

    suspend fun getAllNote() = db.getNoteDao().getAllNote()

    fun searchNote(query: String) = db.getNoteDao().searchNote(query)

    suspend fun createNote(note: Note) = db.getNoteDao().createNote(note)

    suspend fun updateNote(note: Note) = db.getNoteDao().updateNote(note)

    suspend fun deleteNote(note: Note) = db.getNoteDao().deleteNote(note)
}