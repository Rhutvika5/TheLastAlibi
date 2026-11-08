
package com.example.thelastalibi

import android.app.Activity
import android.app.Application
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.media.SoundPool
import android.os.Bundle
import android.view.Gravity
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.TextView
import java.util.WeakHashMap

object GameAudioManager {

    private const val PREFS = "last_alibi_audio"
    private const val KEY_ENABLED = "audio_enabled"
    private const val OVERLAY_TAG = "last_alibi_audio_control"

    private var appContext: Context? = null
    private var mediaPlayer: MediaPlayer? = null
    private var soundPool: SoundPool? = null

    private var initialized = false
    private var callbacksRegistered = false
    private var audioEnabled = true

    private var clickSound = 0
    private var transitionSound = 0
    private var evidenceSound = 0

    private val loadedSounds = mutableSetOf<Int>()

    private val audioControls = WeakHashMap<Activity, TextView>()
    private val audioWiredViews = WeakHashMap<View, Boolean>()
    private val clickWiredViews = WeakHashMap<View, Boolean>()
    private val audioListeners = WeakHashMap<Activity, (Boolean) -> Unit>()

    // ---------------------------------------------------------
    // ACTIVITY LIFECYCLE
    // ---------------------------------------------------------

    private val lifecycleCallbacks =
        object : Application.ActivityLifecycleCallbacks {

            override fun onActivityCreated(
                activity: Activity,
                savedInstanceState: Bundle?
            ) {
                activity.window.decorView.post {
                    configureActivity(activity)

                    if (activity !is MainActivity) {
                        playPageTransition()
                    }
                }
            }

            override fun onActivityResumed(activity: Activity) {
                activity.window.decorView.post {
                    configureActivity(activity)
                }
            }

            override fun onActivityStarted(activity: Activity) {}
            override fun onActivityPaused(activity: Activity) {}
            override fun onActivityStopped(activity: Activity) {}

            override fun onActivitySaveInstanceState(
                activity: Activity,
                outState: Bundle
            ) {}

            override fun onActivityDestroyed(activity: Activity) {
                audioControls.remove(activity)
                audioListeners.remove(activity)
            }
        }

    // ---------------------------------------------------------
    // INITIALIZE AUDIO SYSTEM
    // ---------------------------------------------------------

    @Synchronized
    fun initialize(context: Context) {

        val application = context.applicationContext

        if (!initialized) {

            appContext = application

            audioEnabled = application
                .getSharedPreferences(PREFS, Context.MODE_PRIVATE)
                .getBoolean(KEY_ENABLED, true)

            val attributes = AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_GAME)
                .setContentType(
                    AudioAttributes.CONTENT_TYPE_SONIFICATION
                )
                .build()

            soundPool = SoundPool.Builder()
                .setMaxStreams(5)
                .setAudioAttributes(attributes)
                .build()

            soundPool?.setOnLoadCompleteListener {
                    _, sampleId, status ->

                if (status == 0) {
                    synchronized(loadedSounds) {
                        loadedSounds.add(sampleId)
                    }
                }
            }

            clickSound = soundPool!!.load(
                application,
                R.raw.ui_click,
                1
            )

            transitionSound = soundPool!!.load(
                application,
                R.raw.page_transition,
                1
            )

            evidenceSound = soundPool!!.load(
                application,
                R.raw.evidence_found,
                1
            )

            initialized = true
        }

        if (!callbacksRegistered && application is Application) {

            application.registerActivityLifecycleCallbacks(
                lifecycleCallbacks
            )

            callbacksRegistered = true
        }

