package fr.clarisse.stationsvelib.model

data class Stations(
    val data: StationData
)

data class StationData(
    val stations: List<Station>
)

data class Station(
    val station_id: Long,
    val name: String,
    val lat: Double,
    val lon: Double,
    val capacity: Int
)

data class StationStatusResponse(
    val data: StationStatusData
)

data class StationStatusData(
    val stations: List<StationStatus>
)

data class StationStatus(
    val station_id: Long,
    val num_bikes_available: Int,
    val num_docks_available: Int
)