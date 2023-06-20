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

class StickerPack : Parcelable {
    val identifier: String?
    val name: String?
    val publisher: String?
    val trayImageFile: String?
    val publisherEmail: String?
    val publisherWebsite: String?
    val privacyPolicyWebsite: String?
    val licenseAgreementWebsite: String?
    val imageDataVersion: String?
    val avoidCache: Boolean
    val animatedStickerPack: Boolean
    var iosAppStoreLink: String? = null
    internal var stickers: List<Sticker?>? = null
        private set
    var totalSize: Long = 0
        private set
    var androidPlayStoreLink: String? = null
    var isWhitelisted = false

    constructor(identifier: String?, name: String?, publisher: String?, trayImageFile: String?, publisherEmail: String?, publisherWebsite: String?, privacyPolicyWebsite: String?, licenseAgreementWebsite: String?, imageDataVersion: String?, avoidCache: Boolean, animatedStickerPack: Boolean) {
        this.identifier = identifier
        this.name = name
        this.publisher = publisher
        this.trayImageFile = trayImageFile
        this.publisherEmail = publisherEmail
        this.publisherWebsite = publisherWebsite
        this.privacyPolicyWebsite = privacyPolicyWebsite
        this.licenseAgreementWebsite = licenseAgreementWebsite
        this.imageDataVersion = imageDataVersion
        this.avoidCache = avoidCache
        this.animatedStickerPack = animatedStickerPack
    }

    private constructor(`in`: Parcel) {
        identifier = `in`.readString()
        name = `in`.readString()
        publisher = `in`.readString()
        trayImageFile = `in`.readString()
        publisherEmail = `in`.readString()
        publisherWebsite = `in`.readString()
        privacyPolicyWebsite = `in`.readString()
        licenseAgreementWebsite = `in`.readString()
        iosAppStoreLink = `in`.readString()
        stickers = `in`.createTypedArrayList<Sticker?>(Sticker.Companion.CREATOR)
        totalSize = `in`.readLong()
        androidPlayStoreLink = `in`.readString()
        isWhitelisted = `in`.readByte().toInt() != 0
        imageDataVersion = `in`.readString()
        avoidCache = `in`.readByte().toInt() != 0
        animatedStickerPack = `in`.readByte().toInt() != 0
    }

    internal fun setStickers(stickers: List<Sticker?>) {
        this.stickers = stickers
        totalSize = 0
        for (sticker in stickers) {
            totalSize += sticker!!.size
        }
    }

//    fun setAndroidPlayStoreLink(androidPlayStoreLink: String?) {
//        this.androidPlayStoreLink = androidPlayStoreLink
//    }

//    fun setIosAppStoreLink(iosAppStoreLink: String?) {
//        this.iosAppStoreLink = iosAppStoreLink
//    }

    override fun describeContents(): Int {
        return 0
    }

    override fun writeToParcel(dest: Parcel, flags: Int) {
        dest.writeString(identifier)
        dest.writeString(name)
        dest.writeString(publisher)
        dest.writeString(trayImageFile)
        dest.writeString(publisherEmail)
        dest.writeString(publisherWebsite)
        dest.writeString(privacyPolicyWebsite)
        dest.writeString(licenseAgreementWebsite)
        dest.writeString(iosAppStoreLink)
        dest.writeTypedList(stickers)
        dest.writeLong(totalSize)
        dest.writeString(androidPlayStoreLink)
        dest.writeByte((if (isWhitelisted) 1 else 0).toByte())
        dest.writeString(imageDataVersion)
        dest.writeByte((if (avoidCache) 1 else 0).toByte())
        dest.writeByte((if (animatedStickerPack) 1 else 0).toByte())
    }

    companion object {
        @JvmField
        val CREATOR: Parcelable.Creator<StickerPack> = object : Parcelable.Creator<StickerPack> {
            override fun createFromParcel(`in`: Parcel): StickerPack? {
                return StickerPack(`in`)
            }

            override fun newArray(size: Int): Array<StickerPack?> {
                return arrayOfNulls(size)
            }
        }
    }
}