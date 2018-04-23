package com.semi.clone.transporter.Controllers;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.util.Patterns;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.semi.clone.transporter.Classes.Utils;
import com.semi.clone.transporter.R;

public class User_SignUp extends AppCompatActivity {
    private EditText emailField, passwordField;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.user_signup);
        passwordField = findViewById(R.id.password);
        emailField = findViewById(R.id.email);

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
        ((CheckBox)findViewById(R.id.showPassword)).setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (!isChecked)
                    passwordField.setTransformationMethod(PasswordTransformationMethod.getInstance());
                else
                    passwordField.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
            }
        });
    }

    public void newAccount(final View view) {
        String email = emailField.getText().toString(), password = passwordField.getText().toString();
        boolean isEmail = isValidEmail(email), isPassword = password.length() > 0;
        if(!isEmail)
            setBackground(emailField, true);
        else
            setBackground(emailField, false);
        if(!isPassword)
            setBackground(passwordField, true);
        else
            setBackground(passwordField, false);
        if(isEmail && isPassword) {
            view.setEnabled(false);
            Utils.getAuth().createUserWithEmailAndPassword(email, password)
                    .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if (task.isSuccessful()) {
                                Utils.getAuth().getCurrentUser().sendEmailVerification().addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        Toast.makeText(getBaseContext(),"Verification Email Was Sent To Your Email Address",Toast.LENGTH_LONG).show();
                                        onBackPressed();
                                    }
                                });
                            } else {
                                setBackground(emailField, true);
                                setBackground(passwordField, true);
                                Toast.makeText(getBaseContext(), task.getException().getMessage() ,Toast.LENGTH_LONG).show();
                                view.setEnabled(true);
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
        startActivity(new Intent(this, User_Login.class));
        finish();
    }
}
