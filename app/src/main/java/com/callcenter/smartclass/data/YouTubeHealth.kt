package com.callcenter.smartclass.data

import com.google.gson.annotations.SerializedName

data class VideoItem(
    val id: VideoId,
    val snippet: Snippet,
    val statistics: Statistics?,
    val videoUrl: String?
)

data class VideoId(
    val videoId: String
)

data class Snippet(
    val title: String,
    val description: String,
    val channelTitle: String,
    val channelId: String,
    val thumbnails: Thumbnails
)

data class Thumbnails(
    val default: Thumbnail,
    val medium: Thumbnail,
    val high: Thumbnail
)

data class Thumbnail(
    val url: String
)

data class Statistics(
    val likeCount: String,
    val viewCount: String
)

data class VideoResponse(
    val items: List<VideoItem>
)

data class VideoDetailsResponse(
    val items: List<VideoDetailItem>
)

data class VideoDetailItem(
    val id: String,
    val snippet: Snippet,
    val statistics: Statistics,
    val contentDetails: ContentDetails
)

data class ContentDetails(
    val duration: String
)

data class CommentSnippet(
    @SerializedName("authorDisplayName")
    val authorDisplayName: String,

    @SerializedName("textDisplay")
    val textDisplay: String,

    @SerializedName("authorProfileImageUrl")
    val authorProfileImageUrl: String
)

data class CommentResponse(
    val items: List<CommentThread>
)

data class CommentThread(
    val id: String,
    val snippet: CommentThreadSnippet
)

data class CommentThreadSnippet(
    @SerializedName("topLevelComment")
    val topLevelComment: Comment
)

data class Comment(
    val id: String,
    val snippet: CommentSnippet
)

data class ChannelResponse(
    val items: List<ChannelItem>
)

data class ChannelItem(
    val snippet: ChannelSnippet
)

data class ChannelSnippet(
    val thumbnails: Thumbnails
)