        if (audioEnabled) {
            startMusic()
        }
    }

    // ---------------------------------------------------------
    // GLOBAL AUDIO ON / OFF
    // ---------------------------------------------------------

    fun isAudioEnabled(context: Context): Boolean {

        if (!initialized) {
            return context.getSharedPreferences(
                PREFS,
                Context.MODE_PRIVATE
            ).getBoolean(KEY_ENABLED, true)
        }

        return audioEnabled
    }

    fun setAudioEnabled(
        context: Context,
        enabled: Boolean
    ) {

        initialize(context)

        audioEnabled = enabled

        appContext?.getSharedPreferences(
            PREFS,
            Context.MODE_PRIVATE
        )?.edit()?.putBoolean(
            KEY_ENABLED,
            enabled
        )?.apply()

        if (enabled) {
            soundPool?.autoResume()
            startMusic()
        } else {
            pauseMusic()
            soundPool?.autoPause()
        }

        audioControls.values.toList().forEach {
            updateAudioButton(it)
        }

        audioListeners.values.toList().forEach {
            it(enabled)
        }
    }

    fun setAudioStateListener(
        activity: Activity,
        listener: (Boolean) -> Unit
    ) {
        audioListeners[activity] = listener
        listener(audioEnabled)
    }

    fun clearAudioStateListener(activity: Activity) {
        audioListeners.remove(activity)
    }

    // ---------------------------------------------------------
    // HORROR BACKGROUND MUSIC
    // ---------------------------------------------------------

    private fun startMusic() {

        if (!audioEnabled) return

        val context = appContext ?: return

        try {

            if (mediaPlayer == null) {

                mediaPlayer = MediaPlayer.create(
                    context,
                    R.raw.noir_ambience
                )?.apply {

                    isLooping = true

                    // Moderately high background volume: 72%
                    setVolume(0.72f, 0.72f)

                    setOnErrorListener { player, _, _ ->

                        player.release()
                        mediaPlayer = null

                        true
                    }
                }
            }

            mediaPlayer?.let { player ->

                if (!player.isPlaying) {
                    player.start()
                }
            }

        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun pauseMusic() {

        try {

            if (mediaPlayer?.isPlaying == true) {
                mediaPlayer?.pause()
            }

        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    // ---------------------------------------------------------
    // SOUND EFFECTS
    // ---------------------------------------------------------

    fun playClick() {
        playSound(clickSound, 0.55f)
    }

    fun playPageTransition() {
        playSound(transitionSound, 0.55f)
    }

    fun playEvidenceFound() {
        playSound(evidenceSound, 0.75f)
    }

    private fun playSound(
        soundId: Int,
        volume: Float
    ) {

        if (!audioEnabled || soundId == 0) return

        val ready = synchronized(loadedSounds) {
            loadedSounds.contains(soundId)
        }

        if (!ready) return

        try {

            soundPool?.play(
                soundId,
                volume,
                volume,
                1,
                0,
                1f
            )

        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    // ---------------------------------------------------------
    // CONFIGURE AUDIO CONTROL ON EACH SCREEN
    // ---------------------------------------------------------

    private fun configureActivity(activity: Activity) {

        if (activity.isFinishing || activity.isDestroyed) return

        val content = activity.findViewById<FrameLayout>(
            android.R.id.content
        ) ?: return

        var audioButton: TextView? =
            activity.findViewById(R.id.audioButton)

        if (audioButton == null) {

            audioButton = audioControls[activity]

            if (audioButton == null) {

                audioButton = createAudioButton(activity)

                val menu = findMenuButton(content)
                val parent = menu?.parent

                if (menu != null && parent is LinearLayout) {

                    val index = parent.indexOfChild(menu)

                    val params = LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.WRAP_CONTENT,
                        dp(activity, 38)
                    ).apply {
                        marginStart = dp(activity, 10)
                    }

                    parent.addView(
                        audioButton,
                        index,
                        params
                    )

                } else {

                    val params = FrameLayout.LayoutParams(
                        ViewGroup.LayoutParams.WRAP_CONTENT,
                        dp(activity, 38),
                        Gravity.TOP or Gravity.END
                    ).apply {
                        topMargin = dp(activity, 10)
                        marginEnd = dp(activity, 18)
                    }

                    content.addView(audioButton, params)
                }
            }
        }

        val button = audioButton ?: return

        audioControls[activity] = button

        if (audioWiredViews.put(button, true) == null) {

            button.setOnClickListener {

                playClick()

                setAudioEnabled(
                    activity,
                    !audioEnabled
                )
            }
        }

        updateAudioButton(button)

        wireClickableViews(content, button)
    }

    // ---------------------------------------------------------
    // FIND MAIN MENU BUTTON
    // ---------------------------------------------------------

    private fun findMenuButton(view: View): TextView? {

        if (view is TextView) {

            val text = view.text.toString().trim()

            if (
                view.id == R.id.menuButton ||
                text.equals("Main Menu", ignoreCase = true) ||
                text.equals("Back to Cases", ignoreCase = true)
            ) {
                return view
            }
        }

        if (view is ViewGroup) {

            for (i in 0 until view.childCount) {

                val result = findMenuButton(
                    view.getChildAt(i)
                )

                if (result != null) {
                    return result
                }
            }
        }

        return null
    }

    // ---------------------------------------------------------
    // CREATE AUDIO BUTTON
    // ---------------------------------------------------------

    private fun createAudioButton(
        activity: Activity
    ): TextView {

        return TextView(activity).apply {

            tag = OVERLAY_TAG
            textSize = 11f
            gravity = Gravity.CENTER

            setTextColor(
                Color.rgb(220, 195, 155)
            )

            setPadding(
                dp(activity, 12),
                dp(activity, 5),
                dp(activity, 12),
                dp(activity, 5)
            )

            background = GradientDrawable().apply {

                setColor(Color.rgb(35, 29, 24))

                setStroke(
                    dp(activity, 1),
                    Color.rgb(153, 112, 62)
                )

                cornerRadius = dp(activity, 7).toFloat()
            }

            isClickable = true
            isFocusable = true
        }
    }

    private fun updateAudioButton(button: TextView) {

        button.text = if (audioEnabled) {
            "♪ AUDIO ON"
        } else {
            "♪ AUDIO OFF"
        }
    }

    // ---------------------------------------------------------
    // AUTOMATIC BUTTON CLICK SOUNDS
    // ---------------------------------------------------------

    private fun wireClickableViews(
        view: View,
        audioButton: View
    ) {

        if (
            view !== audioButton &&
            view.isClickable &&
            clickWiredViews.put(view, true) == null
        ) {

            view.setOnTouchListener { touchedView, event ->

                if (
                    event.actionMasked == MotionEvent.ACTION_UP &&
                    touchedView.isEnabled
                ) {
                    playClick()
                }

                false
            }
        }

        if (view is ViewGroup) {

            for (i in 0 until view.childCount) {

                wireClickableViews(
                    view.getChildAt(i),
                    audioButton
                )
            }
        }
    }

    // ---------------------------------------------------------
    // DP CONVERSION
    // ---------------------------------------------------------

    private fun dp(
        context: Context,
        value: Int
    ): Int {

        return (
                value * context.resources.displayMetrics.density
                ).toInt()
    }
}
