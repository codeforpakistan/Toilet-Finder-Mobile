package com.watsoncell.toiletfinder.models

class ReviewToilet {
    var success : Boolean = false
    var response_code : Int = 0
    var message : String = ""
    var errors : String = ""

    constructor()

    constructor(success: Boolean, response_code: Int, message: String, errors : String) {
        this.success = success
        this.response_code = response_code
        this.message = message
        this.errors = errors
    }
}