package com.watsoncell.toiletfinder

import android.Manifest
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.support.design.widget.CoordinatorLayout
import android.support.v4.content.FileProvider
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.View
import android.widget.EditText
import android.widget.ImageView
import android.widget.RatingBar
import android.widget.Toast
import com.awesomedialog.blennersilva.awesomedialoglibrary.AwesomeErrorDialog
import com.awesomedialog.blennersilva.awesomedialoglibrary.AwesomeInfoDialog
import com.awesomedialog.blennersilva.awesomedialoglibrary.AwesomeProgressDialog
import com.awesomedialog.blennersilva.awesomedialoglibrary.AwesomeSuccessDialog
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.karumi.dexter.Dexter
import com.karumi.dexter.MultiplePermissionsReport
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionDeniedResponse
import com.karumi.dexter.listener.PermissionGrantedResponse
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.multi.MultiplePermissionsListener
import com.karumi.dexter.listener.single.PermissionListener
import com.watsoncell.toiletfinder.models.ReviewToilet
import com.watsoncell.toiletfinder.retrofit.Common
import com.watsoncell.toiletfinder.retrofit.IToiletFinderApi
import com.watsoncell.toiletfinder.utils.AppPreference
import com.watsoncell.toiletfinder.utils.InternetConnection
import com.watsoncell.toiletfinder.utils.snackBar
import id.zelory.compressor.Compressor
import kotlinx.android.synthetic.main.activity_review_toilet.*
import okhttp3.MediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.io.IOException


class ReviewToiletActivity : AppCompatActivity() {

    private lateinit var imageViewPreview: ImageView

    private lateinit var currentPhotoPath: String
    private lateinit var photoURI: Uri

    val REQUEST_TAKE_PHOTO = 787
    val REQUEST_PICK_GALLERY_IMAGE = 43

    var toiletId: Int = 0
    private var googleSignInClient: GoogleSignInClient? = null
    private val GOOGLE_SIGNIN_REQUSET_CODE = 121


    private lateinit var imageBitmap: Bitmap

    private var mImageFile: File? = null

    private lateinit var editTextMessage: EditText
    private lateinit var ratingBar: RatingBar
    private lateinit var coordinatorLayout: CoordinatorLayout

    private lateinit var progressDialog: AwesomeProgressDialog
    private lateinit var mServices: IToiletFinderApi

