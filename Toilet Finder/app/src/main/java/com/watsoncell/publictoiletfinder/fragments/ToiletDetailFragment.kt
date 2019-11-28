package com.watsoncell.publictoiletfinder.fragments


import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v4.view.ViewPager
import android.support.v7.app.AppCompatActivity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.*
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.watsoncell.publictoiletfinder.R
import com.watsoncell.publictoiletfinder.ReviewToiletActivity
import com.watsoncell.publictoiletfinder.adapter.UserReviewPagerAdapter
import com.watsoncell.publictoiletfinder.models.NearByToiletDTO
import com.watsoncell.publictoiletfinder.models.UserReviewModel
import com.watsoncell.publictoiletfinder.utils.CalculateToiletRating
import com.watsoncell.publictoiletfinder.utils.Constant
import java.text.DecimalFormat


class ToiletDetailFragment : Fragment(), OnMapReadyCallback {

    private lateinit var leftArrow: ImageView
    private lateinit var rightArrow: ImageView
    private lateinit var mMap: GoogleMap
    private lateinit var viewPager: ViewPager
    private lateinit var userReviewList: ArrayList<UserReviewModel>
    private lateinit var currentToiletLatLng: LatLng

    private lateinit var tvToiletPlaceName: TextView
    private lateinit var tvToiletAddress: TextView
    private lateinit var tvToiletNotReviewed: TextView
    private lateinit var tvToiletDistance: TextView
    private lateinit var tvToiletAvailableFor: TextView
    private lateinit var tvToiletAverageRating: TextView
    private lateinit var tvToiletTotalReviews: TextView
    private lateinit var imgNearByToilet: ImageView

    private lateinit var btnReviewToilet: Button

    private lateinit var layoutPhysicalChallenge: LinearLayout
    private lateinit var layoutHandWash: LinearLayout
    private lateinit var layoutSoap: LinearLayout
    private lateinit var layoutParking: LinearLayout
    private lateinit var layoutSanitoryBin: LinearLayout
    private lateinit var layoutPaymentRequired: LinearLayout
    private lateinit var toiletDetail: NearByToiletDTO
    private lateinit var layoutReviewSlider: LinearLayout

    private lateinit var ratingBar: RatingBar

    private val DEFAULT_ZOOM = 10F

