package com.cs65.gnf.lab4

import android.util.Log
import com.squareup.moshi.FromJson
import com.squareup.moshi.ToJson
import com.squareup.moshi.JsonQualifier
import java.io.Externalizable
import java.io.ObjectInput
import java.io.ObjectOutput

//These annotations will be used to change strings to doubles, ints, and booleans
@JsonQualifier
@Retention(AnnotationRetention.RUNTIME) annotation class StringToInt

@JsonQualifier
@Retention(AnnotationRetention.RUNTIME) annotation class StringToDouble

@JsonQualifier
@Retention(AnnotationRetention.RUNTIME) annotation class StringToBool

@JsonQualifier
@Retention(AnnotationRetention.RUNTIME) annotation class StringToStatus

@JsonQualifier
@Retention(AnnotationRetention.RUNTIME) annotation class StringToDate

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
class StringToDoubleAdapter {
    @FromJson
    @StringToDouble
    fun fromJson(value: String): Double {
        return value.toDouble()
    }

    @ToJson
    fun toJson(@StringToDouble value: Double): String {
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

/**
 * Adapter that changes strings to my own date class while converting from JSON and vice versa
 */
class StringToDateAdapter {
    @FromJson
    @StringToDate
    fun fromJson(value: String): Date {
        return Date.makeFromString(value)
    }

    @ToJson
    fun toJson(@StringToDate value: Date): String {
        return value.toString()
    }
}

enum class Status {
    OK, ERROR
}

class Date(val date: Int, val month: Int, val year: Int): Comparable<Date> {

    companion object {
        @JvmStatic
        fun makeFromString(string: String): Date {
            val args = string
                    .split("/")
                    .map{i->i.toInt()}
            return Date(args[0],args[1],args[2])
        }
    }

    override fun toString(): String {
        return ("$date/$month/$year")
    }

    private fun makeDouble(): Double {
        return (year*365.25)+(month*30.44)+date
    }

    override fun compareTo(other: Date): Int {
        return (this.makeDouble()-other.makeDouble()).toInt()
    }
}

/**
 * Representation of the cat class
 */
data class Cat (
        @StringToInt var catId: Int,
        var picUrl: String,
        var name: String,
        @StringToDouble var lat: Double,
        @StringToDouble var lng: Double,
        @StringToBool var petted: Boolean
): Externalizable {

    constructor() : this(0,"","",0.0,0.0,false)

    override fun readExternal(inp: ObjectInput?) {
        if (inp!= null) {
            catId = inp.readInt()
            picUrl = inp.readObject() as String
            name = inp.readObject() as String
            lat = inp.readDouble()
            lng = inp.readDouble()
            petted = inp.readBoolean()

        }
    }

    override fun writeExternal(out: ObjectOutput?) {
        if (out!= null) {
            out.writeInt(catId)
            out.writeObject(picUrl)
            out.writeObject(name)
            out.writeDouble(lat)
            out.writeDouble(lng)
            out.writeBoolean(petted)
        }
    }

}

data class historicCat(
        @StringToBool var liked: Boolean,
        val name: String,
        @StringToDate val whenCaught: Date,
        val picUrl: String
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
        val error: String?
)

/**
 * Custom class whose changes we can listen to. We can also retrieve the last used ID.
 */
class ListenableCatID(private val listener: ChangeListener){
    var id: Int = 0
    set(value) {
        field = value
        listener.onChange()
    }

    interface ChangeListener {
        fun onChange()
    }

}