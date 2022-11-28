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

import android.content.ContentValues.TAG
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.githubsearch.models.GithubRepoResponse
import com.example.githubsearch.network.GitHubApi
import kotlinx.coroutines.launch
import retrofit2.HttpException
import timber.log.Timber
import java.io.IOException

/**
 * UI state for the Home screen
 */
sealed interface GitHubUiState{
    data class Success(val response: GithubRepoResponse?): GitHubUiState
    object Error : GitHubUiState
    object Loading : GitHubUiState
}

class GitHubViewModel : ViewModel() {
    /** The mutable State that stores the status of the most recent request */
    var githubUiState: GitHubUiState by mutableStateOf(GitHubUiState.Loading)


    fun getGitHubRepos(
        page:Int = 1,
        query:String,
        sort: String = "stars",
        order: String = "desc"
    ) {
        githubUiState = GitHubUiState.Loading

        viewModelScope.launch {
            githubUiState = try {
                val response = GitHubApi.retrofitService.searchGithubRepo(
                    page = page ,
                    sort = sort,
                    order = order,
                    query = query
                ).body()
                Timber.tag(TAG).d(response.toString())
                GitHubUiState.Success(response)
            } catch (e: IOException) {
                GitHubUiState.Error
            } catch (e: HttpException) {
                GitHubUiState.Error
            }
        }
    }
}
