/*
 * Copyright (c) WhatsApp Inc. and its affiliates.
 * All rights reserved.
 *
 * This source code is licensed under the BSD-style license found in the
 * LICENSE file in the root directory of this source tree.
 */
package com.example.whatsappstickers

import android.content.ContentResolver
import android.content.Context
import android.database.Cursor
import android.net.Uri
import android.text.TextUtils
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.util.Arrays

internal object StickerPackLoader {
    /**
     * Get the list of sticker packs for the sticker content provider
     */
    const val ASSET = "asset"
    const val INTERNAL = "internal"

    internal var storage = ASSET
    val authorityUri: Uri
        get() = when (storage) {
            INTERNAL -> CustomStickerContentProvider.Companion.AUTHORITY_URI
            else -> StickerContentProvider.Companion.AUTHORITY_URI
        }
    val authority: String
        get() = when (storage) {
            INTERNAL -> BuildConfig.CUSTOM_CONTENT_PROVIDER_AUTHORITY
            else -> BuildConfig.CONTENT_PROVIDER_AUTHORITY
        }
    @Throws(IllegalStateException::class)
    fun fetchStickerPacks(context: Context): ArrayList<StickerPack> {

        val cursor = context.contentResolver.query(authorityUri,
            null,
            null,
            null,
            null)
            ?: throw IllegalStateException("could not fetch from content provider, " + authority)
        val identifierSet = HashSet<String?>()
        val stickerPackList = fetchFromContentProvider(cursor)
        for (stickerPack in stickerPackList) {
            check(!identifierSet.contains(stickerPack.identifier)) { "sticker pack identifiers should be unique, there are more than one pack with identifier:" + stickerPack.identifier }
            identifierSet.add(stickerPack.identifier)
        }
        check(!stickerPackList.isEmpty()) { "There should be at least one sticker pack in the app" }
        for (stickerPack in stickerPackList) {
            val stickers = getStickersForPack(context, stickerPack)
            stickerPack.setStickers(stickers)
            StickerPackValidator.verifyStickerPackValidity(context, stickerPack)
        }
        return stickerPackList
    }

    private fun getStickersForPack(context: Context, stickerPack: StickerPack): List<Sticker?> {
        val stickers = fetchFromContentProviderForStickers(stickerPack.identifier,
            context.contentResolver)
        for (sticker in stickers) {
            val bytes: ByteArray
            try {
                bytes = fetchStickerAsset(stickerPack.identifier!!,
                    sticker!!.imageFileName!!,
                    context.contentResolver)
                check(bytes.size > 0) { "Asset file is empty, pack: " + stickerPack.name + ", sticker: " + sticker.imageFileName }
                sticker.size = bytes.size.toLong()
            } catch (e: IOException) {
                throw IllegalStateException("Asset file doesn't exist. pack: " + stickerPack.name + ", sticker: " + sticker!!.imageFileName,
                    e)
            } catch (e: IllegalArgumentException) {
                throw IllegalStateException("Asset file doesn't exist. pack: " + stickerPack.name + ", sticker: " + sticker!!.imageFileName,
                    e)
            }
        }
        return stickers
    }

