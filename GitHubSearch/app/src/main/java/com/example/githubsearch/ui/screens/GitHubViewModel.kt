/*
 * Copyright (C) 2022 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.example.githubsearch.ui.screens

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.githubsearch.models.GithubRepoResponse
import com.example.githubsearch.network.GitHubApi
import kotlinx.coroutines.launch
import retrofit2.HttpException
import retrofit2.Response
import java.io.IOException
import java.time.temporal.TemporalQuery

/**
 * UI state for the Home screen
 */
sealed interface GitHubUiState{
    data class Success(val response: Response<GithubRepoResponse>): GitHubUiState
    object Error : GitHubUiState
    object Loading : GitHubUiState
}

class GitHubViewModel() : ViewModel() {
    /** The mutable State that stores the status of the most recent request */
    var githubUiState: GitHubUiState by mutableStateOf(GitHubUiState.Loading)

    /**
     * Call getMarsPhotos() on init so we can display status immediately.
     */
    init {
        getGitHubRepos(1,"VIPlearner")
    }

    /**
     * Gets Mars photos information from the Mars API Retrofit service and updates the
     */
    private fun getGitHubRepos(
        page: Int,
        query: String
    ) {
        viewModelScope.launch {
            githubUiState = try {
                val response = GitHubApi.retrofitService.searchGithubRepo(
                    page = page,
                    query = query
                )
                GitHubUiState.Success(response)
            } catch (e: IOException) {
                GitHubUiState.Error
            } catch (e: HttpException) {
                GitHubUiState.Error
            }
        }
    }
}
