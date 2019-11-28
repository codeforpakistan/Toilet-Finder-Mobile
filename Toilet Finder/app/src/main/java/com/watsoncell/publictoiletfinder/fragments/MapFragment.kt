package com.watsoncell.publictoiletfinder.fragments


import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.watsoncell.publictoiletfinder.CustomInfoWindowAdapter
import com.watsoncell.publictoiletfinder.R
import com.watsoncell.publictoiletfinder.models.NearByToiletDTO
import com.watsoncell.publictoiletfinder.models.NearByToiletResponse
import com.watsoncell.publictoiletfinder.retrofit.Common
import com.watsoncell.publictoiletfinder.retrofit.IToiletFinderApi
import com.watsoncell.publictoiletfinder.utils.CalculateToiletRating
import com.watsoncell.publictoiletfinder.utils.Constant
import com.watsoncell.publictoiletfinder.utils.GPSTracker
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response


class MapFragment : Fragment(), OnMapReadyCallback, GoogleMap.OnMarkerClickListener {


    private lateinit var mMap: GoogleMap
    private var mCurrLocationMarker: Marker? = null

    private val DEFAULT_ZOOM = 12F

    private lateinit var mServices: IToiletFinderApi

    private var mLatitude: Double = 0.0
    private var mLongitude: Double = 0.0

    private var destinationLatitude: Double = 0.0
    private var destinationLongitude: Double = 0.0
    private lateinit var gpsTracker: GPSTracker

    private lateinit var nearbyToiletList: ArrayList<NearByToiletDTO>

    private lateinit var progressBar: ProgressBar

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment

        val view = inflater.inflate(R.layout.fragment_map, container, false)

        progressBar = view.findViewById(R.id.progressBar)

        //getting Retrofit Api
        mServices = Common.getApi()

        val mapFragment = childFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        gpsTracker = GPSTracker(activity!!)

        gpsTracker.getLatitude().let {
            mLatitude = it
        }

        gpsTracker.getLongitude().let {
            mLongitude = it
        }

        return view
    }

    @SuppressLint("MissingPermission")
    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        mMap.isMyLocationEnabled = true
        mMap.uiSettings.isMyLocationButtonEnabled = true

        //custom info window adapter
        val adapter = CustomInfoWindowAdapter(context!!)
        mMap.setInfoWindowAdapter(adapter)

        //marker click listener
        mMap.setOnMarkerClickListener(this)


        if (mCurrLocationMarker != null) {
            mCurrLocationMarker!!.remove()
        }
        //Place current location marker
        val markerOptions = MarkerOptions()
        markerOptions.position(LatLng(mLatitude, mLongitude))
        markerOptions.title("${gpsTracker.getCity()} (You are here)")
        markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_MAGENTA))
        mCurrLocationMarker = mMap.addMarker(markerOptions)
        mMap.moveCamera(
            CameraUpdateFactory.newLatLngZoom(
                LatLng(mLatitude, mLongitude),
                DEFAULT_ZOOM
            )
        )

        getAllToiletList(gpsTracker.getCity())
    }

    private fun getAllToiletList(city: String) {
        //getting all toilet coordinates
        progressBar.visibility = View.VISIBLE
        mServices.getAllToilets().enqueue(object : Callback<NearByToiletResponse> {
            override fun onResponse(
                call: Call<NearByToiletResponse>,
                response: Response<NearByToiletResponse>
            ) {
                if (response.body() != null) {
                    val nearByToiletResponse: NearByToiletResponse = response.body()!!
                    if (nearByToiletResponse.success) {
                        //marker info window click listener
                        mMap.setOnInfoWindowClickListener { marker ->

                            //if marker other than current position marker then it is toilet and review it
                            if (!marker.position.equals(
                                    LatLng(
                                        Constant.latitude,
                                        Constant.longitude
                                    )
                                )
                            ) {
                                //launch google map direction
                                val intent = Intent(
                                    Intent.ACTION_VIEW,
                                    Uri.parse("http://maps.google.com/maps?saddr=${Constant.latitude},${Constant.longitude}&daddr=$destinationLatitude,$destinationLongitude")
                                )
                                startActivity(intent)
                            }
                        }

                        nearbyToiletList = nearByToiletResponse.data!!
                        val bitmapMarker = BitmapFactory.decodeResource(activity!!.resources,R.drawable.ic_toilet_marker)
                        for (i in 0 until nearbyToiletList.size) {
                            val newLatLng = LatLng(
                                nearbyToiletList[i].latitude.toDouble(),
                                nearbyToiletList[i].longitude.toDouble()
                            )


                            val toiletMarker = mMap.addMarker(
                                MarkerOptions()
                                    .position(newLatLng)
                                    .title(nearbyToiletList[i].city)
                                    .icon(
                                        BitmapDescriptorFactory.fromBitmap(bitmapMarker)
                                    )
                            )

                            toiletMarker.tag = i
                        }
                    }
                }
                progressBar.visibility = View.GONE

            }

            override fun onFailure(call: Call<NearByToiletResponse>, t: Throwable) {
                Log.d("arsalan", "all toilet error: ${t.message}")
                progressBar.visibility = View.GONE
            }

        })
    }

    //marker click listener
    override fun onMarkerClick(marker: Marker?): Boolean {

        //if marker tag is not null
        if (marker!!.tag != null) {
            Constant.toiletAvailability = nearbyToiletList[marker.tag as Int].toiletAvailable
            destinationLatitude = nearbyToiletList[marker.tag as Int].latitude.toDouble()
            destinationLongitude = nearbyToiletList[marker.tag as Int].longitude.toDouble()

            if (nearbyToiletList[marker.tag as Int].feedback != null) {
                Constant.toiletAverageRating =
                    CalculateToiletRating.getAverageRating(nearbyToiletList[marker.tag as Int].feedback!!)

                marker.showInfoWindow()
            }

        }
        return true
    }

    override fun onStart() {
        super.onStart()
        (activity as AppCompatActivity).supportActionBar!!.show()
        (activity as AppCompatActivity).supportActionBar!!.title =
            getString(R.string.str_all_toilets)

    }

    override fun onStop() {
        super.onStop()
        (activity as AppCompatActivity).supportActionBar!!.hide()
        (activity as AppCompatActivity).supportActionBar!!.title = ""
    }
}