    private lateinit var preferences: AppPreference
    private var userName: String? = null
    private var userEmail: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_review_toilet)

        imageViewPreview = findViewById(R.id.imgPreview)

        ratingBar = findViewById(R.id.ratingBar)
        editTextMessage = findViewById(R.id.etReviewToilet)
        coordinatorLayout = findViewById(R.id.coordinatorLayoutReview)


        toiletId = intent.getIntExtra("toiletId", 0)

        preferences = AppPreference()

        mServices = Common.getApi()


        //Google sign in
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.server_client_id))
            .requestEmail()
            .build()

        googleSignInClient = GoogleSignIn.getClient(this, gso)


        progressDialog = AwesomeProgressDialog(this)
            .setTitle(getString(R.string.str_submitting_review))
            .setMessage(getString(R.string.str_review_submission_msg))
            .setColoredCircle(R.color.dialogInfoBackgroundColor)
            .setDialogIconAndColor(R.drawable.ic_dialog_info, R.color.white)
            .setCancelable(false)

        //button take picture
        imgTakePicture.setOnClickListener {
            showTakeOrPickImageDialog()
        }

        //button review submit listener
        btnSubmitReview.setOnClickListener {
            if (InternetConnection.isNetworkAvailable(this)) {
                if (preferences.getUserEmail().isNotEmpty() && preferences.getUserName().isNotEmpty()) {

                    userEmail = preferences.getUserEmail()
                    userName = preferences.getUserName()

                    reviewToilet()
                } else {
                    //Google sign-up
                    googleSignUp()
                }

            } else {
                coordinatorLayout.snackBar(getString(R.string.str_internet_msg))
            }


        }
    }

    //Google sign up -> getting user info while reviewing a Toilet
    private fun googleSignUp() {
        val signInIntent = googleSignInClient!!.getSignInIntent()
        startActivityForResult(signInIntent, GOOGLE_SIGNIN_REQUSET_CODE)
    }

    //reviewing a toilet
    private fun reviewToilet() {
        if (InternetConnection.isNetworkAvailable(this)) {
            if (ratingBar.rating > 0) {
                if (editTextMessage.text.isNotEmpty()) {

                    progressDialog.show()

                    //setting body request
                    val toiletIdRequest =
                        RequestBody.create(MediaType.parse("text/plain"), toiletId.toString())
                    val ratingRequest = RequestBody.create(
                        MediaType.parse("text/plain"),
                        ratingBar.rating.toString()
                    )
                    val reviewRequest =
                        RequestBody.create(
                            MediaType.parse("text/plain"),
                            editTextMessage.text.toString()
                        )

                    val nameRequest =
                        RequestBody.create(MediaType.parse("text/plain"), userName!!)

                    val emailRequest =
                        RequestBody.create(MediaType.parse("text/plain"), userEmail!!)


                    var pictureBody: MultipartBody.Part? = null
                    var imageRequest: RequestBody? = null
                    //if user does not upload image then upload drawable placeholder
                    if (mImageFile == null) {
                        Log.d("arsalan", "mImageFile is null")
                        val bitmapPlaceHolder =
                            BitmapFactory.decodeResource(resources, R.drawable.ic_placeholder)
                        mImageFile =
                            saveBitmapPlaceHolder(bitmapPlaceHolder, "optional")
                        try {
                            mImageFile =
                                Compressor(this@ReviewToiletActivity).compressToFile(
                                    mImageFile
                                )

                            imageRequest =
                                RequestBody.create(
                                    MediaType.parse("multipart/form-data"),
                                    mImageFile!!
                                )

                        } catch (e: IOException) {
                            e.printStackTrace()
                            Log.d("arsalan", "file error: " + e.message)
                        }
                    } else {
                        imageRequest =
                            RequestBody.create(MediaType.parse("multipart/form-data"), mImageFile!!)
                    }

                    pictureBody = MultipartBody.Part.createFormData(
                        "picture",
                        mImageFile!!.name,
                        imageRequest!!
                    )


                    mServices.addToiletReview(
                        toiletIdRequest,
                        ratingRequest,
                        pictureBody!!,
                        reviewRequest,
                        nameRequest,
                        emailRequest
                    )
                        .enqueue(object : Callback<ReviewToilet> {

                            override fun onResponse(
                                call: Call<ReviewToilet>,
                                response: Response<ReviewToilet>
                            ) {
                                progressDialog.hide()

                                val reviewToiletResponse: ReviewToilet = response.body()!!

                                if (reviewToiletResponse.success) {

                                    AwesomeSuccessDialog(this@ReviewToiletActivity)
                                        .setTitle(getString(R.string.str_submit_review))
                                        .setMessage(getString(R.string.str_review_thank_you))
                                        .setColoredCircle(R.color.dialogSuccessBackgroundColor)
                                        .setDialogIconAndColor(
                                            R.drawable.ic_dialog_info,
                                            R.color.white
                                        )
                                        .setCancelable(false)
                                        .setPositiveButtonText(getString(R.string.dialog_ok_button))
                                        .setPositiveButtonbackgroundColor(R.color.dialogSuccessBackgroundColor)
                                        .setPositiveButtonTextColor(R.color.white)
                                        .setPositiveButtonClick {
                                            //moving to MainActivity
                                            val intent = Intent(
                                                this@ReviewToiletActivity,
                                                MainActivity::class.java
                                            )
                                            intent.flags =
                                                Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
                                            startActivity(intent)
                                            finish()
                                        }
                                        .show()

                                } else {
                                    displayErrorDialog(
                                        getString(R.string.str_review_not_submitted),
                                        getString(R.string.str_review_submisson_error)
                                    )
                                }
                            }

                            override fun onFailure(call: Call<ReviewToilet>, t: Throwable) {
                                progressDialog.hide()

                                displayErrorDialog(
                                    getString(R.string.str_error),
                                    getString(R.string.str_error_on_server)
                                )
                            }
                        })
                } else {
                    coordinatorLayout.snackBar(getString(R.string.str_review_msg))
                }

            } else {
                coordinatorLayout.snackBar(getString(R.string.str_rate_toilet))
            }
        } else {
            coordinatorLayout.snackBar(getString(R.string.str_internet_msg))
        }
    }

    //displaying action dialog -> Take picture through camera or pick image from gallery
    fun showTakeOrPickImageDialog() {
        AwesomeInfoDialog(this)
            .setTitle(getString(R.string.str_complete_action))
            .setMessage("")
            .setColoredCircle(R.color.dialogInfoBackgroundColor)
            .setDialogIconAndColor(R.drawable.ic_dialog_info, R.color.white)
            .setCancelable(true)
            .setPositiveButtonText(getString(R.string.str_take_img_using_camera))
            .setPositiveButtonbackgroundColor(R.color.dialogInfoBackgroundColor)
            .setPositiveButtonTextColor(R.color.white)
            .setNegativeButtonText(getString(R.string.str_pick_image_from_gallery))
            .setNegativeButtonbackgroundColor(R.color.dialogInfoBackgroundColor)
            .setNegativeButtonTextColor(R.color.white)
            .setPositiveButtonClick {
                //Permission -> for Camera
                Dexter.withActivity(this)
                    .withPermissions(
                        Manifest.permission.CAMERA,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE
                    )
                    .withListener(object : MultiplePermissionsListener {
                        override fun onPermissionsChecked(report: MultiplePermissionsReport?) {
                            if (report!!.areAllPermissionsGranted()) {
                                dispatchTakePictureIntent()
                            } else
                                Toast.makeText(
                                    this@ReviewToiletActivity,
                                    getString(R.string.str_allow_all_permission),
                                    Toast.LENGTH_SHORT
                                )
                                    .show()
                        }

                        override fun onPermissionRationaleShouldBeShown(
                            permissions: MutableList<PermissionRequest>?,
                            token: PermissionToken?
                        ) {
                            token!!.continuePermissionRequest()
                        }

                    }).check()
            }
            .setNegativeButtonClick {
                //permission for picking image from gallery
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                    Dexter.withActivity(this)
                        .withPermission(Manifest.permission.READ_EXTERNAL_STORAGE)
                        .withListener(object : PermissionListener {
                            override fun onPermissionGranted(response: PermissionGrantedResponse) {
                                pickPictureFromGallery()
                            }

                            override fun onPermissionDenied(response: PermissionDeniedResponse) {/* ... */
                                if (response.isPermanentlyDenied)
                                    Toast.makeText(
                                        this@ReviewToiletActivity,
                                        getString(R.string.str_manually_allow_permission),
                                        Toast.LENGTH_SHORT
                                    ).show()

                            }

                            override fun onPermissionRationaleShouldBeShown(
                                permission: PermissionRequest,
                                token: PermissionToken
                            ) {
                                token.continuePermissionRequest()
                            }
                        }).check()
                } else {
                    pickPictureFromGallery()
                }
            }
            .show()

    }

    @Throws(IOException::class)
    private fun createImageFile(): File {
        // Create an image file name
        val fileName = "toilet"
        val storageDir: File = getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        return File.createTempFile(
            fileName, /* prefix */
            ".jpg", /* suffix */
            storageDir /* directory */
        ).apply {
            // Save a file: path for use with ACTION_VIEW intents
            currentPhotoPath = absolutePath
        }
    }

    //taking picture through camera
    private fun dispatchTakePictureIntent() {
        Intent(MediaStore.ACTION_IMAGE_CAPTURE).also { takePictureIntent ->
            // Ensure that there's a camera activity to handle the intent
            takePictureIntent.resolveActivity(packageManager)?.also {
                // Create the File where the photo should go
                val photoFile: File? = try {
                    createImageFile()
                } catch (ex: IOException) {
                    // Error occurred while creating the File
                    null
                }
                // Continue only if the File was successfully created
                photoFile?.also {
                    photoURI = FileProvider.getUriForFile(
                        this,
                        "com.watsoncell.toiletfinder.android.fileprovider",
                        it
                    )
                    takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI)
                    startActivityForResult(takePictureIntent, REQUEST_TAKE_PHOTO)
                }
            }
        }
    }

    //picking image from gallery
    fun pickPictureFromGallery() {
        val intent = Intent(Intent.ACTION_PICK)
        intent.type = "image/*"
        startActivityForResult(intent, REQUEST_PICK_GALLERY_IMAGE)
    }


    override fun onBackPressed() {
        val intent = Intent(this, MainActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        startActivity(intent)
        finish()
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        when (requestCode) {
            REQUEST_TAKE_PHOTO -> {
                imageBitmap = MediaStore.Images.Media.getBitmap(contentResolver, photoURI)

                imageViewPreview.visibility = View.VISIBLE
                imageViewPreview.setImageBitmap(imageBitmap)

                //imageToString = imageToString(imageBitmap)
                val tempUri = getImageUri(this, imageBitmap)
                val imagePath = getPath(tempUri)

                mImageFile = File(imagePath)
                try {
                    mImageFile = Compressor(this).compressToFile(mImageFile)
                } catch (e: IOException) {
                    e.printStackTrace()
                }
            }

            REQUEST_PICK_GALLERY_IMAGE -> {
                val imageUri = data!!.data

                imageBitmap = MediaStore.Images.Media.getBitmap(contentResolver, imageUri)

                imageViewPreview.visibility = View.VISIBLE
                imageViewPreview.setImageBitmap(imageBitmap)

                val imagePath = getPath(imageUri!!)
                mImageFile = File(imagePath)
                try {
                    mImageFile = Compressor(this).compressToFile(mImageFile)
                } catch (e: IOException) {
                    e.printStackTrace()
                }
            }
            GOOGLE_SIGNIN_REQUSET_CODE -> {
                try {
                    Log.d("arsalan", "Google sign in is called")

                    val task = GoogleSignIn.getSignedInAccountFromIntent(data)
                    val account = task.getResult(ApiException::class.java)
                    //onLoggedIn(account)
                    //saving login user email and name

                    userEmail = account.email!!
                    userName = account.displayName!!

                    preferences.addUserName(userName!!)
                    preferences.addUserEmail(userEmail!!)

                    reviewToilet()

                } catch (e: ApiException) {
                    // The ApiException status code indicates the detailed failure reason.
                    Log.d("arsalan", "signInResult:failed code=" + e.statusCode)
                }
            }

        }
    }

    fun getImageUri(inContext: Context, inImage: Bitmap): Uri {
        val bytes = ByteArrayOutputStream();
        inImage.compress(Bitmap.CompressFormat.JPEG, 100, bytes)
        val path =
            MediaStore.Images.Media.insertImage(inContext.contentResolver, inImage, "Title", null)
        return Uri.parse(path)
    }

    //getting image path from uri
    private fun getPath(uri: Uri): String {
        val projection = arrayOf(MediaStore.Images.Media.DATA)
        val cursor = contentResolver.query(uri, projection, null, null, null);
        cursor!!.moveToFirst()
        val columnIndex: Int = cursor.getColumnIndex(projection[0])
        val filePath: String = cursor.getString(columnIndex)

        cursor.close()
        return filePath

    }


    fun displayErrorDialog(title: String, msg: String) {
        AwesomeErrorDialog(this)
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

    fun saveBitmapPlaceHolder(finalBitmap: Bitmap, image_name: String): File {

        val myDir = File(Environment.getExternalStorageDirectory(), "Kcal")
        myDir.mkdirs()
        val fName = "$image_name.PNG"
        val file = File(myDir, fName)

        if (file.exists()) file.delete()
        try {
            val out = FileOutputStream(file)
            finalBitmap.compress(Bitmap.CompressFormat.PNG, 80, out)
            out.flush()
            out.close()
            return File(Environment.getExternalStorageDirectory(), "Kcal/$fName")

        } catch (e: Exception) {
            e.printStackTrace()
            Log.d("arsalan", "Exception: ${e.message}")
        }

        return null!!
    }
}
