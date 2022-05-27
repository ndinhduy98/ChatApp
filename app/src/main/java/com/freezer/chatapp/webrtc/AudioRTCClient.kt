package com.freezer.chatapp.webrtc

import android.content.Context
import org.webrtc.*

class AudioRTCClient(
    context: Context,
    private val observer: PeerConnection.Observer
) {
    companion object {
        private const val LOCAL_TRACK_ID = "local_track"
        private const val LOCAL_STREAM_ID = "local_track"
    }

    init {
        initPeerConnectionFactory(context)
    }

    private val iceServer = listOf(
        PeerConnection.IceServer.builder(listOf(
            "stun:stun.l.google.com:19302"
        ))
            .createIceServer()
    )

    private val peerConnectionFactory by lazy { buildPeerConnectionFactory() }
    private val peerConnection by lazy { buildPeerConnection(observer) }

    private fun initPeerConnectionFactory(context: Context) {
        val options = PeerConnectionFactory.InitializationOptions.builder(context)
            .setEnableInternalTracer(true)
            .setFieldTrials("ISAC")
            .createInitializationOptions()
        PeerConnectionFactory.initialize(options)
    }

    private fun buildPeerConnectionFactory(): PeerConnectionFactory {
        return PeerConnectionFactory
            .builder()
            .setOptions(PeerConnectionFactory.Options().apply {
                disableEncryption = true
                disableNetworkMonitor = true
            })
            .createPeerConnectionFactory()
    }

    private fun buildPeerConnection(observer: PeerConnection.Observer) = peerConnectionFactory.createPeerConnection(
        iceServer,
        observer
    )

    private fun PeerConnection.call(sdpObserver: SdpObserver) {
        val constraints = MediaConstraints().apply {
            mandatory.add(MediaConstraints.KeyValuePair("OfferToReceiveAudio", "true"))
        }

        createOffer(object : SdpObserver by sdpObserver {
            override fun onCreateSuccess(desc: SessionDescription?) {

                setLocalDescription(object : SdpObserver {
                    override fun onSetFailure(p0: String?) {
                    }

                    override fun onSetSuccess() {
                    }

                    override fun onCreateSuccess(p0: SessionDescription?) {
                    }

                    override fun onCreateFailure(p0: String?) {
                    }
                }, desc)
                sdpObserver.onCreateSuccess(desc)
            }
        }, constraints)
    }

    private fun PeerConnection.answer(sdpObserver: SdpObserver) {
        val constraints = MediaConstraints().apply {
            mandatory.add(MediaConstraints.KeyValuePair("OfferToReceiveAudio", "true"))
        }

        createAnswer(object : SdpObserver by sdpObserver {
            override fun onCreateSuccess(desc: SessionDescription?) {
                setLocalDescription(object : SdpObserver {
                    override fun onSetFailure(p0: String?) {
                    }

                    override fun onSetSuccess() {
                    }

                    override fun onCreateSuccess(p0: SessionDescription?) {
                    }

                    override fun onCreateFailure(p0: String?) {
                    }
                }, desc)
                sdpObserver.onCreateSuccess(desc)
            }
        }, constraints)
    }

    fun startAudioCapture() {
        val audioMediaConstraints = MediaConstraints().apply {
            mandatory.add(MediaConstraints.KeyValuePair("googEchoCancellation", "true"))
            mandatory.add(MediaConstraints.KeyValuePair("googNoiseSuppression", "true"))
            mandatory.add(MediaConstraints.KeyValuePair("echoCancellation", "true"))
            mandatory.add(MediaConstraints.KeyValuePair("noiseSuppression", "true"))
        }

        val audioSource = peerConnectionFactory.createAudioSource(audioMediaConstraints)

        val localAudioTrack = peerConnectionFactory.createAudioTrack(LOCAL_TRACK_ID, audioSource)
        localAudioTrack.setVolume(100.0)
        val localStream = peerConnectionFactory.createLocalMediaStream(LOCAL_STREAM_ID)

        localStream.addTrack(localAudioTrack)

        peerConnection?.addStream(localStream)
    }

    fun call(sdpObserver: SdpObserver) = peerConnection?.call(sdpObserver)

    fun answer(sdpObserver: SdpObserver) = peerConnection?.answer(sdpObserver)

    fun end() = peerConnection?.close()

    fun addIceCandidate(iceCandidate: IceCandidate?) {
        peerConnection?.addIceCandidate(iceCandidate)
    }

    fun setRemoteDescription(sessionDescription: SessionDescription) {
        peerConnection?.setRemoteDescription(object : SdpObserver {
            override fun onCreateSuccess(p0: SessionDescription?) {
            }
            override fun onSetSuccess() {
            }
            override fun onCreateFailure(p0: String?) {
            }
            override fun onSetFailure(p0: String?) {
            }
        }, sessionDescription)
    }

}