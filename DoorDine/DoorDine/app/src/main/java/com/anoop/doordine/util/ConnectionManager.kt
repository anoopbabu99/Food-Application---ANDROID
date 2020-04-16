package com.anoop.doordine.util

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkInfo

class ConnectionManager {
    fun checkConnectivity(context: Context ): Boolean{

        //obtain info about what services this device has (WiFi, Data, HotSpot etc):
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

        //check if any of these services are enabled: (but just because they are enabled doesn't mean there is an active connection)
        val activeNetwork : NetworkInfo?=connectivityManager.activeNetworkInfo

        //check if any of these enabled services (if any are enabled) have an active internet connection:
        if(activeNetwork?.isConnected!=null){
            return activeNetwork.isConnected
            //true => service is enabled and there is an active connection.
            //false => service is enabled but there is not active connection.
        }
        else {
            return false;
            //service itself is disabled!
        }
    }
}