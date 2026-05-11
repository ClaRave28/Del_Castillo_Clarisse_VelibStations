package fr.clarisse.stationsvelib

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class FavoritesAdapter(
    private val items: MutableList<StationsFavorites>,
    private val onSupprimer: (Long) -> Unit
) : RecyclerView.Adapter<FavoritesAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val nom: TextView = view.findViewById(R.id.fav_nom)
        val velos: TextView = view.findViewById(R.id.fav_velos)
        val places: TextView = view.findViewById(R.id.fav_places)
        val capacite: TextView = view.findViewById(R.id.fav_capacite)
        val btnSupprimer: ImageButton = view.findViewById(R.id.fav_btn_supprimer)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.favorites_stations, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = items[position]
        holder.nom.text = item.nom
        holder.velos.text = "🚲 Vélos dispo : ${item.velosDispo ?: "-"}"
        holder.places.text = "🅿️ Places libres : ${item.placesLibres ?: "-"}"
        holder.capacite.text = "📦 Capacité : ${item.capacite}"

        holder.btnSupprimer.setOnClickListener {
            onSupprimer(item.id)
            items.removeAt(holder.adapterPosition)
            notifyItemRemoved(holder.adapterPosition)
        }
    }

    override fun getItemCount() = items.size
}