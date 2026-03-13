package edu.nd.pmcburne.hwapp.one

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities

class GameConnectivityMonitor(
    applicationContext: Context
) {
    private val networkManager =
        applicationContext.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

    fun isConnected(): Boolean {
        val currentConnection = networkManager.activeNetwork ?: return false
        val networkCapabilities =
            networkManager.getNetworkCapabilities(currentConnection) ?: return false

        return networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) ||
                networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) ||
                networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET)
    }
}