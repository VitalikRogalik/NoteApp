package com.example.notesapp

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.lifecycle.ViewModelProvider
import com.example.notesapp.databinding.ActivityMainBinding
import com.example.notesapp.db.NoteDatabase
import com.example.notesapp.repository.NoteRepository
import com.example.notesapp.viewmodels.NoteViewModel
import com.example.notesapp.viewmodels.NoteViewModelFactory

class MainActivity : AppCompatActivity() {

    lateinit var noteViewModel: NoteViewModel
    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        //Скрываем action bar
        supportActionBar?.hide()
        binding = ActivityMainBinding.inflate(layoutInflater)

        try{
            setContentView(binding.root)
            val noteRepository = NoteRepository(NoteDatabase(this))
            val viewModelFactory = NoteViewModelFactory(noteRepository)
            noteViewModel = ViewModelProvider(this,
                viewModelFactory).get(NoteViewModel::class.java)
            /*noteViewModel = ViewModelProvider(this,
                viewModelFactory)[NoteViewModel::class.java]*/
        }catch (e: Exception){
            Log.d("Tag", "error")
        }
    }
}