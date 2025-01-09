package com.thatcakeid.zrytezene.services

import android.content.Intent
import android.content.SharedPreferences
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.support.v4.media.MediaBrowserCompat
import android.support.v4.media.session.MediaSessionCompat
import android.support.v4.media.session.PlaybackStateCompat
import androidx.media.MediaBrowserServiceCompat
import com.google.android.exoplayer2.*
import com.google.android.exoplayer2.audio.AudioAttributes
import com.google.android.exoplayer2.database.ExoDatabaseProvider
import com.google.android.exoplayer2.source.DefaultMediaSourceFactory
import com.google.android.exoplayer2.upstream.DefaultHttpDataSource
import com.google.android.exoplayer2.upstream.cache.CacheDataSource
import com.google.android.exoplayer2.upstream.cache.SimpleCache
import com.thatcakeid.zrytezene.ExtraMetadata
import com.thatcakeid.zrytezene.R
import com.thatcakeid.zrytezene.data.MusicEntry

class MusicService : MediaBrowserServiceCompat() {
    private var mMediaSession: MediaSessionCompat? = null
    private lateinit var mStateBuilder: PlaybackStateCompat.Builder

    private var mExoPlayer: SimpleExoPlayer? = null
    private var audioSpeed = 0f

    private val playbackStateListener = PlaybackStateListener()
    private var playerState = 0

    private var playlist : ArrayList<MusicEntry>? = null
    private var currentPos = -1

    private val handler = Handler(Looper.getMainLooper())
    private val runnable = object : Runnable {
        override fun run() {
            mMediaSession?.setPlaybackState(
                PlaybackStateCompat.Builder().setState(
                    playerState,
                    mExoPlayer!!.currentPosition,
                    audioSpeed
                ).build()
            )
            handler.postDelayed(this, 100)
        }
    }

    private lateinit var preferences: SharedPreferences

    private val mMediaSessionCallback = object : MediaSessionCompat.Callback() {
        override fun onCustomAction(action: String?, extras: Bundle?) {
            super.onCustomAction(action, extras)
            if (action == "playFromArray") {
                playlist = extras?.get("array") as ArrayList<MusicEntry>
                play(extras.getInt("pos"))
            }
        }

        override fun onPause() {
            super.onPause()
            pause()
        }

        override fun onStop() {
            super.onStop()
            stop()
        }

        override fun onPlay() {
            super.onPlay()
            play()
        }

        override fun onSeekTo(pos: Long) {
            super.onSeekTo(pos)
            mExoPlayer?.seekTo(pos)
        }

        override fun onSkipToNext() {
            super.onSkipToNext()
            playNext()
        }

        override fun onSkipToPrevious() {
            super.onSkipToPrevious()
        }

        override fun onSetPlaybackSpeed(speed: Float) {
            super.onSetPlaybackSpeed(speed)

            /** Since this method doesn't provide a way to set pitch, we'll just use this method
             * to call a function to update it
             **/
            updateSpeed()
        }
    }

    private val audioAttributes: AudioAttributes =
        AudioAttributes.Builder()
            .setUsage(C.USAGE_MEDIA)
            .setContentType(C.CONTENT_TYPE_MUSIC)
            .build()

    override fun onCreate() {
        super.onCreate()
        preferences = getSharedPreferences("data", MODE_PRIVATE)
        initializePlayer()
        updateSpeed()
        mMediaSession = MediaSessionCompat(baseContext, "PLAYERSESSION").apply {
            mStateBuilder = PlaybackStateCompat.Builder()
                .setActions(PlaybackStateCompat.ACTION_PLAY or PlaybackStateCompat.ACTION_PLAY_PAUSE)

            setPlaybackState(mStateBuilder.build())
            setCallback(mMediaSessionCallback)

            setSessionToken(sessionToken)
            isActive = true
        }
    }

    private fun initializePlayer() {
        val cache = SimpleCache(
            ExtraMetadata.getExoPlayerCacheDir(applicationContext),
            ExtraMetadata.exoPlayerCacheEvictor,
            ExoDatabaseProvider(applicationContext)
        )

        val mediaSourceFactory = DefaultMediaSourceFactory(
            CacheDataSource.Factory().setCache(cache).setUpstreamDataSourceFactory(
                DefaultHttpDataSource.Factory().setUserAgent("ZryteZene")
            )
        )

        mExoPlayer = SimpleExoPlayer.Builder(applicationContext)
            .setMediaSourceFactory(mediaSourceFactory)
            .build()
            .also {
                it.setAudioAttributes(audioAttributes, true)
                it.addListener(playbackStateListener)
            }
    }

    private fun play(pos : Int) {
        handler.removeCallbacks(runnable)
        mExoPlayer?.apply {
            stop()
            setMediaItem(MediaItem.fromUri(playlist!![pos].musicUrl))
            prepare()
        }
    }

    private fun play() {
        mExoPlayer?.play()
        updatePlaybackState(PlaybackStateCompat.STATE_PLAYING)
        mMediaSession?.isActive = true
    }

    private fun playNext() {
        when(preferences.getInt("playMode", 0)) {
            0 -> if (playlist!!.size > currentPos + 1) {
                play(currentPos + 1)
            } else stop()
            1 -> if (currentPos > 0) {
                play(currentPos - 1)
            } else stop()
            2 -> play()
        }
    }

    private fun pause() {
        mExoPlayer?.pause()
        updatePlaybackState(PlaybackStateCompat.STATE_PAUSED)
    }

    private fun stop() {
        currentPos = -1
        playlist = null
        handler.removeCallbacks(runnable)
        mExoPlayer?.stop()
        updatePlaybackState(PlaybackStateCompat.STATE_STOPPED)
    }

    private fun destroy() {
        currentPos = -1
        playlist = null
        handler.removeCallbacks(runnable)
        mExoPlayer?.stop()
        mExoPlayer?.release()
        mExoPlayer = null
        mMediaSession?.isActive = false
        mMediaSession?.release()
    }

    private fun updateSpeed() {
        val speed = preferences.getFloat("speed", 1.0f)
        val pitch = preferences.getFloat("pitch", 1.0f)
        mExoPlayer?.playbackParameters = PlaybackParameters(speed, pitch)
    }

    override fun onTaskRemoved(rootIntent: Intent?) {
        super.onTaskRemoved(rootIntent)
        stopSelf()
    }

    override fun onGetRoot(clientPackageName: String, clientUid: Int, rootHints: Bundle?): BrowserRoot {
        return BrowserRoot(getString(R.string.app_name), null)
    }

    override fun onLoadChildren(parentId: String, result: Result<MutableList<MediaBrowserCompat.MediaItem>>) {
    }

    override fun onDestroy() {
        super.onDestroy()
        destroy()
    }

    private fun updatePlaybackState(state: Int) {
        playerState = state
        mMediaSession?.setPlaybackState(
            PlaybackStateCompat.Builder().setState(
                state,
                mExoPlayer!!.currentPosition,
                audioSpeed
            ).build()
        )
    }

    private inner class PlaybackStateListener : Player.Listener {
        override fun onPlaybackStateChanged(playbackState: Int) {
            super.onPlaybackStateChanged(playbackState)
            when(playbackState) {
                ExoPlayer.STATE_READY -> {
                    handler.post(runnable)
                    play()
                }

                ExoPlayer.STATE_ENDED -> playNext()

                ExoPlayer.STATE_BUFFERING -> {
                    updatePlaybackState(PlaybackStateCompat.STATE_BUFFERING)
                }
                ExoPlayer.STATE_IDLE -> { } // Nothing to do
            }
        }
    }
}