package lk.jiat.xpect.activity;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.ImageView;

import com.google.firebase.firestore.FirebaseFirestore;
import com.squareup.picasso.Picasso;

import lk.jiat.xpect.MainActivity;
import lk.jiat.xpect.R;

public class SplashActivity extends AppCompatActivity {
   private FirebaseFirestore firestore;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        if (getIntent().getExtras() != null) {
//from notification
            String userId = getIntent().getExtras().getString("userId");

            Intent intent = new Intent(SplashActivity.this, MainActivity.class);
            startActivity(intent);
//from notification
        } else {
            setTheme(R.style.Theme_Xpect_FullScreen);
            setContentView(R.layout.activity_splash);
            ImageView imageView = findViewById(R.id.splashLogo);
            Picasso.get().load(R.drawable.xpectlogo)
                    .resize(300, 300)
                    .into(imageView);

            new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
                @Override
                public void run() {
                    findViewById(R.id.splashProgress).setVisibility(View.VISIBLE);
                }
            }, 1000);

            new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
                @Override
                public void run() {
                    findViewById(R.id.splashProgress).setVisibility(View.INVISIBLE);
                    Intent intent = new Intent(SplashActivity.this, MainActivity.class);
                    startActivity(intent);
                    finish();
                }
            }, 1000);

        }


//        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);


    }
}