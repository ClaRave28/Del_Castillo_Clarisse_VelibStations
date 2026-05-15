package fr.clarisse.stationsvelib

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.bottomnavigation.BottomNavigationView
import fr.clarisse.stationsvelib.model.Station
import fr.clarisse.stationsvelib.model.StationStatus

class MainActivity : AppCompatActivity() {

    // Exposé pour que FavorisFragment puisse y accéder
    var toutesLesStations: List<Station> = emptyList()
    var tousLesStatuts: Map<Long, StationStatus> = emptyMap()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Charge le fragment carte par défaut
        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, MapFragment())
                .commit()
        }

        val bottomNav = findViewById<BottomNavigationView>(R.id.bottom_nav)
        bottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_carte -> {
                    supportFragmentManager.beginTransaction()
                        .replace(R.id.fragment_container, MapFragment())
                        .commit()
                    true
                }
                R.id.nav_favoris -> {
                    supportFragmentManager.beginTransaction()
                        .replace(R.id.fragment_container, FavorisFragment())
                        .commit()
                    true
                }
                else -> false
            }
        }
    }
}



