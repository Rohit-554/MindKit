package com.example.mindkit.network

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType
import kotlinx.serialization.Serializable

private const val SAMPLE_POST_URL = "https://jsonplaceholder.typicode.com/posts/1"
private const val SAMPLE_POSTS_URL = "https://jsonplaceholder.typicode.com/posts"

class ApiService(private val client: HttpClient) {

    suspend fun fetchSampleData(): SampleResponse =
        client.get(SAMPLE_POST_URL) {
            contentType(ContentType.Application.Json)
        }.body()

    suspend fun postSampleData(request: SampleRequest): SampleResponse =
        client.post(SAMPLE_POSTS_URL) {
            contentType(ContentType.Application.Json)
            setBody(request)
        }.body()
}

@Serializable
data class SampleResponse(
    val userId: Int,
    val id: Int,
    val title: String,
    val body: String
)

@Serializable
data class SampleRequest(
    val title: String,
    val body: String,
    val userId: Int
)
