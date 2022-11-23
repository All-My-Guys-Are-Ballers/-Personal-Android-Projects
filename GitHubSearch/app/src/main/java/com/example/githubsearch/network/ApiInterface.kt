package com.example.githubsearch.network

import com.example.githubsearch.models.GithubRepoResponse
import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.http.GET
import retrofit2.http.Query

private val retrofit = Retrofit.Builder()
    .addConverterFactory(Json.asConverterFactory("application/json".toMediaType()))
//    .baseUrl(BASE_URL)
    .build()


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