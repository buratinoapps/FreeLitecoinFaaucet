package com.buratinoapps.android.freeltcfaucet;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.buratinoapps.android.freeltcfaucet.databinding.ActivityMainBinding;
import com.google.ads.consent.ConsentForm;
import com.google.android.gms.ads.interstitial.InterstitialAd;
import com.google.android.gms.ads.rewarded.RewardedAd;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class MainActivity<MyActivity> extends AppCompatActivity {

    private ActivityMainBinding binding;
    public MainActivity activity;
    private RewardedAd mRewardedAd;
    private InterstitialAd mInterstitialAd;
    private ConsentForm form;
    private CountDownTimer countDownTimer;

    boolean timerRunning;

    long startTimer = 1200000; //1000 milliseconds in 1 second, here i am taking 10 minutes
    long remainingTime;

    private static final int expTime = 20 * 60 * 1000;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        activity = this;

        final SharedPreferences prefs = getSharedPreferences("app_data", Context.MODE_PRIVATE);

        if (prefs.getLong("exp_time", 0) - System.currentTimeMillis() > 0) {
            remainingTime = prefs.getLong("exp_time", 0) - System.currentTimeMillis();
            startTimer();
            binding.position.setEnabled(false);
        }

        prefs.edit().putLong("exp_time", System.currentTimeMillis() + expTime).apply();
        startTimer();
        binding.position.setVisibility(View.INVISIBLE);
    }

    private void startTimer() {
        countDownTimer = new CountDownTimer(remainingTime, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                remainingTime = millisUntilFinished;
                updateCountTime();
            }

            @Override
            public void onFinish() {
                timerRunning = false;
                updateButton();
            }
        }.start();

        timerRunning = true;
        updateButton();
    }

    public void updateCountTime() {
        int minutes = (int) (remainingTime / 1000) / 60;
        int seconds = (int) (remainingTime / 1000) % 60;

        String timeLeftFormatted = String.format(Locale.getDefault(), "%02d:%02d", minutes, seconds);
        binding.counterClaim.setText(timeLeftFormatted);
    }

    public void updateButton() {
        binding.position.setText("CLAIM");

        if (remainingTime < 1000) {
            binding.position.setVisibility(View.VISIBLE);
        } else {
            binding.position.setVisibility(View.INVISIBLE);
        }

    }

    private void postRequest() {
        RequestQueue requestQueue = Volley.newRequestQueue(MainActivity.this);
        String url = "https://xmonitoring.ru/faucet";
        StringRequest stringRequest = new StringRequest(Request.Method.POST, url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                Toast.makeText(getApplicationContext(), "OK", Toast.LENGTH_SHORT).show();
            }
        }, error -> Toast.makeText(getApplicationContext(), "Post Data : Response Failed", Toast.LENGTH_SHORT).show()) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<String, String>();
                params.put("to", binding.walletAddress.getText().toString());
                return params;
            }

            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> params = new HashMap<String, String>();
                params.put("Content-Type", "application/x-www-form-urlencoded");
                return params;
            }
        };

        requestQueue.add(stringRequest);
    }

    private void shareApp() {
        final String appLink = "\nhttps://play.google.com/store/apps/details?id=" + activity.getPackageName();
        Intent sendInt = new Intent(Intent.ACTION_SEND);
        sendInt.putExtra(Intent.EXTRA_SUBJECT, activity.getString(R.string.app_name));
        sendInt.putExtra(Intent.EXTRA_TEXT, activity.getString(R.string.app_name) + appLink);
        sendInt.setType("text/plain");
        activity.startActivity(Intent.createChooser(sendInt, getString(R.string.share)));

    }

}