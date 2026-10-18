package com.kost.descargador.interno;

import android.app.*;
import android.content.Intent;
import android.os.*;
import java.util.concurrent.atomic.AtomicBoolean;

public class DownloadService extends Service {
    private static final AtomicBoolean busy = new AtomicBoolean();
    private static volatile java.lang.ref.WeakReference<Motor> active = new java.lang.ref.WeakReference<>(null);
    public static void cancel() { Motor motor = active.get(); if (motor != null) motor.cancel(); }
    private void status(String value) {
        getSharedPreferences("job", 0).edit().putString("status", value).apply();
    }
    @Override public int onStartCommand(Intent intent, int flags, int id) {
        NotificationManager manager = getSystemService(NotificationManager.class);
        manager.createNotificationChannel(new NotificationChannel("download", "Descarga", NotificationManager.IMPORTANCE_LOW));
        startForeground(1, new Notification.Builder(this, "download")
            .setSmallIcon(android.R.drawable.stat_sys_download).setContentTitle("Kost motor interno")
            .setContentText("Preparando o descargando. Puedes cancelar desde la app.")
            .setContentIntent(PendingIntent.getActivity(this, 0, new Intent(this, MainActivity.class), PendingIntent.FLAG_IMMUTABLE))
            .setOngoing(true).build());
        if (!busy.compareAndSet(false, true)) return START_NOT_STICKY;
        final String url = intent == null ? null : intent.getStringExtra("url");
        final Motor motor = new Motor(getApplicationContext(), this::status);
        active = new java.lang.ref.WeakReference<>(motor);
        new Thread(() -> {
            PowerManager.WakeLock lock = getSystemService(PowerManager.class)
                .newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "kost:download");
            lock.acquire(12 * 60 * 1000L);
            try { motor.download(url); }
            catch (Exception e) {
                try (java.io.PrintWriter trace = new java.io.PrintWriter(new java.io.FileOutputStream(
                        new java.io.File(getNoBackupFilesDir(), "motor/motor.log"), true))) {
                    e.printStackTrace(trace);
                } catch (java.io.IOException ignored) {}
                status("Falló: " + e.getMessage() + "\nMantén pulsado este mensaje para guardar el registro.");
            }
            finally {
                active.clear(); busy.set(false);
                if (lock.isHeld()) lock.release();
                stopForeground(STOP_FOREGROUND_REMOVE); stopSelf();
            }
        }, "KostMotor").start();
        return START_NOT_STICKY;
    }
    @Override public void onDestroy() { cancel(); super.onDestroy(); }
    @Override public IBinder onBind(Intent intent) { return null; }
}
