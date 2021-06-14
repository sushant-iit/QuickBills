package com.sushant.quickbills.model

class Customer() {
    //Primary constructor is empty so that it works fine with Firebase Recycler View Adapter
    var number: String? = null
    var name: String? = null
    var address: String? = null
    var searchKey : String ?= null

    constructor(number: String, name: String, address: String):this(){
        this.number = number
        this.address = address
        this.name = name
        this.searchKey = this.name!!.replace(" ", "").lowercase()
    }
}