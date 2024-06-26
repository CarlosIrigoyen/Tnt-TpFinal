package com.example.trabajofinal2024

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

@Database(
    entities = [Alimento::class, Encuesta::class],
    version = 1,
    exportSchema = true
)


abstract class AppDatabase: RoomDatabase() {
    abstract fun encuestaDAO(): EncuestaDAO
    abstract fun alimentoDAO(): AlimentoDAO

    private class DatabaseCallback(
        private val scope: CoroutineScope
    ) : RoomDatabase.Callback() {

        override fun onCreate(db: SupportSQLiteDatabase) {
            super.onCreate(db)
            INSTANCE?.let { database ->
                scope.launch {
                    val alimentoDAO = database.alimentoDAO()
                    alimentoDAO.borrarTodos()
                    val encuestaDAO = database.encuestaDAO()
                    encuestaDAO.borrarTodos()
                }
            }
        }
    }

    companion object {

        @Volatile
        private var INSTANCE: AppDatabase? = null




        fun getDatabase(context: Context, scope: CoroutineScope): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "app_database"
                )
                    .addCallback(AppDatabase.DatabaseCallback(scope))
                    .build();
                INSTANCE = instance
                instance
            }
        }





    }



}