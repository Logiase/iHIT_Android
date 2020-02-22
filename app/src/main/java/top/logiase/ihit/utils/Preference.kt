@file:Suppress("UNCHECKED_CAST")

package top.logiase.ihit.utils

import android.content.Context
import android.content.SharedPreferences
import top.logiase.ihit.App
import kotlin.reflect.KProperty

class Preference<T>(
    val name: String,
    private val default: T
) {

    private val prefs: SharedPreferences by lazy {
        App.instance.applicationContext.getSharedPreferences(name, Context.MODE_PRIVATE)
    }

    operator fun getValue(thisRef: Any?, property: KProperty<*>): T = getSharedPreferences(name, default)

    operator fun setValue(thisRef: Any?, property: KProperty<*>, value: T) = putSharedPreferences(name, value)

    private fun putSharedPreferences(name: String, value: T) = with(prefs.edit()) {
        when (value) {
            is Long -> putLong(name, value)
            is String -> putString(name, value)
            is Int -> putInt(name, value)
            is Boolean -> putBoolean(name, value)
            is Float -> putFloat(name, value)
            else -> throw IllegalArgumentException("SharedPreferences type Error")
        }.apply()
    }

    private fun getSharedPreferences(name: String, default: T): T = with(prefs) {
        val res: Any = when (default) {
            is Long -> getLong(name, default)
            is String -> getString(name, default) ?: ""
            is Int -> getInt(name, default)
            is Boolean -> getBoolean(name, default)
            is Float -> getFloat(name, default)
            else -> throw IllegalArgumentException("SharedPreferences type Error")
        }
        return res as T
    }

}