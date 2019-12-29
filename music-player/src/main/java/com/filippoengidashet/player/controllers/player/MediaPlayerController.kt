package com.filippoengidashet.player.controllers.player

import android.content.Context
import android.media.MediaPlayer
import android.os.Build
import android.os.Handler
import com.filippoengidashet.player.controllers.audio.AudioFocusCallback
import com.filippoengidashet.player.controllers.audio.AudioFocusController
import com.filippoengidashet.player.controllers.audio.LegacyAudioFocusController
import com.filippoengidashet.player.controllers.audio.OreoAudioFocusController
import com.filippoengidashet.player.controllers.session.LollipopMediaSessionController
import com.filippoengidashet.player.model.Song
import java.util.*
import java.util.concurrent.TimeUnit

/**
 * @author Filippo Engidashet
 * @version 1.0.0
 * @since Sat, 2019-12-21 at 16:02.
 */
class MediaPlayerController(context: Context, private val callback: Callback) :
    MediaPlayer.OnPreparedListener,
    MediaPlayer.OnErrorListener, MediaPlayer.OnCompletionListener,
    MediaPlayer.OnBufferingUpdateListener, AudioFocusCallback {

    //player progress
    //player state

    enum class PlaybackModeType {
        /**
         * Continuous loop from start to end
         */
        NORMAL,
        /**
         * Play single song and stop
         */
        SINGLE,
        /**
         * Repeat current playing song
         */
        REPEAT,
        /**
         * Play random song
         */
        SHUFFLE
    }

//    sealed class StateTyped {
//
//        object IDLE : StateTyped()
//        object LOADING : StateTyped()
//        class PLAYING : StateTyped()
//        class INTERRUPTED(val reason: String) : StateTyped()
//        object PAUSED : StateTyped()
//        object STOPPED : StateTyped()
//    }

    enum class StateType {
        IDLE,
        LOADING,
        PLAYING,
        PAUSED,
        STOPPED
    }

    private val mediaPlayer: MediaPlayer = MediaPlayer().also {
        it.isLooping = false
//        it.setWakeMode(context, PowerManager.PARTIAL_WAKE_LOCK)
        it.setVolume(1.0f, 1.0f)
        it.setOnPreparedListener(this)
        it.setOnErrorListener(this)
        it.setOnCompletionListener(this)
        it.setOnBufferingUpdateListener(this)
    }

    private val handler: Handler = Handler()
    private val audioFocusController: AudioFocusController =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) OreoAudioFocusController(
            context,
            handler
        ) else LegacyAudioFocusController(context)

    private val playList: MutableList<Song> = mutableListOf()


    private var playerState: StateType = StateType.IDLE
    private var playbackMode: PlaybackModeType = PlaybackModeType.NORMAL
    private var indexOfCurrentSong: Int = 0
    private var audioFocusAcquired: Boolean = false
    private var wasInterrupted: Boolean = false
    private var playbackVolume: Int = 0

    init {
        audioFocusController.setCallback(this)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            val sessionController = LollipopMediaSessionController(context)
        }
    }

    fun setPlaybackMode(mode: PlaybackModeType) {
        playbackMode = mode
    }

    fun setPlaylist(songs: List<Song>) {
        clearList()
        playList.addAll(songs)
    }

    fun addSong(song: Song, index: Int = playList.size) {
        playList.add(index, song)
    }

    fun addPlayList(songs: List<Song>, index: Int = playList.size) {
        playList.addAll(index, songs)
    }

    fun removeAt(index: Int) {
        playList.removeAt(index)
    }

    fun clearList() = playList.clear()

    fun play(position: Int) {
        if (indexOfCurrentSong != position && playList.size > position && position >= 0) {
            indexOfCurrentSong = position
            prepareSong()
        }
    }

    fun play() {
        if (playerState == StateType.PAUSED) {
            startPlaying()
        } else {
            prepareSong()
        }
    }

    private fun prepareSong() {
        try {
            mediaPlayer.reset()
        } catch (e: Exception) {
        }
        val song = playList[indexOfCurrentSong]
        mediaPlayer.setDataSource(song.source)
        notifyPlayerState(StateType.LOADING)
        mediaPlayer.prepareAsync()
        callback.onNotifyPosition(indexOfCurrentSong + 1, song.source)
    }

    private fun startPlaying() {
        if (!audioFocusAcquired) {
            audioFocusController.requestAudioFocus()
        }
        mediaPlayer.start()
        notifyPlayerState(StateType.PLAYING)
        handler.postDelayed(progressUpdateTask, 0)
    }

    fun playNext() {
        val nextPosition = indexOfCurrentSong + 1
        indexOfCurrentSong = if (nextPosition < playList.size) nextPosition else 0
        prepareSong()
    }

    fun playPrevious() {
        val previousPosition = indexOfCurrentSong - 1
        indexOfCurrentSong = if (previousPosition >= 0) previousPosition else playList.size - 1
        prepareSong()
    }

    fun seekTo(progress: Int) {
        val seekProgress = (mediaPlayer.duration / 1000) * progress
        mediaPlayer.seekTo(seekProgress)
//        mediaPlayer.seekTo(progress)
    }

    fun pause(duck: Boolean = false) {
        wasInterrupted = duck
        if (mediaPlayer.isPlaying) {
            mediaPlayer.pause()
            notifyPlayerState(StateType.PAUSED)
        }
    }

    private fun notifyPlayerState(state: StateType) {
        playerState = state
        callback.onPlayerStateChanged(state)
    }

    override fun onPrepared(mp: MediaPlayer) {
        //isPrepared = true
        if (!mp.isPlaying) {
            startPlaying()

            val duration = mp.duration

            val minutes = formatValue(TimeUnit.MILLISECONDS.toMinutes(duration.toLong()))
            val seconds = formatValue(
                TimeUnit.MILLISECONDS.toSeconds(duration.toLong()) - TimeUnit.MINUTES.toSeconds(
                    TimeUnit.MILLISECONDS.toMinutes(duration.toLong())
                )
            )
            callback.onPlayerStart(duration / 1000, "$minutes:$seconds")
        }
    }

    private fun formatValue(value: Long) = "%02d".format(value)

    override fun onError(mp: MediaPlayer, what: Int, extra: Int): Boolean {
        callback.onToast("onError")
        return false
    }

    override fun onCompletion(mp: MediaPlayer) {
        if (playbackMode == PlaybackModeType.REPEAT) {
            mp.seekTo(0)
            mp.start()
        } else if (playbackMode == PlaybackModeType.SHUFFLE) {
            indexOfCurrentSong = Random().nextInt(playList.size)
            prepareSong()
        } else {
            if (playbackMode != PlaybackModeType.SINGLE) {
                playNext()
            }
        }
    }

    override fun onBufferingUpdate(mp: MediaPlayer, percent: Int) {
        callback.onBufferingUpdate(percent)
    }

    override fun onAudioFocusStateChanged(stateType: AudioFocusCallback.StateType) {
        callback.onToast(stateType.name)

        audioFocusAcquired = stateType == AudioFocusCallback.StateType.FOCUS_GAINED
        if (audioFocusAcquired) {
            if (wasInterrupted) startPlaying()
        } else {
//            mediaPlayer.setVolume(0.3f, 0.3f)
//            mediaPlayer.setVolume(1.0f, 1.0f);
            pause(stateType == AudioFocusCallback.StateType.FOCUS_LOST_DUCK)
        }
    }

    fun stop() {
        try {
            mediaPlayer.stop()
        } catch (e: Exception) {
        }
        audioFocusController.abandonAudioFocus()
        audioFocusAcquired = false
        notifyPlayerState(StateType.STOPPED)
    }

    fun cleanup() {
        stop()
        mediaPlayer.reset()
        mediaPlayer.release()
    }

    private val progressUpdateTask = object : Runnable {

        override fun run() {
            if (mediaPlayer.isPlaying) {
                val currentPosition = mediaPlayer.currentPosition
                val currentPositionAsLong = currentPosition.toLong()

                val minutes = formatValue(TimeUnit.MILLISECONDS.toMinutes(currentPositionAsLong))
                val seconds = formatValue(
                    TimeUnit.MILLISECONDS.toSeconds(currentPositionAsLong) - TimeUnit.MINUTES.toSeconds(
                        TimeUnit.MILLISECONDS.toMinutes(currentPositionAsLong)
                    )
                )
                callback.onProgressUpdate(currentPosition / 1000, "$minutes:$seconds")

                handler.postDelayed(this, 1000)
            }
        }
    }

    interface Callback {

        fun onPlayerStateChanged(state: StateType)

        fun onBufferingUpdate(percent: Int)

        fun onPlayerStart(maxDuration: Int, totalDuration: String)

        fun onProgressUpdate(progress: Int, elapsed: String)

        fun onToast(message: String)

        fun onNotifyPosition(position: Int, source: String)
    }
}
