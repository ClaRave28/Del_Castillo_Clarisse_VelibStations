package fr.clarisse.stationsvelib

import java.io.Serializable

data class StationsFavorites(
    val id: Long,
    val nom: String,
    val capacite: Int,
    val velosDispo: Int?,
    val placesLibres: Int?
) : Serializable