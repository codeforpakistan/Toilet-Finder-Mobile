package com.watsoncell.toiletfinder.fragments

import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.SeekBar
import android.widget.TextView
import com.watsoncell.toiletfinder.MainActivity
import com.watsoncell.toiletfinder.R
import com.watsoncell.toiletfinder.utils.Constant
import com.watsoncell.toiletfinder.utils.InternetConnection

class FilterToiletFragment : Fragment(), View.OnClickListener {

    private var distance = 0
    private var gender = "male"
    private var accessibility = "no"
    private lateinit var btnMale :  Button
    private lateinit var btnFemale : Button
    private lateinit var btnBothGender : Button
    private lateinit var btnAccessibilityYes : Button
    private lateinit var btnAccessibilityNo : Button
    private lateinit var btnSearchToilet : Button

    private lateinit var tvDisplayDistance :  TextView
    private lateinit var seekBarDistance : SeekBar

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_filter_toilet, container, false)

        initViews(view)

        return view
    }

    private fun initViews(view: View?) {

        tvDisplayDistance = view!!.findViewById(R.id.tvDisplayDistance)

        seekBarDistance = view.findViewById(R.id.seekBarDistance)

        btnMale = view.findViewById(R.id.btnMale)
        btnFemale = view.findViewById(R.id.btnFemale)
        btnBothGender = view.findViewById(R.id.btnBothGender)
        btnSearchToilet = view.findViewById(R.id.btnSearchToilet)

        btnAccessibilityYes = view.findViewById(R.id.btnAccessibilityYes)
        btnAccessibilityNo = view.findViewById(R.id.btnAccessibilityNo)

        btnMale.setOnClickListener(this)
        btnFemale.setOnClickListener(this)
        btnBothGender.setOnClickListener(this)
        btnAccessibilityYes.setOnClickListener(this)
        btnAccessibilityNo.setOnClickListener(this)
        btnSearchToilet.setOnClickListener(this)

        //selecting default filter button
        btnMale.isSelected =true
        btnAccessibilityNo.isSelected = true

        seekBarDistance.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener{
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                distance = progress
                tvDisplayDistance.text = "$progress ${getString(R.string.str_km)}"
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {
            }

            override fun onStopTrackingTouch(seekBar: SeekBar?) {

            }

        })

    }


    override fun onClick(v: View?) {
        when(v!!.id){
            R.id.btnMale -> {
                btnMale.isSelected = true
                btnFemale.isSelected = false
                btnBothGender.isSelected = false

                gender = "male"

            }

            R.id.btnFemale -> {
                btnMale.isSelected = false
                btnFemale.isSelected = true
                btnBothGender.isSelected = false

                gender = "female"
            }

            R.id.btnBothGender -> {
                btnMale.isSelected = false
                btnFemale.isSelected = false
                btnBothGender.isSelected = true

                gender = "male/female"
            }

            R.id.btnAccessibilityYes -> {
                btnAccessibilityYes.isSelected = true
                btnAccessibilityNo.isSelected = false

                accessibility = "yes"
            }

            R.id.btnAccessibilityNo -> {
                btnAccessibilityYes.isSelected = false
                btnAccessibilityNo.isSelected = true

                accessibility = "no"
            }

            R.id.btnSearchToilet ->{
                if(InternetConnection.isNetworkAvailable(activity!!)){
                    val bundle = Bundle()
                    bundle.putString("accessibility",accessibility)
                    bundle.putString("gender",gender)
                    bundle.putInt("distance",distance)

                    val filterToiletResultFragment = FilterToiletResultFragment()
                    filterToiletResultFragment.arguments = bundle

                    (activity as MainActivity).replaceFragment(filterToiletResultFragment)
                }
                else
                    Constant.showNoInternetConnectionDialog(activity!!)

            }
        }
    }
}