    private fun fetchFromContentProvider(cursor: Cursor): ArrayList<StickerPack> {
        val stickerPackList = ArrayList<StickerPack>()
        cursor.moveToFirst()
        do {
            val identifier = cursor.getString(cursor.getColumnIndexOrThrow(StickerContentProvider.Companion.STICKER_PACK_IDENTIFIER_IN_QUERY))
            val name = cursor.getString(cursor.getColumnIndexOrThrow(StickerContentProvider.Companion.STICKER_PACK_NAME_IN_QUERY))
            val publisher = cursor.getString(cursor.getColumnIndexOrThrow(StickerContentProvider.Companion.STICKER_PACK_PUBLISHER_IN_QUERY))
            val trayImage = cursor.getString(cursor.getColumnIndexOrThrow(StickerContentProvider.Companion.STICKER_PACK_ICON_IN_QUERY))
            val androidPlayStoreLink = cursor.getString(cursor.getColumnIndexOrThrow(
                StickerContentProvider.Companion.ANDROID_APP_DOWNLOAD_LINK_IN_QUERY))
            val iosAppLink = cursor.getString(cursor.getColumnIndexOrThrow(StickerContentProvider.Companion.IOS_APP_DOWNLOAD_LINK_IN_QUERY))
            val publisherEmail = cursor.getString(cursor.getColumnIndexOrThrow(
                StickerContentProvider.Companion.PUBLISHER_EMAIL))
            val publisherWebsite = cursor.getString(cursor.getColumnIndexOrThrow(
                StickerContentProvider.Companion.PUBLISHER_WEBSITE))
            val privacyPolicyWebsite = cursor.getString(cursor.getColumnIndexOrThrow(
                StickerContentProvider.Companion.PRIVACY_POLICY_WEBSITE))
            val licenseAgreementWebsite = cursor.getString(cursor.getColumnIndexOrThrow(
                StickerContentProvider.Companion.LICENSE_AGREENMENT_WEBSITE))
            val imageDataVersion = cursor.getString(cursor.getColumnIndexOrThrow(
                StickerContentProvider.Companion.IMAGE_DATA_VERSION))
            val avoidCache = cursor.getShort(cursor.getColumnIndexOrThrow(StickerContentProvider.Companion.AVOID_CACHE)) > 0
            val animatedStickerPack = cursor.getShort(cursor.getColumnIndexOrThrow(
                StickerContentProvider.Companion.ANIMATED_STICKER_PACK)) > 0
            val stickerPack = StickerPack(identifier,
                name,
                publisher,
                trayImage,
                publisherEmail,
                publisherWebsite,
                privacyPolicyWebsite,
                licenseAgreementWebsite,
                imageDataVersion,
                avoidCache,
                animatedStickerPack)
            stickerPack.androidPlayStoreLink = androidPlayStoreLink
            stickerPack.iosAppStoreLink = iosAppLink
            stickerPackList.add(stickerPack)
        } while (cursor.moveToNext())
        return stickerPackList
    }

    private fun fetchFromContentProviderForStickers(
        identifier: String?,
        contentResolver: ContentResolver
    ): List<Sticker?> {
        val uri = getStickerListUri(identifier)
        val projection = arrayOf<String>(StickerContentProvider.Companion.STICKER_FILE_NAME_IN_QUERY,
            StickerContentProvider.Companion.STICKER_FILE_EMOJI_IN_QUERY)
        val cursor = contentResolver.query(uri, projection, null, null, null)
        val stickers: MutableList<Sticker?> = ArrayList()
        if (cursor != null && cursor.count > 0) {
            cursor.moveToFirst()
            do {
                val name = cursor.getString(cursor.getColumnIndexOrThrow(StickerContentProvider.Companion.STICKER_FILE_NAME_IN_QUERY))
                val emojisConcatenated = cursor.getString(cursor.getColumnIndexOrThrow(
                    StickerContentProvider.Companion.STICKER_FILE_EMOJI_IN_QUERY))
                var emojis: List<String>? = ArrayList(StickerPackValidator.EMOJI_MAX_LIMIT)
                if (!TextUtils.isEmpty(emojisConcatenated)) {
                    emojis = Arrays.asList(*emojisConcatenated.split(",".toRegex())
                        .dropLastWhile { it.isEmpty() }.toTypedArray())
                }
                stickers.add(Sticker(name, emojis))
            } while (cursor.moveToNext())
        }
        cursor?.close()
        return stickers
    }

    @Throws(IOException::class)
    fun fetchStickerAsset(
        identifier: String,
        name: String,
        contentResolver: ContentResolver
    ): ByteArray {
        contentResolver.openInputStream(getStickerAssetUri(identifier, name)).use { inputStream ->
            ByteArrayOutputStream().use { buffer ->
                if (inputStream == null) {
                    throw IOException("cannot read sticker asset:$identifier/$name")
                }
                var read: Int
                val data = ByteArray(16384)
                while (inputStream.read(data, 0, data.size).also { read = it } != -1) {
                    buffer.write(data, 0, read)
                }
                return buffer.toByteArray()
            }
        }
    }

    private fun getStickerListUri(identifier: String?): Uri {
        return Uri.Builder().scheme(ContentResolver.SCHEME_CONTENT)
            .authority(authority)
            .appendPath(StickerContentProvider.Companion.STICKERS).appendPath(identifier).build()
    }

    fun getStickerAssetUri(identifier: String?, stickerName: String?): Uri {
        return Uri.Builder().scheme(ContentResolver.SCHEME_CONTENT)
            .authority(authority)
            .appendPath(StickerContentProvider.Companion.STICKERS_ASSET).appendPath(identifier)
            .appendPath(stickerName).build()
    }
}