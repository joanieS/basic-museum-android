package com.example.aura;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.webkit.WebView;

public class WebActivity extends Activity {

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
	    super.onCreate(savedInstanceState);
	    Intent intent = getIntent();
	    setContentView(R.layout.activity_web);
	    
	    // TODO Auto-generated method stub
	    
	    WebView webView = (WebView)findViewById(R.id.webView);
	    webView.loadUrl(intent.getStringExtra("web"));
	}

}
