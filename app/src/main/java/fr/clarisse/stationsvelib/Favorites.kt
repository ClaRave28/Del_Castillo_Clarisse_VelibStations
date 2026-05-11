package fr.clarisse.stationsvelib

import android.content.Context

object Favorites {

    private const val PREFS_NAME = "favoris"
    private const val KEY_IDS = "station_ids"

    fun ajouter(context: Context, stationId: Long) {
        val set = getIds(context).toMutableSet()
        set.add(stationId.toString())
        save(context, set)
    }

    fun supprimer(context: Context, stationId: Long) {
        val set = getIds(context).toMutableSet()
        set.remove(stationId.toString())
        save(context, set)
    }

    fun estFavori(context: Context, stationId: Long): Boolean {
        return getIds(context).contains(stationId.toString())
    }

    fun getIds(context: Context): Set<String> {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .getStringSet(KEY_IDS, emptySet()) ?: emptySet()
    }

    private fun save(context: Context, set: Set<String>) {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit()
            .putStringSet(KEY_IDS, set)
            .apply()
    }
}