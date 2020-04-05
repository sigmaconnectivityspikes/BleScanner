package se.sigmaconnectivity.blescanner.data.db

import android.content.Context
import androidx.core.content.edit
import timber.log.Timber

private const val SHARED_PREFS = "shared_prefs"
private const val SHARED_PREFS_HASH = "hash"

class SharedPrefs(private val context: Context) {
    private val prefs by lazy {
        context.getSharedPreferences(SHARED_PREFS, Context.MODE_PRIVATE)
    }

    fun getUserHash(): String {
        val hash = prefs.getString(SHARED_PREFS_HASH, "").toString()
        Timber.d("User HASH = $hash")
        return hash
    }

    fun setUserHash(hash: String) {
        prefs.edit(true) {
            putString(SHARED_PREFS_HASH, hash)
        }
    }
}