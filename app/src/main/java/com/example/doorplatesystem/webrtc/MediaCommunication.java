package com.example.doorplatesystem.webrtc;

import android.content.Context;
import android.webkit.JavascriptInterface;

import com.example.doorplatesystem.media.Media;


import org.json.JSONObject;
import org.webrtc.IceCandidate;
import org.webrtc.MediaConstraints;
import org.webrtc.MediaStream;
import org.webrtc.PeerConnection;
import org.webrtc.SessionDescription;

import java.util.Objects;

public class MediaCommunication {
    private SignalingClient signalingClient;
    private PeerConnect peerConnect;
    private Context applicationContext;

    public MediaCommunication(Context applicationContext) {
        this.applicationContext = applicationContext;
    }

    @JavascriptInterface
    public void init(String identification) {
        peerConnect = PeerConnect.get(applicationContext);
        signalingClient = SignalingClient.get();
        signalingClient.init(new SignalingClient.Callback() {
            @Override
            public void onOfferReceived(JSONObject data) {
                String from = data.optString("from");
                //
                if(Objects.equals(data.optString("mediaType"),"DisplayMedia")){
                    MediaStream displayMediaStream = Media.get().getDisplayMediaStream();
                    PeerConnection pc = peerConnect.createPeerConnection(data);
                    pc.addStream(displayMediaStream);

                    pc.setRemoteDescription(new SdpAdapter(from),
                            new SessionDescription(SessionDescription.Type.OFFER, data.optString("sdp")));

                    pc.createAnswer(new SdpAdapter("localAnswerSdp") {
                        @Override
                        public void onCreateSuccess(SessionDescription sdp) {
                            super.onCreateSuccess(sdp);
                            pc.setLocalDescription(new SdpAdapter(from), sdp);
                            SignalingClient.get().sendSessionDescription(sdp, data.optString("from"));
                        }

                        @Override
                        public void onCreateFailure(String s) {
                            super.onCreateFailure(s);
                        }
                    }, new MediaConstraints());

                }
            }

            @Override
            public void onAnswerReceived(JSONObject data) {

            }

            @Override
            public void onIceCandidateReceived(JSONObject data) {
                String from = data.optString("from");
                PeerConnection peerConnection = peerConnect.getPeerConnect(from);
                if(peerConnection!=null){
                    peerConnection.addIceCandidate(new IceCandidate(
                            data.optString("id"),
                            data.optInt("label"),
                            data.optString("candidate")
                    ));
                }

            }

            @Override
            public void onDisconnect(String form) {
                peerConnect.disconnect(form);
            }
        }, identification + "-android");
    }
}
