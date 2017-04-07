package co.muhit;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

public class SplashActivity extends AppCompatActivity {

    /**
     * The duration that the splash screen will be displayed, to be acquired from resources
     */
    private int mSplashDurationMS;

    /**
     * Handler for the splash timer
     */
    private Handler mHandler;

    /**
     * Runnable for the splash timer
     */
    private Runnable mRunnable;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        // Acquire the splash duration
        mSplashDurationMS = getResources().getInteger(R.integer.splash_duration_ms);

        mHandler = new Handler();
        mRunnable = new Runnable() {
            @Override
            public void run() {
                proceedBeyondSplash();
            }
        };

        // allow user to click and dismiss the splash screen prematurely
        View rootView = findViewById(android.R.id.content);
        rootView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                proceedBeyondSplash();
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();

        // Start a timer and afterward run the runnable
        mHandler.postDelayed(mRunnable, mSplashDurationMS);
    }

    @Override
    protected void onPause() {
        super.onPause();

        // Disallow background calls to the runnable
        mHandler.removeCallbacks(mRunnable);
    }

    /**
     * Method to close the splash screen and continue to the next Activity.
     */
    private void proceedBeyondSplash() {
        startActivity(new Intent(SplashActivity.this, WebActivityFullscreen.class));
        finish();
    }
}
