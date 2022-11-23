package com.example.githubsearch.data

import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.example.githubsearch.models.GithubRepo
import com.example.githubsearch.network.GitHubApi

class UsersDataSource(
    private val repo: GitHubApi,
    private val query: String
) : PagingSource<Int, GithubRepo>() {

    override fun getRefreshKey(state: PagingState<Int, GithubRepo>): Int? {
        return state.anchorPosition?.let { anchorPosition ->
            val anchorPage = state.closestPageToPosition(anchorPosition)
            anchorPage?.prevKey?.plus(1) ?: anchorPage?.nextKey?.minus(1)
        }
    }

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, GitHubRepo> {
        return try {
            val nextPageNumber = params.key ?: 1
            val response = repo.getUsers(nextPageNumber, 10)
            LoadResult.Page(
                data = response.users,
                prevKey = null,
                nextKey = if (response.users.isNotEmpty()) response.page + 1 else null
            )
        } catch (e: Exception) {
            LoadResult.Error(e)
        }
    }
}
