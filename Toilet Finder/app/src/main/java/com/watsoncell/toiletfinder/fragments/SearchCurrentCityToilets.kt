package com.watsoncell.toiletfinder.fragments


import android.inputmethodservice.KeyboardView
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import com.awesomedialog.blennersilva.awesomedialoglibrary.AwesomeProgressDialog
import com.watsoncell.toiletfinder.Interface.SearchToiletListener
import com.watsoncell.toiletfinder.MainActivity
import com.watsoncell.toiletfinder.R
import com.watsoncell.toiletfinder.adapter.SearchCityToiletListAdapter
import com.watsoncell.toiletfinder.models.NearByToiletResponse
import com.watsoncell.toiletfinder.retrofit.Common
import com.watsoncell.toiletfinder.retrofit.IToiletFinderApi
import com.watsoncell.toiletfinder.utils.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response


class SearchCurrentCityToilets : Fragment(), SearchToiletListener {

    private lateinit var imgClearSearch: ImageView
    private lateinit var editTextSearch: EditText
    private lateinit var btnAdvanceSearch: Button
    private lateinit var tvNoToiletFound: TextView

    private lateinit var adapter: SearchCityToiletListAdapter

    private lateinit var layoutNoToiletFound: LinearLayout

    private lateinit var progressBar: ProgressBar
    private val mRadius = 10

    val mServices: IToiletFinderApi = Common.getApi()
    private lateinit var gpsTracker: GPSTracker
    private lateinit var recyclerView: RecyclerView

    private lateinit var spinner: Spinner

