package com.remap.glassBGV;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import com.google.android.glass.touchpad.Gesture;
import com.google.android.glass.touchpad.GestureDetector;
import com.remap.glassBGV.R;

import android.provider.Settings.Secure;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.Camera;
import android.media.CamcorderProfile;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.os.Environment;
import android.os.PowerManager;
import android.os.Vibrator;
import android.os.PowerManager.WakeLock;
import android.util.Log;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.Window;
import android.view.WindowManager;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

/**
 * @author Alex Nano
 */
@SuppressLint("SetJavaScriptEnabled")
public class GlassChatBGV extends Activity {
	// debug string
	private static final String TAG = "GlassChatBGV";
	// the URL that serves the HTML page
	private static final String URL = "http://ether.remap.ucla.edu/glass/index.html?uid=";

	static final String START = "com.remap.glass.GlassChat.START_RECORDING";
	static final String STOP = "com.remap.glass.GlassChat.STOP_RECORDING";
	//IntentFilter intentFilter = new IntentFilter(START);
	
	// STATII
	//
	// No problems during recording
	private static final int STATUS_OK = 0;
	// A problem occurred when trying to create the output file
	private static final int STATUS_FILE_ERROR = 1;
	// A problem occurred when trying to start recording
	private static final int STATUS_START_ERROR = 2;

	//The status of recording. Currently it could be {@link #STATUS_OK}, {@link #STATUS_FILE_ERROR} or {@link #STATUS_START_ERROR}
	private int mStatus = STATUS_OK;

	// these are for the media player
	MediaRecorder recorder;
	SurfaceHolder holder;
	private WakeLock wl;
	
	// and for glass
	private GestureDetector mGestureDetector;
	
	private static Camera mCamera;
	
	private final BroadcastReceiver stop = new BroadcastReceiver() {

		@Override
		public void onReceive(Context context, Intent intent) {
			String response = "unknown error";
			if(mStatus == STATUS_FILE_ERROR)
				response = "file error";
			else if(mStatus == STATUS_START_ERROR)
				response = "start error";
			else if (stopRecorder())
				response = "stopped";

			setStatus(STATUS_OK);
			Toast.makeText(GlassChatBGV.this, response, Toast.LENGTH_SHORT).show();
			Log.d(TAG, "stop");
		}
	};
	
	private final BroadcastReceiver start = new BroadcastReceiver() {

		@Override
		public void onReceive(Context context, Intent intent) {
			Log.d(TAG, "start received...");
			startRecorder();
			
		}
	};


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        
		//requestWindowFeature(Window.FEATURE_NO_TITLE);
		//getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
		//		WindowManager.LayoutParams.FLAG_FULLSCREEN);
        
		
        setContentView(R.layout.main);
        
        SurfaceView cameraView = (SurfaceView) findViewById(R.id.camera_view);
        
        // get glass UID
        String android_id = Secure.getString(this.getContentResolver(),Secure.ANDROID_ID); 
        
        // initialize webkit view
        WebView engine = (WebView) findViewById(R.id.web_engine);
        mGestureDetector = createGestureDetector(this);
        // this is an attempt at getting HTML5 video to play
        // turns out it's far more complicated... yet leaving here as it does not hurt :) 
        WebChromeClient chromeClient = null;
		engine.setWebChromeClient(chromeClient);
        WebViewClient wvClient = null;
		engine.setWebViewClient(wvClient);
        engine.getSettings().setJavaScriptEnabled(true);
        engine.setBackgroundColor(0x00000000);
        
        android.provider.Settings.System.putInt(getContentResolver(),
                android.provider.Settings.System.SCREEN_BRIGHTNESS_MODE, 0);
        
        android.provider.Settings.System.putInt(getContentResolver(),
        android.provider.Settings.System.SCREEN_BRIGHTNESS, 75);
        
        // load the page for display on glass
        engine.loadUrl(URL+android_id);
        
