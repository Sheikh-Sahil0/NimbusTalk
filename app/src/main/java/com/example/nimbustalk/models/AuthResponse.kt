package com.example.nimbustalk.models

import com.google.gson.annotations.SerializedName

/**
 * Supabase authentication response
 */
data class AuthResponse(
    @SerializedName("access_token")
    val accessToken: String?,

    @SerializedName("token_type")
    val tokenType: String?,

    @SerializedName("expires_in")
    val expiresIn: Int?,

    @SerializedName("refresh_token")
    val refreshToken: String?,

    @SerializedName("user")
    val user: AuthUser?
)

/**
 * Supabase auth user object
 */
data class AuthUser(
    @SerializedName("id")
    val id: String,

    @SerializedName("aud")
    val aud: String?,

    @SerializedName("role")
    val role: String?,

    @SerializedName("email")
    val email: String,

    @SerializedName("email_confirmed_at")
    val emailConfirmedAt: String?,

    @SerializedName("phone")
    val phone: String?,

    @SerializedName("confirmed_at")
    val confirmedAt: String?,

    @SerializedName("last_sign_in_at")
    val lastSignInAt: String?,

    @SerializedName("app_metadata")
    val appMetadata: Map<String, Any>?,

    @SerializedName("user_metadata")
    val userMetadata: Map<String, Any>?,

    @SerializedName("identities")
    val identities: List<Any>?,

    @SerializedName("created_at")
    val createdAt: String?,

    @SerializedName("updated_at")
    val updatedAt: String?
)

/**
 * Registration request body
 */
data class RegisterRequest(
    @SerializedName("email")
    val email: String,

    @SerializedName("password")
    val password: String,

    @SerializedName("data")
    val data: RegisterUserMetadata
)

/**
 * User metadata for registration
 */
data class RegisterUserMetadata(
    @SerializedName("username")
    val username: String,

    @SerializedName("display_name")
    val displayName: String
)

/**
 * Login request body
 */
data class LoginRequest(
    @SerializedName("email")
    val email: String,

    @SerializedName("password")
    val password: String,

    @SerializedName("grant_type")
    val grantType: String = "password"
)

/**
 * Password reset request
 */
data class PasswordResetRequest(
    @SerializedName("email")
    val email: String
)

/**
 * Token refresh request
 */
data class RefreshTokenRequest(
    @SerializedName("refresh_token")
    val refreshToken: String,

    @SerializedName("grant_type")
    val grantType: String = "refresh_token"
)