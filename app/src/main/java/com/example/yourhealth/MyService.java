package com.example.yourhealth;

import android.app.Service;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.IBinder;
import android.provider.Settings;

import androidx.annotation.Nullable;

import com.example.yourhealth.R;

public class MyService extends Service {
    MediaPlayer player;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    // Called when the service starts
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        player = MediaPlayer.create(this, R.raw.music);
        player.setLooping(true);
        player.start();
        return START_STICKY;
    }

    // Called when the service stopped
    @Override
    public void onDestroy() {
        super.onDestroy();
        player.stop();
    }
}
