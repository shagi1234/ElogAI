package com.selbiconsulting.elog.domain.use_case

import android.annotation.SuppressLint
import android.location.Geocoder
import android.util.Log
import com.google.android.gms.location.FusedLocationProviderClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import java.io.IOException
import javax.inject.Inject
import kotlin.coroutines.resume

class UseCaseGetLastLocation @Inject constructor(
    private val fusedLocationClient: FusedLocationProviderClient,
    private val geocoder: Geocoder
) {
    @OptIn(ExperimentalCoroutinesApi::class)
    @SuppressLint("MissingPermission")
    suspend fun execute(): String = withContext(Dispatchers.IO) {
        suspendCancellableCoroutine { continuation ->
            fusedLocationClient.lastLocation
                .addOnSuccessListener { location ->
                    location?.let {
                        val locationName =
                            getLocationName(latitude = it.latitude, longitude = it.longitude)
                        Log.e("LOCATION_NAME", "execute: $locationName")
                        continuation.resume(locationName) {}
                    } ?: continuation.resume("")
                }
        }
    }

    private fun getLocationName(latitude: Double, longitude: Double): String {
        return try {
            val addresses = geocoder.getFromLocation(latitude, longitude, 1)
            var addressString = ""
            if (!addresses.isNullOrEmpty()) {
                val address = addresses[0]
                addressString = address.getAddressLine(0) // Get full address
                Log.e("LOCATION_NAME", addressString)
            } else {
                Log.e("LOCATION_NAME", "No address found for the location")
            }
            Log.e("LOCATION_NAME", "getLocationName: $addressString")
            addressString
        } catch (e: IOException) {
            println(e.message)
            ""
        }
    }
}

