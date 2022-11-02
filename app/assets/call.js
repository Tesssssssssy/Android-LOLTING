let localVideo = document.getElementById("local-video")
let remoteVideo = document.getElementById("remote-video")

localVideo.style.opacity = 0
remoteVideo.style.opacity = 0

localVideo.onplaying = () => { localVideo.style.opacity = 1 }
remoteVideo.onplaying = () => { remoteVideo.style.opacity = 1 }

let peer
function init(userId){
    peer = new Peer(userId, {
        host: '43.200.222.54',
        port: 80,
        path: '/videocallapp'
    })

    peer.on('open', ()=>{
        Android.onPeerConnected()
    })
    listen()

}
let localStream
// 받
function listen() {
    peer.on('call',(call)=>{
//      통신 미디어 종류 선택
        navigator.getUserMedia({
            audio: true,
//            video: true
        }, (stream) =>{
            localVideo.srcObject = stream
            localStream = stream

            call.answer(stream)
            call.on('stream', (remoteStream) => {
                            remoteVideo.srcObject = remoteStream

                            remoteVideo.className = "primary-video"
                            localVideo.className = "secondary-video"

                        })
        })
    })
}

//보내기
function startCall(otherUserId) {
    navigator.getUserMedia({
        audio: true,
//        video: true
    }, (stream) => {

        localVideo.srcObject = stream
        localStream = stream

//      타인의 userID, 미디어 stream(데이터) 가 필요
        const call = peer.call(otherUserId, stream)

        call.on('stream', (remoteStream) => {
            remoteVideo.srcObject = remoteStream

            remoteVideo.className = "primary-video"
            localVideo.className = "secondary-video"
        })

    })
}

// 미디어 장치 on/off

//
//function toggleVideo(b) {
//    if (b == "true") {
//        localStream.getVideoTracks()[0].enabled = true
//    } else {
//        localStream.getVideoTracks()[0].enabled = false
//    }
//}

function toggleAudio(b) {
    if (b == "true") {
        localStream.getAudioTracks()[0].enabled = true
    } else {
        localStream.getAudioTracks()[0].enabled = false
    }
}
