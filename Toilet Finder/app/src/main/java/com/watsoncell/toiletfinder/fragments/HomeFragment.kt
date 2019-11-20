package com.watsoncell.toiletfinder.fragments


import android.content.Context
import android.content.Intent
import android.location.LocationManager
import android.os.Bundle
import android.provider.Settings
import android.support.v4.app.Fragment
import android.support.v4.widget.SwipeRefreshLayout
import android.support.v7.app.AlertDialog
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import com.watsoncell.toiletfinder.MainActivity
import com.watsoncell.toiletfinder.R
import com.watsoncell.toiletfinder.adapter.NearbyToiletAdapter
import com.watsoncell.toiletfinder.dialog.ExploreFragmentDialog
import com.watsoncell.toiletfinder.dialog.NavigateFragmentDialog
import com.watsoncell.toiletfinder.dialog.UserRateFragmentDialog
import com.watsoncell.toiletfinder.models.NearByToiletDTO
import com.watsoncell.toiletfinder.models.NearByToiletResponse
import com.watsoncell.toiletfinder.retrofit.Common
import com.watsoncell.toiletfinder.retrofit.IToiletFinderApi
import com.watsoncell.toiletfinder.services.FetchToiletListService
import com.watsoncell.toiletfinder.utils.Constant
import com.watsoncell.toiletfinder.utils.GPSTracker
import com.watsoncell.toiletfinder.utils.InternetConnection
import com.watsoncell.toiletfinder.utils.toast
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response


class HomeFragment : Fragment(), NearbyToiletAdapter.NearbyToiletInterface {

    private lateinit var layoutSearchToilet: LinearLayout
    private lateinit var layoutNoToiletFoundIn25Km: LinearLayout
    private lateinit var recyclerView: RecyclerView
    private lateinit var nearbyToiletList: ArrayList<NearByToiletDTO>
    private lateinit var tvAddNewToilet: TextView
    private lateinit var mServices: IToiletFinderApi
    private var mLatitude: Double = 0.0
    private var mLongitude: Double = 0.0
    private var mRadius = 10

    private lateinit var progressBar: ProgressBar

    private lateinit var adapter: NearbyToiletAdapter

