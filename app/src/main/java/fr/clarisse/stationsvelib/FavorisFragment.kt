package fr.clarisse.stationsvelib

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class FavorisFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.fragment_favoris, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val recycler = view.findViewById<RecyclerView>(R.id.recycler_favoris)
        val empty = view.findViewById<TextView>(R.id.tv_empty)

        val mainActivity = requireActivity() as MainActivity
        val toutesLesStations = mainActivity.toutesLesStations
        val tousLesStatuts = mainActivity.tousLesStatuts

        val ids = Favorites.getIds(requireContext())

        val favoris: MutableList<StationsFavorites> = if (toutesLesStations.isNotEmpty()) {
            // Connexion disponible : données fraîches depuis l'API
            toutesLesStations
                .filter { ids.contains(it.station_id.toString()) }
                .map { station ->
                    val status = tousLesStatuts[station.station_id]
                    StationsFavorites(
                        id = station.station_id,
                        nom = station.name,
                        capacite = station.capacity,
                        velosDispo = status?.num_bikes_available,
                        placesLibres = status?.num_docks_available
                    )
                }.toMutableList()
        } else {
            // Pas de connexion : données locales
            Favorites.getDonnees(requireContext()).toMutableList()
        }

        if (favoris.isEmpty()) {
            empty.visibility = View.VISIBLE
            recycler.visibility = View.GONE
        } else {
            empty.visibility = View.GONE
            recycler.visibility = View.VISIBLE
            recycler.layoutManager = LinearLayoutManager(requireContext())
            recycler.adapter = FavoritesAdapter(favoris) { stationId ->
                Favorites.supprimer(requireContext(), stationId)
            }
        }
    }


}