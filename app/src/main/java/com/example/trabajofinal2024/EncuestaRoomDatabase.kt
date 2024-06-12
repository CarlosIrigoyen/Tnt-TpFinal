package com.example.trabajofinal2024

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

@Database (
    entities = arrayOf(Encuesta::class),
    version = 1,
    exportSchema = true
)


public abstract class EncuestaRoomDatabase: RoomDatabase() {

    abstract fun encuestaDAO(): EncuestaDAO

    private class encuestaDatabaseCallBack(
        private val scope: CoroutineScope
    ): RoomDatabase.Callback() {

        override fun onCreate(db: SupportSQLiteDatabase) {
            super.onCreate(db)
            INSTANCE?.let {
                database -> scope.launch{
                    var encuestaDAO = database.encuestaDAO()
                    encuestaDAO.borrarTodos()
            }
            }
        }
    }
    companion object {

        @Volatile
        private var INSTANCE: EncuestaRoomDatabase? = null

        fun getDatabase(context: Context, scope: CoroutineScope): EncuestaRoomDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    EncuestaRoomDatabase::class.java,
                    "encuesta_database"
                )
                    .addCallback(encuestaDatabaseCallBack(scope))
                    .build();
                INSTANCE = instance
                instance
            }
        }
    }


}