    private val decimalFormat = DecimalFormat("#.##")

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_toilet_detail, container, false)

        toiletDetail = Constant.nearByToiletDetail!!


        initViews(view)

        return view
    }

    private fun setWindowFlag(activity: Activity, bits: Int, on: Boolean) {
        val win = activity.getWindow()
        val winParams: WindowManager.LayoutParams = win.getAttributes();
        if (on) {
            winParams.flags = winParams.flags or bits
        } else {
            winParams.flags = winParams.flags and bits.inv()
        }
        win.attributes = winParams
    }

    //initializing views
    private fun initViews(view: View?) {
        viewPager = view!!.findViewById(R.id.viewPager_Review)

        tvToiletAverageRating = view.findViewById(R.id.tvToiletAverageReview)
        tvToiletTotalReviews = view.findViewById(R.id.tvToiletTotalReview)
        btnReviewToilet = view.findViewById(R.id.btnReviewToilet)

        ratingBar = view.findViewById(R.id.rtToiletAverageReview)

        tvToiletPlaceName = view.findViewById(R.id.tvToiletPlace)
        tvToiletAddress = view.findViewById(R.id.tvToiletAddress)
        tvToiletNotReviewed = view.findViewById(R.id.tvToiletNotReviewed)
        tvToiletDistance = view.findViewById(R.id.tvToiletDistance)
        tvToiletAvailableFor = view.findViewById(R.id.tvToiletVerification)
        imgNearByToilet = view.findViewById(R.id.imgNearbyToilet)

        layoutPhysicalChallenge = view.findViewById(R.id.layoutPhysicalAccessibility)
        layoutHandWash = view.findViewById(R.id.layoutHandWash)
        layoutParking = view.findViewById(R.id.layoutParking)
        layoutSanitoryBin = view.findViewById(R.id.layoutDisposalBin)
        layoutSoap = view.findViewById(R.id.layoutSoap)
        layoutPaymentRequired = view.findViewById(R.id.layoutPaymentRequired)
        layoutReviewSlider = view.findViewById(R.id.layoutReviewSlider)

        leftArrow = view.findViewById(R.id.imgLeftArrow)
        rightArrow = view.findViewById(R.id.imgRightArrow)

        imgNearByToilet.setImageResource(Constant.toiletImageResoureId)

        tvToiletAddress.text = toiletDetail.address
        tvToiletPlaceName.text = toiletDetail.city.capitalize()
        tvToiletAvailableFor.text = toiletDetail.toiletAvailable.capitalize()
        tvToiletDistance.text = "Distance - ${decimalFormat.format(toiletDetail.distance.toDouble())} km"

        val mapFragment = childFragmentManager
            .findFragmentById(R.id.singleToiletMap) as SupportMapFragment
        mapFragment.getMapAsync(this)

        userReviewList = ArrayList()

        //slide user review to left side
        leftArrow.setOnClickListener {
            var tab = viewPager.currentItem
            if (tab > 0) {
                tab--
                viewPager.currentItem = tab
            } else if (tab == 0)
                viewPager.currentItem = tab
        }

        //slide user review to right side
        rightArrow.setOnClickListener {
            var tab = viewPager.currentItem
            if (tab < userReviewList.size) {
                tab++
                viewPager.currentItem = tab
            }
        }

        //displaying and hiding toilet available facility
        visibleOrHideToiletFacility(
            layoutPhysicalChallenge,
            toiletDetail.accessiblePhysicalChallenge
        )
        visibleOrHideToiletFacility(layoutHandWash, toiletDetail.handWash)
        visibleOrHideToiletFacility(layoutParking, toiletDetail.parking)
        visibleOrHideToiletFacility(layoutSanitoryBin, toiletDetail.sanitaryDisposalBin)
        visibleOrHideToiletFacility(layoutSoap, toiletDetail.soap)
        visibleOrHideToiletFacility(layoutPaymentRequired, toiletDetail.paymentRequired)

        //user toilet review
        if (toiletDetail.feedback != null && toiletDetail.feedback!!.size > 0) {

            //hiding and displaying left and right arrow of review slider
            if (toiletDetail.feedback!!.size > 1) {
                leftArrow.visibility = View.VISIBLE
                rightArrow.visibility = View.VISIBLE
            } else {
                leftArrow.visibility = View.GONE
                rightArrow.visibility = View.GONE
            }


            layoutReviewSlider.visibility = View.VISIBLE
            tvToiletNotReviewed.visibility = View.GONE
            userReviewList = toiletDetail.feedback!!

            tvToiletAverageRating.visibility = View.VISIBLE
            tvToiletTotalReviews.visibility = View.VISIBLE
            ratingBar.visibility = View.VISIBLE

            tvToiletAverageRating.text = "${CalculateToiletRating.getAverageRating(userReviewList)}"

            if (userReviewList.size > 1)
                tvToiletTotalReviews.setText("${userReviewList.size} reviews")
            else
                tvToiletTotalReviews.setText("${userReviewList.size} review")

            val userReviewAdapter = UserReviewPagerAdapter(activity!!, userReviewList)
            viewPager.adapter = userReviewAdapter

        } else {
            layoutReviewSlider.visibility = View.GONE
            tvToiletNotReviewed.visibility = View.VISIBLE

            tvToiletAverageRating.visibility = View.GONE
            tvToiletTotalReviews.visibility = View.GONE
            ratingBar.visibility = View.GONE
        }
        //lunching review fragment in order to review toilet
        btnReviewToilet.setOnClickListener {
            val intent = Intent(activity!!, ReviewToiletActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            intent.putExtra("toiletId", toiletDetail.id)
            startActivity(intent)
            //activity!!.finish()
        }

        //launching google build direction
        val btnGetDirection = view.findViewById<Button>(R.id.btnGetDirection)
        btnGetDirection.setOnClickListener {
            val intent = Intent(
                Intent.ACTION_VIEW,
                Uri.parse("http://maps.google.com/maps?saddr=${Constant.latitude},${Constant.longitude}&daddr=${toiletDetail.latitude},${toiletDetail.longitude}")
            )
            startActivity(intent)
        }
    }

    override fun onResume() {
        super.onResume()
        (activity as AppCompatActivity).supportActionBar!!.hide()
    }

    override fun onStop() {
        super.onStop()
        (activity as AppCompatActivity).supportActionBar!!.show()

    }

    //loading map
    override fun onMapReady(googleMap: GoogleMap?) {
        mMap = googleMap!!

        currentToiletLatLng =
            LatLng(toiletDetail.latitude.toDouble(), toiletDetail.longitude.toDouble())

        mMap.addMarker(
            MarkerOptions()
                .position(currentToiletLatLng)
                .icon(
                    BitmapDescriptorFactory.defaultMarker(
                        BitmapDescriptorFactory.HUE_VIOLET
                    )
                )
        )
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(currentToiletLatLng, DEFAULT_ZOOM))
    }


    private fun visibleOrHideToiletFacility(view: View, facilityAvailable: String) {

        if (facilityAvailable == "1")
            view.visibility = View.VISIBLE
        else
            view.visibility = View.GONE

    }
}
