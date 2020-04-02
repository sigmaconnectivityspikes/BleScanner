package se.sigmaconnectivity.blescanner.data.db

import android.content.Context
import androidx.core.content.edit
import timber.log.Timber
import java.util.*

private const val SHARED_PREFS = "shared_prefs"
private const val SHARED_PREFS_UUID = "uuid"
class SharedPrefs(private val context: Context) {
    private val prefs by lazy {
        context.getSharedPreferences(SHARED_PREFS, Context.MODE_PRIVATE)
    }

    fun getUserUuid(): String {
        val uuid = prefs.getString(SHARED_PREFS_UUID, "").toString()
        Timber.d("Current UUID = $uuid")
        return if(uuid.isBlank()) {
            generateUserUUIDAndSave()
        } else {
            uuid
        }
    }

    private fun generateUserUUIDAndSave(): String {
        val uuid = UUID.randomUUID().toString()
        prefs.edit(true) {
            putString(SHARED_PREFS_UUID, UUID.randomUUID().toString())
        }
        Timber.d("Generated UUID = $uuid")
        return uuid
    }
}