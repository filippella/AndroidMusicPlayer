package com.filippoengidashet.player.controllers.audio

import android.content.Context
import android.media.AudioManager

/**
 * @author Filippo Engidashet
 * @version 1.0.0
 * @since Sat, 2019-12-21 at 15:49.
 */
@Suppress("DEPRECATION")
class LegacyAudioFocusController(context: Context) :
    AudioFocusController(context), AudioManager.OnAudioFocusChangeListener {

    override fun requestAudioFocus() {
        val result: Int = audioManager.requestAudioFocus(
            this, AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN
        )

        if (result == AudioManager.AUDIOFOCUS_REQUEST_GRANTED) {
            focusCallback?.onAudioFocusStateChanged(AudioFocusCallback.StateType.FOCUS_GAINED)
        }
    }

    override fun abandonAudioFocus() {
        val result: Int = audioManager.abandonAudioFocus(this)
        if (result == AudioManager.AUDIOFOCUS_REQUEST_GRANTED) {
            focusCallback?.onAudioFocusStateChanged(AudioFocusCallback.StateType.FOCUS_LOST)
        } else {
            focusCallback?.onAudioFocusStateChanged(AudioFocusCallback.StateType.FAILED)
        }
    }

    override fun onAudioFocusChange(focusChange: Int) {
        when (focusChange) {
            AudioManager.AUDIOFOCUS_GAIN,
            AudioManager.AUDIOFOCUS_GAIN_TRANSIENT,
            AudioManager.AUDIOFOCUS_GAIN_TRANSIENT_MAY_DUCK -> {
                focusCallback?.onAudioFocusStateChanged(AudioFocusCallback.StateType.FOCUS_GAINED)
            }
            AudioManager.AUDIOFOCUS_LOSS -> {
                focusCallback?.onAudioFocusStateChanged(AudioFocusCallback.StateType.FOCUS_LOST)
            }
            AudioManager.AUDIOFOCUS_LOSS_TRANSIENT,
            AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK -> {
                focusCallback?.onAudioFocusStateChanged(AudioFocusCallback.StateType.FOCUS_LOST_DUCK)
            }
            AudioManager.AUDIOFOCUS_REQUEST_FAILED -> {
                focusCallback?.onAudioFocusStateChanged(AudioFocusCallback.StateType.FAILED)
            }
            else -> {
                focusCallback?.onAudioFocusStateChanged(AudioFocusCallback.StateType.UNKNOWN)
            }
        }
    }
}
