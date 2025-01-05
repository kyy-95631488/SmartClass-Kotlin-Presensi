package com.callcenter.smartclass.data

import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query

interface YouTubeApi {
    @GET("search")
    fun searchVideos(
        @Query("part") part: String = "snippet",
        @Query("q") query: String,
        @Query("type") type: String = "video",
        @Query("maxResults") maxResults: Int = 999999999,
        @Query("key") apiKey: String
    ): Call<VideoResponse>

    @GET("videos")
    fun getVideoDetails(
        @Query("part") part: String = "snippet,statistics",
        @Query("id") id: String,
        @Query("key") apiKey: String
    ): Call<VideoDetailsResponse>

    @GET("channels")
    fun getChannelDetails(
        @Query("part") part: String = "snippet",
        @Query("id") channelId: String,
        @Query("key") apiKey: String
    ): Call<ChannelResponse>

    @GET("commentThreads")
    fun getComments(
        @Query("part") part: String = "snippet",
        @Query("videoId") videoId: String,
        @Query("key") apiKey: String,
        @Query("maxResults") maxResults: Int = 10
    ): Call<CommentResponse>

}