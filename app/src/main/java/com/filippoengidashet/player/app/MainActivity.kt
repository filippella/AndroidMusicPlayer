package com.filippoengidashet.player.app

import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.*
import com.filippoengidashet.player.controllers.player.MediaPlayerController
import com.filippoengidashet.player.model.Song

class MainActivity : AppCompatActivity(), MediaPlayerController.Callback {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val song0 = Song("http://techslides.com/demos/samples/sample.mp3")
        val song1 =
            Song("http://www.music.helsinki.fi/tmt/opetus/uusmedia/esim/a2002011001-e02-128k.mp3")
        val song2 = Song("http://www.villopim.com.br/android/Music_01.mp3")
        val song3 = Song("https://www.soundhelix.com/examples/mp3/SoundHelix-Song-1.mp3")
        val song4 = Song("https://www.soundhelix.com/examples/mp3/SoundHelix-Song-16.mp3")
        val song5 =
            Song("https://github.com/filippella/Sample-API-Files/raw/master/LHiver%20Indien%20-%20Balijo.mp3")

        val songs: List<Song> = listOf(song0, song1, song2, song3, song4, song5)

        val controller = MediaPlayerController(this, this)
        controller.setPlaylist(songs)
        val s1 = Song(
            "https://file-examples.com/wp-content/uploads/2017/11/file_example_MP3_700KB.mp3"
        )
        val s2 = Song("http://www.hochmuth.com/mp3/Haydn_Cello_Concerto_D-1.mp3")
        val s3 = Song("http://www.hochmuth.com/mp3/Tchaikovsky_Rococo_Var_orch.mp3")
        val s4 = Song("http://www.hochmuth.com/mp3/Vivaldi_Sonata_eminor_.mp3")
        val s5 = Song("http://www.hochmuth.com/mp3/Tchaikovsky_Nocturne__orch.mp3")
        val s6 = Song("http://www.hochmuth.com/mp3/Haydn_Adagio.mp3")
        val s7 = Song("http://www.hochmuth.com/mp3/Boccherini_Concerto_478-1.mp3")
        val s8 = Song("http://www.hochmuth.com/mp3/Bloch_Prayer.mp3")
        val s9 = Song("http://www.hochmuth.com/mp3/Beethoven_12_Variation.mp3")

        controller.addPlayList(listOf(s1, s2, s3, s4, s5, s6, s7, s8, s9))

        findViewById<RadioGroup>(R.id.playerMode)
            .setOnCheckedChangeListener { _, checkedId ->
                when (checkedId) {
                    R.id.radioButtonNormal -> {
                        controller.setPlaybackMode(MediaPlayerController.PlaybackModeType.NORMAL)
                    }
                    R.id.radioButtonShuffle -> {
                        controller.setPlaybackMode(MediaPlayerController.PlaybackModeType.SHUFFLE)
                    }
                    R.id.radioButtonRepeat -> {
                        controller.setPlaybackMode(MediaPlayerController.PlaybackModeType.REPEAT)
                    }
                    R.id.radioButtonSingle -> {
                        controller.setPlaybackMode(MediaPlayerController.PlaybackModeType.SINGLE)
                    }
                }
            }

        val btnPlay = findViewById<View>(R.id.buttonPlay)
            .setOnClickListener {
                controller.play()
            }
        val btnPause = findViewById<View>(R.id.buttonPause)
            .setOnClickListener {
                controller.pause()
            }
        val btnStop = findViewById<View>(R.id.buttonStop)
            .setOnClickListener {
                controller.stop()
            }
        val btnNext = findViewById<View>(R.id.buttonNext)
            .setOnClickListener {
                controller.playNext()
            }
        val btnPrevious = findViewById<View>(R.id.buttonPrevious)
            .setOnClickListener {
                controller.playPrevious()
            }

        val viewSeekBar = findViewById<SeekBar>(R.id.slider_player_progress).apply {
            setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
                override fun onProgressChanged(
                    seekBar: SeekBar?,
                    progress: Int,
                    fromUser: Boolean
                ) {
                    if (fromUser) {
                        controller.seekTo(progress)
                    }
                }

                override fun onStartTrackingTouch(seekBar: SeekBar?) {

                }

                override fun onStopTrackingTouch(seekBar: SeekBar?) {
                }
            })
        }
        val viewSongAvatar = findViewById<ImageView>(R.id.image_singer_avatar)
        val viewSongName = findViewById<TextView>(R.id.text_song_name).apply {
            isSelected = true
            setSingleLine()
        }
        val buttonPlayPause = findViewById<ToggleButton>(R.id.controlButtonPlayPause)
            .setOnCheckedChangeListener { _, isChecked ->
                if(isChecked) {
                    //play
                    controller.play()
                } else {
                    //pause
                    controller.pause()
                }
            }
    }

    override fun onPlayerStateChanged(state: MediaPlayerController.StateType) {
        state.name.toast(this)
    }

    override fun onBufferingUpdate(percent: Int) {
        val seekBar = findViewById<SeekBar>(R.id.slider_player_progress)
        seekBar.secondaryProgress = seekBar.max * percent
    }

    override fun onPlayerStart(maxDuration: Int, totalDuration: String) {
        findViewById<SeekBar>(R.id.slider_player_progress).max = maxDuration
        findViewById<TextView>(R.id.text_duration).text = totalDuration
    }

    override fun onProgressUpdate(progress: Int, elapsed: String) {
        findViewById<SeekBar>(R.id.slider_player_progress).progress = progress
        findViewById<TextView>(R.id.text_elapsed).text = elapsed
    }

    override fun onToast(message: String) {
        message.toast(this)
    }

    override fun onNotifyPosition(position: Int, source: String) {
        findViewById<TextView>(R.id.textViewPosition).text = position.toString()
        findViewById<TextView>(R.id.text_song_name).text = source
    }

    fun String.toast(context: Context) = Toast.makeText(context, this, Toast.LENGTH_SHORT).show()
}