    private var mCustomKeyboard: CustomKeyboard? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_search_current_city_toilets, container, false)

        gpsTracker = GPSTracker(activity!!)

        initViews(view)

        return view
    }

    //Search Edit Text change listener
    val textTextWatcher = object : TextWatcher {
        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

        }

        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            val str = s.toString()
            if (str.isEmpty())
                imgClearSearch.visibility = View.GONE
            else
                imgClearSearch.visibility = View.VISIBLE

            adapter.filter.filter(str)
        }

        override fun afterTextChanged(s: Editable?) {

        }

    }

    fun initializeKeyboard(editText: EditText) {
        try {
            mCustomKeyboard!!.registerEditText(editText)
        } catch (ex: Exception) {
        }
    }

    private fun initViews(view: View?) {

        //  searchView = view.findViewById(R.id.searchView)
        imgClearSearch = view!!.findViewById(R.id.imgClearSearch)

        layoutNoToiletFound = view.findViewById(R.id.layoutNoToiletFound)

        progressBar = view.findViewById(R.id.progressBarSearch)

        editTextSearch = view.findViewById(R.id.editTextSearch)

        btnAdvanceSearch = view.findViewById(R.id.btnAdvanceSearch)

        tvNoToiletFound = view.findViewById(R.id.tvNoToiletFound)

        editTextSearch.addTextChangedListener(textTextWatcher)

        spinner = view.findViewById(R.id.spinnerCity)

        spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(parent: AdapterView<*>?) {

            }

            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {

                val city: String = spinner.selectedItem.toString()

                if (!city.equals("Select a city") or !city.equals("شہر کے لحاظ سے تلاش کریں")
                    or !city.equals("د یوه ښار لخوا لټون") && position != 0
                ) {
                    Log.d("arsalan", "item: ${spinner.selectedItem}")
                    if (InternetConnection.isNetworkAvailable(activity!!))
                        fetchToiletListAccordingToCity(city)
                    else
                        Constant.showNoInternetConnectionDialog(activity!!)

                }
            }

        }

        btnAdvanceSearch.setOnClickListener {
            (activity as MainActivity).replaceFragment(FilterToiletFragment())
        }

        imgClearSearch.setOnClickListener {
            editTextSearch.setText("")
        }


        val preference = AppPreference.getInstance(context!!)

        //if selected language is urdu then change keyboard to urdu
        if (preference.getLanguage().equals(Constant.LANGUAGE_URDU)) {
            mCustomKeyboard = CustomKeyboard(
                activity,
                view.findViewById(R.id.keyboardview) as KeyboardView, R.xml.urdu_keyboard
            )
        } else if (preference.getLanguage().equals(Constant.LANGUAGE_PASHTO)) {
            mCustomKeyboard = CustomKeyboard(
                activity,
                view.findViewById(R.id.keyboardview) as KeyboardView, R.xml.pashto_keyboard
            )
        }

        //if selected language is urdu then register the edittext to custom keyboard
        if (!preference.getLanguage().equals(Constant.DEFAULT_LANGUAGE)) {
            initializeKeyboard(editTextSearch)
        }


        recyclerView = view.findViewById<RecyclerView>(R.id.recyclerViewSearchToilet)
        recyclerView.hasFixedSize()
        recyclerView.layoutManager = LinearLayoutManager(activity)


        //if fragment is re-lunch again and spinner item is selected then again fetch current city toilet
        val city: String = spinner.selectedItem.toString()
        Log.d("arsalan","list size: ${Constant.currentCityToiletList.size}")
        if (Constant.currentCityToiletList.size == 0 && (city != "Select a city") or (city != "شہر کے لحاظ سے تلاش کریں")
            or (city != "د یوه ښار لخوا لټون")
        ) {
            Log.d("arsalan", "item: ${spinner.selectedItem}")
            if (InternetConnection.isNetworkAvailable(activity!!))
                fetchToiletListAccordingToCity(city)
            else
                Constant.showNoInternetConnectionDialog(activity!!)

        }

        //if data is not fetched in service then again fetch it here
        else if (Constant.currentCityToiletList.size > 0) {
            adapter = SearchCityToiletListAdapter(activity!!, Constant.currentCityToiletList, this)
            recyclerView.adapter = adapter
        } else {
            if (InternetConnection.isNetworkAvailable(activity!!))
                fetchCurrentCityToiletList()
            else
                Constant.showNoInternetConnectionDialog(activity!!)
        }
    }

    //fetch current city toilet list
    private fun fetchCurrentCityToiletList() {

        progressBar.visibility = View.VISIBLE

        gpsTracker.getLatitude().let { latitude ->
            mServices.getNearByToilets(
                latitude,
                gpsTracker.getLongitude(),
                mRadius
            )
                .enqueue(object : Callback<NearByToiletResponse> {

                    override fun onResponse(
                        call: Call<NearByToiletResponse>, response: Response<NearByToiletResponse>
                    ) {
                        progressBar.visibility = View.GONE

                        if (response.isSuccessful) {
                            val nearByToiletResponse = response.body()
                            Constant.currentCityToiletList = nearByToiletResponse!!.data!!

                            if (Constant.currentCityToiletList.size == 0) {
                                recyclerView.visibility = View.GONE
                                layoutNoToiletFound.visibility = View.VISIBLE
                                tvNoToiletFound.text =
                                    context!!.getString(R.string.str_no_toilet_found_in_10_km)
                            } else {
                                recyclerView.visibility = View.VISIBLE
                                layoutNoToiletFound.visibility = View.GONE
                            }
                            adapter = SearchCityToiletListAdapter(
                                activity!!,
                                Constant.currentCityToiletList,
                                this@SearchCurrentCityToilets
                            )
                            recyclerView.adapter = adapter
                        } else
                            activity!!.toast("server side error: ${response.errorBody().toString()}")
                    }

                    override fun onFailure(call: Call<NearByToiletResponse>, t: Throwable) {
                        activity!!.toast("error: ${t.message}")
                        progressBar.visibility = View.GONE
                    }
                })
        }
    }

    //fetch current city toilet list
    private fun fetchToiletListAccordingToCity(city: String) {

        val dialog = AwesomeProgressDialog(activity)
            .setTitle(R.string.app_name)
            .setMessage(getString(R.string.str_searching_toilet))
            .setColoredCircle(R.color.dialogInfoBackgroundColor)
            .setDialogIconAndColor(R.drawable.ic_dialog_info, R.color.white)
            .setCancelable(true)

        dialog.show()

        Constant.currentCityToiletList.clear()

        mServices.getCurrentCityToiletList(
            Constant.latitude, Constant.longitude, city
        )
            .enqueue(object : Callback<NearByToiletResponse> {

                override fun onResponse(
                    call: Call<NearByToiletResponse>, response: Response<NearByToiletResponse>
                ) {
                    dialog.hide()
                    if (response.isSuccessful) {
                        val nearByToiletResponse = response.body()
                        Constant.currentCityToiletList = nearByToiletResponse!!.data!!

                        if (Constant.currentCityToiletList.size == 0) {
                            recyclerView.visibility = View.GONE
                            layoutNoToiletFound.visibility = View.VISIBLE
                            tvNoToiletFound.text =
                                context!!.getString(R.string.str_no_search_result)
                        } else {
                            recyclerView.visibility = View.VISIBLE
                            layoutNoToiletFound.visibility = View.GONE
                        }
                        adapter = SearchCityToiletListAdapter(
                            activity!!,
                            Constant.currentCityToiletList,
                            this@SearchCurrentCityToilets
                        )
                        recyclerView.adapter = adapter
                        //adapter.notifyDataSetChanged()

                    } else {
                        activity!!.toast("server side error: ${response.errorBody().toString()}")
                        Log.d("arsalan", "error response: ${response.errorBody().toString()}")
                    }
                }

                override fun onFailure(call: Call<NearByToiletResponse>, t: Throwable) {
                    activity!!.toast("error: ${t.message}")
                    dialog.hide()
                    Log.d("arsalan", "error server: ${t.message}")
                }
            })
    }


    override fun searchToiletViewClickListener(position: Int) {
        (activity as MainActivity).replaceFragment(ToiletDetailFragment())
    }

    override fun noSearchedToiletIsFound(isNotEmpty: Boolean) {
        if (isNotEmpty) {
            recyclerView.visibility = View.VISIBLE
            layoutNoToiletFound.visibility = View.GONE
        } else {
            recyclerView.visibility = View.GONE
            layoutNoToiletFound.visibility = View.VISIBLE
            tvNoToiletFound.text = context!!.getString(R.string.str_no_search_result)
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
}
