
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

    // Activities currently in the foreground.
    private val resumedActivities = mutableSetOf<Activity>()

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

                // App is now in the foreground.
                resumedActivities.add(activity)

                if (audioEnabled) {
                    soundPool?.autoResume()
                    startMusic()
                }

                activity.window.decorView.post {
                    configureActivity(activity)
                }
            }

            override fun onActivityPaused(activity: Activity) {

                resumedActivities.remove(activity)

                // Stop all audio when no Activity remains resumed.
                if (resumedActivities.isEmpty()) {
                    pauseMusic()
                    soundPool?.autoPause()
                }
            }

            override fun onActivityStarted(activity: Activity) {}

            override fun onActivityStopped(activity: Activity) {}

            override fun onActivitySaveInstanceState(
                activity: Activity,
                outState: Bundle
            ) {}

            override fun onActivityDestroyed(activity: Activity) {

                resumedActivities.remove(activity)

                audioControls.remove(activity)
                audioListeners.remove(activity)

                if (resumedActivities.isEmpty()) {
                    pauseMusic()
                    soundPool?.autoPause()
                }
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
                .getSharedPreferences(
                    PREFS,
                    Context.MODE_PRIVATE
                )
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

        // Never start music just because initialize() was called.
        // It must also have a resumed Activity.
        if (audioEnabled && resumedActivities.isNotEmpty()) {
            startMusic()
        } else {
            pauseMusic()
            soundPool?.autoPause()
        }
    }

    // ---------------------------------------------------------
    // FOREGROUND STATE
    // ---------------------------------------------------------

    private fun isAppInForeground(): Boolean {
        return resumedActivities.isNotEmpty()
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

            if (isAppInForeground()) {
                soundPool?.autoResume()
                startMusic()
            } else {
                pauseMusic()
                soundPool?.autoPause()
            }

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
    // BACKGROUND MUSIC
    // ---------------------------------------------------------

    private fun startMusic() {

        if (!audioEnabled) return

        // Do not play music while the app is in the background.
        if (!isAppInForeground()) return

        val context = appContext ?: return

        try {

            if (mediaPlayer == null) {

                mediaPlayer = MediaPlayer.create(
                    context,
                    R.raw.noir_ambience
                )?.apply {

                    isLooping = true

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

        // Sound effects are also restricted to foreground use.
        if (!audioEnabled || !isAppInForeground()) return

        if (soundId == 0) return

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

        // Prefer an audio button already declared in XML.
        val xmlAudioButton = activity.findViewById<TextView>(
            R.id.audioButton
        )

        val button: TextView

        if (xmlAudioButton != null) {

            removeInjectedAudioButtons(
                content,
                xmlAudioButton
            )

            val previousButton = audioControls[activity]

            if (
                previousButton != null &&
                previousButton !== xmlAudioButton &&
                previousButton.parent != null
            ) {
                (previousButton.parent as? ViewGroup)
                    ?.removeView(previousButton)
            }

            button = xmlAudioButton

        } else {

            val existingButton = audioControls[activity]
                ?.takeIf { it.parent != null }

            if (existingButton != null) {

                button = existingButton

            } else {

                button = createAudioButton(activity)

                val menu = findMenuButton(content)
                val parent = menu?.parent

                if (
                    menu != null &&
                    parent is LinearLayout &&
                    parent.orientation == LinearLayout.HORIZONTAL
                ) {

                    val index = parent.indexOfChild(menu)

                    val params = LinearLayout.LayoutParams(
                        dp(activity, 38),
                        dp(activity, 34)
                    ).apply {
                        marginStart = dp(activity, 6)
                    }

                    parent.addView(
                        button,
                        index,
                        params
                    )

                } else {

                    val params = FrameLayout.LayoutParams(
                        dp(activity, 38),
                        dp(activity, 34),
                        Gravity.TOP or Gravity.END
                    ).apply {

                        topMargin = dp(activity, 2)
                        marginEnd = dp(activity, 5)
                    }

                    content.addView(button, params)
                }
            }
        }

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
    // REMOVE DUPLICATE INJECTED AUDIO CONTROLS
    // ---------------------------------------------------------

    private fun removeInjectedAudioButtons(
        view: View,
        keepButton: View
    ) {

        if (
            view !== keepButton &&
            view.tag == OVERLAY_TAG
        ) {

            (view.parent as? ViewGroup)?.removeView(view)
            return
        }

        if (view is ViewGroup) {

            for (i in view.childCount - 1 downTo 0) {

                removeInjectedAudioButtons(
                    view.getChildAt(i),
                    keepButton
                )
            }
        }
    }

    // ---------------------------------------------------------
    // FIND MAIN MENU / BACK BUTTON
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
    // CREATE COMPACT AUDIO ICON
    // ---------------------------------------------------------

    private fun createAudioButton(
        activity: Activity
    ): TextView {

        return TextView(activity).apply {

            tag = OVERLAY_TAG

            textSize = 18f

            gravity = Gravity.CENTER

            minimumWidth = dp(activity, 38)
            minimumHeight = dp(activity, 34)

            setTextColor(
                Color.rgb(236, 211, 169)
            )

            setPadding(0, 0, 0, 0)

            background = GradientDrawable().apply {

                setColor(
                    Color.rgb(35, 29, 24)
                )

                setStroke(
                    dp(activity, 1),
                    Color.rgb(153, 112, 62)
                )

                cornerRadius = dp(activity, 6).toFloat()
            }

            isClickable = true
            isFocusable = true
        }
    }

    // ---------------------------------------------------------
    // UPDATE AUDIO ICON ON ALL SCREENS
    // ---------------------------------------------------------

    private fun updateAudioButton(button: TextView) {

        button.text = if (audioEnabled) {
            "🔊"
        } else {
            "🔇"
        }

        button.textSize = 18f

        button.gravity = Gravity.CENTER

        button.contentDescription = if (audioEnabled) {
            "Mute game audio"
        } else {
            "Enable game audio"
        }

        button.setPadding(0, 0, 0, 0)

        button.layoutParams?.let { params ->

            params.width = dp(button.context, 38)
            params.height = dp(button.context, 34)

            button.layoutParams = params
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
