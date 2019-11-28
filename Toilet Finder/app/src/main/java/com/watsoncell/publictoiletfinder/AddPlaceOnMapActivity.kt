package com.watsoncell.publictoiletfinder

import android.location.Geocoder
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.View
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.watsoncell.publictoiletfinder.utils.Constant
import kotlinx.android.synthetic.main.activity_add_place_on_map.*

class AddPlaceOnMapActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var mMap: GoogleMap
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_place_on_map)


        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.mapAddToilet) as SupportMapFragment
        mapFragment.getMapAsync(this)


        btnAddPlace.text = getString(R.string.str_add_place)
    }

    override fun onMapReady(googleMap: GoogleMap?) {
        mMap = googleMap!!

        mMap.moveCamera(CameraUpdateFactory.newLatLng(LatLng(Constant.latitude, Constant.longitude)))
        mMap.animateCamera(CameraUpdateFactory.zoomTo(12F))
        mMap.uiSettings.isZoomControlsEnabled = true

        mMap.setOnCameraIdleListener {
            //get latlng at the center by calling
            val midLatLng = mMap.getCameraPosition().target

            Constant.midLatLng = midLatLng

            try {
                //getting city name
                val geocoder = Geocoder(this)
                val addressList = geocoder.getFromLocation(midLatLng.latitude, midLatLng.longitude, 1)
                if (addressList != null && addressList.size > 0) {
                    Constant.CITY_NAME = addressList[0].locality

                    Constant.ADDRESS_LINE = addressList[0].getAddressLine(0)
                    Log.d("arsalan","address line: ${addressList[0].getAddressLine(0)}")
                }
            } catch (ex: Exception) {
                Log.d("arsalan", "Exception: ${ex.message}")
            }
        }
    }

    //adding new Place
    fun btnAddNewPlace(view: View) {
        finish()
    }

}
