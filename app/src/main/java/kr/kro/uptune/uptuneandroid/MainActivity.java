package kr.kro.uptune.uptuneandroid;

import android.content.Intent;
import android.os.Handler;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;

import java.net.CookieManager;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        CookieManager cookieManager = new CookieManager();
        cookieManager.setDefault(cookieManager);

        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            public void run() {
                Intent intent = new Intent(MainActivity.this, LoginActivity.class) ;
                startActivity(intent) ;
            }
        }, 1000);   //5 seconds
    }
}
