package com.semi.clone.transporter.Controllers;

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
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.semi.clone.transporter.Classes.Utils;
import com.semi.clone.transporter.R;

public class User_ForgotPassword extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.user_forgot_password);

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
    }
    public void reset(final View view) {
        final EditText emailField = findViewById(R.id.email);
        String email= emailField.getText().toString();
        boolean isEmail = isValidEmail(email);
        if(!isEmail)
            setBackground(emailField, true);
        else{
            setBackground(emailField, false);
            view.setEnabled(false);
            Utils.getAuth().sendPasswordResetEmail(email)
                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()) {
                                Toast.makeText(getBaseContext(), getString(R.string.resetMessage), Toast.LENGTH_LONG).show();
                                onBackPressed();
                            } else {
                                setBackground(emailField, true);
                                view.setEnabled(true);
                                Toast.makeText(getBaseContext(), task.getException().getMessage(),Toast.LENGTH_LONG).show();
                            }
                        }
                    });
        }

    }
    private boolean isValidEmail(CharSequence target) {
        return !TextUtils.isEmpty(target) && Patterns.EMAIL_ADDRESS.matcher(target).matches();
    }

    private void setBackground(View view, boolean error){
        int resource;
        if(error)
            resource = R.drawable.input_warn;
        else
            resource = R.drawable.input_def;
        view.setBackgroundResource(resource);
    }

    @Override
    public void onBackPressed() {
        startActivity(new Intent(getBaseContext(), User_Login.class));
        finish();
    }
}