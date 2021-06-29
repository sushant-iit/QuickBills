package com.sushant.quickbills.utils

class ConverterTime(private val currDateTime : Long) {
    fun getTimeInGMT() : Long{
        return currDateTime - 19800000L
    }
}