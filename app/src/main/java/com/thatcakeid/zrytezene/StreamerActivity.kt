package com.thatcakeid.zrytezene

import android.content.SharedPreferences
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.widget.SeekBar
import android.widget.SeekBar.OnSeekBarChangeListener
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetBehavior.BottomSheetCallback
import com.google.firebase.FirebaseApp
import com.thatcakeid.zrytezene.ZryteApp.Companion.parseDuration
import com.thatcakeid.zrytezene.databinding.ActivityStreamerBinding

class StreamerActivity : AppCompatActivity() {
    private val binding: ActivityStreamerBinding by lazy { ActivityStreamerBinding.inflate(layoutInflater) }

    private val bottomSheetBehavior: BottomSheetBehavior<*> by lazy {
        BottomSheetBehavior
            .from(binding.fpu.root)
            .also {
                it.state = BottomSheetBehavior.STATE_HIDDEN
            }
    }

    private val windowInsetsController: WindowInsetsControllerCompat by lazy {
        WindowCompat.getInsetsController(window, window.decorView)
    }

    // TODO: (10/11/2025) Migrate to Media3 API
    /**
    private val exoPlayer: SimpleExoPlayer by lazy {
        val cache = SimpleCache(
            getExoPlayerCacheDir(applicationContext),
            exoPlayerCacheEvictor,
            ExoDatabaseProvider(applicationContext)
        )

        val mediaSourceFactory = DefaultMediaSourceFactory(
            CacheDataSource.Factory().setCache(cache).setUpstreamDataSourceFactory(
                DefaultHttpDataSource.Factory().setUserAgent("ZryteZene")
            )
        )

        SimpleExoPlayer.Builder(this)
            .setMediaSourceFactory(mediaSourceFactory)
            .build()
            .also {
                it.setAudioAttributes(audioAttributes, true)
                it.addListener(playbackStateListener)
            }
    }

    private val audioAttributes: AudioAttributes =
        AudioAttributes.Builder()
            .setUsage(C.USAGE_MEDIA)
            .setContentType(C.CONTENT_TYPE_MUSIC)
            .build()

    private val playbackStateListener = PlaybackStateListener()
    **/

    private var handler: Handler = Handler(Looper.getMainLooper())
    private val runnable: Runnable = object : Runnable {
        override fun run() {
            binding.fpu.apply {
                // Part of Media3 migration; check to-do comment at L62
                /**
                if (exoPlayer.isPlaying) {
                    seekBar.progress = exoPlayer.currentPosition.toInt() / 100

                    imageView5.setImageResource(R.drawable.ic_pause)
                    imageView8.setImageResource(R.drawable.ic_pause)
                } else {
                    imageView5.setImageResource(R.drawable.ic_play_arrow)
                    imageView8.setImageResource(R.drawable.ic_play_arrow)
                }

                val progress = exoPlayer.bufferedPercentage * exoPlayer.duration.toInt() / 10000

                seekBar.secondaryProgress = progress
                progressBar3.secondaryProgress = progress
                **/
            }

            handler.postDelayed(this, 100)
        }
    }

    private var preferences: SharedPreferences? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.enableEdgeToEdge(window)
        setContentView(binding.root)

        binding.fpu.textView4.isSelected = true
        binding.fpu.textView7.isSelected = true
        preferences = getSharedPreferences(getString(R.string.main_preferences_key), MODE_PRIVATE)

        FirebaseApp.initializeApp(this)

        windowInsetsController.systemBarsBehavior =
            WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE

        /**
        binding.userAppbarHome.setOnClickListener {
            startActivity(
                if (auth.currentUser == null) {
                    Intent(this@HomeActivity, LoginActivity::class.java)
                } else {
                    Intent(this@HomeActivity, ProfileActivity::class.java).apply {
                        putExtra("uid", auth.uid)
                    }
                }
            )
        }
        **/

        binding.fpu.apply {
            imageView2.setOnClickListener {
                bottomSheetBehavior.setState(
                    if (bottomSheetBehavior.state == BottomSheetBehavior.STATE_COLLAPSED)
                        BottomSheetBehavior.STATE_EXPANDED
                    else
                        BottomSheetBehavior.STATE_COLLAPSED
                )
            }

        }


        binding.fpu.seekBar.setOnSeekBarChangeListener(object : OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
                binding.fpu.textView9.text = parseDuration((progress * 100).toLong())
                binding.fpu.progressBar3.setProgress(progress, true)
            }

            override fun onStartTrackingTouch(seekBar: SeekBar) {}

            override fun onStopTrackingTouch(seekBar: SeekBar) {}
        })

        binding.rvItemsHome.apply {
            layoutManager = LinearLayoutManager(applicationContext)
            this.adapter = adapter
        }

        bottomSheetBehavior.addBottomSheetCallback(object : BottomSheetCallback() {
            override fun onStateChanged(bottomSheet: View, newState: Int) {
                if (newState == BottomSheetBehavior.STATE_EXPANDED) {
                    windowInsetsController.hide(WindowInsetsCompat.Type.systemBars())
                } else {
                    windowInsetsController.show(WindowInsetsCompat.Type.systemBars())
                }
                binding.appBarLayout.visibility =
                    if (newState == BottomSheetBehavior.STATE_EXPANDED) View.GONE else View.VISIBLE
            }

            override fun onSlide(bottomSheet: View, slideOffset: Float) {
                binding.fpu.apply {
                    binding.appBarLayout.alpha = 1f - (slideOffset.coerceIn(0f, 1f))

                    imageView2.rotation =
                        if (slideOffset >= 0) slideOffset * 180f else 0f

                    compactPlayer.alpha =
                        if (slideOffset >= 0) 1 - slideOffset else slideOffset + 1

                    compactPlayer.visibility =
                        if (slideOffset < 1 && slideOffset > -1) View.VISIBLE else View.INVISIBLE
                }
            }
        })
    }

    // Part of Media3 migration; check to-do comment at L62
    /**
    private inner class PlaybackStateListener : Player.Listener {
        override fun onPlaybackStateChanged(state: Int) {
            fpuBinding.apply {
                when (state) {
                    ExoPlayer.STATE_READY -> {
                        if (!isReady) {
                            textView9.text = "0:00"
                            textView10.text = parseDuration(exoPlayer.duration)

                            seekBar.max = exoPlayer.duration.toInt() / 100
                            seekBar.isEnabled = true

                            progressBar3.max = exoPlayer.duration.toInt() / 100

                            isReady = true
                            handler.post(runnable)
                            exoPlayer.play()
                        }

                        progressBar.visibility = View.INVISIBLE
                        progressBar2.visibility = View.INVISIBLE

                        imageView5.visibility = View.VISIBLE
                        imageView8.visibility = View.VISIBLE
                    }

                    ExoPlayer.STATE_BUFFERING -> {
                        progressBar.visibility = View.VISIBLE
                        progressBar2.visibility = View.VISIBLE

                        imageView5.visibility = View.INVISIBLE
                        imageView8.visibility = View.INVISIBLE
                    }

                    ExoPlayer.STATE_ENDED -> playNext()

                    ExoPlayer.STATE_IDLE -> {
                        // Do nothing
                    }
                }
            }
        }
    }
     **/
}