        // this sleeps the screen... we don't want that ! 
		PowerManager pm = (PowerManager)getSystemService(Context.POWER_SERVICE);
		wl = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, TAG);
		wl.acquire();



		//setContentView(R.layout.camera_layout);

        //SurfaceView cameraView = (SurfaceView) findViewById(R.id.camera_view);
		holder = cameraView.getHolder();
		//holder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);

		//registerReceiver(start, new IntentFilter(Intent.ACTION_MAIN));
		registerReceiver(start, new IntentFilter(START));
		registerReceiver(stop, new IntentFilter(STOP));
       
		//startRecorder();
		
		//Intent i = new Intent(GlassChat.this, );
		//Intent i = new Intent();
		//startActivity(i); 
    }
    
    @Override
	public void onDestroy() {
		super.onDestroy();

		Vibrator v = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
		v.vibrate(100);

		Log.d(TAG, "unregister");
		unregisterReceiver(start);
		unregisterReceiver(stop);

		if(recorder != null) {
			recorder.stop();
			recorder.release();
			recorder = null;
		}

		wl.release();
	}

	private void startRecorder() {
		Log.d(TAG,"starting recorder...");
		//Toast.makeText(GlassChat.this, "starting recorder", Toast.LENGTH_SHORT).show();
		if (recorder == null) {
			
			File file = getOutputMediaFile();
			if (file == null) {
				setStatus(STATUS_FILE_ERROR);
				recorder = null;
				return;
			}
			
			Log.d(TAG,"have file, let's get camera...");
			// glass specific bug
			// https://code.google.com/p/google-glass-api/issues/detail?id=360
			// https://code.google.com/p/google-glass-api/issues/detail?id=228&can=1&q=MediaRecorder&colspec=ID%20Type%20Status%20Priority%20Owner%20Component%20Summary
			Log.d(TAG,"have camera, let's reset preview per bug...");
			//mCamera = getCameraInstance();
			/*
			try {
				mCamera.setPreviewDisplay(null);
			} catch (java.io.IOException ioe) {
				Log.d(TAG, "IOException nullifying preview display: " + ioe.getMessage());
			}
			*/
			//mCamera.stopPreview();
			//mCamera.unlock();
			 
			//Log.d(TAG,"bug done, camera unlocked...");
			 
			Log.d(TAG,"actually starting recorder...");
		
			recorder = new MediaRecorder();
			//recorder.setCamera(mCamera);
			
			// this worked, but only when screen is blank
			recorder.setAudioSource(MediaRecorder.AudioSource.CAMCORDER);
			recorder.setVideoSource(MediaRecorder.VideoSource.DEFAULT);
			//CamcorderProfile cpHigh = CamcorderProfile.get(CamcorderProfile.QUALITY_HIGH);
			CamcorderProfile cpHigh = CamcorderProfile.get(CamcorderProfile.QUALITY_480P);
			recorder.setProfile(cpHigh);

			// this supposedly works, not tested yet
			/*
			recorder.setVideoSource(0);
			recorder.setAudioSource(0);
			recorder.setOutputFormat(2);
			recorder.setVideoEncoder(2);
			recorder.setVideoEncodingBitRate(0x4c4b40);
			recorder.setVideoFrameRate(30);
			recorder.setVideoSize(1280, 720);
			recorder.setAudioChannels(2);
			recorder.setAudioEncoder(3);
			recorder.setAudioEncodingBitRate(0x17700);
			recorder.setAudioSamplingRate(44100);
			*/
			
			recorder.setPreviewDisplay(holder.getSurface());
			recorder.setOutputFile(file.getAbsolutePath());
			
			//recorder.setOutputFile(getPFD().getFileDescriptor());

			try {
				recorder.prepare();
				recorder.start();
			} catch (IllegalStateException e) {
				e.printStackTrace();
				setStatus(STATUS_START_ERROR);
				finish();
			} catch (IOException e) {
				e.printStackTrace();
				setStatus(STATUS_START_ERROR);
				finish();
			}
			
			Log.d("Vplayer","looks good");
		}
	}

	private boolean stopRecorder() {
		if (recorder != null) {
			recorder.stop();
			recorder.release();
			recorder = null;
			return true;
		}
		return false;
	}
    
	/** Create a File for saving video */
	private static File getOutputMediaFile(){
		// To be safe, you should check that the SDCard is mounted
		// using Environment.getExternalStorageState() before doing this.

		File mediaStorageDir = new File(Environment.getExternalStoragePublicDirectory(
				Environment.DIRECTORY_MOVIES), TAG);
		// Create the storage directory if it does not exist
		if (! mediaStorageDir.exists()){
			if (! mediaStorageDir.mkdirs()){
				Log.d(TAG, "failed to create directory");
				return null;
			}
		}

		// Create a media file name
		String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
		return new File(mediaStorageDir.getPath() + File.separator +
				"VID_"+ timeStamp + ".mp4");
	}
    
	/**
	 * Sets the status for recording the video. Will vibrate for a short duration if status
	 * is an error
	 * @param status
	 */
	private void setStatus(int status) {
		if(mStatus == STATUS_OK && status != STATUS_OK) {
			Vibrator v = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
			v.vibrate(100);
		}
		mStatus = status;
	}
	
    // gestures
    
   private GestureDetector createGestureDetector(Context context) {
        GestureDetector gestureDetector = new GestureDetector(context);
            //Create a base listener for generic gestures
        	Log.d(TAG,"MADE GESTURE DETECTOR");
            gestureDetector.setBaseListener( new GestureDetector.BaseListener() {
                @Override
                public boolean onGesture(Gesture gesture) {
                    if (gesture == Gesture.TAP) {
                        // do something on tap
                    	 Log.d(TAG,"tapping...)");
                    	 Intent i = new Intent(START);
                    	 sendBroadcast(i);
                    	// startRecorder();
                       return true;
                    } else if (gesture == Gesture.TWO_TAP) {
                        // do something on two finger tap
                    	Log.d(TAG,"two tap");
                    	//stopRecorder();
                        return true;
                    } else if (gesture == Gesture.SWIPE_RIGHT) {
                        // do something on right (forward) swipe
                    	Log.d(TAG,"forward swipe");
                   	 	Intent i = new Intent(STOP);
                   	 	sendBroadcast(i);
                        return true;
                    } else if (gesture == Gesture.SWIPE_LEFT) {
                        // do something on left (backwards) swipe
                    	Log.d(TAG,"backward swipe");
                        return true;
                    }
                    return false;
                }
            });
            gestureDetector.setFingerListener(new GestureDetector.FingerListener() {
                @Override
                public void onFingerCountChanged(int previousCount, int currentCount) {
                  // do something on finger count changes
                	//Log.d("Vplayer","numfinger change");
               	 	//VideoURL = "http://ether.remap.ucla.edu/glass/test/5.mp4";
               		//prepVideo();
                }
            });
            gestureDetector.setScrollListener(new GestureDetector.ScrollListener() {
                @Override
                public boolean onScroll(float displacement, float delta, float velocity) {
                	 // do something on scrolling
                	Log.d("Vplayer","scroll...");
					return false;
                   
                };
            });
            return gestureDetector;
        }
   
   /*
    * Send generic motion events to the gesture detector
    */
   @Override
   public boolean onGenericMotionEvent(MotionEvent event) {
       if (mGestureDetector != null) {
           return mGestureDetector.onMotionEvent(event);
       }
       return false;
   }
   
   /** A safe way to get an instance of the Camera object. */
   public static Camera getCameraInstance(){
       Camera c = null;
       try {
           c = Camera.open(); // attempt to get a Camera instance
       }
       catch (Exception e){
           // Camera is not available (in use or does not exist)
       }
       return c; // returns null if camera is unavailable
   }
}
