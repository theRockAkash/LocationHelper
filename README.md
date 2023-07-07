# LocationHelper.kt

```

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
```

#MainActivity.kt

```
 private val locationHelper: LocationHelper by lazy { LocationHelper(this, ::onLocationUpdate) }

    private fun onLocationUpdate(address: Address?) {
        viewModel.location = address
        Log.w(TAG, "onLocationUpdate: $address")
        address?.let {
            if (binding.tvLocation.visibility == View.GONE) {
                expand(binding.tvLocation)
            }
            binding.tvLocation.text = it.locality
            locationHelper.removeLocationUpdates()
        }

    }
   private fun checkAndGetLocationUpdates() {
        val result =
            ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
        val result1 =
            ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
        val isGranted =
            result == PackageManager.PERMISSION_GRANTED || result1 == PackageManager.PERMISSION_GRANTED

        if (isGranted) {
            getLocationUpdates()
        } else requestPermission()
    }

 private fun getLocationUpdates() {
        locationHelper.getLocationUpdates()
    }

  private fun requestPermission() {
        requestPermissionLauncher.launch(
            arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION
            ),
        )
    }

 private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions: Map<String, Boolean> ->
        var isGranted = false
        permissions.entries.forEach {
            if (it.value) {
                isGranted = true
            }
        }
        if (isGranted) {
            getLocationUpdates()
        } else {
            Utils.toasty(this, "Location permission denied")
        }
    }

@Deprecated("Deprecated in Java")
override fun onActivityResult(requestCode: Int, resultCode: Int, @Nullable data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (100 == requestCode) {
            if (RESULT_OK == resultCode) {
                locationHelper.getLocationUpdates()
                Log.w(TAG, "onActivityResult: ")
            } else {
                Utils.toasty(this, "Please turn on location")
            }
        }
    }


```

#onCreate()

```
Handler(Looper.getMainLooper()).postDelayed({ checkAndGetLocationUpdates() }, 2000)

```

#Animation Fuction

```
 fun expand(view: View) {
            view.measure(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
            val targetHeight = view.measuredHeight

            // Set initial height to 0 and show the view
            view.layoutParams.height = 0
            view.visibility = View.VISIBLE
            val anim = ValueAnimator.ofInt(view.measuredHeight, targetHeight)
            anim.interpolator = AccelerateInterpolator()
            anim.duration = 300
            anim.addUpdateListener { animation ->
                val layoutParams = view.layoutParams
                layoutParams.height = (targetHeight * animation.animatedFraction).toInt()
                view.layoutParams = layoutParams
            }
            anim.addListener(object : AnimatorListenerAdapter() {
                override fun onAnimationEnd(animation: Animator) {
                    // At the end of animation, set the height to wrap content
                    // This fix is for long views that are not shown on screen
                    val layoutParams = view.layoutParams
                    layoutParams.height = ViewGroup.LayoutParams.WRAP_CONTENT
                }
            })
            anim.start()
        }
```

#TextView

```
                         <TextView
                            android:id="@+id/tv_location"
                            android:layout_width="match_parent"
                            android:layout_height="0dp"
                            android:ellipsize="end"
                            android:lines="1"
                            android:text="Lucknow"
                            android:textColor="@color/textColor"
                            android:textSize="14sp"
                            android:visibility="gone"
                            tools:ignore="Suspicious0dp" />
```

