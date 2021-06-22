package com.sushant.quickbills.utils

import com.sushant.quickbills.model.Bill
import java.text.DateFormat
import java.util.*

fun billCreationDateAndTime(bill: Bill): String {
    val dateObj = Date(-bill.purchasedAt!!.toLong())
    return DateFormat.getDateInstance(DateFormat.MEDIUM)
        .format(dateObj) +" "+ DateFormat.getTimeInstance(
        DateFormat.MEDIUM
    ).format(dateObj)
}