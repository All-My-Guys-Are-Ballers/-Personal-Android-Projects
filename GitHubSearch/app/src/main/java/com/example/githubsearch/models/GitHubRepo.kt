package com.example.githubsearch.models

import com.example.githubsearch.network.ApiInterface
import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import okhttp3.MediaType
import retrofit2.Retrofit
import java.security.acl.Owner


@Serializable
data class GithubRepo(
    @SerialName( "id")
    val id: Int,
    @SerialName( "name")
    val name: String,
    @SerialName( "full_name")
    val fullName: String,
    @SerialName( "owner")
    val owner: Owner,
    @SerialName( "description")
    val description: String?,
    @SerialName( "stargazers_count")
    val stargazersCount: Int,
    @SerialName( "watchers_count")
    val watchersCount: Int,
    @SerialName( "forks_count")
    val forksCount: Int,
)
