package com.example.sogating_app.audio

class JavascriptInterface(var callActivity: CallActivity) {
    @android.webkit.JavascriptInterface
    public fun onPeerConnected() {
        callActivity.onPeerConnected()
    }
}