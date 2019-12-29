package com.filippoengidashet.player.controllers.session

import android.annotation.TargetApi
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.media.AudioManager
import android.media.MediaMetadata
import android.media.session.MediaSession
import android.media.session.PlaybackState
import android.os.Build
import android.view.KeyEvent
import androidx.annotation.RequiresApi
import com.filippoengidashet.player.utilities.ImageUtils

/**
 * @author Filippo Engidashet
 * @version 1.0.0
 * @since Sun, 2019-12-22 at 18:31.
 */
@RequiresApi(Build.VERSION_CODES.LOLLIPOP)
@TargetApi(Build.VERSION_CODES.LOLLIPOP)
class LollipopMediaSessionController(private val context: Context) : MediaSessionController() {

    companion object {
        const val TAG = "LollipopMediaSessionController"
    }

    private val mediaSession = MediaSession(context, TAG).apply {
        setCallback(mediaSessionCallback)
        setFlags(MediaSession.FLAG_HANDLES_MEDIA_BUTTONS or MediaSession.FLAG_HANDLES_TRANSPORT_CONTROLS)

        setMetadata(
            MediaMetadata.Builder()
                .putString(MediaMetadata.METADATA_KEY_TITLE, "Title")
                .putString(
                    MediaMetadata.METADATA_KEY_ALBUM_ART_URI,
                    ImageUtils.getResourceUri(
                        context.resources,
                        android.R.mipmap.sym_def_app_icon
                    ).toString()
                )
                .build()
        )

        setPlaybackState(
            PlaybackState.Builder()
                .setActions(PlaybackState.ACTION_PLAY or PlaybackState.ACTION_PLAY_PAUSE or PlaybackState.ACTION_PAUSE or PlaybackState.ACTION_REWIND or PlaybackState.ACTION_FAST_FORWARD)
                .setState(PlaybackState.STATE_PAUSED, PlaybackState.PLAYBACK_POSITION_UNKNOWN, 1.0f)
                .build()
        )
    }.also {
        it.isActive = true
    }
    private val receiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {

            if (Intent.ACTION_MEDIA_BUTTON == intent.action) {
                val event: KeyEvent? = intent.getParcelableExtra(Intent.EXTRA_KEY_EVENT)
            }
        }
    }

    init {
        val intentFilter = IntentFilter("android.intent.action.MEDIA_BUTTON")
        intentFilter.addAction(AudioManager.ACTION_AUDIO_BECOMING_NOISY)
        context.registerReceiver(receiver, intentFilter)
    }

    private val mediaSessionCallback = object : MediaSession.Callback() {

        override fun onMediaButtonEvent(mediaButtonIntent: Intent): Boolean {
            return super.onMediaButtonEvent(mediaButtonIntent)
        }

        override fun onStop() {
            super.onStop()
        }

        override fun onSkipToNext() {
            super.onSkipToNext()
        }

        override fun onSkipToPrevious() {
            super.onSkipToPrevious()
        }

        override fun onSeekTo(pos: Long) {
            super.onSeekTo(pos)
        }

        override fun onRewind() {
            super.onRewind()
        }

        override fun onPause() {
            super.onPause()
        }

        override fun onPlay() {
            super.onPlay()
        }
    }

    fun clean() {
        mediaSession.release()
        context.unregisterReceiver(receiver)
    }
}
