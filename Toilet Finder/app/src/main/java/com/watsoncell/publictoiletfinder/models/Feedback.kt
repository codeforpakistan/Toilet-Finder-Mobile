package com.watsoncell.publictoiletfinder.models
class Feedback{
    
    public var comments: String = ""

    public var updated_at: String = ""

    public var review: Int = 0

    public var name: String = ""

    public var created_at: String = ""

    public var id: Int = 0

    public var toilet_id: Int = 0

    public var email: String = ""

    public var picture: String = ""

    constructor() 

    constructor(comments: String, updated_at: String, review: Int, name: String, created_at: String, id: Int, toilet_id: Int, email: String, picture: String){
        this.comments = comments
        this.updated_at = updated_at
        this.review = review
        this.name = name
        this.created_at = created_at
        this.id = id
        this.toilet_id = toilet_id
        this.email = email
        this.picture = picture
    }

}
