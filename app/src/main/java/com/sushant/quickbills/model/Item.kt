package com.sushant.quickbills.model

class Item() {
    var name : String ?= null
    var price : Double ?= null
    var searchKey : String ?=null

    constructor(name : String, price: Double, searchKey: String): this(){
        this.name = name
        this.price = price
        this.searchKey = searchKey
    }
}