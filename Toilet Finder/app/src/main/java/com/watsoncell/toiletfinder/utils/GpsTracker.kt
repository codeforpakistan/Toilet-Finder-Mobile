package com.watsoncell.toiletfinder.utils

import android.annotation.SuppressLint
import android.content.Context
import android.location.Geocoder
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import android.util.Log

class GPSTracker internal constructor(private val mContext: Context) : LocationListener {
    private var location: Location? = null
    private var latitude: Double = 0.toDouble()
    private var longitude: Double = 0.toDouble()
    private var altitude: Double = 0.toDouble()
    private var city = ""

    init {
        getLocation()
    }

    @SuppressLint("MissingPermission")
    fun getLocation(): Location? {
        try {
            val locationManager = (mContext
                .getSystemService(Context.LOCATION_SERVICE) as LocationManager)
            val isGPSEnabled = locationManager
                .isProviderEnabled(LocationManager.GPS_PROVIDER)
            val isNetworkEnabled = locationManager
                .isProviderEnabled(LocationManager.NETWORK_PROVIDER)
            if (isGPSEnabled || isNetworkEnabled) {
                if (isNetworkEnabled) {
                    locationManager.requestLocationUpdates(
                        LocationManager.NETWORK_PROVIDER,
                        MIN_TIME_BW_UPDATES,
                        MIN_DISTANCE_CHANGE_FOR_UPDATES.toFloat(), this
                    )
                    Log.d("arsalan", "Network Enabled")
                    location = locationManager
                        .getLastKnownLocation(LocationManager.NETWORK_PROVIDER)
                    if (location != null) {
                        latitude = location!!.latitude
                        longitude = location!!.longitude

                        altitude = location!!.altitude

                        //latitude, longitude & address-line for adding new Toilet
                        Constant.latitude = latitude
                        Constant.longitude = longitude

                    }
                }
                if (isGPSEnabled) {
                    if (location == null) {
                        locationManager.requestLocationUpdates(
                            LocationManager.GPS_PROVIDER,
                            MIN_TIME_BW_UPDATES,
                            MIN_DISTANCE_CHANGE_FOR_UPDATES.toFloat(), this
                        )
                        Log.d("GPS", "GPS Enabled")
                        location = locationManager
                            .getLastKnownLocation(LocationManager.GPS_PROVIDER)
                        if (location != null) {
                            latitude = location!!.latitude
                            longitude = location!!.longitude
                        }
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        return location
    }

    internal fun getLatitude(): Double {
        if (location != null) {
            latitude = location!!.latitude
        }
        return latitude
    }

    internal fun getLongitude(): Double {
        if (location != null) {
            longitude = location!!.longitude
        }
        return longitude
    }

    internal fun getCity(): String {
        try {
            //getting city name
            val geocoder = Geocoder(mContext)
            val addressList = geocoder.getFromLocation(location!!.latitude, location!!.longitude, 1)
            if (addressList != null && addressList.size > 0) {
                city = addressList[0].locality
            }

            Constant.CITY_NAME = city

            val str = StringBuilder()
            for (i in 0..addressList[0].maxAddressLineIndex) {
                str.append(addressList[0].getAddressLine(i)).append(", ")
            }
            val finalAddressLine = str.substring(0, str.lastIndexOf(","))

            Constant.addressLine = finalAddressLine
        } catch (ex: Exception) {
            Log.d("arsalan", "Exception: ${ex.message}")
        }

        return city
    }

    override fun onLocationChanged(arg0: Location) {
        // TODO Auto-generated method stub
    }

    override fun onProviderDisabled(arg0: String) {
        // TODO Auto-generated method stub
    }

    override fun onProviderEnabled(arg0: String) {
        // TODO Auto-generated method stub
    }

    override fun onStatusChanged(arg0: String, arg1: Int, arg2: Bundle) {
        // TODO Auto-generated method stub
    }

    companion object {
        private val MIN_DISTANCE_CHANGE_FOR_UPDATES: Long = 10 // 10 meters
        private val MIN_TIME_BW_UPDATES = (1000 * 60).toLong() // 1 minute
    }
}

