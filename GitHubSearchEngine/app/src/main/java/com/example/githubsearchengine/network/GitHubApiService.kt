package com.example.githubsearchengine.network

import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import kotlinx.serialization.json.Json
import okhttp3.MediaType
import com.example.githubsearchengine.models.github.GithubRepoResponse
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.http.GET
import retrofit2.http.Query

private const val BASE_URL =
    "https://android-kotlin-fun-mars-server.appspot.com"

/**
 * Use the Retrofit builder to build a retrofit object using a kotlinx.serialization converter
 */
private val retrofit = Retrofit.Builder()
    .addConverterFactory(Json.asConverterFactory(MediaType.get("application/json")))
    .baseUrl(BASE_URL)
    .build()

/**
 * Retrofit service object for creating api calls
 */
interface ApiInterface {
    // ----------------------------------------------------------------
    // Search Github Repositories
    // Help: https://docs.github.com/en/rest/reference/search#search-repositories
    // ----------------------------------------------------------------

    @GET("https://api.github.com/search/repositories")
    suspend fun searchGithubRepo(
        @Query("page") page: Int,
        @Query("sort") sort: String,
        @Query("order") order: String,
        @Query("q") query: String,
    ): Response<GithubRepoResponse>
}

object GitHubApi {
    val retrofitService: ApiInterface by lazy {
        retrofit.create(ApiInterface::class.java)
    }
}
