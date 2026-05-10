package fr.clarisse.stationsvelib

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.FolderOverlay
import org.osmdroid.views.overlay.Marker
import fr.clarisse.stationsvelib.model.Stations
import fr.clarisse.stationsvelib.service.Client

class MainActivity : AppCompatActivity() {

    private lateinit var map: MapView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        Configuration.getInstance().apply {
            load(applicationContext, getSharedPreferences("osmdroid", MODE_PRIVATE))
            userAgentValue = packageName
        }

        setContentView(R.layout.activity_main)

        map = findViewById(R.id.map)
        map.setTileSource(TileSourceFactory.MAPNIK)
        map.setMultiTouchControls(true)

        map.controller.apply {
            setZoom(12.0)
            setCenter(GeoPoint(48.8566, 2.3522))
        }

        chargerStations()
    }

    private fun chargerStations() {
        lifecycleScope.launch {
            try {
                val stations = Client.api.getStations().data.stations
                val overlay = FolderOverlay()

                stations.forEach { station ->
                    val marker = Marker(map).apply {
                        position = GeoPoint(station.lat, station.lon)
                        title = station.name
                        snippet = "Capacité : ${station.capacity} vélos"
                        setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                    }
                    overlay.add(marker)
                }

                map.overlays.add(overlay)
                map.invalidate()
            } catch (e: Exception) {
                Toast.makeText(this@MainActivity, "Erreur : ${e.message}", Toast.LENGTH_LONG).show()  // ✅ MainActivity
            }
        }
    }

    override fun onResume() { super.onResume(); map.onResume() }
    override fun onPause() { super.onPause(); map.onPause() }
}