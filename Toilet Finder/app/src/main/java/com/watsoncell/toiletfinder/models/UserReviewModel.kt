package com.watsoncell.toiletfinder.models

import com.google.gson.annotations.SerializedName

class UserReviewModel {

    var id : Int = 0
    var toilet_id : String = ""
    @SerializedName("name")
    var userName : String = ""
    @SerializedName("review")
    var userRating : Float = 0F
    @SerializedName("comments")
    var userReviewMsg : String = ""
    @SerializedName("email")
    var userEmail : String = ""
    @SerializedName("picture")
    var toiletImage : String = ""
    var created_at : String = ""
    var updated_at : String = ""


    constructor()

    constructor(
        id: Int,
        toilet_id: String,
        userName: String,
        userRating: Float,
        userReviewMsg: String,
        userEmail: String,
        toiletImage: String,
        created_at: String,
        updated_at: String
    ) {
        this.id = id
        this.toilet_id = toilet_id
        this.userName = userName
        this.userRating = userRating
        this.userReviewMsg = userReviewMsg
        this.userEmail = userEmail
        this.toiletImage = toiletImage
        this.created_at = created_at
        this.updated_at = updated_at
    }

}