    var isRefreshed = false

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_home, container, false)

        mServices = Common.getApi()

        //request location enable
        val manager =
            activity!!.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        if (!manager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            buildAlertMessageNoGps()
        }

        initViews(view)


        if (InternetConnection.isNetworkAvailable(activity!!)) {
            //getting latitude and longitude
            //GPS Tracker
            val gpsTracker = GPSTracker(activity!!)

            gpsTracker.getLatitude().let {
                mLatitude = it
            }

            gpsTracker.getLongitude().let {
                mLongitude = it
            }

/*            if (Constant.nearbyToiletList.size > 0) {
                adapter = NearbyToiletAdapter(activity!!, this@HomeFragment, Constant.nearbyToiletList)
                recyclerView.adapter = adapter
            } else
                getNearByToiletList()*/

            getNearByToiletList()

            //starting service in order to get current city toilet list and all database toilet list
            val intentService = Intent(activity!!, FetchToiletListService::class.java)
            intentService.putExtra("latitude", mLatitude)
            intentService.putExtra("longitude", mLongitude)
            intentService.putExtra("city", gpsTracker.getCity())
            activity!!.startService(intentService)
        } else
            Constant.showNoInternetConnectionDialog(activity!!)


        return view
    }

    //initializing views
    private fun initViews(view: View?) {
        layoutNoToiletFoundIn25Km = view!!.findViewById(R.id.layoutNoToiletIn25Km)
        layoutSearchToilet = view.findViewById(R.id.layoutSearchToilet)
        tvAddNewToilet = view.findViewById(R.id.tvNoToiletIn25Km)
        layoutSearchToilet.setOnClickListener {

            //hiding keyboard
            val imm =
                activity!!.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.hideSoftInputFromWindow(view.windowToken, 0)

            (activity as MainActivity).replaceFragment(SearchCurrentCityToilets())
        }

        nearbyToiletList = ArrayList()

        layoutSearchToilet = view.findViewById(R.id.layoutSearchToilet)

        progressBar = view.findViewById(R.id.progressNearByToilet)

        recyclerView = view.findViewById(R.id.recyclerView_nearby)
        recyclerView.hasFixedSize()
        recyclerView.layoutManager =
            LinearLayoutManager(activity!!, LinearLayoutManager.HORIZONTAL, false)

        val layoutExplore = view.findViewById<LinearLayout>(R.id.layoutExplore)
        val layoutNavigate = view.findViewById<LinearLayout>(R.id.layoutNavigator)
        val layoutUserRate = view.findViewById<LinearLayout>(R.id.layoutUserRate)

        layoutExplore.setOnClickListener {
            val dialog = ExploreFragmentDialog()
            dialog.show(childFragmentManager, dialog.tag)
        }

        layoutNavigate.setOnClickListener {
            val dialog = NavigateFragmentDialog()
            dialog.show(childFragmentManager, dialog.tag)
        }

        layoutUserRate.setOnClickListener {
            val dialog = UserRateFragmentDialog()
            dialog.show(childFragmentManager, dialog.tag)
        }

        tvAddNewToilet.setOnClickListener {
            showAddNewToiletOptionDialog()
        }

        val swipeRefreshLayout = view.findViewById<SwipeRefreshLayout>(R.id.layoutSwipeRefresh);
        swipeRefreshLayout.setOnRefreshListener {
            isRefreshed = true
            getNearByToiletList()
        }
    }

    //Near by toilet adapter itemView Click listener
    override fun OnNearByToiletClickListener(position: Int) {
        (activity as MainActivity).replaceFragment(ToiletDetailFragment())
    }

    //getting nearby toilet list
    private fun getNearByToiletList() {
        if (!isRefreshed)
            progressBar.visibility = View.VISIBLE

        mServices.getNearByToilets(
            mLatitude,
            mLongitude,
            mRadius
        ).enqueue(object : Callback<NearByToiletResponse> {
            override fun onResponse(
                call: Call<NearByToiletResponse>,
                response: Response<NearByToiletResponse>
            ) {
                if (response.isSuccessful) {
                    parseNearByToilet(response)
                }
            }

            override fun onFailure(call: Call<NearByToiletResponse>, t: Throwable) {
                progressBar.visibility = View.GONE
                Log.d("arsalan", "error on server side: ${t.message}")
            }
        })


    }

    //parsing nearby toilets and passing data to adapter
    private fun parseNearByToilet(response: Response<NearByToiletResponse>) {
        progressBar.visibility = View.GONE
        var nearByToiletResponse: NearByToiletResponse? = null
        if (response.body() != null && response.isSuccessful) {
            nearByToiletResponse = response.body()!!
            if (nearByToiletResponse.success) {
                nearbyToiletList = nearByToiletResponse.data!!

                if (nearbyToiletList.size > 0) {

                    Constant.nearbyToiletList = nearbyToiletList

                    adapter = NearbyToiletAdapter(activity!!, this@HomeFragment, nearbyToiletList)
                    recyclerView.adapter = adapter

                } else {
                    //if nearby toilet is not found in 5 radius then increment radius and again fetch data

                    mRadius += 5

                    //if radius is equal to 25 km then send no more request to server
                    if (mRadius >= 25) {
                        recyclerView.visibility = View.GONE
                        layoutNoToiletFoundIn25Km.visibility = View.VISIBLE
                    } else
                        getNearByToiletList()

                    Log.d("arsalan", "radius: $mRadius")
                }

            }
        } else
            activity!!.toast("A problem occur on server side")

    }

    fun buildAlertMessageNoGps() {
        try {
            val builder = AlertDialog.Builder(activity!!)
            builder.setTitle(getString(R.string.str_location_title))
            builder.setMessage(getString(R.string.str_location_msg))
                .setCancelable(false)
                .setPositiveButton(
                    getString(R.string.str_setting)
                ) { dialog, id ->
                    startActivity(Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS))
                }
                .setNegativeButton(
                    getString(R.string.str_cancel)
                ) { dialog, id -> dialog.cancel() }
            val alert = builder.create()
            alert.show()
        } catch (e: Exception) {
            e.printStackTrace()
        }

    }


    /*  Showing dialog -> Giving Following Option to user while adding new Toilet
        1. Use user current Coordinate while adding new Toilet
        2. Choose coordinate from Map while adding new Toilet
     */
    private fun showAddNewToiletOptionDialog() {
        val alertDialog = AlertDialog.Builder(activity!!)
        val view =
            LayoutInflater.from(activity).inflate(R.layout.layout_new_toilet_option_dialog, null)

        val btnUserCurrentLocation = view.findViewById<Button>(R.id.btnUserCurrentLocation)
        val btnCoordinateFromMap = view.findViewById<Button>(R.id.btnCoordinateFromMap)

        alertDialog.setView(view)
        val dialog = alertDialog.create()

        btnUserCurrentLocation.setOnClickListener {
            dialog.dismiss()
            (activity as MainActivity).replaceFragment(AddToiletUseCurrentLocation())
        }

        btnCoordinateFromMap.setOnClickListener {
            dialog.dismiss()
            (activity as MainActivity).replaceFragment(AddToiletUsingMapCoordinate())
        }

        dialog.show()
    }

}
