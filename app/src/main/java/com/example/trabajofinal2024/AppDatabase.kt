package com.example.trabajofinal2024

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import kotlinx.coroutines.CoroutineScope

@Database(
    entities = [Alimento::class, Encuesta::class],
    version = 3,
    exportSchema = true
)
abstract class AppDatabase : RoomDatabase() {
    // Esto DEBE devolver EncuestaDAO (la interfaz)
    abstract fun encuestaDAO(): EncuestaDAO

    // Esto DEBE devolver AlimentoDAO (la interfaz)
    abstract fun alimentoDAO(): AlimentoDAO

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("ALTER TABLE encuestas ADD COLUMN user_uid TEXT DEFAULT ''")
                database.execSQL("ALTER TABLE encuestas ADD COLUMN current_index INTEGER DEFAULT 0")
                database.execSQL("ALTER TABLE encuestas ADD COLUMN activa INTEGER DEFAULT 1")
                database.execSQL("ALTER TABLE encuestas ADD COLUMN updated_at INTEGER")
            }
        }

        fun getDatabase(context: Context, scope: CoroutineScope): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "app_database"
                )
                    .fallbackToDestructiveMigration()
                    .addMigrations(MIGRATION_1_2)
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}