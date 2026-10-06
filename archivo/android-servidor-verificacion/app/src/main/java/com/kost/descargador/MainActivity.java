package com.kost.descargador;

import android.app.Activity;
import android.content.ContentValues;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import android.widget.Toast;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.ByteArrayOutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import org.json.JSONObject;

public class MainActivity extends Activity {
    private EditText address, video;
    private Spinner format;
    private TextView status;
    private Button download;

    @Override public void onCreate(Bundle state) {
        super.onCreate(state);
        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(24, 32, 24, 16);
        TextView label = new TextView(this);
        label.setText("Ubuntu debe estar encendido y conectado al mismo Wi-Fi. Inicia: .venv/bin/python server.py --lan");
        root.addView(label);
        address = new EditText(this);
        address.setSingleLine(true);
        address.setHint("Dirección completa http://IP:8000/?key=...");
        address.setText(getPreferences(MODE_PRIVATE).getString("address", ""));
        root.addView(address);
        video = new EditText(this);
        video.setSingleLine(true);
        video.setHint("Enlace público de YouTube");
        root.addView(video);
        format = new Spinner(this);
        format.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, new String[]{"Video", "Audio MP3"}));
        root.addView(format);
        download = new Button(this);
        download.setText("Descargar");
        download.setOnClickListener(v -> startDownload());
        root.addView(download);
        status = new TextView(this);
        root.addView(status);
        setContentView(root);
    }

    private void message(String text) { runOnUiThread(() -> status.setText(text)); }

    private void startDownload() {
        final String base = address.getText().toString().trim();
        final String videoUrl = video.getText().toString().trim();
        final Uri uri;
        try { uri = Uri.parse(base); }
        catch (Exception e) { status.setText("Dirección local inválida"); return; }
        String host = uri.getHost();
        String key = uri.getQueryParameter("key");
        if (!"http".equals(uri.getScheme()) || host == null || !host.matches("(192\\.168\\.|10\\.|172\\.(1[6-9]|2[0-9]|3[01])\\.).*") || uri.getPort() != 8000 || key == null || key.isEmpty()) {
            status.setText("Pega la dirección local completa, incluido ?key=..."); return;
        }
        if (!videoUrl.startsWith("https://")) { status.setText("Pega un enlace https de YouTube"); return; }
        final String mode = format.getSelectedItemPosition() == 0 ? "video" : "audio";
        getPreferences(MODE_PRIVATE).edit().putString("address", base).apply();
        download.setEnabled(false);
        status.setText("Procesando; espera mientras Ubuntu descarga el archivo…");
        new Thread(() -> {
            HttpURLConnection connection = null;
            Uri saved = null;
            try {
                connection = (HttpURLConnection) new URL("http://" + host + ":8000/api/youtube").openConnection();
                connection.setConnectTimeout(10000);
                connection.setReadTimeout(300000);
                connection.setRequestMethod("POST");
                connection.setRequestProperty("X-Access-Token", key);
                connection.setRequestProperty("Content-Type", "application/json");
                connection.setDoOutput(true);
                byte[] body = new JSONObject().put("url", videoUrl).put("mode", mode).toString().getBytes(StandardCharsets.UTF_8);
                connection.setFixedLengthStreamingMode(body.length);
                try (OutputStream out = connection.getOutputStream()) { out.write(body); }
                if (connection.getResponseCode() != 200) {
                    try (InputStream error = connection.getErrorStream()) {
                        ByteArrayOutputStream collected = new ByteArrayOutputStream();
                        byte[] chunk = new byte[1024]; int n;
                        while (collected.size() < 4096 && (n = error.read(chunk, 0, Math.min(chunk.length, 4096 - collected.size()))) != -1)
                            collected.write(chunk, 0, n);
                        byte[] data = collected.toByteArray();
                        message(new JSONObject(new String(data, StandardCharsets.UTF_8)).optString("error", "Error del servidor"));
                    }
                    return;
                }
                String disposition = connection.getHeaderField("Content-Disposition");
                String extension = mode.equals("audio") ? "mp3" : "mp4";
                if (disposition != null && disposition.matches(".*\\.(mp3|mp4|webm|mkv|m4a)\\\".*"))
                    extension = disposition.replaceAll(".*\\.(mp3|mp4|webm|mkv|m4a)\\\".*", "$1");
                ContentValues values = new ContentValues();
                values.put(MediaStore.Downloads.DISPLAY_NAME, "descarga_" + System.currentTimeMillis() + "." + extension);
                values.put(MediaStore.Downloads.MIME_TYPE, extension.equals("mp3") ? "audio/mpeg" : "video/" + extension);
                values.put(MediaStore.Downloads.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS);
                values.put(MediaStore.Downloads.IS_PENDING, 1);
                saved = getContentResolver().insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, values);
                if (saved == null) throw new Exception("No se pudo guardar en Descargas");
                try (InputStream input = connection.getInputStream(); OutputStream out = getContentResolver().openOutputStream(saved)) {
                    byte[] buffer = new byte[65536]; int count;
                    while ((count = input.read(buffer)) != -1) out.write(buffer, 0, count);
                }
                values.clear(); values.put(MediaStore.Downloads.IS_PENDING, 0);
                getContentResolver().update(saved, values, null, null);
                saved = null;
                message("Descarga terminada. Revisa la carpeta Descargas.");
            } catch (Exception e) {
                if (saved != null) getContentResolver().delete(saved, null, null);
                message("Error de conexión o guardado: " + e.getClass().getSimpleName() + ": " + e.getMessage());
            } finally {
                if (connection != null) connection.disconnect();
                runOnUiThread(() -> download.setEnabled(true));
            }
        }).start();
    }
}
