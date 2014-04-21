package com.remap.glass;
import android.provider.Settings.Secure;

import android.app.Activity;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.view.SurfaceHolder;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;

/**
 * @author Alex Nano
 */
public class GlassChat extends Activity {
	private static final String URL = "http://ether.remap.ucla.edu/glass/index.html?uid=";
	
	// these are for the media player
	MediaRecorder recorder;
	SurfaceHolder holder;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        
        // get glass UID
        String android_id = Secure.getString(this.getContentResolver(),Secure.ANDROID_ID); 
        
        // initalize webkit view
        WebView engine = (WebView) findViewById(R.id.web_engine);
        
        
        // this is an attempt at getting HTML5 video to play
        // turns out it's far more complicated... yet leaving
        WebChromeClient chromeClient = null;
		engine.setWebChromeClient(chromeClient);
        WebViewClient wvClient = null;
		engine.setWebViewClient(wvClient);
        engine.getSettings().setJavaScriptEnabled(true);
        engine.getSettings().setPluginsEnabled(true);
        
        // load the page for display on glass
        engine.loadUrl(URL+android_id);
       
        
    }
}
