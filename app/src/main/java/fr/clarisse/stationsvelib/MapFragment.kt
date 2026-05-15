package fr.clarisse.stationsvelib

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.google.android.gms.location.LocationServices
import fr.clarisse.stationsvelib.model.Station
import fr.clarisse.stationsvelib.model.StationStatus
import fr.clarisse.stationsvelib.service.Client
import kotlinx.coroutines.launch
import org.osmdroid.config.Configuration
import org.osmdroid.events.MapEventsReceiver
import org.osmdroid.events.MapListener
import org.osmdroid.events.ScrollEvent
import org.osmdroid.events.ZoomEvent
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.FolderOverlay
import org.osmdroid.views.overlay.MapEventsOverlay
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.infowindow.InfoWindow

class MapFragment : Fragment() {

    private lateinit var map: MapView
    private val PERMISSION_REQUEST_CODE = 1001

    private var openedInfoWindow: Infos? = null
    private var stationsOverlay = FolderOverlay()

    private var toutesLesStations: List<Station> = emptyList()
    private var tousLesStatuts: Map<Long, StationStatus> = emptyMap()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(R.layout.map, container, false)

        Configuration.getInstance().apply {
            load(requireContext(), requireContext().getSharedPreferences("osmdroid", 0))
            userAgentValue = requireContext().packageName
        }

        map = view.findViewById(R.id.map)
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
        chargerDonnees()

        return view
    }

    private fun chargerDonnees() {
        lifecycleScope.launch {
            try {
                toutesLesStations = Client.api.getStations().data.stations
                tousLesStatuts = Client.api.getStationStatus().data.stations
                    .associateBy { it.station_id }

                (requireActivity() as MainActivity).toutesLesStations = toutesLesStations
                (requireActivity() as MainActivity).tousLesStatuts = tousLesStatuts

                chargerStationsDansZone()
            } catch (e: Exception) {
                Toast.makeText(requireContext(), "Erreur : ${e.message}", Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun chargerStationsDansZone() {
        val boundingBox = map.boundingBox
        map.overlays.remove(stationsOverlay)
        stationsOverlay = FolderOverlay()

        if (map.zoomLevelDouble < 13.0) {
            map.invalidate()
            Toast.makeText(requireContext(), "Zoomez pour voir les stations", Toast.LENGTH_SHORT).show()
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
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION)
            == PackageManager.PERMISSION_GRANTED
        ) {
            afficherMaPosition()
        } else {
            requestPermissions(
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                PERMISSION_REQUEST_CODE
            )
        }
    }

    private fun afficherMaPosition() {
        if (ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION)
            != PackageManager.PERMISSION_GRANTED) return

        val fusedClient = LocationServices.getFusedLocationProviderClient(requireContext())
        fusedClient.lastLocation.addOnSuccessListener { location ->
            if (location != null) {
                val point = GeoPoint(location.latitude, location.longitude)
                val marker = Marker(map).apply {
                    position = point
                    title = "Ma position"
                    setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                    icon = ContextCompat.getDrawable(requireContext(), org.osmdroid.library.R.drawable.ic_menu_mylocation)
                }
                map.overlays.add(marker)
                map.controller.animateTo(point, 16.0, null)
                map.invalidate()
            } else {
                Toast.makeText(requireContext(), "Position introuvable", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<out String>, grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == PERMISSION_REQUEST_CODE &&
            grantResults.isNotEmpty() &&
            grantResults[0] == PackageManager.PERMISSION_GRANTED
        ) {
            afficherMaPosition()
        } else {
            Toast.makeText(requireContext(), "Permission refusée", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onResume() { super.onResume(); map.onResume() }
    override fun onPause() { super.onPause(); map.onPause() }
}