package com.kost.descargador;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import java.lang.ref.WeakReference;

public class MainActivity extends Activity {
    private static final String PERMISSION = "com.termux.permission.RUN_COMMAND";
    private static final String TERMUX_BIN = "/data/data/com.termux/files/usr/bin/bash";
    // Script fijo: la URL se entrega como $1, nunca se concatena al texto de bash.
    private static final String SCRIPT =
            "set -e\n" +
            "if ! command -v python >/dev/null || ! command -v ffmpeg >/dev/null || ! command -v deno >/dev/null; then\n" +
            "  pkg update -y && pkg install -y python ffmpeg deno\n" +
            "fi\n" +
            "echo 'Preparación verificada'\n" +
            "if ! timeout 90s python -m pip install --no-deps -U yt-dlp >/dev/null; then\n" +
            "  if ! command -v yt-dlp >/dev/null; then echo 'yt-dlp no disponible' >&2; exit 16; fi\n" +
            "  echo 'AVISO: la actualización falló; se usará la versión instalada' >&2\n" +
            "fi\n" +
            "tmp=$(mktemp)\n" +
            "trap 'rm -f \"$tmp\"' EXIT\n" +
            "timeout -k 10s 600s yt-dlp --no-playlist --remote-components ejs:github " +
            "--socket-timeout 20 --retries 2 --fragment-retries 2 -f 'bv*+ba/b' --restrict-filenames " +
            "--paths /storage/emulated/0/Download " +
            "-o '%(title)s_%(id)s.%(ext)s' " +
            "--print-to-file after_move:filepath \"$tmp\" \"$1\"\n" +
            "[ -s \"$tmp\" ] || { echo 'No se generó ruta de archivo' >&2; exit 21; }\n" +
            "while IFS= read -r path; do\n" +
            "  [ -s \"$path\" ] || { echo 'Archivo vacío o inexistente' >&2; exit 22; }\n" +
            "  ffprobe -v error -select_streams v:0 -show_entries stream=index -of csv=p=0 \"$path\" | grep -q . || { echo 'Archivo sin video' >&2; exit 23; }\n" +
            "  ffprobe -v error -select_streams a:0 -show_entries stream=index -of csv=p=0 \"$path\" | grep -q . || { echo 'Archivo sin audio' >&2; exit 24; }\n" +
            "done < \"$tmp\"\n" +
            "if command -v termux-media-scan >/dev/null; then\n" +
            "  while IFS= read -r path; do termux-media-scan \"$path\" >/dev/null 2>&1 || true; done < \"$tmp\"\n" +
            "fi\n";
    private EditText link;
    private TextView status;
    private Button download;
    private static WeakReference<MainActivity> visible = new WeakReference<>(null);

    @Override public void onCreate(Bundle state) {
        super.onCreate(state);
        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(32, 48, 32, 24);
        link = new EditText(this);
        link.setSingleLine(true);
        link.setHint("Enlace de YouTube");
        root.addView(link);
        download = new Button(this);
        download.setText("Descargar");
        download.setOnClickListener(v -> startDownload());
        root.addView(download);
        status = new TextView(this);
        status.setOnLongClickListener(v -> {
            getSharedPreferences("download_status", MODE_PRIVATE).edit().putBoolean("running", false).apply();
            download.setEnabled(true);
            setStatus("Estado reiniciado. Comprueba que no haya otra descarga activa en Termux antes de reintentar.");
            return true;
        });
        root.addView(status);
        setContentView(root);
    }

    @Override protected void onResume() {
        super.onResume();
        visible = new WeakReference<>(this);
        String last = getSharedPreferences("download_status", MODE_PRIVATE).getString("last_status", "");
        status.setText(last);
        download.setEnabled(true);
        if (getSharedPreferences("download_status", MODE_PRIVATE).getBoolean("running", false))
            status.setText(last + "\nSi ya no hay procesos en Termux, mantén pulsado este texto para desbloquear.");
    }

    @Override protected void onPause() {
        visible.clear();
        super.onPause();
    }

    private void setStatus(String message) {
        getSharedPreferences("download_status", MODE_PRIVATE).edit().putString("last_status", message).apply();
        status.setText(message);
    }

