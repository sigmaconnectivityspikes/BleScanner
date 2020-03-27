package se.sigmaconnectivity.blescanner.data.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "contacts_table")
data class Contact(
    @PrimaryKey val hash: String,
    @ColumnInfo(name = "last_timestamp") val lastTimeStamp: Long,
    @ColumnInfo(name = "contact_counter") val contactCounter: Long,
    @ColumnInfo(name = "total_contact_time") val totalContactTime: Long
)