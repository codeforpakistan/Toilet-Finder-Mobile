package com.watsoncell.publictoiletfinder

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.location.LocationManager
import android.os.Bundle
import android.provider.Settings
import android.support.design.widget.NavigationView
import android.support.v4.app.Fragment
import android.support.v4.view.GravityCompat
import android.support.v7.app.ActionBarDrawerToggle
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.Toolbar
import android.util.Log
import android.view.LayoutInflater
import android.view.MenuItem
import android.widget.Button
import android.widget.RadioButton
import android.widget.RadioGroup
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.analytics.FirebaseAnalytics
import com.watsoncell.publictoiletfinder.Interface.BackButtonInterface
import com.watsoncell.publictoiletfinder.appIntro.AppIntroActivity
import com.watsoncell.publictoiletfinder.fragments.*
import com.watsoncell.publictoiletfinder.utils.*
import kotlinx.android.synthetic.main.activity_main.*


class MainActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {


    lateinit var toolbar: Toolbar
    lateinit var preference: AppPreference

    private var googleSignInClient: GoogleSignInClient? = null
    private val GOOGLE_SIGNIN_REQUSET_CODE = 101
    private lateinit var backButtonListener: BackButtonInterface

    private lateinit var firebaseAnalytics: FirebaseAnalytics

