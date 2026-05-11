package fr.clarisse.stationsvelib

import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class FavoritesActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_favorites)

        supportActionBar?.title = "Mes favoris"
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        val stations = intent.getSerializableExtra("stations") as? List<StationsFavorites> ?: emptyList()

        val recycler = findViewById<RecyclerView>(R.id.recycler_favoris)
        val empty = findViewById<TextView>(R.id.tv_empty)

        if (stations.isEmpty()) {
            empty.visibility = android.view.View.VISIBLE
            recycler.visibility = android.view.View.GONE
        } else {
            empty.visibility = android.view.View.GONE
            recycler.visibility = android.view.View.VISIBLE
            recycler.layoutManager = LinearLayoutManager(this)
            recycler.adapter = FavoritesAdapter(stations.toMutableList()) { stationId ->
                Favorites.supprimer(this, stationId)
            }
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }
}