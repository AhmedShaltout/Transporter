package com.semi.clone.transporter.Controllers;

import android.app.Service;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.os.Handler;
import android.os.IBinder;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.Toast;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.InterstitialAd;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.semi.clone.transporter.Classes.Message;
import com.semi.clone.transporter.Classes.Utils;
import com.semi.clone.transporter.R;
import java.util.Timer;
import java.util.TimerTask;

public class ClipboardMonitorService extends Service {
    private InterstitialAd interstitialAd;
    private static int counter;
    private static ClipboardManager mClipboardManager;
    private View send_head;
    private WindowManager windowManager;
    private WindowManager.LayoutParams params;
    private static ClipboardManager.OnPrimaryClipChangedListener listener;
    private final Timer timer;
    private final int DELAY = 60000;
    private final Handler autoRemove;
    public ClipboardMonitorService(){
        timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                startJob();
            }
        },DELAY);
        autoRemove = new Handler();
    }

    private void checkCounter() {
        if(counter%4==0){
            if(counter > 1000)
                counter = 0;
            interstitialAd = new InterstitialAd(this);
            interstitialAd.setAdUnitId(getString(R.string.fullAd));
            interstitialAd.loadAd(new AdRequest.Builder().build());
            interstitialAd.setAdListener(new AdListener(){
                @Override
                public void onAdLoaded() {
                    interstitialAd.show();
                }
            });
        }
    }

    private void startJob() {
        try {
            mClipboardManager.removePrimaryClipChangedListener(listener);
        }catch (Exception ignore){}
        mClipboardManager.addPrimaryClipChangedListener(listener);
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                startJob();
            }
        },DELAY);
    }

    @Override
    public void onCreate() {
        super.onCreate();
        mClipboardManager = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
        send_head = LayoutInflater.from(getBaseContext()).inflate(R.layout.send_head,null);
        params = new WindowManager.LayoutParams(
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.TYPE_PHONE,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                PixelFormat.TRANSLUCENT
        );
        params.gravity = Gravity.TOP|Gravity.RIGHT;
        params.x = 0;
        params.y = 100;
        windowManager = (WindowManager) getSystemService(WINDOW_SERVICE);
        listener = clipListener();
        mClipboardManager.addPrimaryClipChangedListener(listener);
    }

    private ClipboardManager.OnPrimaryClipChangedListener clipListener() {
        return new ClipboardManager.OnPrimaryClipChangedListener() {
            @Override
            public void onPrimaryClipChanged() {
                remove();
                final ClipData clipData = mClipboardManager.getPrimaryClip();
                send_head.findViewById(R.id.close).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        remove();
                    }
                });
                send_head.findViewById(R.id.send).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        sendData(clipData.getItemAt(0).getText().toString());
                    }
                });
                send_head.findViewById(R.id.tap).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        sendData(clipData.getItemAt(0).getText().toString());
                    }
                });
                autoRemove.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        send_head.findViewById(R.id.tap).setVisibility(View.GONE);
                    }
                },DELAY/30);
                windowManager.addView(send_head, params);
                autoRemove.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        remove();
                    }
                }, DELAY/3);
            }
        };
    }

    private void sendData(String message) {
        remove();
        FirebaseUser user =Utils.getAuth().getCurrentUser();
        if (user!=null && user.isEmailVerified()) {
            Toast.makeText(this, getString(R.string.send), Toast.LENGTH_SHORT).show();
            Message m = new Message(message);
            DatabaseReference myRef = Utils.getDatabase().getReference(user.getUid());
            myRef.push().setValue(m);
            counter++;
            checkCounter();
        }else {
            Toast.makeText(this,getString(R.string.plsLog),Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(this, User_Login.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
        }
    }

    private void remove() {
        try {
            send_head.findViewById(R.id.tap).setVisibility(View.VISIBLE);
            windowManager.removeViewImmediate(send_head);
        }catch (IllegalArgumentException ignore){}
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        super.onStartCommand(intent, flags, startId);
        counter =
                getSharedPreferences("com.semi.clone.transporter.ClipboardMonitorService",MODE_PRIVATE)
                .getInt("counter",0);
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        stop();
        timer.cancel();
        timer.purge();
        try {
            getSharedPreferences("com.semi.clone.transporter.ClipboardMonitorService", MODE_PRIVATE)
                    .edit().putInt("counter", counter).apply();
        }catch (NullPointerException ignore){}
        Intent broadcastIntent = new Intent("ActivityRecognition.RestartSensor");
        sendBroadcast(broadcastIntent);
    }

    public static void stop() {
        mClipboardManager.removePrimaryClipChangedListener(listener);
    }

    public static void start(){
        mClipboardManager.addPrimaryClipChangedListener(listener);
    }
}
