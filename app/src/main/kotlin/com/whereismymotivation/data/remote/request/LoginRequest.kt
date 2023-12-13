package com.whereismymotivation.data.remote.request

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class BasicLoginRequest(
    @Json(name = "email")
    val email: String,

    @Json(name = "password")
    val password: String
)

@JsonClass(generateAdapter = true)
data class GoogleLoginRequest(
    @Json(name = "googleUserId")
    val googleUserId: String,

    @Json(name = "googleIdToken")
    val idToken: String,
)

@JsonClass(generateAdapter = true)
data class FacebookLoginRequest(
    @Json(name = "fbUserId")
    val facebookUserId: String,

    @Json(name = "fbAccessToken")
    val facebookAccessToken: String,
)