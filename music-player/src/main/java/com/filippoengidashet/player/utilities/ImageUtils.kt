package com.filippoengidashet.player.utilities

import android.content.ContentResolver
import android.content.res.Resources
import android.net.Uri

/**
 * @author Filippo Engidashet
 * @version 1.0.0
 * @since Sun, 2019-12-22 at 18:12.
 */
object ImageUtils {

    fun getResourceUri(r: Resources, resId: Int): Uri {
        return Uri.parse(
            ContentResolver.SCHEME_ANDROID_RESOURCE + "://" + r.getResourcePackageName(
                resId
            ) + '/' + r.getResourceTypeName(resId) + '/' + r.getResourceEntryName(
                resId
            )
        )
    }
}
