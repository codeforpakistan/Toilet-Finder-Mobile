package com.watsoncell.publictoiletfinder.fragments


import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.ProgressBar
import com.watsoncell.publictoiletfinder.Interface.SearchToiletListener
import com.watsoncell.publictoiletfinder.MainActivity
import com.watsoncell.publictoiletfinder.R
import com.watsoncell.publictoiletfinder.adapter.SearchCityToiletListAdapter
import com.watsoncell.publictoiletfinder.models.NearByToiletDTO
import com.watsoncell.publictoiletfinder.models.NearByToiletResponse
import com.watsoncell.publictoiletfinder.retrofit.Common
import com.watsoncell.publictoiletfinder.retrofit.IToiletFinderApi
import com.watsoncell.publictoiletfinder.utils.Constant
import com.watsoncell.publictoiletfinder.utils.GPSTracker
import com.watsoncell.publictoiletfinder.utils.InternetConnection
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class FilterToiletResultFragment : Fragment(), SearchToiletListener {


    private lateinit var recyclerView: RecyclerView
    private lateinit var progressBar: ProgressBar
    private lateinit var layoutNoToiletFound: LinearLayout

    private lateinit var adapter: SearchCityToiletListAdapter
    var filterToiletList: ArrayList<NearByToiletDTO> = ArrayList()

    private lateinit var mService: IToiletFinderApi


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_filter_toilet_result, container, false)

        mService = Common.getApi()

        layoutNoToiletFound = view.findViewById(R.id.layoutNoToiletFound)
        recyclerView = view.findViewById(R.id.recyclerViewFilterToilet)
        progressBar = view.findViewById(R.id.progressBarFilter)

        recyclerView.setHasFixedSize(true)
        recyclerView.layoutManager = LinearLayoutManager(activity!!)

        val bundle = arguments

        val gender = bundle!!["gender"] as String
        val accessibility = bundle["accessibility"] as String
        val distance = bundle["distance"] as Int

        if (InternetConnection.isNetworkAvailable(activity!!))
            getFilterToiletList(gender, accessibility, distance)
        else
            Constant.showNoInternetConnectionDialog(activity!!)

        return view
    }

    private fun getFilterToiletList(gender: String, accessibility: String, distance: Int) {

        progressBar.visibility = View.VISIBLE

        val gpsTracker = GPSTracker(activity!!)
        val latitude = gpsTracker.getLatitude()
        val longitude = gpsTracker.getLongitude()
        Log.d("arsalan", "lat: $latitude, long: $longitude")

        mService.getToiletFilterResult(
            gender, accessibility,
            distance,
            Constant.latitude,
            Constant.longitude
        ).enqueue(object : Callback<NearByToiletResponse> {
            override fun onResponse(
                call: Call<NearByToiletResponse>,
                response: Response<NearByToiletResponse>
            ) {
                if (response.body() != null && response.isSuccessful) {

                    val toiletResponse = response.body()

                    if (toiletResponse!!.data!!.isNotEmpty()) {
                        recyclerView.visibility = View.VISIBLE
                        layoutNoToiletFound.visibility = View.GONE

                        //...
                        filterToiletList = response.body()!!.data!!

                        if (filterToiletList.size > 0) {
                            adapter = SearchCityToiletListAdapter(
                                activity!!,
                                filterToiletList,
                                this@FilterToiletResultFragment
                            )
                            recyclerView.adapter = adapter
                        } else {
                            recyclerView.visibility = View.GONE
                            layoutNoToiletFound.visibility = View.VISIBLE
                        }

/*

                        for(i in 0 until toiletResponse.data!!.size){
                            if(toiletResponse.data!![i].distance.equals(distance)){
                                filterToiletList.add(toiletResponse.data!![i])
                            }
                        }
                        if(filterToiletList.size > 0){
                            adapter = SearchCityToiletListAdapter(activity!!, filterToiletList,this@FilterToiletResultFragment)
                            recyclerView.adapter = adapter
                        }
                        else{
                            recyclerView.visibility = View.GONE
                            layoutNoToiletFound.visibility = View.VISIBLE
                        }
*/

                    } else {
                        recyclerView.visibility = View.GONE
                        layoutNoToiletFound.visibility = View.VISIBLE
                    }


                }
                progressBar.visibility = View.GONE

            }

            override fun onFailure(call: Call<NearByToiletResponse>, t: Throwable) {
                progressBar.visibility = View.GONE
                Log.d("arsalan", "error on server side: ${t.message}")
            }
        })
    }


    //search city toilet list adapter, view click listener -> going to toilet detail fragment ->in order to display search toilet detail
    override fun searchToiletViewClickListener(position: Int) {
        Constant.nearByToiletDetail = filterToiletList[position]
        (activity as MainActivity).replaceFragment(ToiletDetailFragment())
    }

    override fun noSearchedToiletIsFound(isNotEmpty: Boolean) {
    }


    override fun onStart() {
        super.onStart()
        (activity as AppCompatActivity).supportActionBar!!.show()
        (activity as AppCompatActivity).supportActionBar!!.title =
            getString(R.string.str_filter_a_toilet)

    }

    override fun onStop() {
        super.onStop()
        (activity as AppCompatActivity).supportActionBar!!.hide()
        (activity as AppCompatActivity).supportActionBar!!.title = ""
    }

}
