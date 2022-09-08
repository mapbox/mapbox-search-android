package com.mapbox.search.utils

import android.os.Build
import android.os.Parcel
import android.os.Parcelable

internal inline fun <reified T : Parcelable> T.cloneFromParcel(): T {
    val bytes = marshall(this)
    return unmarshallParcelable(bytes)
}

internal inline fun <reified P : Parcelable> unmarshallParcelable(bytes: ByteArray): P {
    return with(unmarshall(bytes)) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            readParcelable(P::class.java.classLoader, P::class.java) as P
        } else {
            @Suppress("DEPRECATION")
            readParcelable<P>(P::class.java.classLoader) as P
        }
    }
}

internal fun unmarshall(bytes: ByteArray): Parcel {
    return Parcel.obtain().apply {
        unmarshall(bytes, 0, bytes.size)
        setDataPosition(0)
    }
}

internal fun marshall(parcelable: Parcelable): ByteArray {
    return Parcel.obtain().use {
        it.writeParcelable(parcelable, 0)
        it.marshall()
    }
}

private fun <T> Parcel.use(block: (Parcel) -> T): T {
    return try {
        block(this)
    } finally {
        this.recycle()
    }
}
