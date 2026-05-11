package fr.clarisse.stationsvelib

import android.Manifest
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
import org.osmdroid.views.overlay.mylocation.GpsMyLocationProvider
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay
import fr.clarisse.stationsvelib.service.Client
import com.google.android.gms.location.LocationServices

class MainActivity : AppCompatActivity() {

    private lateinit var map: MapView
    private val PERMISSION_REQUEST_CODE = 1001

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

        demanderPermissionEtLocaliser()
        chargerStations()
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
                map.controller.animateTo(point)
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
                Toast.makeText(this@MainActivity, "Erreur : ${e.message}", Toast.LENGTH_LONG).show()
            }
        }
    }

    override fun onResume() { super.onResume(); map.onResume() }
    override fun onPause() { super.onPause(); map.onPause() }
}