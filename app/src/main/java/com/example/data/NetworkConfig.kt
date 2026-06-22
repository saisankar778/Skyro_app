package com.example.data

/**
 * Central network configuration for the Skyro app.
 *
 * ─────────────────────────────────────────────────────────────────────────────
 * HOW TO UPDATE THE URL:
 *   1. Start ngrok:  ngrok http 8000
 *   2. Copy the https://xxxxx.ngrok-free.app URL shown in terminal
 *   3. Paste it below as DEFAULT_BASE_URL  (keep the trailing slash removed)
 *   4. Rebuild the app in Android Studio
 * ─────────────────────────────────────────────────────────────────────────────
 */
object NetworkConfig {

    /**
     * Primary backend URL — your ngrok HTTPS tunnel.
     * UPDATE THIS every time you restart ngrok (the subdomain changes).
     */
    const val DEFAULT_BASE_URL = "https://9773-103-217-237-56.ngrok-free.app"

    /**
     * Custom Drone Backend Base URL (port 8080 ngrok forwarding URL)
     */
    const val DEFAULT_DRONE_BASE_URL = "https://a8c4-103-217-237-56.ngrok-free.app"

    /**
     * The ngrok browser-warning header value required on every request.
     * Without this, ngrok returns an HTML warning page instead of JSON.
     */
    const val NGROK_HEADER_NAME  = "ngrok-skip-browser-warning"
    const val NGROK_HEADER_VALUE = "true"

    // API endpoint paths (kept here so any future rename is in one place)
    const val PATH_RESTAURANTS  = "api/restaurants"
    const val PATH_MENU_ITEMS   = "api/menu-items"
    const val PATH_LOCATIONS    = "api/locations"
    const val PATH_ORDERS       = "api/orders"
    const val PATH_CATEGORIES   = "api/categories"
}
