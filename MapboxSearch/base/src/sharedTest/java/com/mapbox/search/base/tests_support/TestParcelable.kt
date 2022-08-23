package com.mapbox.search.base.tests_support

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class TestParcelable(val id: String) : Parcelable
