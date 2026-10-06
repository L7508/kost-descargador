package com.kost.descargador;

import android.app.Activity;
import android.content.ContentValues;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
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
import dev.ffmpegkit_maintained.ytdlp.YtDlp;
import dev.ffmpegkit_maintained.ytdlp.YtDlpRequest;
import dev.ffmpegkit_maintained.ytdlp.YtDlpResponse;
import java.io.File;
import java.io.FileInputStream;
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
    private volatile boolean nativeInitialized;

    @Override public void onCreate(Bundle state) {
        super.onCreate(state);
        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(24, 32, 24, 16);
        TextView label = new TextView(this);
        label.setText("Motor en el teléfono: inicia python server.py --phone en Termux y pega abajo la dirección completa que imprime. El servidor de Ubuntu sigue disponible como alternativa.");
        root.addView(label);
        address = new EditText(this);
        address.setSingleLine(true);
        address.setHint("http://127.0.0.1:8000/?key=...");
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
        download.setText("Descargar con Termux o Ubuntu");
        download.setOnClickListener(v -> startDownload());
        root.addView(download);
        Button localTest = new Button(this);
        localTest.setText("Prueba de biblioteca interna (aún da 403)");
        localTest.setOnClickListener(v -> testOnPhone(localTest));
        root.addView(localTest);
        status = new TextView(this);
        root.addView(status);
        setContentView(root);
    }

    private void message(String text) { runOnUiThread(() -> status.setText(text)); }

    private void testOnPhone(Button button) {
        final String url = video.getText().toString().trim();
        Uri parsed = Uri.parse(url);
        String host = parsed.getHost();
        if (!"https".equals(parsed.getScheme()) || host == null ||
                !(host.equals("youtube.com") || host.endsWith(".youtube.com") || host.equals("youtu.be"))) {
            status.setText("Pega un enlace https de YouTube en el segundo campo.");
            return;
        }
        button.setEnabled(false);
        status.setText("Inicializando yt-dlp en Android…");
        final Handler clock = new Handler(Looper.getMainLooper());
        final long began = android.os.SystemClock.elapsedRealtime();
        final boolean[] finished = {false};
        final long[] lastProgress = {began};
        Runnable ticker = new Runnable() {
            @Override public void run() {
                if (finished[0]) return;
                long elapsed = (android.os.SystemClock.elapsedRealtime() - began) / 1000;
                long silent = (android.os.SystemClock.elapsedRealtime() - lastProgress[0]) / 1000;
                if (silent >= 15) status.setText("Procesando en Android… " + elapsed + " s (sin progreso nuevo desde hace " + silent + " s)");
                clock.postDelayed(this, 5000);
            }
        };
        clock.postDelayed(ticker, 15000);
        new Thread(() -> {
            File working = new File(getCacheDir(), "prueba_" + System.currentTimeMillis());
            try {
                if (!working.mkdirs()) throw new Exception("No se pudo crear carpeta temporal");
                if (!nativeInitialized) {
                    message("Preparando biblioteca en Android…");
                    YtDlp.init(getApplicationContext());
                    nativeInitialized = true;
                }
                YtDlpRequest request = new YtDlpRequest(url)
                        .setOutputTemplate(new File(working, "video.%(ext)s").getAbsolutePath())
                        .addOption("--no-playlist")
                        .addOption("--newline")
                        .addOption("--socket-timeout", "20")
                        .addOption("--retries", "2")
                        .addOption("-f", "best[height<=720]/best");
                message("Descargando en Android…");
                YtDlpResponse response = YtDlp.execute(request, (progress, eta, line) -> {
                    lastProgress[0] = android.os.SystemClock.elapsedRealtime();
                    message("Descargando en Android: " + (int) progress + "%");
                });
                if (!response.isSuccess())
                    throw new Exception("yt-dlp: " + response.getErrorOutput());
                File[] files = working.listFiles((dir, name) -> name.startsWith("video.") &&
                        !name.endsWith(".part") && !name.endsWith(".ytdl"));
                if (files == null || files.length != 1 || files[0].length() == 0)
                    throw new Exception("No se encontró un único video descargado");
                File source = files[0];
                String ext = source.getName().substring(source.getName().lastIndexOf('.') + 1);
                ContentValues values = new ContentValues();
                values.put(MediaStore.Downloads.DISPLAY_NAME, "video_android_" + System.currentTimeMillis() + "." + ext);
                values.put(MediaStore.Downloads.MIME_TYPE,
                        ext.equals("mp4") ? "video/mp4" : ext.equals("webm") ? "video/webm" : "application/octet-stream");
                values.put(MediaStore.Downloads.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS);
                values.put(MediaStore.Downloads.IS_PENDING, 1);
                Uri saved = getContentResolver().insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, values);
                if (saved == null) throw new Exception("No se pudo crear archivo en Descargas");
                try {
                    try (InputStream in = new FileInputStream(source);
                         OutputStream out = getContentResolver().openOutputStream(saved)) {
                        byte[] buffer = new byte[65536]; int n;
                        while ((n = in.read(buffer)) != -1) out.write(buffer, 0, n);
                    }
                    values.clear(); values.put(MediaStore.Downloads.IS_PENDING, 0);
                    getContentResolver().update(saved, values, null, null);
                } catch (Exception e) {
                    getContentResolver().delete(saved, null, null);
                    throw e;
                }
                message("Video guardado en Descargas, procesado sin Ubuntu.");
            } catch (Exception e) {
                String reason = e.getMessage() == null ? e.getClass().getSimpleName() : e.getMessage();
                message("Prueba sin Ubuntu falló: " + reason.substring(0, Math.min(350, reason.length())));
            } finally {
                finished[0] = true;
                clock.removeCallbacks(ticker);
                File[] items = working.listFiles();
                if (items != null) for (File item : items) item.delete();
                working.delete();
                runOnUiThread(() -> button.setEnabled(true));
            }
        }).start();
    }

    private void startDownload() {
        final String base = address.getText().toString().trim();
        final String videoUrl = video.getText().toString().trim();
        final Uri uri;
        try { uri = Uri.parse(base); }
        catch (Exception e) { status.setText("Dirección local inválida"); return; }
        String host = uri.getHost();
        String key = uri.getQueryParameter("key");
        if (!"http".equals(uri.getScheme()) || host == null ||
                !(host.equals("127.0.0.1") || host.matches("(192\\.168\\.|10\\.|172\\.(1[6-9]|2[0-9]|3[01])\\.).*")) ||
                uri.getPort() != 8000 || key == null || key.isEmpty()) {
            status.setText("Pega la dirección completa de Termux o Ubuntu, incluido ?key=..."); return;
        }
        if (!videoUrl.startsWith("https://")) { status.setText("Pega un enlace https de YouTube"); return; }
        final String mode = format.getSelectedItemPosition() == 0 ? "video" : "audio";
        getPreferences(MODE_PRIVATE).edit().putString("address", base).apply();
        download.setEnabled(false);
        status.setText("Procesando en " + (host.equals("127.0.0.1") ? "este teléfono" : "Ubuntu") + "…");
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
