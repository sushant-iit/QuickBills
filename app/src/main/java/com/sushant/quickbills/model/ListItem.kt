package com.sushant.quickbills.model

abstract class ListItem {
    val typeDate = 0
    val typeBill = 1
    abstract fun getType(): Int
}