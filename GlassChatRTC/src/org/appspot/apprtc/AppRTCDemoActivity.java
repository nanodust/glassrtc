/*
 * libjingle
 * Copyright 2013, Google Inc.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 *  1. Redistributions of source code must retain the above copyright notice,
 *     this list of conditions and the following disclaimer.
 *  2. Redistributions in binary form must reproduce the above copyright notice,
 *     this list of conditions and the following disclaimer in the documentation
 *     and/or other materials provided with the distribution.
 *  3. The name of the author may not be used to endorse or promote products
 *     derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE AUTHOR ``AS IS'' AND ANY EXPRESS OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF
 * MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO
 * EVENT SHALL THE AUTHOR BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS;
 * OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY,
 * WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR
 * OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF
 * ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package org.appspot.apprtc;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Point;
import android.media.AudioManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.Settings.Secure;
import android.util.Log;
import android.view.WindowManager;
import android.webkit.JavascriptInterface;
import android.widget.EditText;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;
import org.webrtc.DataChannel;
import org.webrtc.IceCandidate;
import org.webrtc.Logging;
import org.webrtc.MediaConstraints;
import org.webrtc.MediaStream;
import org.webrtc.PeerConnection;
import org.webrtc.PeerConnectionFactory;
import org.webrtc.SdpObserver;
import org.webrtc.SessionDescription;
import org.webrtc.StatsObserver;
import org.webrtc.StatsReport;
import org.webrtc.VideoCapturer;
import org.webrtc.VideoRenderer;
import org.webrtc.VideoRenderer.I420Frame;
import org.webrtc.VideoSource;
import org.webrtc.VideoTrack;
import org.appspot.apprtc.R;

import android.util.Log;
import android.provider.Settings.Secure;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.EnumSet;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Main Activity of the AppRTCDemo Android app demonstrating interoperability
 * between the Android/Java implementation of PeerConnection and the
 * apprtc.appspot.com demo webapp.
 */