    override fun attachBaseContext(newBase: Context?) {
        super.attachBaseContext(LocalHelper.onAttach(newBase!!))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        toolbar = findViewById(R.id.toolbar_main)
        toolbar.title = ""
        setSupportActionBar(toolbar)

        firebaseAnalytics = FirebaseAnalytics.getInstance(this)

        //request location enable, if not enable
        val manager =
            getSystemService(Context.LOCATION_SERVICE) as LocationManager
        if (!manager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            buildAlertMessageNoGps()
        }

        preference = AppPreference.getInstance(this)


        val navigationView: NavigationView = findViewById(R.id.navigation_view)

        val toggle =
            ActionBarDrawerToggle(
                this,
                drawerLayout,
                toolbar,
                R.string.str_drawer_open,
                R.string.str_drawer_close
            )
        drawerLayout.addDrawerListener(toggle)
        toggle.syncState()
        navigationView.setNavigationItemSelectedListener(this)

        //adding HOME fragment to container
        supportFragmentManager.beginTransaction()
            .add(R.id.layoutFragmentContainer, HomeFragment())
            .commit()


        //Google sign in
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.server_client_id))
            .requestEmail()
            .build()

        googleSignInClient = GoogleSignIn.getClient(this, gso)

    }

    fun setOnBackButtonListener(listener: BackButtonInterface) {
        backButtonListener = listener
    }

    override fun onBackPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START))
            drawerLayout.closeDrawer(GravityCompat.START) // close drawer

        else if (Constant.isNotInMainActivity) {
            //Interface listener to hide custom keyboard in add toilet fragment
            if (backButtonListener is AddToiletUsingMapCoordinate || backButtonListener is AddToiletUseCurrentLocation)
                backButtonListener.onBackPressedListener()
        }
        else if(supportFragmentManager.backStackEntryCount > 0){
            supportFragmentManager.popBackStackImmediate()
        }
        else
            super.onBackPressed()
    }

    //calling this method, when fragment active and custom keyboard is also hide, so that to back to MapFragment from other add toilet fragment
    fun backPressed() {
        super.onBackPressed()
    }

    override fun onNavigationItemSelected(menuItem: MenuItem): Boolean {

        when (menuItem.itemId) {
            R.id.menu_home -> {
                supportFragmentManager.beginTransaction()
                    .replace(R.id.layoutFragmentContainer, HomeFragment())
                    .commit()
            }
            R.id.menu_add_new_toilet -> {
                if (InternetConnection.isNetworkAvailable(this)) {
                    //if user is already login then don't need to show google sign-in
                    if (preference.getUserName().isNotEmpty() && preference.getUserEmail().isNotEmpty()) {
                        showAddNewToiletOptionDialog()
                    } else {
                        googleSignUp()
                    }
                } else {
                    coordinatorLayoutMain.snackBar(getString(R.string.str_internet_msg))
                }
            }
            R.id.menu_change_language -> {
                showChangeLanguageDialog()
            }

            R.id.menu_all_toilets -> {
                supportFragmentManager.beginTransaction()
                    .replace(R.id.layoutFragmentContainer, MapFragment())
                    .addToBackStack(null)
                    .commit()
            }

            R.id.menu_help -> {
                Constant.isIntroOpenFromHelp = true
                val intent = Intent(this, AppIntroActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
                startActivity(intent)
            }

            R.id.menu_about_us -> {
                replaceFragment(AboutUsFragment())
            }
        }


        drawerLayout.closeDrawer(GravityCompat.START)

        return true
    }

    //displaying change language dialog
    private fun showChangeLanguageDialog() {
        val alertDialog = AlertDialog.Builder(this@MainActivity)
        val view = LayoutInflater.from(this).inflate(R.layout.layout_change_language_dialog, null)

        val radioGroupLanguage = view.findViewById<RadioGroup>(R.id.radioGroupLanguage)
        val radioButtonEnglish = view.findViewById<RadioButton>(R.id.radioLngEnglish)
        val radioButtonUrdu = view.findViewById<RadioButton>(R.id.radioLngUrdu)
        val radioButtonPashto = view.findViewById<RadioButton>(R.id.radioLngPashto)

        when {
            preference.getLanguage() == Constant.DEFAULT_LANGUAGE -> radioButtonEnglish.isChecked =
                true
            preference.getLanguage() == Constant.LANGUAGE_URDU -> radioButtonUrdu.isChecked = true
            preference.getLanguage() == Constant.LANGUAGE_PASHTO -> radioButtonPashto.isChecked =
                true
        }


        alertDialog.setView(view)
        val dialog = alertDialog.create()

        radioGroupLanguage.setOnCheckedChangeListener { group, checkedId ->
            when (checkedId) {
                R.id.radioLngEnglish -> preference.setLanguage(Constant.DEFAULT_LANGUAGE)
                R.id.radioLngUrdu -> preference.setLanguage(Constant.LANGUAGE_URDU)
                R.id.radioLngPashto -> preference.setLanguage(Constant.LANGUAGE_PASHTO)
            }
            dialog.dismiss()
            refreshMainActivity()
        }


        dialog.show()
    }

    private fun refreshMainActivity() {
        val refreshIntent = Intent(this, MainActivity::class.java)
        refreshIntent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
        startActivity(refreshIntent)
        finish()
    }

    //replacing fragment
    fun replaceFragment(fragmentInstance: Fragment) {
        var fragment: Fragment? = null
        try {
            fragment = fragmentInstance

            supportFragmentManager.beginTransaction()
                .replace(R.id.layoutFragmentContainer, fragment)
                .addToBackStack(fragmentInstance.tag)
                .commit()

        } catch (ex: Exception) {
            ex.printStackTrace()
        }
    }

    //Google sign up -> getting user info while adding new Toilet
    private fun googleSignUp() {
        val signInIntent = googleSignInClient!!.getSignInIntent()
        startActivityForResult(signInIntent, GOOGLE_SIGNIN_REQUSET_CODE)
    }


    /*  Showing dialog -> Giving Following Option to user while adding new Toilet
        1. Use user current Coordinate while adding new Toilet
        2. Choose coordinate from Map while adding new Toilet
     */
    private fun showAddNewToiletOptionDialog() {
        val alertDialog = AlertDialog.Builder(this@MainActivity)
        val view = LayoutInflater.from(this).inflate(R.layout.layout_new_toilet_option_dialog, null)

        val btnUserCurrentLocation = view.findViewById<Button>(R.id.btnUserCurrentLocation)
        val btnCoordinateFromMap = view.findViewById<Button>(R.id.btnCoordinateFromMap)

        alertDialog.setView(view)
        val dialog = alertDialog.create()

        btnUserCurrentLocation.setOnClickListener {

            dialog.dismiss()

            supportFragmentManager.beginTransaction()
                .replace(R.id.layoutFragmentContainer, AddToiletUseCurrentLocation())
                .addToBackStack(null)
                .commit()
        }

        btnCoordinateFromMap.setOnClickListener {
            supportFragmentManager.beginTransaction()
                .replace(
                    R.id.layoutFragmentContainer,
                    AddToiletUsingMapCoordinate()
                )
                .addToBackStack(null)
                .commit()
            dialog.dismiss()
        }

        dialog.show()

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK) {
            when (requestCode) {
                GOOGLE_SIGNIN_REQUSET_CODE -> {
                    try {
                        val task = GoogleSignIn.getSignedInAccountFromIntent(data)
                        val account = task.getResult(ApiException::class.java)

                        //saving login user email and name
                        preference.addUserName(account.displayName!!)
                        preference.addUserEmail(account.email!!)

                        //for firebase analytics
                        val params = Bundle()
                        params.putString("name", account.displayName)
                        params.putString("email", account.email)
                        firebaseAnalytics.logEvent("login_user", params)

                        showAddNewToiletOptionDialog()

                    } catch (e: ApiException) {
                        // The ApiException status code indicates the detailed failure reason.
                        Log.d("arsalan", "signInResult:failed code=" + e.statusCode)
                    }

                }
            }
        }
    }

    fun buildAlertMessageNoGps() {
        try {
            val builder = AlertDialog.Builder(this)
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

    override fun onDestroy() {
        super.onDestroy()

        Constant.addressLine = ""
        Constant.longitude = 0.0
        Constant.longitude = 0.0

        Constant.isIntroOpenFromHelp = false

    }
}
