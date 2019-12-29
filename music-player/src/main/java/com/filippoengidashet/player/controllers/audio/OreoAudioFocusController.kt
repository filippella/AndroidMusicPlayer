package com.filippoengidashet.player.controllers.audio

import android.annotation.TargetApi
import android.content.Context
import android.media.AudioAttributes
import android.media.AudioFocusRequest
import android.media.AudioManager
import android.os.Build
import android.os.Handler
import androidx.annotation.RequiresApi

/**
 * @author Filippo Engidashet
 * @version 1.0.0
 * @since Sat, 2019-12-21 at 15:46.
 */
@TargetApi(Build.VERSION_CODES.O)
@RequiresApi(Build.VERSION_CODES.O)
class OreoAudioFocusController(context: Context, private val handler: Handler) :
    AudioFocusController(context), AudioManager.OnAudioFocusChangeListener {

    private val focusLock = Any()

    private val audioAttributes = AudioAttributes.Builder().run {
        setUsage(AudioAttributes.USAGE_MEDIA)
        setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
        return@run build()
    }

    private val focusRequest = AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN)
        .also { arb ->
            arb.setAudioAttributes(audioAttributes)
            arb.setAcceptsDelayedFocusGain(true)
            arb.setWillPauseWhenDucked(true)
            arb.setOnAudioFocusChangeListener(this, handler)
            arb.build()
        }.build()

    private var playbackDelayed = false
    private var playbackNowAuthorized = false
    private var resumeOnFocusGain = false

    override fun requestAudioFocus() {
        val result = audioManager.requestAudioFocus(focusRequest)
        synchronized(focusLock) {
            playbackNowAuthorized = when (result) {
                AudioManager.AUDIOFOCUS_REQUEST_FAILED -> false
                AudioManager.AUDIOFOCUS_REQUEST_GRANTED -> {
                    focusCallback?.onAudioFocusStateChanged(AudioFocusCallback.StateType.FOCUS_GAINED)
                    true
                }
                AudioManager.AUDIOFOCUS_REQUEST_DELAYED -> {
                    playbackDelayed = true
                    false
                }
                else -> false
            }
        }
    }

    override fun onAudioFocusChange(focusChange: Int) {
        when (focusChange) {
            AudioManager.AUDIOFOCUS_GAIN,
            AudioManager.AUDIOFOCUS_GAIN_TRANSIENT,
            AudioManager.AUDIOFOCUS_GAIN_TRANSIENT_MAY_DUCK -> {
                if (playbackDelayed || resumeOnFocusGain) {
                    synchronized(focusLock) {
                        playbackDelayed = false
                        resumeOnFocusGain = false
                    }
                    focusCallback?.onAudioFocusStateChanged(AudioFocusCallback.StateType.FOCUS_GAINED)
                }
            }
            AudioManager.AUDIOFOCUS_LOSS -> {
                synchronized(focusLock) {
                    resumeOnFocusGain = false
                    playbackDelayed = false
                }
                focusCallback?.onAudioFocusStateChanged(AudioFocusCallback.StateType.FOCUS_LOST)
            }
            AudioManager.AUDIOFOCUS_LOSS_TRANSIENT,
            AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK -> {
                synchronized(focusLock) {
                    resumeOnFocusGain = true
                    playbackDelayed = false
                }
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

    override fun abandonAudioFocus() {
        audioManager.abandonAudioFocusRequest(focusRequest)
    }
}
