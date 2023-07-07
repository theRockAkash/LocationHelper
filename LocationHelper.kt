package com.zapp.app.utils

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.location.Address
import android.location.Geocoder
import android.location.Location
import android.location.LocationManager
import android.os.Looper
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.LocationSettingsRequest
import com.google.android.gms.location.Priority
import java.util.Locale


class LocationHelper(
    private val context: Context,
    private val callback: (Address?) -> Unit
) {
    private val fusedLocationProviderClient: FusedLocationProviderClient by lazy {
        LocationServices.getFusedLocationProviderClient(context)
    }

    private val locationRequest by lazy {
        LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 60)
            .setWaitForAccurateLocation(false)
            .setMinUpdateIntervalMillis(30)
            .setMaxUpdateDelayMillis(2)
            .build()
    }


    // LocationCallback - Called when FusedLocationProviderClient has a new Location.
    private val locationCallback: LocationCallback by lazy {
        object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                super.onLocationResult(locationResult)
                sendLastLocation(locationResult.lastLocation)

            }

        }
    }

    private fun sendLastLocation(locationResult: Location?) {
        currentLocation = locationResult
        if (currentLocation != null) {
            val geocoder = Geocoder(context, Locale.getDefault())
            try {
                val list: List<Address>? =
                    geocoder.getFromLocation(
                        currentLocation!!.latitude,
                        currentLocation!!.longitude,
                        1
                    )
                callback.invoke(list?.get(0))
            } catch (e: Exception) {
                e.printStackTrace()
            }


        }
    }


    private var currentLocation: Location? = null


    @SuppressLint("MissingPermission")
    private fun isLocationEnabled(): Boolean {
        val locationManager: LocationManager =
            context.getSystemService(Context.LOCATION_SERVICE) as LocationManager

        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) || locationManager.isProviderEnabled(
            LocationManager.NETWORK_PROVIDER
        )
    }


    @SuppressLint("MissingPermission")
    fun getLocationUpdates() {
        if (isLocationEnabled()) {
            fusedLocationProviderClient.requestLocationUpdates(
                locationRequest,
                locationCallback,
                Looper.getMainLooper()
            )
        } else {
            val builder = LocationSettingsRequest.Builder()
                .addLocationRequest(locationRequest)
                .setAlwaysShow(true).build()

            LocationServices.getSettingsClient(context).checkLocationSettings(builder)
                .addOnFailureListener { ex ->
                    val resolvable = ex as ResolvableApiException
                    resolvable.startResolutionForResult(
                        context as Activity,
                        100
                    )
                }
        }

    }

    fun removeLocationUpdates() {
        fusedLocationProviderClient.removeLocationUpdates(locationCallback)
    }


}