# LocationHelper in Android


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

#Animation Function to animate view from 0p to WRAP_CONTENT

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

