package com.watsoncell.publictoiletfinder.retrofit

import com.watsoncell.publictoiletfinder.models.AddNewToilet
import com.watsoncell.publictoiletfinder.models.NearByToiletResponse
import com.watsoncell.publictoiletfinder.models.ReviewToilet
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Call
import retrofit2.http.*

interface IToiletFinderApi {
    //getting all toilets latitude & longitude, api

  /*  @FormUrlEncoded
    @POST("all_toilets")
    fun getAllToiletList(@Field("city") city: String): Call<ListAllToilets>
*/
    //add new Toilet api
    @FormUrlEncoded
    @POST("toilet")
    fun addNewToilet(
      @Field("title") title : String,
        @Field("latitude") latitude: String,
        @Field("longitude") longitude: String,
        @Field("verify") verify: String,
        @Field("address") address: String,
        @Field("providers") providers: String,
        @Field("added_by") added_by: String,
        @Field("toilet_available") toiletGender: String,
        @Field("accessible_physical_challenge") accessiblePhysicalChallenge: String,
        @Field("sanitary_disposal_bin") sanitaryDisposalBinAvailability: String,
        @Field("payment_required") paymentRequired: String,
        @Field("parking") parkingAvailability: String,
        @Field("email") userEmail: String,
        @Field("city") city : String,
        @Field("hand_wash") handWash : String,
        @Field("soap") soap : String
    ): Call<AddNewToilet>

   // Review a Toilet
    @Multipart
    @POST("review")
    fun addToiletReview(
        @Part("toilet_id") toiletId : RequestBody,
        @Part("review") rating : RequestBody,
        @Part imageFile: MultipartBody.Part,
        @Part("comments") message : RequestBody,
        @Part("name") name : RequestBody,
        @Part("email") email : RequestBody
    ): Call<ReviewToilet>

    //Getting nearby toilets
    @FormUrlEncoded
    @POST("get_toilet_near")
    fun getNearByToilets(
        @Field("latitude") latitude : Double,
        @Field("longitude") longitude : Double,
        @Field("radius") radius : Int
    ) : Call<NearByToiletResponse>

    //Search by a city and get all toilet list according to city
    @FormUrlEncoded
    @POST("all_toilets")
    fun getCurrentCityToiletList(
        @Field("latitude") latitude : Double,
        @Field("longitude") longitude : Double,
        @Field("city") city: String)
    :Call<NearByToiletResponse>

    //toilet filter api
    @FormUrlEncoded
    @POST("filter")
    fun getToiletFilterResult(
        @Field("toilet_available") gender : String,
        @Field("accessible_physical_challenge") accessibility : String,
        @Field("max_distance") distance : Int,
        @Field("latitude") latitude : Double,
        @Field("longitude") longitude : Double
    ) :Call<NearByToiletResponse>


    //getting all toilets
    @GET("toiletlist")
    fun getAllToilets() : Call<NearByToiletResponse>

}