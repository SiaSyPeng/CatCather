package com.cs65.gnf.lab3

import com.squareup.moshi.Json
import com.squareup.moshi.KotlinJsonAdapterFactory
import com.squareup.moshi.Moshi

val json="""
    { "catId":  1,
    "picUrl": "http://...",
    "lat": 79.172653871,
    "lng": 76.127368,
    "petted": false
  }
"""


data class Cat(
        val catId: Int,
        val picUrl: String,
        val lat: Float,
        val lng: Float,
        val petted: Boolean
)

fun main(args: Array<String>) {
    val moshi= Moshi.Builder()
            .add(KotlinJsonAdapterFactory())
            .build()

    val catAdaptor = moshi.adapter(Cat::class.java)

    val newCat = catAdaptor.fromJson(json)

    println(newCat)
}