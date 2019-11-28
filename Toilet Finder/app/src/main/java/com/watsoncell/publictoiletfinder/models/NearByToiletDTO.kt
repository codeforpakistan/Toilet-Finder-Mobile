package com.watsoncell.publictoiletfinder.models

import com.google.gson.annotations.SerializedName

data class NearByToiletDTO(

	@field:SerializedName("id")
	var id: Int = 0,

	@field:SerializedName("title")
	var title : String = "",

	@field:SerializedName("address")
	var address: String = "",

	@field:SerializedName("city")
	var city: String = "",

	@field:SerializedName("added_by")
	var addedBy: String = "",

	@field:SerializedName("toilet_available")
	var toiletAvailable: String = "",

	@field:SerializedName("accessible_physical_challenge")
	var accessiblePhysicalChallenge: String = "",

	@field:SerializedName("parking")
	var parking: String = "",

	@field:SerializedName("sanitary_disposal_bin")
	var sanitaryDisposalBin: String = "",

	@field:SerializedName("payment_required")
	var paymentRequired: String = "",

	@field:SerializedName("hand_wash")
	var handWash: String = "",

	@field:SerializedName("soap")
	var soap: String = "",

	@field:SerializedName("providers")
	var providers: String = "",

	@field:SerializedName("latitude")
	var latitude: String = "",

	@field:SerializedName("longitude")
	var longitude: String = "",

	@field:SerializedName("verify")
	var verify: String = "",

	@field:SerializedName("created_at")
	var createdAt: String = "",

	@field:SerializedName("updated_at")
	var updatedAt: String = "",

	@field:SerializedName("distance")
	var distance: String = "",

	@field:SerializedName("feedback")
	var feedback: ArrayList<UserReviewModel>? = null
)