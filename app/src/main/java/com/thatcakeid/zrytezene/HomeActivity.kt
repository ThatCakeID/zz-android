package com.thatcakeid.zrytezene

import android.content.SharedPreferences
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.widget.SeekBar
import android.widget.SeekBar.OnSeekBarChangeListener
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.edit
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetBehavior.BottomSheetCallback
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.DocumentChange
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.QuerySnapshot
import com.thatcakeid.zrytezene.ZryteApp.Companion.parseDuration
import com.thatcakeid.zrytezene.adapters.HomeItemsRecyclerViewAdapter
import com.thatcakeid.zrytezene.adapters.HomeItemsRecyclerViewAdapter.ClickListener
import com.thatcakeid.zrytezene.data.MusicEntry
import com.thatcakeid.zrytezene.databinding.ActivityHomeBinding
import java.util.Random

class HomeActivity : AppCompatActivity() {
    private val binding: ActivityHomeBinding by lazy { ActivityHomeBinding.inflate(layoutInflater) }

//    private var musicEntries: ArrayList<HashMap<String, Any>> = ArrayList()
//    private var musicIndexes: ArrayList<String> = ArrayList()
    private var userIndexes: HashMap<String, String> = HashMap()
    private var currentPlaylist: ArrayList<MusicEntry>? = null
    private var playlistIndex: ArrayList<String>? = null

    private val musics: HashMap<String, MusicEntry> = HashMap()

    private val auth: FirebaseAuth by lazy { FirebaseAuth.getInstance() }
    private val musicCollection: CollectionReference by lazy {
        FirebaseFirestore.getInstance().collection("musics")
    }

    private val userCollection: CollectionReference by lazy {
        FirebaseFirestore.getInstance().collection("users")
    }

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

    private var currentPos = -1
    private var isReady = false
    private var isDragging = false

    private var preferences: SharedPreferences? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.enableEdgeToEdge(window)
        setContentView(binding.root)

        initializeVar()
        refreshRepeatState(preferences!!.getInt("playMode", 0))

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

