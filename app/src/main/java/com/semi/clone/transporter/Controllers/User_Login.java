package com.semi.clone.transporter.Controllers;

import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Patterns;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.semi.clone.transporter.Classes.Utils;
import com.semi.clone.transporter.R;

public class User_Login extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private Intent serviceIntent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.user_login);
        MobileAds.initialize(this, getString(R.string.appID));
        final AdView mAdView = findViewById(R.id.adView);
        AdRequest adRequest = new AdRequest.Builder().build();
        mAdView.loadAd(adRequest);
        if(mAdView.getAdListener() == null)
            mAdView.setAdListener(new AdListener(){
                @Override
                public void onAdLoaded() {
                    super.onAdLoaded();
                    mAdView.setVisibility(View.VISIBLE);
                }

                @Override
                public void onAdFailedToLoad(int i) {
                    super.onAdFailedToLoad(i);
                    mAdView.setVisibility(View.GONE);
                }
            });
        if(serviceIntent == null ) {
            serviceIntent = new Intent(this, ClipboardMonitorService.class);
            startService();
        }
        mAuth = Utils.getAuth();
        if (mAuth.getCurrentUser() != null) {
            if(mAuth.getCurrentUser().isEmailVerified()) {
                startActivity(new Intent(this, Messages.class));
                onBackPressed();
            }
        }
    }

    private void startService() {
        if (!isMyServiceRunning(ClipboardMonitorService.class)) {
            startService(serviceIntent);
        }
    }

    private boolean isMyServiceRunning(Class<?> serviceClass) {
        ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }

    @Override
    protected void onDestroy() {
        stopService(new Intent(this, ClipboardMonitorService.class));
        super.onDestroy();
    }

    public void login(final View view) {
        final EditText emailField = findViewById(R.id.email), passwordField = findViewById(R.id.password);
        String email = emailField.getText().toString();
        String password = passwordField.getText().toString();
        boolean isPassword = password.length() > 0, isEmail = isValidEmail(email);
        if (!isEmail)
            setBackground(emailField, true);
        else
            setBackground(emailField, false);
        if (!isPassword)
            setBackground(passwordField, true);
        else
            setBackground(passwordField, false);
        if (isEmail && isPassword) {
            view.setEnabled(false);
            mAuth.signInWithEmailAndPassword(email, password)
                    .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if (task.isSuccessful()) {
                                if(mAuth.getCurrentUser().isEmailVerified()) {
                                    startActivity(new Intent(getBaseContext(), Messages.class));
                                    onBackPressed();
                                }else {
                                    mAuth.getCurrentUser().sendEmailVerification().addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            Toast.makeText(getBaseContext(),
                                                getString(R.string.sentAgain),
                                                Toast.LENGTH_LONG).show();
                                                view.setEnabled(true);
                                        }
                                    });
                                }
                            } else {
                                Toast.makeText(getBaseContext(), task.getException().getMessage(), Toast.LENGTH_LONG);
                                setBackground(emailField, true);
                                setBackground(passwordField, true);
                                view.setEnabled(true);
                            }
                        }
                    });
        }
    }
    private boolean isValidEmail(CharSequence target) {
        return !TextUtils.isEmpty(target) && Patterns.EMAIL_ADDRESS.matcher(target).matches();
    }
    private void setBackground(View view, boolean error) {
        int resource;
        if(error)
            resource = R.drawable.input_warn;
        else
            resource = R.drawable.input_def;
        view.setBackgroundResource(resource);
    }
    public void newAccount(View view) {
        startActivity(new Intent(this, User_SignUp.class));
        onBackPressed();
    }
    @Override
    public void onBackPressed() {
        finish();
    }
    public void forgotPassword(View view) {
        startActivity(new Intent(this, User_ForgotPassword.class));
        onBackPressed();
    }
}