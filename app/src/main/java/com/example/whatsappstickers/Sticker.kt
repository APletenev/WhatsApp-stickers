/*
 * Copyright (c) WhatsApp Inc. and its affiliates.
 * All rights reserved.
 *
 * This source code is licensed under the BSD-style license found in the
 * LICENSE file in the root directory of this source tree.
 */
package com.example.whatsappstickers

import android.os.Parcel
import android.os.Parcelable

internal class Sticker : Parcelable {
    val imageFileName: String?
    val emojis: List<String>?
    var size: Long = 0

    constructor(imageFileName: String?, emojis: List<String>?) {
        this.imageFileName = imageFileName
        this.emojis = emojis
    }

    private constructor(`in`: Parcel) {
        imageFileName = `in`.readString()
        emojis = `in`.createStringArrayList()
        size = `in`.readLong()
    }


    override fun describeContents(): Int {
        return 0
    }

    override fun writeToParcel(dest: Parcel, flags: Int) {
        dest.writeString(imageFileName)
        dest.writeStringList(emojis)
        dest.writeLong(size)
    }

    companion object {
        @JvmField
        val CREATOR: Parcelable.Creator<Sticker?> = object : Parcelable.Creator<Sticker?> {
            override fun createFromParcel(`in`: Parcel): Sticker? {
                return Sticker(`in`)
            }

            override fun newArray(size: Int): Array<Sticker?> {
                return arrayOfNulls(size)
            }
        }
    }
}