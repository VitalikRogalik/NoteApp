package com.example.notesapp.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.notesapp.models.Note

@Database(
    entities = [Note::class],
    version = 1,
    exportSchema = false
)
abstract class NoteDatabase: RoomDatabase() {

    abstract fun getNoteDao(): DAO

    companion object{
        @Volatile
        private var INSTANCE: NoteDatabase? = null

        // переопределяем логику когда Instance был вызван
        //создает базу только при первом обращении к ней
        operator fun invoke(context: Context) = INSTANCE ?: synchronized(this){
            INSTANCE ?: createDatabase(context).also{
                INSTANCE = it
            }
        }

        private fun createDatabase(context: Context) = Room.databaseBuilder(
            context.applicationContext,
            NoteDatabase::class.java,
            "note_database"
        ).build()

        /*fun getDatabase(context: Context): NoteDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    NoteDatabase::class.java,
                    "note_database"
                ).build()
                INSTANCE = instance
                instance
            }
        }*/
    }

}