            imageView4.setOnClickListener { playPrevious() }
            imageView6.setOnClickListener { playNext() }
            imageView5.setOnClickListener { togglePlay() }
            imageView8.setOnClickListener { togglePlay() }
            imageView9.setOnClickListener {
                var mode = preferences!!.getInt("playMode", 0)

                if (mode < 3) {
                    mode++
                } else {
                    mode = 0
                }

                preferences!!.edit { putInt("playMode", mode) }
                refreshRepeatState(mode)
            }
        }

        val adapter = HomeItemsRecyclerViewAdapter(
            applicationContext,
            musics.values.toList(),
            userIndexes
        )

        adapter.setOnItemClickListener(object : ClickListener {
            override fun onItemClick(position: Int, v: View) {
                currentPlaylist = ArrayList(musics.values)
                playlistIndex = ArrayList(musics.keys)
                currentPos = position

                play()
            }

            override fun onItemLongClick(position: Int, v: View) {}
        })

        binding.fpu.seekBar.setOnSeekBarChangeListener(object : OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
                binding.fpu.textView9.text = parseDuration((progress * 100).toLong())
                binding.fpu.progressBar3.setProgress(progress, true)
            }

            override fun onStartTrackingTouch(seekBar: SeekBar) {
                isDragging = true

                // Part of Media3 migration; check to-do comment at L62
                // exoPlayer.pause()
            }

            override fun onStopTrackingTouch(seekBar: SeekBar) {
                isDragging = false

                // Part of Media3 migration; check to-do comment at L62
                // exoPlayer.seekTo((seekBar.progress * 100).toLong())
                // exoPlayer.play()
            }
        })

        binding.rvItemsHome.apply {
            layoutManager = LinearLayoutManager(applicationContext)
            this.adapter = adapter
        }

        userCollection.addSnapshotListener { value: QuerySnapshot?, _ ->
            if (value == null) {
                Toast.makeText(
                    this@HomeActivity,
                    getString(R.string.collection_users_update_err_null),
                    Toast.LENGTH_SHORT
                ).show()

                return@addSnapshotListener
            }

            for (dc in value.documentChanges) {
                val data = dc.document.data as HashMap<String, Any>

                when (dc.type) {
                    DocumentChange.Type.ADDED, DocumentChange.Type.MODIFIED -> {
                        userIndexes[dc.document.id] = data["username"] as String

                        if (dc.document.id == auth.uid) {
                            if (data["img_url"] == "") {
                                // binding.userAppbarHome.setImageResource(R.drawable.ic_account_circle)
                            } else {
                                /**
                                Glide.with(applicationContext)
                                        .load(data["img_url"] as String?)
                                        .into(binding.userAppbarHome)
                                **/
                            }
                        }
                    }

                    DocumentChange.Type.REMOVED -> {
                        userIndexes.remove(dc.document.id)

                        if (dc.document.id == auth.uid) {
                            // binding.userAppbarHome.setImageResource(R.drawable.ic_account_circle)
                        }
                    }
                }

                adapter.updateItems(this, null, userIndexes)
                @Suppress("NotifyDataSetChanged")
                adapter.notifyDataSetChanged()
            }
        }

        musicCollection.addSnapshotListener { value: QuerySnapshot?, _ ->
            if (value == null) {
                Toast.makeText(
                    this@HomeActivity,
                    getString(R.string.collection_musics_update_err_null),
                    Toast.LENGTH_SHORT
                ).show()

                return@addSnapshotListener
            }

            for (dc in value.documentChanges) {
                val data = MusicEntry.from(dc.document.data)
                when (dc.type) {
                    DocumentChange.Type.ADDED -> {
                        musics[dc.document.id] = data
                    }

                    DocumentChange.Type.MODIFIED -> {
                        musics[dc.document.id] = data

                        if (currentPlaylist != null) currentPlaylist!![playlistIndex!!.indexOf(dc.document.id)] = data
                    }

                    DocumentChange.Type.REMOVED -> {
                        musics.remove(dc.document.id)

                        if (currentPlaylist != null) {
                            currentPlaylist!!.remove(data)
                            playlistIndex!!.remove(dc.document.id)
                        }
                    }
                }

                adapter.updateItems(this, musics.values.toList(), null)
                @Suppress("NotifyDataSetChanged")
                adapter.notifyDataSetChanged()
            }
        }

        bottomSheetBehavior.addBottomSheetCallback(object : BottomSheetCallback() {
            override fun onStateChanged(bottomSheet: View, newState: Int) {
                if (newState == BottomSheetBehavior.STATE_HIDDEN && currentPos != -1) {
                    stop()
                }
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

    private fun initializeVar() {
        binding.fpu.textView4.isSelected = true
        binding.fpu.textView7.isSelected = true
        preferences = getSharedPreferences(getString(R.string.main_preferences_key), MODE_PRIVATE)
    }

    private fun play() {
        if (currentPlaylist != null && currentPos > -1) {
            handler.removeCallbacks(runnable)

            // Part of Media3 migration; check to-do comment at L62
            // exoPlayer.stop()
            isReady = false

            // Part of Media3 migration; check to-do comment at L62
            // exoPlayer.setMediaItem(MediaItem.fromUri((currentPlaylist!![currentPos].musicUrl)))
            // exoPlayer.prepare()

            binding.fpu.apply {
                textView7.text = currentPlaylist!![currentPos].title
                textView8.text = if (userIndexes.containsKey(currentPlaylist!![currentPos].authorUserId)) userIndexes[currentPlaylist!![currentPos].authorUserId] else currentPlaylist!![currentPos].authorUserId
                textView4.text = currentPlaylist!![currentPos].title
                textView6.text = if (userIndexes.containsKey(currentPlaylist!![currentPos].authorUserId)) userIndexes[currentPlaylist!![currentPos].authorUserId] else currentPlaylist!![currentPos].authorUserId

                resetProgressBar()

                textView9.text = "--:--"
                textView10.text = "--:--"

                if (currentPlaylist!![currentPos].thumb == null) {
                    imageView3.setImageResource(R.drawable.ic_zrytezene)
                    imageView7.setImageResource(R.drawable.ic_zrytezene)

                } else {
                    Glide.with(applicationContext)
                        .load(currentPlaylist!![currentPos].thumb)
                        .into(imageView3)

                    Glide.with(applicationContext)
                        .load(currentPlaylist!![currentPos].thumb)
                        .into(imageView7)
                }

                seekBar.isEnabled = false

                if (bottomSheetBehavior.state == BottomSheetBehavior.STATE_HIDDEN)
                    bottomSheetBehavior.state = BottomSheetBehavior.STATE_EXPANDED

                musicCollection
                    .document(playlistIndex!![currentPos])
                    .update("plays", currentPlaylist!![currentPos].plays.toInt() + 1)
            }

        } else {
            Toast.makeText(
                this,
                getString(R.string.music_playlist_empty),
                Toast.LENGTH_LONG
            ).show()
        }
    }

    private fun togglePlay() {
        // Part of Media3 migration; check to-do comment at L62
        // if (exoPlayer.isPlaying) exoPlayer.pause() else exoPlayer.play()
    }

    private fun playNext() {
        when (preferences!!.getInt("playMode", 0)) {
            0 -> if (currentPos + 1 < currentPlaylist!!.size) {
                currentPos++
                play()
            } else {
                stop()
            }

            1 -> {
                if (currentPos + 1 < currentPlaylist!!.size) {
                    currentPos++
                } else {
                    currentPos = 0
                }
                play()
            }

            2 -> play()
            3 -> {
                currentPos = Random().nextInt(currentPlaylist!!.size)
                play()
            }
        }
    }

    private fun playPrevious() {
        when (preferences!!.getInt("playMode", 0)) {
            0 -> if (currentPos > 0) {
                currentPos--
                play()
            } else stop()

            1 -> {
                if (currentPos > 0) {
                    currentPos--
                } else {
                    currentPos = currentPlaylist!!.size - 1
                }

                play()
            }

            2 -> play()

            3 -> {
                currentPos = Random().nextInt(currentPlaylist!!.size)
                play()
            }
        }
    }

    private fun stop() {
        handler.removeCallbacks(runnable)
        // Part of Media3 migration; check to-do comment at L62
        // exoPlayer.stop()
        isReady = false
        currentPos = -1
        currentPlaylist = null
        playlistIndex = null
        bottomSheetBehavior.state = BottomSheetBehavior.STATE_HIDDEN
    }

    private fun resetProgressBar() {
        binding.fpu.apply {
            seekBar.setProgress(0, true)
            seekBar.max = 100
            seekBar.secondaryProgress = 0
            progressBar3.max = 100
            progressBar3.secondaryProgress = 0
        }
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

    private fun refreshRepeatState(mode: Int) {
        binding.fpu.imageView9.apply {
            alpha = 1.0f

            when (mode) {
                0 -> {
                    alpha = 0.5f
                    setImageResource(R.drawable.ic_repeat)
                }

                1 -> setImageResource(R.drawable.ic_repeat)
                2 -> setImageResource(R.drawable.ic_repeat_one)
                3 -> setImageResource(R.drawable.ic_shuffle)
            }
        }
    }
}