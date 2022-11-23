package com.example.githubsearch.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable


@Serializable
data class GithubRepoResponse(
    @SerialName("total_count")
    val totalCount: Int?,
    @SerialName("incomplete_results")
    val incompleteResults: Boolean?,
    @SerialName("items")
    val items: List<GithubRepo>?,
    @SerialName("message")
    val message: String?
)