package com.watsoncell.publictoiletfinder.fragments


import android.inputmethodservice.KeyboardView
import android.os.Bundle
import android.support.design.widget.CoordinatorLayout
import android.support.design.widget.Snackbar
import android.support.v4.app.Fragment
import android.support.v7.app.AppCompatActivity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import com.awesomedialog.blennersilva.awesomedialoglibrary.AwesomeErrorDialog
import com.awesomedialog.blennersilva.awesomedialoglibrary.AwesomeProgressDialog
import com.awesomedialog.blennersilva.awesomedialoglibrary.AwesomeSuccessDialog
import com.watsoncell.publictoiletfinder.Interface.BackButtonInterface
import com.watsoncell.publictoiletfinder.MainActivity
import com.watsoncell.publictoiletfinder.R
import com.watsoncell.publictoiletfinder.models.AddNewToilet
import com.watsoncell.publictoiletfinder.retrofit.Common
import com.watsoncell.publictoiletfinder.retrofit.IToiletFinderApi
import com.watsoncell.publictoiletfinder.utils.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response


class AddToiletUseCurrentLocation : Fragment(), CompoundButton.OnCheckedChangeListener, View.OnClickListener,
    BackButtonInterface {

    private lateinit var etToiletLatLng: EditText
    private lateinit var etToiletAddress: EditText
    private lateinit var etToiletName: EditText
    private lateinit var etToiletAddedBy: EditText
    private lateinit var spinnerProvider: Spinner
    private lateinit var btnAddNewToilet: Button
    private lateinit var coordinatorLayout: CoordinatorLayout

    private lateinit var cbAccessiblePhysicalChallenge: CheckBox
    private lateinit var cbHandWashFacility: CheckBox
    private lateinit var cbSoap: CheckBox
    private lateinit var cbParking: CheckBox
    private lateinit var cbSanitaryBin: CheckBox
    private lateinit var cbPaymentRequired: CheckBox

    private var gender: String = "0"
    private var accessiblePhysicalChallenge = "0"
    private var parking = "0"
    private var sanitaryDisposalBin = "0"
    private var paymentRequired = "0"
    private var handWashFacility = "0"
    private var soap = "0"

    private lateinit var mService: IToiletFinderApi
    private lateinit var preference: AppPreference

    private lateinit var progressDialog: AwesomeProgressDialog
    private var mCustomKeyboard: CustomKeyboard? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_add_toilet_use_current_location, container, false)

        mService = Common.getApi()
        preference = AppPreference()

        initViews(view)

        return view
    }

    private fun initViews(view: View?) {
        btnAddNewToilet = view!!.findViewById(R.id.btnAddNewToilet)
        coordinatorLayout = view.findViewById(R.id.coordinatorLayoutAddToilet)
        etToiletLatLng = view.findViewById(R.id.etToiletLatLng)
        etToiletAddress = view.findViewById(R.id.etToiletAddress)
        etToiletAddedBy = view.findViewById(R.id.etToiletAddedBy)
        etToiletName = view.findViewById(R.id.etToiletTitle)
        spinnerProvider = view.findViewById(R.id.spinnerProvider)

        etToiletAddedBy.setText(preference.getUserName())
        etToiletLatLng.setText("${Constant.latitude}, ${Constant.longitude}")
        etToiletAddress.setText(Constant.addressLine)

        (activity as MainActivity).setOnBackButtonListener(this)

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
            initializeKeyboard(etToiletLatLng)
            initializeKeyboard(etToiletAddress)
            initializeKeyboard(etToiletAddedBy)
        }


        cbAccessiblePhysicalChallenge = view.findViewById(R.id.cbAccessiblePhysicalChallenge)
        cbParking = view.findViewById(R.id.cbParking)
        cbSanitaryBin = view.findViewById(R.id.cbSanitaryBin)
        cbPaymentRequired = view.findViewById(R.id.cbPaymentRequired)
        cbHandWashFacility = view.findViewById(R.id.cbHandWash)
        cbSoap = view.findViewById(R.id.cbSoap)

        progressDialog = AwesomeProgressDialog(context)
            .setTitle(R.string.str_add_new_toilet)
            .setMessage(getString(R.string.str_add_new_toilet_msg))
            .setColoredCircle(R.color.dialogInfoBackgroundColor)
            .setDialogIconAndColor(R.drawable.ic_dialog_info, R.color.white)
            .setCancelable(false)

        val genderRadioGroup: RadioGroup = view.findViewById(R.id.genderRadioGroup)

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
                    "Yes"
                else
                    "No"
            }

            R.id.cbHandWash -> {
                handWashFacility = if (isChecked)
                    "Yes"
                else
                    "No"
            }

            R.id.cbSoap -> {
                soap = if (isChecked)
                    "Yes"
                else
                    "No"
            }

            R.id.cbParking -> {
                parking = if (isChecked)
                    "Yes"
                else
                    "No"
            }
            R.id.cbSanitaryBin -> {
                sanitaryDisposalBin = if (isChecked)
                    "Yes"
                else
                    "No"
            }
            R.id.cbPaymentRequired -> {
                paymentRequired = if (isChecked)
                    "Yes"
                else
                    "No"
            }
        }
    }

    override fun onClick(v: View?) {
        if (v?.id == R.id.btnAddNewToilet) {

            if (InternetConnection.isNetworkAvailable(activity!!)) {
                if (isValid(etToiletName) && isValid(etToiletAddress) && isValid(etToiletLatLng)
                    && isValid(etToiletAddedBy)
                ) {

                    //validating spinner i.e. provider
                    if (preference.getLanguage().equals(Constant.DEFAULT_LANGUAGE) && spinnerProvider.selectedItem.toString().equals(
                            "Select Provider"
                        )
                    ) {
                        coordinatorLayout.snackBar(getString(R.string.str_provider_required))
                        return
                    } else if (preference.getLanguage().equals(Constant.LANGUAGE_URDU) && (spinnerProvider.selectedItem.toString().equals(
                            "فراہم کنندہ کو منتخب کریں"
                        ))
                    ) {
                        coordinatorLayout.snackBar(getString(R.string.str_provider_required))
                        return
                    } else if (preference.getLanguage().equals(Constant.LANGUAGE_URDU) && (spinnerProvider.selectedItem.toString().equals(
                            "برابرونکی غوره کړئ"
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
                                Constant.latitude.toString(),
                                Constant.longitude.toString(),
                                "0",
                                etToiletAddress.text.toString(),
                                spinnerProvider.selectedItem.toString(),
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
                                                    //moving to Home fragment
                                                    activity!!.supportFragmentManager.beginTransaction()
                                                        .replace(R.id.layoutFragmentContainer, HomeFragment())
                                                        .commit()
                                                }
                                                .show()
                                        } else {
                                            displayErrorDialog(getString(R.string.str_toilet_not_added), newToiletResponse!!.message)
                                        }
                                    }

                                    override fun onFailure(call: Call<AddNewToilet>, t: Throwable) {
                                        progressDialog.hide()
                                        displayErrorDialog(getString(R.string.str_toilet_not_added), getString(
                                                                                    R.string.str_error_on_server_side))
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

    fun displayErrorDialog(title: String, msg: String) {
        AwesomeErrorDialog(activity)
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

    override fun onStart() {
        super.onStart()
        (activity as AppCompatActivity).supportActionBar!!.show()
        (activity as AppCompatActivity).supportActionBar!!.title =
            getString(R.string.str_add_new_toilet)

    }

    override fun onStop() {
        super.onStop()
        (activity as AppCompatActivity).supportActionBar!!.hide()
        (activity as AppCompatActivity).supportActionBar!!.title = ""
    }
}
