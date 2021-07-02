package com.sushant.quickbills.model

class User() {
    //Default data initialisation
    var brandName : String = "Your Company Pvt. Ltd"
    var brandNumber : String = "9876543210"
    var brandAddress : String = "New Delhi, India"
    constructor(brandName : String, brandNumber: String, brandAddress : String)    : this(){
        this.brandName = brandName
        this.brandNumber = brandNumber
        this.brandAddress = brandAddress
    }
}