package com.gpssimulator.data.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class PaceConfig(
    val baseSeconds: Int
) : Parcelable
