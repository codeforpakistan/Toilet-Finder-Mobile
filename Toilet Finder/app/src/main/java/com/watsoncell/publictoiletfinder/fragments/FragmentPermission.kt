package com.watsoncell.publictoiletfinder.fragments


import android.Manifest
import android.content.Context
import android.content.Intent
import android.location.LocationManager
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.support.v4.app.Fragment
import android.support.v7.app.AlertDialog
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import com.karumi.dexter.Dexter
import com.karumi.dexter.MultiplePermissionsReport
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.multi.MultiplePermissionsListener
import com.watsoncell.publictoiletfinder.MainActivity
import com.watsoncell.publictoiletfinder.R
import com.watsoncell.publictoiletfinder.utils.AppPreference

class FragmentPermission : Fragment() {


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_permission, container, false)

        //request location enable
        val manager =
            activity!!.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        if (!manager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            buildAlertMessageNoGps()
        }

        val btnPermissionGrant = view.findViewById<Button>(R.id.btnGrantPermission)
        btnPermissionGrant.setOnClickListener {

            if(Build.VERSION.SDK_INT >= 23){
                Dexter.withActivity(activity)
                    .withPermissions(
                        Manifest.permission.ACCESS_COARSE_LOCATION
                        ,Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.CAMERA,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE,
                        Manifest.permission.READ_EXTERNAL_STORAGE)
                    .withListener(object: MultiplePermissionsListener {
                        override fun onPermissionsChecked(report: MultiplePermissionsReport?) {
                            report?.let {
                                    val intent = Intent(activity, MainActivity::class.java)
                                    intent.flags =
                                        Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
                                    startActivity(intent)
                                    activity!!.finish()

                                    val preference = AppPreference()
                                    preference.setAppIntro(true) // not to show app intro again
                                }
                        }
                        override fun onPermissionRationaleShouldBeShown(
                            permissions: MutableList<PermissionRequest>?,
                            token: PermissionToken?
                        ) {

                            token?.continuePermissionRequest()
                        }
                    })
                    .withErrorListener {
                    }
                    .check()
            }
            else{
                val intent = Intent(activity, MainActivity::class.java)
                intent.flags =
                    Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
                startActivity(intent)
                activity!!.finish()

                val preference = AppPreference()
                preference.setAppIntro(true) // not to show app intro again
            }

        }
        return view
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

}
