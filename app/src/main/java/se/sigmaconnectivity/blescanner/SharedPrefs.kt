package se.sigmaconnectivity.blescanner

import android.content.Context
import androidx.core.content.edit
import timber.log.Timber
import java.util.*

class SharedPrefs(private val context: Context) {
    private val prefs by lazy {
        context.getSharedPreferences(Consts.SHARED_PREFS, Context.MODE_PRIVATE)
    }

    fun generateUserUUIDAndSave() {
        val uuid = UUID.randomUUID().toString()
        prefs.edit(true) {
            putString(Consts.SHARED_PREFS_UUID, UUID.randomUUID().toString())
        }
        Timber.d("Generated UUID = $uuid")
    }

    fun getUserUUID(): String {
        val uuid = prefs.getString(Consts.SHARED_PREFS_UUID, "").toString()
        Timber.d("Current UUID = $uuid")
        return uuid
    }
}