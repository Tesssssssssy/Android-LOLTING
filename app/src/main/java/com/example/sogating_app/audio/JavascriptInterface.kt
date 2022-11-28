package com.example.sogating_app.audio

import com.example.sogating_app.Messenger.ChatActivity

class JavascriptInterface(var callActivity: ChatActivity) {
    @android.webkit.JavascriptInterface
    public fun onPeerConnected() {
        callActivity.onPeerConnected()
    }
}