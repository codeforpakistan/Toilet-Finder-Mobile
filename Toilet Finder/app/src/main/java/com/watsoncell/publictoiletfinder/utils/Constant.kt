package com.watsoncell.publictoiletfinder.utils

import android.content.Context
import com.awesomedialog.blennersilva.awesomedialoglibrary.AwesomeWarningDialog
import com.google.android.gms.maps.model.LatLng
import com.watsoncell.publictoiletfinder.R
import com.watsoncell.publictoiletfinder.models.NearByToiletDTO

class Constant {
   companion object{
       var latitude: Double = 0.0
       var longitude: Double = 0.0
       var addressLine: String = ""

       var midLatLng : LatLng ?=  null

       var clickMarkerPosition : Int = 0

       val DEFAULT_LANGUAGE = "en"
       val LANGUAGE_URDU = "ur"
       val LANGUAGE_PASHTO = "ps"

       var CITY_NAME = "peshawar"

       //average rating array
       var toiletAverageRating : Float = 0F

       var isNotInMainActivity = false

       var ADDRESS_LINE : String ?= null

       var nearByToiletDetail : NearByToiletDTO? = null

        var nearbyToiletList: ArrayList<NearByToiletDTO> = ArrayList()

       var toiletImageResoureId : Int = 0


       var isIntroOpenFromHelp = false

       var toiletAvailability : String = ""

       //showing no internet connection dialog
       fun showNoInternetConnectionDialog(context : Context){
           AwesomeWarningDialog(context)
               .setTitle(context.getString(R.string.str_no_internet_connection))
               .setMessage(context.getString(R.string.str_internet_msg))
               .setColoredCircle(R.color.colorPrimaryDark)
               .setDialogIconAndColor(R.drawable.ic_dialog_warning, R.color.white)
               .setCancelable(true).setButtonText(context.getString(R.string.dialog_ok_button))
               .setButtonBackgroundColor(R.color.colorPrimaryDark)
               .setButtonTextColor(R.color.white)
               .setButtonText(context.getString(R.string.dialog_ok_button))
               .setWarningButtonClick {
               }
               .show()

       }
   }
}