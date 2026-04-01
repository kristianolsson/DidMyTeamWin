package com.kristianolsson.didmyteamwin.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [TrackedTeam::class], version = 1, exportSchema = true)
abstract class AppDatabase : RoomDatabase() {

    abstract fun trackedTeamDao(): TrackedTeamDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "did_my_team_win.db"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}
