package com.cs65.gnf.lab3

import android.util.Log
import com.squareup.moshi.FromJson
import com.squareup.moshi.ToJson
import com.squareup.moshi.JsonQualifier

//These annotations will be used to change strings to floats, ints, and booleans
@JsonQualifier
@Retention(AnnotationRetention.RUNTIME) annotation class StringToInt

@JsonQualifier
@Retention(AnnotationRetention.RUNTIME) annotation class StringToFloat

@JsonQualifier
@Retention(AnnotationRetention.RUNTIME) annotation class StringToBool

@JsonQualifier
@Retention(AnnotationRetention.RUNTIME) annotation class StringToStatus

/**
 * Adapter that changes strings to ints when converting from JSON and vice versa
 */
class StringToIntAdapter {
    @FromJson
    @StringToInt
    fun fromJson(value: String): Int {
        return value.toInt()
    }

    @ToJson
    fun toJson(@StringToInt value: Int): String {
        return value.toString()
    }
}

/**
 * Adapter that changes strings to floats when converting from JSON and vice versa
 */
class StringToFloatAdapter {
    @FromJson
    @StringToFloat
    fun fromJson(value: String): Float {
        return value.toFloat()
    }

    @ToJson
    fun toJson(@StringToFloat value: Float): String {
        return value.toString()
    }
}

/**
 * Adapter that changes strings to booleans when converting from JSON and vice versa
 */
class StringToBoolAdapter {
    @FromJson
    @StringToBool
    fun fromJson(value: String): Boolean {
        return value.toBoolean()
    }

    @ToJson
    fun toJson(@StringToBool value: Boolean): String {
        return value.toString()
    }
}

/**
 * Adapter that changes strings to the status enum class when converting from JSON and vice versa
 */
class StringToStatusAdapter {
    @FromJson
    @StringToStatus
    fun fromJson(value: String): Status {
        return if (value=="OK") Status.OK else Status.ERROR
    }

    @ToJson
    fun toJson(@StringToStatus value: Status): String {
        return if (value==Status.OK) "OK" else "ERROR"
    }
}

enum class Status {
    OK, ERROR
}

/**
 * Representation of the cat class
 */
data class Cat(
        @StringToInt val catId: Int,
        val picUrl: String,
        val name: String,
        @StringToFloat val lat: Float,
        @StringToFloat val lng: Float,
        @StringToBool val petted: Boolean
)

/**
 * Result of the petting
 */
data class PetResult(
        @StringToInt val catId: Int = 0,
        @StringToStatus val status: Status,
        val code: String?,
        val reason: String?
)

/**
 * Result of resetting the cat list, or of changing password
 */
data class OpResult(
        @StringToStatus val status: Status,
        val code: String?,
        val error: String
)