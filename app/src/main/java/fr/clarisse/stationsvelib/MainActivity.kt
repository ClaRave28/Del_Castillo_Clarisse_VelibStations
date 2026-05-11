package fr.clarisse.stationsvelib

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.FolderOverlay
import org.osmdroid.views.overlay.Marker
import fr.clarisse.stationsvelib.service.Client
import com.google.android.gms.location.LocationServices
import org.osmdroid.events.MapListener
import org.osmdroid.events.ScrollEvent
import org.osmdroid.events.ZoomEvent
import org.osmdroid.views.overlay.infowindow.InfoWindow
import org.osmdroid.events.MapEventsReceiver
import org.osmdroid.views.overlay.MapEventsOverlay
import fr.clarisse.stationsvelib.model.Station
import fr.clarisse.stationsvelib.model.StationStatus
import com.google.android.material.bottomnavigation.BottomNavigationView

class MainActivity : AppCompatActivity() {

    private lateinit var map: MapView
    private val PERMISSION_REQUEST_CODE = 1001

    private var openedInfoWindow: Infos? = null
    private var stationsOverlay = FolderOverlay()

    private var toutesLesStations: List<Station> = emptyList()
    private var tousLesStatuts: Map<Long, StationStatus> = emptyMap()

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
            setZoom(16.0)
            setCenter(GeoPoint(48.8566, 2.3522))
        }

        val mapEventsOverlay = MapEventsOverlay(object : MapEventsReceiver {
            override fun singleTapConfirmedHelper(p: GeoPoint?): Boolean {
                InfoWindow.closeAllInfoWindowsOn(map)
                openedInfoWindow = null
                return true
            }
            override fun longPressHelper(p: GeoPoint?): Boolean = false
        })
        map.overlays.add(0, mapEventsOverlay)

        map.addMapListener(object : MapListener {
            override fun onScroll(event: ScrollEvent?): Boolean {
                chargerStationsDansZone()
                return true
            }
            override fun onZoom(event: ZoomEvent?): Boolean {
                chargerStationsDansZone()
                return true
            }
        })

        demanderPermissionEtLocaliser()

        val bottomNav = findViewById<BottomNavigationView>(R.id.bottom_nav)
        bottomNav.selectedItemId = R.id.nav_carte
        bottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_carte -> true
                R.id.nav_favoris -> {
                    val ids = Favorites.getIds(this)
                    val favoris = toutesLesStations
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
                        }

                    val intent = Intent(this, FavoritesActivity::class.java)
                    intent.putExtra("stations", ArrayList(favoris))
                    startActivity(intent)
                    true
                }

            }
            true
        }

        chargerDonnees() // fetch API une seule fois
    }


    private fun chargerDonnees() {
        lifecycleScope.launch {
            try {
                toutesLesStations = Client.api.getStations().data.stations
                tousLesStatuts = Client.api.getStationStatus().data.stations
                    .associateBy { it.station_id }
                chargerStationsDansZone()
            } catch (e: Exception) {
                Toast.makeText(this@MainActivity, "Erreur : ${e.message}", Toast.LENGTH_LONG).show()
            }
        }
    }


    private fun chargerStationsDansZone() {
        val boundingBox = map.boundingBox

        map.overlays.remove(stationsOverlay)
        stationsOverlay = FolderOverlay()

        if (map.zoomLevelDouble < 13.0) {
            map.invalidate()
            Toast.makeText(this, "Zoomez pour voir les stations", Toast.LENGTH_SHORT).show()
            return
        }

        toutesLesStations
            .filter { boundingBox.contains(GeoPoint(it.lat, it.lon)) }
            .forEach { station ->
                val status = tousLesStatuts[station.station_id]
                val infoWindow = Infos(map, station.name, station.capacity,
                    status?.num_bikes_available, status?.num_docks_available, station.station_id)

                val marker = Marker(map).apply {
                    position = GeoPoint(station.lat, station.lon)
                    setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                    this.infoWindow = infoWindow
                    setOnMarkerClickListener { clickedMarker, _ ->
                        if (openedInfoWindow == infoWindow && infoWindow.isOpen) {
                            infoWindow.close()
                            openedInfoWindow = null
                        } else {
                            InfoWindow.closeAllInfoWindowsOn(map)
                            clickedMarker.showInfoWindow()
                            openedInfoWindow = infoWindow
                        }
                        true
                    }
                }
                stationsOverlay.add(marker)
            }

        map.overlays.add(stationsOverlay)
        map.invalidate()
    }

    private fun demanderPermissionEtLocaliser() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
            == PackageManager.PERMISSION_GRANTED
        ) {
            afficherMaPosition()
        } else {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                PERMISSION_REQUEST_CODE
            )
        }
    }

    private fun afficherMaPosition() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
            != PackageManager.PERMISSION_GRANTED) return

        val fusedClient = LocationServices.getFusedLocationProviderClient(this)

        fusedClient.lastLocation.addOnSuccessListener { location ->
            if (location != null) {
                val point = GeoPoint(location.latitude, location.longitude)
                val marker = Marker(map).apply {
                    position = point
                    title = "Ma position"
                    setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                    icon = ContextCompat.getDrawable(this@MainActivity, org.osmdroid.library.R.drawable.ic_menu_mylocation)
                }
                map.overlays.add(marker)
                map.controller.animateTo(point, 16.0, null)
                map.invalidate()
            } else {
                Toast.makeText(this, "Position introuvable", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == PERMISSION_REQUEST_CODE &&
            grantResults.isNotEmpty() &&
            grantResults[0] == PackageManager.PERMISSION_GRANTED
        ) {
            afficherMaPosition()
        } else {
            Toast.makeText(this, "Permission refusée : position non affichée", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onResume() { super.onResume(); map.onResume() }
    override fun onPause() { super.onPause(); map.onPause() }
}