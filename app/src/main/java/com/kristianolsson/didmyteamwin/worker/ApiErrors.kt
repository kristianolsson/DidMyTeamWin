package com.kristianolsson.didmyteamwin.worker

import retrofit2.HttpException
import java.io.IOException
import java.net.SocketTimeoutException
import java.net.UnknownHostException

object ApiErrors {

    /**
     * True for failures likely to succeed on a later attempt: no network, timeouts,
     * rate limiting (429) and server errors (5xx).
     */
    fun isTransient(e: Throwable): Boolean = when (e) {
        is HttpException -> e.code() == 429 || e.code() >= 500
        is IOException -> true
        else -> false
    }

    /** Short human label for notifications, e.g. "no network", "HTTP 429". */
    fun shortLabel(e: Throwable): String = when (e) {
        is UnknownHostException -> "no network"
        is SocketTimeoutException -> "timeout"
        is HttpException -> "HTTP ${e.code()}"
        is IOException -> "network error"
        else -> e.javaClass.simpleName
    }

    /** Detailed description stored in the DB and shown on the Debug screen. */
    fun describe(e: Throwable): String {
        val detail = if (e is HttpException) "HTTP ${e.code()} ${e.message()}" else e.message
        return "${e.javaClass.simpleName}: ${detail ?: "(no message)"}"
    }
}
