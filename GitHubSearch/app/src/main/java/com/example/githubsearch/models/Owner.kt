package com.example.githubsearch.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Owner(
    @SerialName("login")
    val login: String,
    @SerialName("avatar_url")
    val avatarUrl: String
)
