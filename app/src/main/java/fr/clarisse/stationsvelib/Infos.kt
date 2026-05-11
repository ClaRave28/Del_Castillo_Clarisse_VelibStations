package fr.clarisse.stationsvelib

import android.widget.ImageButton
import android.widget.TextView
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.infowindow.InfoWindow

class Infos(
    mapView: MapView,
    val nom: String,
    val capacite: Int,
    val velosDispo: Int?,
    val placesLibres: Int?,
    val stationId: Long
) : InfoWindow(R.layout.infos, mapView) {

    override fun onOpen(item: Any?) {
        mView.findViewById<TextView>(R.id.title).text = nom
        mView.findViewById<TextView>(R.id.capacite).text = "📦 Capacité : $capacite vélos"
        mView.findViewById<TextView>(R.id.velos).text = "🚲 Vélos dispo : ${velosDispo ?: "-"}"
        mView.findViewById<TextView>(R.id.places).text = "🅿️ Places libres : ${placesLibres ?: "-"}"
        mView.findViewById<TextView>(R.id.close_btn).setOnClickListener { close() }
        mView.setOnClickListener { close() }

        val btnFavori = mView.findViewById<ImageButton>(R.id.btn_favori)
        val context = mView.context

        // Affiche l'état actuel
        majIconeFavori(btnFavori, Favorites.estFavori(context, stationId))

        btnFavori.setOnClickListener {
            if (Favorites.estFavori(context, stationId)) {
                Favorites.supprimer(context, stationId)
            } else {
                Favorites.ajouter(context, stationId)
            }
            majIconeFavori(btnFavori, Favorites.estFavori(context, stationId))
        }
    }
    private fun majIconeFavori(btn: ImageButton, estFavori: Boolean) {
        btn.setImageResource(
            if (estFavori) android.R.drawable.btn_star_big_on
            else android.R.drawable.btn_star_big_off
        )
    }

    override fun onClose() {}
}