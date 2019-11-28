package com.watsoncell.publictoiletfinder.fragments


import android.app.Activity.RESULT_OK
import android.content.ActivityNotFoundException
import android.content.Intent
import android.os.Bundle
import android.speech.RecognizerIntent
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
import com.watsoncell.publictoiletfinder.utils.toast
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.*
import kotlin.collections.ArrayList


class SearchCurrentCityToilets : Fragment(), SearchToiletListener {

    private lateinit var imgClearSearch: ImageView
    private lateinit var editTextSearch: EditText
    private lateinit var btnAdvanceSearch: Button
    private lateinit var tvNoToiletFound: TextView
    private lateinit var imgSpeechToText: ImageView

    private lateinit var adapter: SearchCityToiletListAdapter

    private lateinit var layoutNoToiletFound: LinearLayout

    private lateinit var progressBar: ProgressBar
    private val mRadius = 10

    val mServices: IToiletFinderApi = Common.getApi()
    private lateinit var gpsTracker: GPSTracker
    private lateinit var recyclerView: RecyclerView

    private lateinit var spinner: Spinner
    private var mCurrentCity = ""

    val REQUEST_CODE_SPEECH_INPUT = 34

    var currentCityToiletList: ArrayList<NearByToiletDTO> = ArrayList()
    var spinnerCityList: Array<String>? = null
    private var isCityFound = false


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_search_current_city_toilets, container, false)

        gpsTracker = GPSTracker(activity!!)

        spinnerCityList = context!!.resources.getStringArray(R.array.city_names_english)

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

            if (str.isNotEmpty())
                adapter.filter.filter(str)

        }

        override fun afterTextChanged(s: Editable?) {

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

        imgSpeechToText = view.findViewById(R.id.imgSpeechToText)

        editTextSearch.addTextChangedListener(textTextWatcher)


        spinner = view.findViewById(R.id.spinnerCity)

        imgSpeechToText.setOnClickListener {
            promptSpeechInput()
        }

        //search city in english only b/c the city name in db is in english only
        val cityNames = context!!.resources.getStringArray(R.array.city_names_english)

        spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(parent: AdapterView<*>?) {

            }

            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                mCurrentCity = cityNames[position]
                if ((mCurrentCity != "Select a city") or !mCurrentCity.equals("شہر کے لحاظ سے تلاش کریں")
                    or !mCurrentCity.equals("د یوه ښار لخوا لټون") && position != 0
                ) {
                    if (editTextSearch.text.toString().isNotEmpty())
                        editTextSearch.setText("")

                    if (InternetConnection.isNetworkAvailable(activity!!))
                        fetchToiletListAccordingToCity(mCurrentCity)
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
            adapter.filter.filter("")
        }

        recyclerView = view.findViewById<RecyclerView>(R.id.recyclerViewSearchToilet)
        recyclerView.hasFixedSize()
        recyclerView.layoutManager = LinearLayoutManager(activity)


        //if fragment is re-lunch again and spinner item is selected then again fetch current city toilet
        if (mCurrentCity.isNotEmpty() && mCurrentCity != "Select a city" && (mCurrentCity != "شہر کے لحاظ سے تلاش کریں")
            or (mCurrentCity != "د یوه ښار لخوا لټون")
        ) {

            if (InternetConnection.isNetworkAvailable(activity!!))
                fetchToiletListAccordingToCity(mCurrentCity)
            else
                Constant.showNoInternetConnectionDialog(activity!!)
        }
        //fetch current city toilet list within 10km radius
        else if (InternetConnection.isNetworkAvailable(activity!!))
            fetchCurrentCityToiletList()
        else
            Constant.showNoInternetConnectionDialog(activity!!)
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
                            currentCityToiletList = nearByToiletResponse!!.data!!

                            if (currentCityToiletList.size == 0) {
                                recyclerView.visibility = View.GONE
                                layoutNoToiletFound.visibility = View.VISIBLE
                                tvNoToiletFound.text =
                                    context!!.getString(R.string.str_no_toilet_found_in_10_km)
                            } else {
                                recyclerView.visibility = View.VISIBLE
                                layoutNoToiletFound.visibility = View.GONE
                                adapter = SearchCityToiletListAdapter(
                                    activity!!,
                                    currentCityToiletList,
                                    this@SearchCurrentCityToilets
                                )
                                recyclerView.adapter = adapter
                            }

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

        currentCityToiletList.clear()

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
                        currentCityToiletList = nearByToiletResponse!!.data!!

                        if (currentCityToiletList.size == 0) {
                            recyclerView.visibility = View.GONE
                            layoutNoToiletFound.visibility = View.VISIBLE
                            tvNoToiletFound.text =
                                context!!.getString(R.string.str_no_search_result)
                        } else {
                            recyclerView.visibility = View.VISIBLE
                            layoutNoToiletFound.visibility = View.GONE

                            adapter = SearchCityToiletListAdapter(
                                activity!!,
                                currentCityToiletList,
                                this@SearchCurrentCityToilets
                            )
                            recyclerView.adapter = adapter
                        }

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
        if (editTextSearch.text.toString().isNotEmpty())
            editTextSearch.setText("")
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

    //opening speech to text dialog
    private fun promptSpeechInput() {
        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH)
        intent.putExtra(
            RecognizerIntent.EXTRA_LANGUAGE_MODEL,
            RecognizerIntent.LANGUAGE_MODEL_FREE_FORM
        )
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault())
        intent.putExtra(
            RecognizerIntent.EXTRA_PROMPT,
            getString(R.string.speech_prompt)
        )
        try {
            startActivityForResult(intent, REQUEST_CODE_SPEECH_INPUT)
        } catch (a: ActivityNotFoundException) {
            Toast.makeText(
                activity,
                getString(R.string.speech_not_supported),
                Toast.LENGTH_SHORT
            ).show()
        }

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        Log.d("arsalan", "request: $REQUEST_CODE_SPEECH_INPUT")

        if (requestCode == REQUEST_CODE_SPEECH_INPUT) {
            if (resultCode == RESULT_OK && null != data) {

                val result: ArrayList<String> = data
                    .getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
                if (result.size > 0) {
                    editTextSearch.setText(result.get(0))
                    editTextSearch.setSelection(0, editTextSearch.text.length)
                    filterSpinnerCity(result.get(0))
                }
            }
        }

    }

    //filter city and select spinner to current city as search by user
    private fun filterSpinnerCity(city: String) {
        for (i in 0 until spinnerCityList!!.size) {
            val strCity = spinnerCityList!![i]
            if (city == strCity) {
                spinner.setSelection(i)
            }
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
