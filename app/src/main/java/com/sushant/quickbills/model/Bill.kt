package com.sushant.quickbills.model

import java.util.*
import kotlin.collections.ArrayList

class Bill() {
    var customer : Customer ?= null
    var purchasedAt : Date ?= null
    var particulars : ArrayList <Bill.ParticularItem>? = null
    var totalAmount : Double?=null

    constructor(customer: Customer, purchasedAt: Date, particulars: ArrayList<Bill.ParticularItem>, totalAmount: Double) : this() {
        this.customer = customer
        this.purchasedAt = purchasedAt
        this.particulars = particulars
        this.totalAmount = totalAmount
    }

    class ParticularItem(var itemName: String,var itemPrice : Double,var itemQty: Double,var itemAmount : Double)
}