public class AppRTCDemoActivity extends Activity
    implements AppRTCClient.IceServersObserver {
  private static final String TAG = "AppRTCDemoActivity";
  private PeerConnectionFactory factory;
  private VideoSource videoSource;
  private boolean videoSourceStopped;
  private PeerConnection pc;
  private final PCObserver pcObserver = new PCObserver();
  private final SDPObserver sdpObserver = new SDPObserver();
  private final GAEChannelClient.MessageHandler gaeHandler = new GAEHandler();
  private AppRTCClient appRtcClient = new AppRTCClient(this, gaeHandler, this);
  private VideoStreamsView vsv;
  private Toast logToast;
  private LinkedList<IceCandidate> queuedRemoteCandidates =
      new LinkedList<IceCandidate>();
  // Synchronize on quit[0] to avoid teardown-related crashes.
  private final Boolean[] quit = new Boolean[] { false };
  private MediaConstraints sdpMediaConstraints;
private boolean active;

  private static final String adminURL = "http://ether.remap.ucla.edu/glass/index.html?uid=";
  private static final String roomBaseURL = "https://graceplains.appspot.com/?glass=1&r=";
  public static final String defaultUrlParams = "audio=false&video=maxWidth=320,maxHeight=180";
//  private static final String roomBaseURL = "https://graceplains.appspot.com/?glass=1&audio=NoiseSupression=false,EchoCancellation=false,AutoGainControl=false&video=maxWidth=320&dtls=false&r=";
  private static final String characterRecordURL = "http://ether.remap.ucla.edu/glass/graceplains/backup/web/data.py/getContentFor?uid=";
  
  
  @Override
  public void onCreate(Bundle savedInstanceState) {
    Thread.setDefaultUncaughtExceptionHandler(
            new RtcExceptionHandler(this)); 
    
    super.onCreate(savedInstanceState);
    
    Log.d(TAG, "lifecycle: onCreate");
    
    setUpMainWebView();
  
    getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
    getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
   
    Point displaySize = new Point();
    getWindowManager().getDefaultDisplay().getSize(displaySize);
    vsv = new VideoStreamsView(this, displaySize);
    
    Log.d(TAG, "lifecycle: initializing android globals...");
    abortUnless(PeerConnectionFactory.initializeAndroidGlobals(this),
        "Failed to initializeAndroidGlobals");
    Log.d(TAG, "lifecycle: initialized android globals");

    sdpMediaConstraints = new MediaConstraints();
    sdpMediaConstraints.mandatory.add(new MediaConstraints.KeyValuePair(
        "OfferToReceiveAudio", "true"));
    sdpMediaConstraints.mandatory.add(new MediaConstraints.KeyValuePair(
        "OfferToReceiveVideo", "true"));
    
    active = true;
    (new ChatRoomIdGetter(this)).execute(getChraracterRecordUrl());
  }
  
  @Override
  public void onPause() {
	  active = false;
	  super.onPause();
	  Log.d(TAG, "lifecycle: onPause");
//    vsv.onPause();
//    if (videoSource != null) {
//      videoSource.stop();
//      videoSourceStopped = true;
//    }

    //disconnectAndExit();
  }

  @Override
  public void onResume() {
    super.onResume();
    Log.d(TAG, "lifecycle: onResume");
    vsv.onResume();
    if (videoSource != null && videoSourceStopped) {
      videoSource.restart();
    }
  }

  @Override
  public void onRestart() {
	  super.onRestart();
	    Log.d(TAG, "lifecycle: onRestart");
  }
  
  @Override
  public void onStart() {
	  super.onStart();
	    Log.d(TAG, "lifecycle: onStart");
  }
  
  @Override
  protected void onStop() {
      super.onStop();  
      
      Log.d(TAG, "lifecycle: onStop");
      Log.d(TAG, "disconnect from chat");
      disconnectAndExit();
  }
  
  @Override
  protected void onDestroy() {
    disconnectAndExit();
    try {
    	super.onDestroy();
    }
    catch (RuntimeException e)
    {
    	Log.d(TAG, "it happens =(");
    }
    	Log.d(TAG, "lifecycle: onDestroy");
  }
  
  private String getGlassUid(){
	String glassUid = Secure.getString(this.getContentResolver(), Secure.ANDROID_ID);
	Log.d(TAG, "lifecycle: glass UID: "+ glassUid);
	return glassUid;
  }
  
  private String getAdminUrl(){
	  return adminURL+getGlassUid();
  }
  
  private String getChraracterRecordUrl(){
	  return characterRecordURL+getGlassUid();
  }
  
  private void chatRoomIdReady(String chatRoomId, String urlParams){
	  if (active)
	  {
		  logAndToast("Connecting to room "+chatRoomId);
		  
		  String fullUrl = roomBaseURL+chatRoomId+"&"+urlParams;
		  Log.d(TAG, "lifecycle: full room URL: "+ fullUrl);
		  
		  connectToRoom(fullUrl);
	  }
	  else
	  {
		  Log.d(TAG, "fetched chatRoomId but app is not active anymore");
	  }
  }
  
  private void setUpMainWebView() {
	  setContentView(R.layout.main);
      
      WebView engine = (WebView) findViewById(R.id.web_engine);
      WebChromeClient chromeClient = null;
      engine.setWebChromeClient(chromeClient);
      
      WebViewClient wvClient = null;
      engine.setWebViewClient(wvClient);
      engine.getSettings().setJavaScriptEnabled(true);
     
      engine.loadUrl(getAdminUrl());  
  }
  
  private void showGetRoomUI() {
    final EditText roomInput = new EditText(this);
    roomInput.setText("https://apprtc.appspot.com/?r=");
    roomInput.setSelection(roomInput.getText().length());
    DialogInterface.OnClickListener listener =
        new DialogInterface.OnClickListener() {
          @Override public void onClick(DialogInterface dialog, int which) {
            abortUnless(which == DialogInterface.BUTTON_POSITIVE, "lolwat?");
            dialog.dismiss();
            connectToRoom(roomInput.getText().toString());
          }
        };
    AlertDialog.Builder builder = new AlertDialog.Builder(this);
    builder
        .setMessage("Enter room URL").setView(roomInput)
        .setPositiveButton("Go!", listener).show();
  }

  private void connectToRoom(String roomUrl) {
		  appRtcClient.connectToRoom(roomUrl);
  }
  
  // Just for fun (and to regression-test bug 2302) make sure that DataChannels
  // can be created, queried, and disposed.
  private static void createDataChannelToRegressionTestBug2302(
      PeerConnection pc) {
    DataChannel dc = pc.createDataChannel("dcLabel", new DataChannel.Init());
    abortUnless("dcLabel".equals(dc.label()), "Unexpected label corruption?");
    dc.close();
    dc.dispose();
  }

  @Override
  public void onIceServers(List<PeerConnection.IceServer> iceServers) {
    factory = new PeerConnectionFactory();

    MediaConstraints pcConstraints = appRtcClient.pcConstraints();
//    pcConstraints.optional.add(
//        new MediaConstraints.KeyValuePair("RtpDataChannels", "true"));
    pc = factory.createPeerConnection(iceServers, pcConstraints, pcObserver);

    //createDataChannelToRegressionTestBug2302(pc);  // See method comment.

    // Uncomment to get ALL WebRTC tracing and SENSITIVE libjingle logging.
    // NOTE: this _must_ happen while |factory| is alive!
    // Logging.enableTracing(
    //     "logcat:",
    //     EnumSet.of(Logging.TraceLevel.TRACE_ALL),
    //     Logging.Severity.LS_SENSITIVE);

    {
      final PeerConnection finalPC = pc;
      final Runnable repeatedStatsLogger = new Runnable() {
          public void run() {
            synchronized (quit[0]) {
              if (quit[0]) {
                return;
              }
              final Runnable runnableThis = this;
              boolean success = finalPC.getStats(new StatsObserver() {
                  public void onComplete(StatsReport[] reports) {
                    for (StatsReport report : reports) {
                      Log.d(TAG, "Stats: " + report.toString());
                    }
                    vsv.postDelayed(runnableThis, 10000);
                  }
                }, null);
              if (!success) {
                throw new RuntimeException("getStats() return false!");
              }
            }
          }
        };
      vsv.postDelayed(repeatedStatsLogger, 10000);
    }

    try
    {
      logAndToast("Creating local video source...");
      MediaStream lMS = factory.createLocalMediaStream("ARDAMS");
      if (appRtcClient.videoConstraints() != null) {
        VideoCapturer capturer = getVideoCapturer();
        if (capturer != null)
        {
        	videoSource = factory.createVideoSource(
        			capturer, appRtcClient.videoConstraints());
        	VideoTrack videoTrack =
        			factory.createVideoTrack("ARDAMSv0", videoSource);
        	videoTrack.addRenderer(new VideoRenderer(new VideoCallbacks(
        			vsv, VideoStreamsView.Endpoint.LOCAL)));
        	lMS.addTrack(videoTrack);
        }
      }
      
      if (appRtcClient.audioConstraints() != null) {
    	  Log.d(TAG, "lifecycle: audio contraints: "+appRtcClient.audioConstraints());
        lMS.addTrack(factory.createAudioTrack(
            "ARDAMSa0",
            factory.createAudioSource(appRtcClient.audioConstraints())));
      }
      pc.addStream(lMS, new MediaConstraints());
      logAndToast("Waiting for ICE candidates...");
    }
    catch (RuntimeException e)
    {
    	Log.d(TAG, "got exception while add media capturer in 'onIceServers' "+e.getLocalizedMessage());
    }
  }

  // Cycle through likely device names for the camera and return the first
  // capturer that works, or crash if none do.
  private VideoCapturer getVideoCapturer() {
    String[] cameraFacing = { "front", "back" };
    int[] cameraIndex = { 0, 1 };
    int[] cameraOrientation = { 0, 90, 180, 270 };
    for (String facing : cameraFacing) {
      for (int index : cameraIndex) {
        for (int orientation : cameraOrientation) {
          String name = "Camera " + index + ", Facing " + facing +
              ", Orientation " + orientation;
          VideoCapturer capturer = VideoCapturer.create(name);
          if (capturer != null) {
        	  Log.d(TAG, "lifecycle: got capturer: "+ name);
            logAndToast("Using camera: " + name);
            return capturer;
          }
        }
      }
    }
    Log.d(TAG, "lifecycle: failed to open capturer");
    logAndToast("Can't open camera. Overheat?");
    //throw new RuntimeException("Failed to open capturer");
    return null;
  }

  // Poor-man's assert(): die with |msg| unless |condition| is true.
  private static void abortUnless(boolean condition, String msg) {
    if (!condition) {
    	Log.d(TAG, "lifecycle: abortUnless: FATAL error: "+msg);
      throw new RuntimeException(msg);
    }
  }

  // Log |msg| and Toast about it.
  private void logAndToast(String msg) {
    Log.d(TAG, msg);
    if (logToast != null) {
      logToast.cancel();
    }
    logToast = Toast.makeText(this, msg, Toast.LENGTH_SHORT);
    logToast.show();
  }

  // Send |json| to the underlying AppEngine Channel.
  private void sendMessage(JSONObject json) {
    appRtcClient.sendMessage(json.toString());
  }

  // Put a |key|->|value| mapping in |json|.
  private static void jsonPut(JSONObject json, String key, Object value) {
    try {
      json.put(key, value);
    } catch (JSONException e) {
      throw new RuntimeException(e);
    }
  }

  // Mangle SDP to prefer ISAC/16000 over any other audio codec.
  // peetonn mods: trying to prioritize PCMU/8000 instead of ISAC
  private String preferISAC(String sdpDescription) {
    String[] lines = sdpDescription.split("\r\n");
    int mLineIndex = -1;
    String isac16kRtpMap = null;
    Pattern isac16kPattern =
        Pattern.compile("^a=rtpmap:(\\d+) ISAC/16000[\r]?$");
    	//  Pattern.compile("^a=rtpmap:(\\d+) PCMU/8000[\r]?$");
    
    for (int i = 0;
         (i < lines.length) && (mLineIndex == -1 || isac16kRtpMap == null);
         ++i) {
      if (lines[i].startsWith("m=audio ")) {
        mLineIndex = i;
        continue;
      }
      Matcher isac16kMatcher = isac16kPattern.matcher(lines[i]);
      if (isac16kMatcher.matches()) {
        isac16kRtpMap = isac16kMatcher.group(1);
        continue;
      }
    }
    if (mLineIndex == -1) {
      Log.d(TAG, "No m=audio line, so can't prefer iSAC");
      return sdpDescription;
    }
    if (isac16kRtpMap == null) {
      Log.d(TAG, "No ISAC/16000 line, so can't prefer iSAC");
      return sdpDescription;
    }
    String[] origMLineParts = lines[mLineIndex].split(" ");
    StringBuilder newMLine = new StringBuilder();
    int origPartIndex = 0;
    // Format is: m=<media> <port> <proto> <fmt> ...
    newMLine.append(origMLineParts[origPartIndex++]).append(" ");
    newMLine.append(origMLineParts[origPartIndex++]).append(" ");
    newMLine.append(origMLineParts[origPartIndex++]).append(" ");
    newMLine.append(isac16kRtpMap);
    for (; origPartIndex < origMLineParts.length; ++origPartIndex) {
      if (!origMLineParts[origPartIndex].equals(isac16kRtpMap)) {
        newMLine.append(" ").append(origMLineParts[origPartIndex]);
      }
    }
    lines[mLineIndex] = newMLine.toString();
    StringBuilder newSdpDescription = new StringBuilder();
    for (String line : lines) {
      newSdpDescription.append(line).append("\r\n");
    }
    return newSdpDescription.toString();
  }

  // Implementation detail: observe ICE & stream changes and react accordingly.
  private class PCObserver implements PeerConnection.Observer {
    @Override public void onIceCandidate(final IceCandidate candidate){
      runOnUiThread(new Runnable() {
          public void run() {
            JSONObject json = new JSONObject();
            jsonPut(json, "type", "candidate");
            jsonPut(json, "label", candidate.sdpMLineIndex);
            jsonPut(json, "id", candidate.sdpMid);
            jsonPut(json, "candidate", candidate.sdp);
            sendMessage(json);
          }
        });
    }

    @Override public void onError(){
      runOnUiThread(new Runnable() {
          public void run() {
            throw new RuntimeException("PeerConnection error!");
          }
        });
    }

    @Override public void onSignalingChange(
        PeerConnection.SignalingState newState) {
    }

    @Override public void onIceConnectionChange(
        PeerConnection.IceConnectionState newState) {
    }

    @Override public void onIceGatheringChange(
        PeerConnection.IceGatheringState newState) {
    }

    @Override public void onAddStream(final MediaStream stream){
    	Log.d(TAG, "ignore adding remote stream");
    	/*
      runOnUiThread(new Runnable() {
          public void run() {
            abortUnless(stream.audioTracks.size() <= 1 &&
                stream.videoTracks.size() <= 1,
                "Weird-looking stream: " + stream);
            if (stream.videoTracks.size() == 1) {
            	
            	logAndToast("adding remote stream...");
              stream.videoTracks.get(0).addRenderer(new VideoRenderer(
                  new VideoCallbacks(vsv, VideoStreamsView.Endpoint.REMOTE)));
            }
          }
        });*/
    }

    @Override public void onRemoveStream(final MediaStream stream){
      runOnUiThread(new Runnable() {
          public void run() {
            stream.videoTracks.get(0).dispose();
          }
        });
    }

    @Override public void onDataChannel(final DataChannel dc) {
      runOnUiThread(new Runnable() {
          public void run() {
            throw new RuntimeException(
                "AppRTC doesn't use data channels, but got: " + dc.label() +
                " anyway!");
          }
        });
    }

    @Override public void onRenegotiationNeeded() {
      // No need to do anything; AppRTC follows a pre-agreed-upon
      // signaling/negotiation protocol.
    }
  }

  // Implementation detail: handle offer creation/signaling and answer setting,
  // as well as adding remote ICE candidates once the answer SDP is set.
  private class SDPObserver implements SdpObserver {
    @Override public void onCreateSuccess(final SessionDescription origSdp) {
      runOnUiThread(new Runnable() {
          public void run() {
        	  String codec = preferISAC(origSdp.description);
            SessionDescription sdp = new SessionDescription(
                origSdp.type, codec);
            pc.setLocalDescription(sdpObserver, sdp);
          }
        });
    }

    // Helper for sending local SDP (offer or answer, depending on role) to the
    // other participant.
    private void sendLocalDescription(PeerConnection pc) {
      SessionDescription sdp = pc.getLocalDescription();
      logAndToast("Sending " + sdp.type);
      JSONObject json = new JSONObject();
      jsonPut(json, "type", sdp.type.canonicalForm());
      jsonPut(json, "sdp", sdp.description);
      sendMessage(json);
    }

    @Override public void onSetSuccess() {
      runOnUiThread(new Runnable() {
          public void run() {
            if (appRtcClient.isInitiator()) {
              if (pc.getRemoteDescription() != null) {
                // We've set our local offer and received & set the remote
                // answer, so drain candidates.
                drainRemoteCandidates();
              } else {
                // We've just set our local description so time to send it.
                sendLocalDescription(pc);
              }
            } else {
              if (pc.getLocalDescription() == null) {
                // We just set the remote offer, time to create our answer.
                logAndToast("Creating answer");
                pc.createAnswer(SDPObserver.this, sdpMediaConstraints);
              } else {
                // Answer now set as local description; send it and drain
                // candidates.
                sendLocalDescription(pc);
                drainRemoteCandidates();
              }
            }
          }
        });
    }

    @Override public void onCreateFailure(final String error) {
      runOnUiThread(new Runnable() {
          public void run() {
            throw new RuntimeException("createSDP error: " + error);
          }
        });
    }

    @Override public void onSetFailure(final String error) {
      runOnUiThread(new Runnable() {
          public void run() {
            throw new RuntimeException("setSDP error: " + error);
          }
        });
    }

    private void drainRemoteCandidates() {
      for (IceCandidate candidate : queuedRemoteCandidates) {
        pc.addIceCandidate(candidate);
      }
      queuedRemoteCandidates = null;
    }
  }

  // Implementation detail: handler for receiving GAE messages and dispatching
  // them appropriately.
  private class GAEHandler implements GAEChannelClient.MessageHandler {
    @JavascriptInterface public void onOpen() {
      if (!appRtcClient.isInitiator()) {
        return;
      }
      logAndToast("Creating offer...");
      pc.createOffer(sdpObserver, sdpMediaConstraints);
    }

    @JavascriptInterface public void onMessage(String data) {
      try {
        JSONObject json = new JSONObject(data);
        String type = (String) json.get("type");
        if (type.equals("candidate")) {
          IceCandidate candidate = new IceCandidate(
              (String) json.get("id"),
              json.getInt("label"),
              (String) json.get("candidate"));
          if (queuedRemoteCandidates != null) {
        	  Log.d(TAG, "lifecycle: added remote candidate "+json+" "+(String)json.get("candidate"));
            queuedRemoteCandidates.add(candidate);
          } else {
        	  Log.d(TAG, "lifecycle: added ICE candidate "+json+" "+(String)json.get("candidate"));
            pc.addIceCandidate(candidate);
          }
        } else if (type.equals("answer") || type.equals("offer")) {
        	String codec = preferISAC((String) json.get("sdp"));
          SessionDescription sdp = new SessionDescription(
              SessionDescription.Type.fromCanonicalForm(type),
              codec);
          pc.setRemoteDescription(sdpObserver, sdp);
        } else if (type.equals("bye")) {
          logAndToast("Remote end hung up; dropping PeerConnection");
          disconnectAndExit();
        } else {
          throw new RuntimeException("Unexpected message: " + data);
        }
      } catch (JSONException e) {
        throw new RuntimeException(e);
      }
    }

    @JavascriptInterface public void onClose() {
      disconnectAndExit();
    }

    @JavascriptInterface public void onError(int code, String description) {
      disconnectAndExit();
    }
  }

  // Disconnect from remote resources, dispose of local resources, and exit.
  private void disconnectAndExit() {
	  Log.d(TAG, "disconnect and exit");
    synchronized (quit[0]) {
      if (quit[0]) {
        return;
      }
      quit[0] = true;
      if (pc != null) {
        pc.dispose();
        pc = null;
      }
      if (appRtcClient != null) {
        appRtcClient.sendMessage("{\"type\": \"bye\"}");
        appRtcClient.disconnect();
        appRtcClient = null;
      }
      if (videoSource != null) {
        videoSource.dispose();
        videoSource = null;
      }
      if (factory != null) {
        factory.dispose();
        factory = null;
      }
      finish();
    }
  }

  // Implementation detail: bridge the VideoRenderer.Callbacks interface to the
  // VideoStreamsView implementation.
  private class VideoCallbacks implements VideoRenderer.Callbacks {
    private final VideoStreamsView view;
    private final VideoStreamsView.Endpoint stream;

    public VideoCallbacks(
        VideoStreamsView view, VideoStreamsView.Endpoint stream) {
      this.view = view;
      this.stream = stream;
    }

    @Override
    public void setSize(final int width, final int height) {
      view.queueEvent(new Runnable() {
          public void run() {
            view.setSize(stream, width, height);
          }
        });
    }

    @Override
    public void renderFrame(I420Frame frame) {
      view.queueFrame(stream, frame);
    }
  }
  
  // gets admin json in separate thread and retrieves chatRoomId from it and urlParams
  private class ChatRoomIdGetter extends AsyncTask<String, Void, String[]> {
	private AppRTCDemoActivity listener;  
  
	public ChatRoomIdGetter(AppRTCDemoActivity listener) {
		this.listener = listener;
	}
	
	    @Override
	    protected String[] doInBackground(String... urls) {
	      if (urls.length != 1) {
	        throw new RuntimeException("Must be called with a single URL");
	      }
	      try {
	        return getChatRoomId(urls[0]);
	      } catch (IOException e) {
	        throw new RuntimeException(e);
	      }
	    }

	    @Override
	    protected void onPostExecute(String[] params) {
	    	String chatRoomId = params[0];
	    	String urlParams = params[1];
	      listener.chatRoomIdReady(chatRoomId, urlParams);
	    }
	    
	    private String[] getChatRoomId(String adminUrl) throws IOException {
	  	  String chatRoomId = "12345678";
	  	  String urlParameters = listener.defaultUrlParams;
	  	  
	  	  try {
	  		  InputStream is = (new URL(adminUrl)).openConnection().getInputStream();
	  		  String characterJson =
	  	          appRtcClient.drainStream(is);
	  		JSONObject json = new JSONObject(characterJson);
	  		chatRoomId = json.optString("chatroomID");
	  		urlParameters = json.optString("queryString");
	  	  }
	  	  catch (RuntimeException e)
	  	  {
	  		  throw new RuntimeException(e);
	  	  }
	  	  catch (JSONException jex)
	  	  {
	  		  Log.d(listener.TAG, "json error");
	  	  }
	  	  
	  	  String[] params = new String[2];
	  	  params[0] = chatRoomId;
	  	  params[1] = urlParameters;
	  	  
	  	  return params;
	    }
  }
  
  private class RtcExceptionHandler extends UnhandledExceptionHandler
  {
	  private static final String TAG = "AppRTCDemoActivity";
	  
	  public RtcExceptionHandler(final Activity activity) {
		  super(activity);
	  }
	  
	  @Override
	  public void uncaughtException(Thread unusedThread, final Throwable e) {
		  Log.d(TAG, "lifecycle: Fatal error: " + getTopLevelCauseMessage(e));
	  }
	  
	  private String getTopLevelCauseMessage(Throwable t) {
		    Throwable topLevelCause = t;
		    while (topLevelCause.getCause() != null) {
		      topLevelCause = topLevelCause.getCause();
		    }
		    return topLevelCause.getMessage();
		  }
  }
  
}
