package edu.nd.pmcburne.hwapp.one

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Url

interface RhythmicAppAPICalls {
    @GET
    suspend fun fetchGameScores(@Url finishPoint: String): GameScoreRespond
}

object APIProvider {
    private const val API_Connect = "https://ncaa-api.henrygd.me/"

    val ConnectionAPI: RhythmicAppAPICalls by lazy {
        Retrofit.Builder()
            .baseUrl(API_Connect)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(RhythmicAppAPICalls::class.java)
    }
}