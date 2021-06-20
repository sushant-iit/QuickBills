package com.sushant.quickbills.model


class Bill() : ListItem() {
    var customerDetails : Customer ?= null
    var customerId : String ?= null
    var purchasedAt : Long?= null
    var particulars : ArrayList <ParticularItem>? = null
    var totalAmount : Double?=null

    constructor(customerDetails: Customer,customerId: String, purchasedAt: Long, particulars: ArrayList<ParticularItem>, totalAmount: Double) : this() {
        this.customerDetails = customerDetails
        this.purchasedAt = purchasedAt
        this.particulars = particulars
        this.totalAmount = totalAmount
        this.customerId = customerId
    }

    class ParticularItem(){
        var itemName : String ?= null
        var itemPrice : Double ?=null
        var itemQty : Double ?= null
        var itemAmount : Double ?= null

        constructor(itemName: String, itemPrice : Double, itemQty: Double, itemAmount : Double) : this() {
            this.itemName = itemName
            this.itemPrice = itemPrice
            this.itemQty = itemQty
            this.itemAmount = itemAmount
        }
    }

    override fun getType(): Int {
        return typeBill
    }
}