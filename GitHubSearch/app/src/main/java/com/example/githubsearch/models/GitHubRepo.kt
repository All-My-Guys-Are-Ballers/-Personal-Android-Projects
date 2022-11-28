package com.example.githubsearch.models

import androidx.annotation.Keep
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Keep
@Serializable
data class GithubRepo(
    @SerialName( "id")
    val id: Int,
    @SerialName("node_id")
    val nodeId: String,
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
