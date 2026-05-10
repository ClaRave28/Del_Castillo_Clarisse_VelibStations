package fr.clarisse.stationsvelib.service

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object Client {
    val api: VelibApiService by lazy {
        Retrofit.Builder()
            .baseUrl("https://velib-metropole-opendata.smovengo.cloud/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(VelibApiService::class.java)
    }
}