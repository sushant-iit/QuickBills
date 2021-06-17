package com.sushant.quickbills.model

import kotlin.collections.ArrayList


class Bill() {
    var customerDetails : Customer ?= null
    var customerId : String ?= null
    var purchasedAt : Map<String,String>?= null
    var particulars : ArrayList <ParticularItem>? = null
    var totalAmount : Double?=null

    constructor(customer: Customer,customerId: String, purchasedAt: Map<String,String>, particulars: ArrayList<ParticularItem>, totalAmount: Double) : this() {
        this.customerDetails = customer
        this.purchasedAt = purchasedAt
        this.particulars = particulars
        this.totalAmount = totalAmount
        this.customerId = customerId
    }

    class ParticularItem(var itemName: String,var itemPrice : Double,var itemQty: Double,var itemAmount : Double)
}