package com.albertowisdom.wisdomspark.data.remote

object RemoteConfig {
    const val CONNECT_TIMEOUT_SECONDS = 30L
    const val READ_TIMEOUT_SECONDS = 30L  
    const val WRITE_TIMEOUT_SECONDS = 30L
    const val SYNC_INTERVAL_HOURS = 24L
    const val SYNC_WORK_TAG = "quote_sync"
    const val SYNC_WORK_NAME = "quote_sync_periodic"
}

object LogConfig {
    const val ENABLE_NETWORK_LOGGING = true
}
