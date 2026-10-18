package com.kost.descargador.interno;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.content.Intent;
import android.widget.*;

public class MainActivity extends Activity {
    private final Handler handler = new Handler();
    private TextView status;
    private final Runnable refresh = new Runnable() {
        public void run() {
            status.setText(getSharedPreferences("job", 0).getString("status", "Listo. Pega un enlace de YouTube."));
            handler.postDelayed(this, 750);
        }
    };
    @Override public void onCreate(Bundle state) {
        super.onCreate(state);
        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(32, 48, 32, 24);
        layout.setOnApplyWindowInsetsListener((view, insets) -> {
            layout.setPadding(32, 32 + insets.getSystemWindowInsetTop(), 32, 24 + insets.getSystemWindowInsetBottom());
            return insets;
        });
        EditText url = new EditText(this);
        url.setHint("https://www.youtube.com/watch?v=…");
        url.setInputType(android.text.InputType.TYPE_CLASS_TEXT | android.text.InputType.TYPE_TEXT_VARIATION_URI);
        url.setSingleLine(true);
        Button start = new Button(this); start.setText(R.string.download);
        Button cancel = new Button(this); cancel.setText(R.string.cancel);
        status = new TextView(this); status.setTextIsSelectable(true);
        ScrollView scroll = new ScrollView(this); scroll.addView(status);
        layout.addView(url); layout.addView(start); layout.addView(cancel); layout.addView(scroll);
        setContentView(layout);
        if (android.os.Build.VERSION.SDK_INT >= 33 && checkSelfPermission("android.permission.POST_NOTIFICATIONS") != android.content.pm.PackageManager.PERMISSION_GRANTED)
            requestPermissions(new String[]{"android.permission.POST_NOTIFICATIONS"}, 1);
        start.setOnClickListener(v -> {
            String value = url.getText().toString().trim();
            if (!Motor.validUrl(value)) { Toast.makeText(this, "Usa un enlace HTTPS de youtube.com o youtu.be.", Toast.LENGTH_LONG).show(); return; }
            Intent intent = new Intent(this, DownloadService.class).putExtra("url", value);
            startForegroundService(intent);
        });
        cancel.setOnClickListener(v -> DownloadService.cancel());
        status.setOnLongClickListener(v -> {
            Intent save = new Intent(Intent.ACTION_CREATE_DOCUMENT).setType("text/plain")
                .addCategory(Intent.CATEGORY_OPENABLE).putExtra(Intent.EXTRA_TITLE, "kost-motor.txt");
            startActivityForResult(save, 2); return true;
        });
    }
    @Override protected void onActivityResult(int request, int result, Intent data) {
        super.onActivityResult(request,result,data);
        if (request != 2 || result != RESULT_OK || data == null || data.getData() == null) return;
        try (java.io.InputStream in = new java.io.FileInputStream(new java.io.File(getNoBackupFilesDir(), "motor/motor.log"));
             java.io.OutputStream out = getContentResolver().openOutputStream(data.getData())) {
            if (out == null) throw new java.io.IOException("Destino no disponible");
            byte[] buffer = new byte[8192]; int n;
            while ((n = in.read(buffer)) != -1) out.write(buffer,0,n);
            Toast.makeText(this,"Registro guardado",Toast.LENGTH_SHORT).show();
        } catch (Exception e) { Toast.makeText(this,"No se pudo guardar el registro: " + e.getMessage(),Toast.LENGTH_LONG).show(); }
    }
    @Override protected void onResume() { super.onResume(); handler.post(refresh); }
    @Override protected void onPause() { handler.removeCallbacks(refresh); super.onPause(); }
}
