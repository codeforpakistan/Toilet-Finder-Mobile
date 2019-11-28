package com.watsoncell.publictoiletfinder.models

import com.google.gson.annotations.SerializedName

class ListAllToilets {

    var responsecode: Int = 0
    @SerializedName("data")
    var dataList: List<Data>? = null

    var success: Boolean = false

    var message: String = ""

    constructor()

    constructor(responsecode: Int, data: List<Data>, success: Boolean, message: String) {
        this.responsecode = responsecode
        this.dataList = data
        this.success = success
        this.message = message
    }


    class Data {

        var address: String = ""

        var updated_at: String = ""

        var latitude: Double = 0.0

        var verify: String = "0"

        var created_at: String = ""

        var id: Int = 0

        var longitude: Double = 0.0

        var feedback: ArrayList<Feedback>? = null


        constructor()

        constructor(
            address: String,
            updated_at: String,
            latitude: Double,
            verify: String,
            created_at: String,
            id: Int,
            longitude: Double,
            feedback: ArrayList<Feedback>
        ) {
            this.address = address
            this.updated_at = updated_at
            this.latitude = latitude
            this.verify = verify
            this.created_at = created_at
            this.id = id
            this.longitude = longitude
            this.feedback = feedback
        }

    }
}