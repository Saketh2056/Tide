package com.tide.app.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverter
import androidx.room.TypeConverters

class Converters {
    @TypeConverter
    fun fromPackageList(value: List<String>): String = value.joinToString("\n")

    @TypeConverter
    fun toPackageList(value: String): List<String> =
        if (value.isEmpty()) emptyList() else value.split("\n")
}

@Database(
    entities = [
        ShieldEntity::class,
        ScheduleEntity::class,
        FocusSessionEntity::class,
        DailyUsageEntity::class
    ],
    version = 1,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class TideDatabase : RoomDatabase() {
    abstract fun shieldDao(): ShieldDao
    abstract fun scheduleDao(): ScheduleDao
    abstract fun focusSessionDao(): FocusSessionDao
    abstract fun dailyUsageDao(): DailyUsageDao

    companion object {
        @Volatile
        private var instance: TideDatabase? = null

        fun get(context: Context): TideDatabase =
            instance ?: synchronized(this) {
                instance ?: Room.databaseBuilder(
                    context.applicationContext,
                    TideDatabase::class.java,
                    "tide.db"
                )
                    .fallbackToDestructiveMigration(dropAllTables = true)
                    .build()
                    .also { instance = it }
            }
    }
}
