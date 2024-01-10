package otang.app.network.util

import android.content.Context
import android.content.SharedPreferences

class PrefUtils(context: Context) {
    private val preferences: SharedPreferences =
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
    private val editor: SharedPreferences.Editor = preferences.edit()

    fun saveAs(tag: String?, value: Long) {
        editor.putLong(tag, value).commit()
    }

    fun getLong(tag: String?): Long {
        return preferences.getLong(tag, 0)
    }

    companion object {
        private const val PREFS = "otang.network_preferences"
    }
}