package com.watsoncell.toiletfinder.models

import com.google.gson.annotations.SerializedName

data class NearByToiletResponse(

	@field:SerializedName("success")
	var success: Boolean = false,

	@field:SerializedName("response_code")
	var responseCode: Int = 0,

	@field:SerializedName("message")
	var message: String = "",

	@field:SerializedName("data")
	var data: ArrayList<NearByToiletDTO>? = null


)