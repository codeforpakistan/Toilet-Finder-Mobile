package com.watsoncell.toiletfinder

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.watsoncell.toiletfinder.utils.Constant

class CustomInfoWindowAdapter(ctx: Context) : GoogleMap.InfoWindowAdapter {
    private var context: Context = ctx

    @SuppressLint("SetTextI18n")
    override fun getInfoContents(marker: Marker?): View {

        val view = LayoutInflater.from(context).inflate(R.layout.layout_marker_info_window, null)

        val tvTitle = view.findViewById<TextView>(R.id.tvMarkerTitle)
        val tvDistance = view.findViewById<TextView>(R.id.tvToiletAvailability)
        val tvRatingAverage = view.findViewById<TextView>(R.id.tvRatingAverage)
        val btnGetDirection = view.findViewById<Button>(R.id.btnGetDirections)
        val layoutRating = view.findViewById<LinearLayout>(R.id.layoutRating)

        if (marker!!.position.equals(LatLng(Constant.latitude, Constant.longitude))) {
            btnGetDirection.visibility = View.GONE
            //tvRatingAverage.visibility = View.GONE
            tvDistance.visibility = View.GONE
            layoutRating.visibility = View.GONE

        }
        if (Constant.toiletAverageRating.isNaN())
            layoutRating.visibility = View.GONE

        tvDistance.text = "Verified - ${Constant.toiletAvailability}"
        tvRatingAverage.text = Constant.toiletAverageRating.toString()
        tvTitle.text = marker.title


        return view
    }

    override fun getInfoWindow(p0: Marker?): View? {
        return null
    }


}
