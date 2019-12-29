package com.filippoengidashet.player.controllers.audio

import android.content.Context
import android.media.AudioManager

/**
 * @author Filippo Engidashet
 * @version 1.0.0
 * @since Sat, 2019-12-21 at 15:42.
 */
abstract class AudioFocusController(context: Context) {

    protected val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
    protected var focusCallback: AudioFocusCallback? = null

    fun setCallback(callback: AudioFocusCallback) {
        focusCallback = callback
    }

    abstract fun requestAudioFocus()
    abstract fun abandonAudioFocus()
}
