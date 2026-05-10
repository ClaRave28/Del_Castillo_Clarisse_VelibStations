package fr.clarisse.stationsvelib.service


import retrofit2.http.GET
import fr.clarisse.stationsvelib.model.Stations

interface VelibApiService {

    @GET("opendata/Velib_Metropole/station_information.json")
    suspend fun getStations(): Stations
}

