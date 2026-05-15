package fr.clarisse.stationsvelib

import android.content.Context
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

object Favorites {

    private const val PREFS_NAME = "favoris"
    private const val KEY_IDS = "station_ids"
    private const val KEY_DONNEES = "station_donnees"

    private val gson = Gson()

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

    fun sauvegarderDonnees(context: Context, station: StationsFavorites) {
        val donnees = getDonnees(context).toMutableList()
        donnees.removeAll { it.id == station.id } // évite les doublons
        donnees.add(station)
        saveDonnees(context, donnees)
    }

    fun getDonnees(context: Context): List<StationsFavorites> {
        val json = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .getString(KEY_DONNEES, null) ?: return emptyList()
        val type = object : TypeToken<List<StationsFavorites>>() {}.type
        return gson.fromJson(json, type)
    }

    private fun saveDonnees(context: Context, donnees: List<StationsFavorites>) {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit().putString(KEY_DONNEES, gson.toJson(donnees)).apply()
    }
}