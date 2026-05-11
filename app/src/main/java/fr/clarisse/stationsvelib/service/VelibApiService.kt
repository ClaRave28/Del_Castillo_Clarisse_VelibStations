package fr.clarisse.stationsvelib.service


import fr.clarisse.stationsvelib.model.StationStatusResponse
import retrofit2.http.GET
import fr.clarisse.stationsvelib.model.Stations

interface VelibApiService {

    @GET("opendata/Velib_Metropole/station_information.json")
    suspend fun getStations(): Stations

    @GET("opendata/Velib_Metropole/station_status.json")
    suspend fun getStationStatus(): StationStatusResponse
}

