package com.dc.coordinates

import android.content.Context
import android.location.Address
import android.location.Geocoder
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.annotation.RequiresApi
import java.io.IOException
import java.util.Locale

data class LatLon(val lat: Double, val lon: Double)
{
    /**
     *  address string including street, number, and neighborhood.
     */
    fun getAddressOld(context: Context): String {
        val geocoder = Geocoder(context)
        try {
            val addresses = geocoder.getFromLocation(lat, lon, 1)
            if (addresses?.isNotEmpty() == true) {
                val address = addresses[0]
                val street = address.thoroughfare ?: ""
                val number = address.subThoroughfare ?: ""
                val neighborhood = address.subLocality?.let { ", $it" } ?: ""
                return "$street $number$neighborhood".trim().takeIf { it.isNotEmpty() } ?: "Endereço desconhecido."
            }
        } catch (e: IOException) {
            // Handle network or I/O errors
            Log.e("AddrError", "Unable to query location")
            return "Endereço desconhecido."
        }
        return "Endereço desconhecido."
    }

    fun getAddress(context: Context, lat: Double, lon: Double, callback: (String) -> Unit) {
        val geocoder = Geocoder(context, Locale.getDefault())

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            geocoder.getFromLocation(lat, lon, 1, object : Geocoder.GeocodeListener {
                override fun onGeocode(addresses: MutableList<Address>) {
                    callback(formatAddress(addresses.firstOrNull()))
                }
                override fun onError(errorMessage: String?) {
                    callback("Endereço desconhecido.")
                }
            })
        } else {
            Thread {
                try {
                    val addresses = geocoder.getFromLocation(lat, lon, 1)
                    val result = formatAddress(addresses?.firstOrNull())
                    Handler(Looper.getMainLooper()).post {
                        callback(result)
                    }
                } catch (e: Exception) {
                    Handler(Looper.getMainLooper()).post {
                        callback("Endereço desconhecido.")
                    }
                }
            }.start()
        }
    }

    private fun formatAddress(address: Address?): String {
        if (address == null) return "Endereço desconhecido."
        val street = address.thoroughfare ?: ""
        val number = address.subThoroughfare ?: ""
        val neighborhood = address.subLocality?.let { ", $it" } ?: ""
        return "$street $number$neighborhood".trim().ifEmpty { "Endereço desconhecido." }
    }

}

