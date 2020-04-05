package se.sigmaconnectivity.blescanner.data.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "contacts_table")
data class Contact(
    val hash: String,
    @ColumnInfo(name = "last_timestamp") val lastTimeStamp: Long,
    @ColumnInfo(name = "duration") val duration: Long,
    @PrimaryKey(autoGenerate = true) val id: Long = 0
)