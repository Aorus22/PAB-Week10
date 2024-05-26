package com.example.ppab_10_l0122018_alyzakhoirunnadif

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class UserModel(
    var name: String? = null,
    var email: String? = null,
    var age: Int = 0,
    var phoneNumber: String? = null,
    var gender: String? = null,
    var profileImage: String? = null
) : Parcelable