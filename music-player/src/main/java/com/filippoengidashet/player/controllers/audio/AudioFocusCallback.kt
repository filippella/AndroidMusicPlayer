package com.filippoengidashet.player.controllers.audio

/**
 * @author Filippo Engidashet
 * @version 1.0.0
 * @since Sat, 2019-12-21 at 17:45.
 */
interface AudioFocusCallback {

    enum class StateType {
        UNKNOWN,
        FOCUS_GAINED,
        FOCUS_LOST,
        FOCUS_LOST_DUCK,
        FAILED
    }

    fun onAudioFocusStateChanged(stateType: StateType)
}
