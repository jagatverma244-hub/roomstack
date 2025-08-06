//package com.roomstack.service;
//
//
//import android.os.Bundle;
//import android.webkit.WebSettings;
//import android.webkit.WebView;
//import android.webkit.WebViewClient;
//
//import androidx.appcompat.app.AppCompatActivity;
//
//public class MainActivity extends AppCompatActivity {
//
//    WebView myWeb;
//
//    @Override
//    protected void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//
//        myWeb = new WebView(this);
//        setContentView(myWeb);
//
//        myWeb.setWebViewClient(new WebViewClient());
//        WebSettings webSettings = myWeb.getSettings();
//        webSettings.setJavaScriptEnabled(true); // Thymeleaf pages me JS chahiye
//
//        myWeb.loadUrl("https://yourapp.onrender.com"); // ← apna URL daalna
//    }
//}  ye kh rha hu m