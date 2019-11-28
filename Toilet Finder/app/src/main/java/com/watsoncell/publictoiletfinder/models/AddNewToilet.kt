package com.watsoncell.publictoiletfinder.models

class AddNewToilet{
     var success : Boolean = false
     var response_code : Int = 0
     var message : String = ""

    constructor()

    constructor(success: Boolean, response_code: Int, message: String) {
        this.success = success
        this.response_code = response_code
        this.message = message
    }

}