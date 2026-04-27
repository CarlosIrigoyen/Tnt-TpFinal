package com.example.trabajofinal2024

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import kotlinx.coroutines.CoroutineScope

@Database(
    entities = [Alimento::class, Encuesta::class, TurnoEntity::class],
    version = 5,
    exportSchema = true
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun encuestaDAO(): EncuestaDAO
    abstract fun alimentoDAO(): AlimentoDAO
    abstract fun turnoDao(): TurnoDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        val MIGRATION_4_5 = object : Migration(4, 5) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("""
                    CREATE TABLE IF NOT EXISTS `turnos` (
                        `firestoreId` TEXT NOT NULL,
                        `uidVoluntario` TEXT NOT NULL,
                        `estado` TEXT NOT NULL,
                        `dia` TEXT NOT NULL,
                        `horario` TEXT NOT NULL,
                        `direccion` TEXT NOT NULL,
                        `descripcion` TEXT NOT NULL,
                        `createdAt` INTEGER NOT NULL,
                        `updatedAt` INTEGER NOT NULL,
                        PRIMARY KEY(`firestoreId`)
                    )
                """.trimIndent())
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
                    .addMigrations(MIGRATION_4_5)
                    .build()

                INSTANCE = instance
                instance
            }
        }
    }
}