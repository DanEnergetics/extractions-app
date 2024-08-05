package com.example.espress

import androidx.room.ColumnInfo
import androidx.room.Dao
import androidx.room.Database
import androidx.room.Delete
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.RoomDatabase
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Entity
data class Extraction (
    @PrimaryKey(autoGenerate = true) val uid: Int = 0,
    @ColumnInfo(name = "duration") val duration: Long = 0L,
    @ColumnInfo(name = "grind") val grind: Float = 0f,
    @ColumnInfo(name = "grind_time") val grindTime: Long = 0L,
    @ColumnInfo(name = "time") val time: String = "",
    @ColumnInfo(name = "yield") val yield: Float = 0f,
//    @ColumnInfo(name = "beans") val beans: String = "",
//    @ColumnInfo(name = "dose") val dose: Float = 0f,
//    @ColumnInfo(name = "temperature") val temperature: Float = 0f,
) {
    fun snap(
        grindTimeRange: ClosedRange<Long>
    ) = this.copy(
        grindTime = this.grindTime.coerceIn(grindTimeRange)
    )
}

@Dao
interface ExtractionDao {
    @Query("SELECT * FROM Extraction")
    fun getAll(): Flow<List<Extraction>>

    @Insert
    fun insertAll(vararg extractions: Extraction)

    @Update
    fun update(vararg extraction: Extraction)

    @Delete
    fun delete(extraction: Extraction)
}

@Database(
    entities = [Extraction::class],
    version = 4,
//    autoMigrations = [
//        AutoMigration(from = 1, to = 2, spec = AppDatabase.MyAutoMigration::class)
//    ]
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun extractionDao(): ExtractionDao

//    @DeleteTable(tableName="Extraction")
//    class MyAutoMigration : AutoMigrationSpec
}
