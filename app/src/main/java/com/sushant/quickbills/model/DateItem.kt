package com.sushant.quickbills.model

class DateItem(val date : String) : ListItem(){
    override fun getType(): Int {
        return typeDate
    }
}