    private void startDownload() {
        if (getSharedPreferences("download_status", MODE_PRIVATE).getBoolean("running", false)) {
            setStatus("Ya hay una descarga en curso. Si terminó sin respuesta, mantén pulsado este texto para desbloquear.");
            return;
        }
        String url = link.getText().toString().trim();
        Uri uri = Uri.parse(url);
        String host = uri.getHost();
        if (!"https".equals(uri.getScheme()) || host == null ||
                !("youtube.com".equals(host) || host.endsWith(".youtube.com") || "youtu.be".equals(host))) {
            setStatus("Pega un enlace HTTPS válido de YouTube.");
            return;
        }
        if (url.length() > 2048) { setStatus("El enlace es demasiado largo."); return; }
        if (checkSelfPermission(PERMISSION) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{PERMISSION}, 421);
            setStatus("Autoriza que esta app ejecute descargas en Termux y vuelve a pulsar Descargar.");
            return;
        }
        getSharedPreferences("download_status", MODE_PRIVATE).edit().putBoolean("running", true).apply();
        try {
            Intent result = new Intent(this, DownloadResultReceiver.class);
            result.putExtra("requested_url", url);
            int requestCode = (int) (System.currentTimeMillis() & 0x7fffffff);
            PendingIntent callback = PendingIntent.getBroadcast(this, requestCode, result,
                    PendingIntent.FLAG_ONE_SHOT | PendingIntent.FLAG_MUTABLE);
            Intent command = new Intent("com.termux.RUN_COMMAND");
            command.setClassName("com.termux", "com.termux.app.RunCommandService");
            command.putExtra("com.termux.RUN_COMMAND_PATH", TERMUX_BIN);
            command.putExtra("com.termux.RUN_COMMAND_ARGUMENTS", new String[]{"-s", "--", url});
            command.putExtra("com.termux.RUN_COMMAND_STDIN", SCRIPT);
            command.putExtra("com.termux.RUN_COMMAND_BACKGROUND", true);
            command.putExtra("com.termux.RUN_COMMAND_PENDING_INTENT", callback);
            if (startService(command) == null) throw new Exception("No se encontró Termux.");
            setStatus("Preparando Termux, buscando actualización y descargando…");
        } catch (Exception e) {
            getSharedPreferences("download_status", MODE_PRIVATE).edit().putBoolean("running", false).apply();
            setStatus("No se pudo iniciar Termux. Comprueba su permiso RUN_COMMAND y allow-external-apps=true.");
        }
    }

    @Override public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] results) {
        super.onRequestPermissionsResult(requestCode, permissions, results);
        if (requestCode == 421 && results.length > 0 && results[0] == PackageManager.PERMISSION_GRANTED)
            setStatus("Permiso concedido. Pulsa Descargar.");
        else if (requestCode == 421)
            setStatus("Se necesita autorizar ‘Ejecutar comandos en Termux’ en los permisos de esta app.");
    }

    public static class DownloadResultReceiver extends BroadcastReceiver {
        @Override public void onReceive(Context context, Intent intent) {
            Bundle result = intent.getBundleExtra("result");
            String message;
            if (result == null) message = "Termux no devolvió el resultado. Revisa Termux.";
            else {
                int exitCode = result.getInt("exitCode", -1);
                String stderr = result.getString("stderr", "");
                if (exitCode == 0) {
                    context.getSharedPreferences("download_status", Context.MODE_PRIVATE)
                            .edit().putBoolean("setup_done", true).apply();
                    message = stderr.contains("AVISO: la actualización falló")
                            ? "Descarga terminada en Descargas. No se pudo actualizar yt-dlp."
                            : "Descarga terminada. Revisa Descargas.";
                }
                else if (exitCode == 124 || exitCode == 137)
                    message = "La descarga superó 10 minutos y se detuvo. Revisa la conexión y el archivo parcial.";
                else if (exitCode == 23) message = "El archivo descargado no contiene video. No se marcó como terminado.";
                else if (exitCode == 24) message = "El archivo descargado no contiene audio. No se marcó como terminado.";
                else if (stderr.contains("403")) message = "La descarga falló: HTTP 403.";
                else message = "La descarga falló (código " + exitCode + "). Revisa Termux y el acceso a Descargas.";
            }
            context.getSharedPreferences("download_status", Context.MODE_PRIVATE).edit().putString("last_status", message).apply();
            context.getSharedPreferences("download_status", Context.MODE_PRIVATE).edit().putBoolean("running", false).apply();
            MainActivity activity = visible.get();
            if (activity != null) activity.runOnUiThread(() -> {
                activity.status.setText(message);
                activity.download.setEnabled(true);
            });
        }
    }
}
