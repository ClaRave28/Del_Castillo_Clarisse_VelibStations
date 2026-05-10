package fr.clarisse.stationsvelib.model

data class Stations(
    val data: StationData
)

data class StationData(
    val stations: List<Station>
)

data class Station(
    val station_id: String,
    val name: String,
    val lat: Double,
    val lon: Double,
    val capacity: Int
)