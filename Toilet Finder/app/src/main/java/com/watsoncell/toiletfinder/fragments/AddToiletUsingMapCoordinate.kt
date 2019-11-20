package com.watsoncell.toiletfinder.fragments


import android.content.Intent
import android.inputmethodservice.KeyboardView
import android.os.Bundle
import android.support.design.widget.CoordinatorLayout
import android.support.design.widget.Snackbar
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import com.awesomedialog.blennersilva.awesomedialoglibrary.AwesomeErrorDialog
import com.awesomedialog.blennersilva.awesomedialoglibrary.AwesomeProgressDialog
import com.awesomedialog.blennersilva.awesomedialoglibrary.AwesomeSuccessDialog
import com.watsoncell.toiletfinder.AddPlaceOnMapActivity
import com.watsoncell.toiletfinder.Interface.BackButtonInterface
import com.watsoncell.toiletfinder.MainActivity
import com.watsoncell.toiletfinder.R
import com.watsoncell.toiletfinder.models.AddNewToilet
import com.watsoncell.toiletfinder.retrofit.Common
import com.watsoncell.toiletfinder.retrofit.IToiletFinderApi
import com.watsoncell.toiletfinder.utils.*
import kotlinx.android.synthetic.main.fragment_add_toilet_using_map_coordinate.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class AddToiletUsingMapCoordinate : Fragment(), CompoundButton.OnCheckedChangeListener, View.OnClickListener,
    BackButtonInterface {

    private lateinit var btnAddNewToilet: Button
    private lateinit var etToiletLocation: EditText
    private lateinit var etToiletName: EditText
    private lateinit var etToiletAddedBy: EditText
    private lateinit var etToiletAddress : EditText
    private lateinit var categorySpinner: Spinner

    private lateinit var cbAccessiblePhysicalChallenge: CheckBox
    private lateinit var cbHandWashFacility: CheckBox
    private lateinit var cbSoap: CheckBox
    private lateinit var cbParking: CheckBox
    private lateinit var cbSanitaryBin: CheckBox
    private lateinit var cbPaymentRequired: CheckBox

    private lateinit var coordinatorLayout: CoordinatorLayout

    private var gender: String = "0"
    private var accessiblePhysicalChallenge = "0"
    private var parking = "0"
    private var sanitaryDisposalBin = "0"
    private var paymentRequired = "0"
    private var handWashFacility = "0"
    private var soap = "0"


    private lateinit var preference: AppPreference
    private lateinit var mService: IToiletFinderApi

    private lateinit var progressDialog: AwesomeProgressDialog
    private var mCustomKeyboard: CustomKeyboard? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_add_toilet_using_map_coordinate, container, false)

        mService = Common.getApi()
        preference = AppPreference()

        initViews(view)

        (activity as MainActivity).setOnBackButtonListener(this)

        val btnAddLocationFromMap: Button = view.findViewById(R.id.btnAddLocationFromMap)
        btnAddLocationFromMap.setOnClickListener {
            val intent = Intent(context, AddPlaceOnMapActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
            activity!!.startActivity(intent)
        }

        return view
    }

    private fun initViews(view: View?) {
        btnAddNewToilet = view!!.findViewById(R.id.btnAddNewToilet)
        coordinatorLayout = view.findViewById(R.id.coordinatorLayoutAddToilet)
        etToiletAddedBy = view.findViewById(R.id.etToiletAddedBy)
        etToiletLocation = view.findViewById(R.id.etToiletLocation)
        etToiletAddress = view.findViewById(R.id.etToiletAddress)
        etToiletName = view.findViewById(R.id.etToiletName)
        categorySpinner = view.findViewById(R.id.spinnerCategory)
        cbAccessiblePhysicalChallenge = view.findViewById(R.id.cbAccessiblePhysicalChallenge)
        cbParking = view.findViewById(R.id.cbParking)
        cbSanitaryBin = view.findViewById(R.id.cbSanitaryBin)
        cbPaymentRequired = view.findViewById(R.id.cbPaymentRequired)

        cbHandWashFacility = view.findViewById(R.id.cbHandWash)
        cbSoap = view.findViewById(R.id.cbSoap)


        val genderRadioGroup: RadioGroup = view.findViewById(R.id.genderRadioGroup)

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
            initializeKeyboard(etToiletName)
            initializeKeyboard(etToiletLocation)
            initializeKeyboard(etToiletAddedBy)
        }

        etToiletAddedBy.setText(preference.getUserName())

        progressDialog = AwesomeProgressDialog(context)
            .setTitle(R.string.str_add_new_toilet)
            .setMessage(getString(R.string.str_add_new_toilet_msg))
            .setColoredCircle(R.color.dialogInfoBackgroundColor)
            .setDialogIconAndColor(R.drawable.ic_dialog_info, R.color.white)
            .setCancelable(false)

        genderRadioGroup.setOnCheckedChangeListener { group, checkedId ->

            when (checkedId) {
                R.id.radioMale -> gender = context!!.getString(R.string.str_male)
                R.id.radioFemale -> gender = context!!.getString(R.string.str_female)
                R.id.radioBothMaleFemale -> gender = getString(R.string.str_male_female)
            }
        }

        cbAccessiblePhysicalChallenge.setOnCheckedChangeListener(this)
        cbParking.setOnCheckedChangeListener(this)
        cbSanitaryBin.setOnCheckedChangeListener(this)
        cbPaymentRequired.setOnCheckedChangeListener(this)

        cbHandWashFacility.setOnCheckedChangeListener(this)
        cbSoap.setOnCheckedChangeListener(this)


        btnAddNewToilet.setOnClickListener(this)

    }

    fun initializeKeyboard(editText: EditText) {
        try {
            mCustomKeyboard!!.registerEditText(editText)
        } catch (ex: Exception) {
        }
    }

    override fun onBackPressedListener() {
        if (mCustomKeyboard != null && mCustomKeyboard!!.isCustomKeyboardVisible) {
            mCustomKeyboard!!.hideCustomKeyboard()
        } else {
            Constant.isNotInMainActivity = false
            (activity as MainActivity).backPressed()
        }
    }

    override fun onCheckedChanged(buttonView: CompoundButton?, isChecked: Boolean) {
        when (buttonView!!.id) {
            R.id.cbAccessiblePhysicalChallenge -> {
                accessiblePhysicalChallenge = if (isChecked)
                    "1"
                else
                    "0"
            }

            R.id.cbHandWash -> {
                handWashFacility = if (isChecked)
                    "1"
                else
                    "0"
            }

            R.id.cbSoap -> {
                soap = if (isChecked)
                    "1"
                else
                    "0"
            }

            R.id.cbParking -> {
                parking = if (isChecked)
                    "1"
                else
                    "0"
            }
            R.id.cbSanitaryBin -> {
                sanitaryDisposalBin = if (isChecked)
                    "1"
                else
                    "0"
            }
            R.id.cbPaymentRequired -> {
                paymentRequired = if (isChecked)
                    "1"
                else
                    "0"
            }
        }
    }

    override fun onClick(v: View?) {
        if (v?.id == R.id.btnAddNewToilet) {

            if (InternetConnection.isNetworkAvailable(activity!!)) {
                if (isValid(etToiletName) && isValid(etToiletLocation)
                    && isValid(etToiletAddedBy)
                ) {

                    //validating spinner i.e. category
                    if (preference.getLanguage().equals(Constant.DEFAULT_LANGUAGE) && categorySpinner.selectedItem.toString().equals(
                            "Select Category"
                        )
                    ) {
                        coordinatorLayout.snackBar(getString(R.string.str_provider_required))
                        return
                    } else if (preference.getLanguage().equals(Constant.LANGUAGE_URDU) && (categorySpinner.selectedItem.toString().equals(
                            "زمرہ منتخب کریں"
                        ))
                    ) {
                        coordinatorLayout.snackBar(getString(R.string.str_provider_required))
                        return
                    } else if (preference.getLanguage().equals(Constant.LANGUAGE_URDU) && (categorySpinner.selectedItem.toString().equals(
                            "کټګورۍ غوره کړئ"
                        ))
                    ) {
                        coordinatorLayout.snackBar(getString(R.string.str_provider_required))
                        return
                    }
                    //Validating Radio Buttons
                    if (gender.isNotEmpty()) {

                        progressDialog.show()
                        //adding new Toilet
                        try {

                            mService.addNewToilet(
                                etToiletName.text.toString(),
                                Constant.midLatLng!!.latitude.toString(),
                                Constant.midLatLng!!.longitude.toString(),
                                "0",
                                etToiletAddress.text.toString(),
                                spinnerCategory.selectedItem.toString(),
                                etToiletAddedBy.text.toString(),
                                gender,
                                accessiblePhysicalChallenge,
                                sanitaryDisposalBin,
                                paymentRequired,
                                parking,
                                preference.getUserEmail(),
                                Constant.CITY_NAME,
                                handWashFacility,
                                soap
                            )
                                .enqueue(object : Callback<AddNewToilet> {

                                    override fun onResponse(
                                        call: Call<AddNewToilet>,
                                        response: Response<AddNewToilet>
                                    ) {
                                        progressDialog.hide()

                                        val newToiletResponse = response.body()

                                        if (newToiletResponse != null && newToiletResponse.success) {
                                            //success dialog
                                            AwesomeSuccessDialog(context)
                                                .setTitle(R.string.app_name)
                                                .setMessage(getString(R.string.str_new_toilet_added_successfuly))
                                                .setColoredCircle(R.color.dialogSuccessBackgroundColor)
                                                .setDialogIconAndColor(R.drawable.ic_dialog_info, R.color.white)
                                                .setCancelable(false)
                                                .setPositiveButtonText(getString(R.string.dialog_ok_button))
                                                .setPositiveButtonbackgroundColor(R.color.dialogSuccessBackgroundColor)
                                                .setPositiveButtonTextColor(R.color.white)
                                                .setPositiveButtonClick {
                                                    //moving to home fragment
                                                    activity!!.supportFragmentManager.beginTransaction()
                                                        .replace(R.id.layoutFragmentContainer, HomeFragment())
                                                        .commit()
                                                }
                                                .show()
                                        } else {
                                            displayErrorDialog("Toilet Not Added!", newToiletResponse!!.message)
                                        }
                                    }

                                    override fun onFailure(call: Call<AddNewToilet>, t: Throwable) {
                                        progressDialog.hide()
                                        displayErrorDialog("Toilet Not Added!", "Server side error!")
                                    }

                                })

                        } catch (ex: java.lang.Exception) {
                            ex.printStackTrace()
                        }

                    } else {
                        Snackbar.make(
                            coordinatorLayout, getString(R.string.str_gender_required),
                            Snackbar.LENGTH_SHORT
                        ).show()
                    }
                }
            } else {
                coordinatorLayout.snackBar(getString(R.string.str_internet_msg))
            }

        }
    }

    fun isValid(editText: EditText): Boolean {
        var viewName = ""

        if (editText.text.isNotEmpty()) {
            return true
        } else {
            when (editText.id) {
                R.id.etToiletTitle -> viewName = "Toilet Name "
                R.id.etToiletAddress -> viewName = "Toilet Address "
                R.id.etToiletLatLng -> viewName = "Toilet Latitude & Longitude "
                R.id.etToiletAddedBy -> viewName = "Toilet Added by "
            }
            editText.error = "$viewName ${getString(R.string.str_required)}"
            return false
        }
    }

    override fun onResume() {
        super.onResume()
        if (Constant.midLatLng != null) {
            etToiletLocation.setText("${Constant.midLatLng!!.latitude}, ${Constant.midLatLng!!.longitude}")
        }
        if(Constant.ADDRESS_LINE != null)
            etToiletAddress.setText(Constant.ADDRESS_LINE)
    }

    override fun onDestroy() {
        super.onDestroy()
        Constant.midLatLng = null
    }

    fun displayErrorDialog(title: String, msg: String) {
        AwesomeErrorDialog(activity!!.applicationContext)
            .setTitle(title)
            .setMessage(msg)
            .setColoredCircle(R.color.dialogErrorBackgroundColor)
            .setDialogIconAndColor(R.drawable.ic_dialog_error, R.color.white)
            .setCancelable(true).setButtonText(getString(R.string.dialog_ok_button))
            .setButtonBackgroundColor(R.color.dialogErrorBackgroundColor)
            .setButtonText(getString(R.string.dialog_ok_button))
            .setErrorButtonClick {
            }
            .show()
    }
}
