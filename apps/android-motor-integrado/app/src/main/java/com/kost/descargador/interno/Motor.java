package com.kost.descargador.interno;

import android.content.*;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.system.Os;
import org.apache.commons.compress.archivers.zip.*;
import org.json.*;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.security.MessageDigest;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

/** Experimental subprocess runtime. No shell and no dependency on the Termux application. */
final class Motor {
    private final Context context;
    private final Consumer<String> status;
    private final File root, nativeDir, pythonHome, ffmpegHome, pidFile;
    private volatile Process process;
    private volatile boolean cancelled;
    private PrintWriter log;
    Motor(Context context, Consumer<String> status) {
        this.context = context; this.status = status;
        root = new File(context.getNoBackupFilesDir(), "motor");
        nativeDir = new File(context.getApplicationInfo().nativeLibraryDir);
        pythonHome = new File(root, "python"); ffmpegHome = new File(root, "ffmpeg");
        pidFile = new File(root, "job.pid");
    }
    static boolean validUrl(String text) {
        try {
            Uri uri = Uri.parse(text);
            String host = uri.getHost();
            return text != null && !text.matches("(?s).*[\\x00-\\x20\\x7f].*")
                && "https".equals(uri.getScheme()) && uri.getUserInfo() == null
                && (uri.getPort() == -1 || uri.getPort() == 443) && host != null
                && (host.equalsIgnoreCase("youtu.be") || host.equalsIgnoreCase("youtube.com")
                    || host.toLowerCase(Locale.ROOT).endsWith(".youtube.com"));
        } catch (Exception e) { return false; }
    }
    void cancel() { cancelled = true; terminate(); }
    private void terminate() {
        // Python writes its pid before starting yt-dlp; kill children before their parent.
        try { killTree(Integer.parseInt(readText(pidFile).trim())); } catch (Exception ignored) {}
        Process p = process;
        if (p != null) { p.destroy(); p.destroyForcibly(); }
    }
    private void killTree(int pid) throws Exception {
        if (pid <= 1 || pid == android.os.Process.myPid()) return;
        File children = new File("/proc/" + pid + "/task/" + pid + "/children");
        if (children.exists()) {
            String text = readText(children).trim();
            if (!text.isEmpty()) for (String child : text.split("\\s+")) {
                try { killTree(Integer.parseInt(child)); } catch (Exception ignored) {}
            }
        }
        Os.kill(pid, 9);
    }
    void download(String url) throws Exception {
        if (!validUrl(url)) throw new IOException("Enlace no válido");
        root.mkdirs();
        try (PrintWriter writer = new PrintWriter(new OutputStreamWriter(new FileOutputStream(new File(root, "motor.log")), StandardCharsets.UTF_8), true)) {
            log = writer;
            status.accept("1/5 Preparando motor interno…");
            installBundle("libpython.zip.so", pythonHome);
            installBundle("libffmpeg.zip.so", ffmpegHome);
            copyAsset("yt-dlp.bin", new File(root, "yt-dlp"));
            verifyYtdlp();
            File runner = new File(root, "runner.py");
            writeText(runner, "import os,sys,runpy\nopen(sys.argv.pop(1),'w').write(str(os.getpid()))\nscript=sys.argv.pop(1)\nsys.argv[0]=script\nrunpy.run_path(script,run_name='__main__')\n");
            status.accept("2/5 Comprobando Python, curl-cffi y QuickJS…");
            run(Arrays.asList(executable("libpython.so"), "-c", "import sys,curl_cffi; print(sys.version); print('curl-cffi',curl_cffi.__version__)"), 30, false);
            if (!new File(nativeDir, "libqjs.so").isFile()) throw new IOException("Falta QuickJS en el paquete Python");
            run(Arrays.asList(executable("libqjs.so"), "-e", "console.log('QuickJS OK')"), 30, false);
            File output = new File(context.getCacheDir(), "download-" + UUID.randomUUID());
            if (!output.mkdirs()) throw new IOException("No se pudo crear la carpeta temporal");
            try {
                File result = new File(output, "result.txt");
                List<String> command = new ArrayList<>(Arrays.asList(executable("libpython.so"), runner.getAbsolutePath(), pidFile.getAbsolutePath(), new File(root, "yt-dlp").getAbsolutePath(),
                    "--ignore-config", "--no-playlist", "--newline", "--verbose", "--no-cache-dir",
                    "--socket-timeout", "20", "--retries", "2", "--fragment-retries", "2", "--extractor-retries", "2",
                    "--js-runtimes", "quickjs:" + executable("libqjs.so"), "--remote-components", "ejs:github",
                    "--ffmpeg-location", executable("libffmpeg.so"),
                    "-f", "bv[height<=720]+ba/b[height<=720]/b", "--merge-output-format", "mkv",
                    "--restrict-filenames", "-P", output.getAbsolutePath(), "-o", "%(title).100s_%(id)s.%(ext)s",
                    "--print-to-file", "after_move:filepath", result.getAbsolutePath(), "--", url));
                status.accept("3/5 Descargando video y audio…");
                log.println("[KOST RUTA 1] Motor interno con QuickJS; sin impersonación forzada");
                long deadline = android.os.SystemClock.elapsedRealtime() + 600000L;
                try { run(command, 600, true); }
                catch (IOException first) {
                    int remaining = (int)((deadline - android.os.SystemClock.elapsedRealtime()) / 1000);
                    if (cancelled || remaining <= 0 || !String.valueOf(first.getMessage()).contains("403")) throw first;
                    log.println("[KOST RUTA 1] Falló con 403. Se prueba curl-cffi.");
                    status.accept("3/5 HTTP 403: probando impersonación con curl-cffi…");
                    int separator = command.indexOf("--");
                    command.addAll(separator, Arrays.asList("--impersonate", "chrome", "--no-continue", "--force-overwrites"));
                    log.println("[KOST RUTA 2] Impersonación chrome; mismo límite total de diez minutos");
                    run(command, remaining, true);
                }
                if (!result.isFile()) throw new IOException("yt-dlp no informó un archivo final");
                File file = new File(readText(result).trim());
                if (!file.getCanonicalPath().startsWith(output.getCanonicalPath() + File.separator)
                    || !file.isFile() || file.length() == 0) throw new IOException("Archivo final inválido");
                status.accept("4/5 Verificando pistas de video y audio…");
                String probe = run(Arrays.asList(executable("libffprobe.so"), "-v", "error", "-show_entries", "stream=codec_type", "-of", "json", file.getAbsolutePath()), 30, false);
                JSONArray streams = new JSONObject(probe).getJSONArray("streams");
                boolean video = false, audio = false;
                for (int i = 0; i < streams.length(); i++) {
                    String type = streams.getJSONObject(i).optString("codec_type");
                    video |= type.equals("video"); audio |= type.equals("audio");
                }
                if (!video || !audio) throw new IOException("El archivo no contiene ambas pistas: video y audio");
                if (cancelled) throw new IOException("Cancelado");
                status.accept("5/5 Guardando en Descargas…");
                publish(file);
                log.println("[KOST FIN] Archivo publicado; ffprobe confirmó pistas de video y audio.");
                status.accept("Terminó: " + file.getName() + "\nVideo y audio verificados. Guardado en Descargas/Kost.");
            } finally { delete(output); }
        } finally { process = null; pidFile.delete(); }
    }
    private String executable(String name) throws IOException {
        File file = new File(nativeDir, name);
        if (!file.isFile()) throw new IOException("Falta " + name + "; esta prueba requiere ARM64");
        return file.getAbsolutePath();
    }
    private Map<String,String> environment() {
        Map<String,String> env = new HashMap<>();
        env.put("PYTHONHOME", new File(pythonHome, "usr").getAbsolutePath());
        env.put("HOME", root.getAbsolutePath());
        env.put("TMPDIR", context.getCacheDir().getAbsolutePath());
        env.put("LD_LIBRARY_PATH", new File(pythonHome,"usr/lib") + ":" + ffmpegHome + ":" + new File(ffmpegHome,"usr/lib") + ":" + nativeDir);
        env.put("SSL_CERT_FILE", new File(pythonHome,"usr/etc/tls/cert.pem").getAbsolutePath());
        env.put("PATH", nativeDir + ":/system/bin");
        return env;
    }
    private String run(List<String> arguments, int timeout, boolean progress) throws Exception {
        if (cancelled) throw new IOException("Cancelado");
        pidFile.delete();
        ProcessBuilder builder = new ProcessBuilder(arguments).redirectErrorStream(true);
        builder.environment().putAll(environment());
        Process current = builder.start(); process = current;
        if (cancelled) { terminate(); throw new IOException("Cancelado"); }
        StringBuilder capture = new StringBuilder();
        Thread reader = new Thread(() -> {
            try (BufferedReader input = new BufferedReader(new InputStreamReader(current.getInputStream(), StandardCharsets.UTF_8))) {
                String line;
                while ((line = input.readLine()) != null) {
                    log.println(line);
                    synchronized(capture) { if (capture.length() < 262144) capture.append(line).append('\n'); }
                    if (progress && (line.startsWith("[download]") || line.startsWith("[Merger]") || line.startsWith("ERROR:")))
                        status.accept("3/5 Descargando…\n" + line);
                }
            } catch (IOException ignored) {}
        }, "KostOutput");
        reader.start();
        boolean ended = current.waitFor(timeout, TimeUnit.SECONDS);
        if (!ended || cancelled) terminate();
        reader.join(3000);
        process = null; pidFile.delete();
        if (cancelled) throw new IOException("Cancelado");
        if (!ended) throw new IOException("Tiempo agotado (" + timeout + " segundos)");
        if (current.exitValue() != 0) {
            String text = capture.toString();
            int error = text.lastIndexOf("ERROR:");
            throw new IOException(error >= 0 ? text.substring(error).trim() : "Proceso terminó con código " + current.exitValue());
        }
        return capture.toString();
    }
    private void publish(File file) throws Exception {
        boolean mkv = file.getName().endsWith(".mkv");
        ContentValues values = new ContentValues();
        values.put(MediaStore.MediaColumns.DISPLAY_NAME, file.getName());
        values.put(MediaStore.MediaColumns.MIME_TYPE, mkv ? "video/x-matroska" : file.getName().endsWith(".webm") ? "video/webm" : "video/mp4");
        values.put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS + "/Kost");
        values.put(MediaStore.MediaColumns.IS_PENDING, 1);
        ContentResolver resolver = context.getContentResolver();
        Uri uri = resolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, values);
        if (uri == null) throw new IOException("No se pudo crear el archivo en Descargas");
        try {
            try (InputStream in = new FileInputStream(file); OutputStream out = resolver.openOutputStream(uri)) {
                if (out == null) throw new IOException("No se pudo abrir el destino");
                byte[] buffer = new byte[65536]; int size;
                while ((size = in.read(buffer)) != -1) { if (cancelled) throw new IOException("Cancelado"); out.write(buffer,0,size); }
            }
            values.clear(); values.put(MediaStore.MediaColumns.IS_PENDING,0);
            resolver.update(uri,values,null,null);
        } catch (Exception e) { resolver.delete(uri,null,null); throw e; }
    }
    private void copyAsset(String asset, File destination) throws IOException {
        try (InputStream in = context.getAssets().open(asset); OutputStream out = new FileOutputStream(destination)) {
            byte[] buffer = new byte[65536]; int size;
            while ((size = in.read(buffer)) != -1) out.write(buffer,0,size);
        }
    }
    private void verifyYtdlp() throws Exception {
        MessageDigest hash = MessageDigest.getInstance("SHA-256");
        try (InputStream in = new FileInputStream(new File(root,"yt-dlp"))) {
            byte[] b = new byte[65536]; int n;
            while ((n=in.read(b)) != -1) hash.update(b,0,n);
        }
        StringBuilder hex = new StringBuilder(); for(byte b:hash.digest()) hex.append(String.format("%02x",b & 255));
        try (InputStream in = context.getAssets().open("yt-dlp.sha256")) {
            if (!hex.toString().equals(new String(readBytes(in),StandardCharsets.UTF_8).trim())) throw new IOException("SHA-256 de yt-dlp incorrecto");
        }
    }
    private void installBundle(String name, File target) throws Exception {
        File source = new File(nativeDir,name), marker = new File(target,".ready");
        String version = source.length() + ":" + source.lastModified();
        if (marker.isFile() && version.equals(readText(marker))) return;
        delete(target); target.mkdirs();
        try (ZipFile zip = new ZipFile(source)) {
            Enumeration<ZipArchiveEntry> entries = zip.getEntries();
            while(entries.hasMoreElements()) {
                if (cancelled) throw new IOException("Cancelado");
                ZipArchiveEntry entry = entries.nextElement(); File dest = new File(target,entry.getName());
                String base = target.getCanonicalPath() + File.separator;
                if (!dest.getCanonicalPath().startsWith(base)) throw new IOException("Ruta fuera del paquete");
                if (entry.isDirectory()) { dest.mkdirs(); continue; }
                dest.getParentFile().mkdirs();
                try (InputStream in = zip.getInputStream(entry)) {
                    if (entry.isUnixSymlink()) {
                        String link = new String(readBytes(in),StandardCharsets.UTF_8);
                        if (new File(link).isAbsolute() || !new File(dest.getParentFile(),link).getCanonicalPath().startsWith(base))
                            throw new IOException("Enlace fuera del paquete: " + entry.getName());
                        Os.symlink(link,dest.getAbsolutePath());
                    } else {
                        try (OutputStream out = new FileOutputStream(dest)) {
                            byte[] b=new byte[65536]; int n; while((n=in.read(b))!=-1) out.write(b,0,n);
                        }
                    }
                }
            }
        }
        writeText(marker,version);
    }
    private static byte[] readBytes(InputStream in) throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        byte[] buffer = new byte[8192]; int n;
        while ((n = in.read(buffer)) != -1) out.write(buffer,0,n);
        return out.toByteArray();
    }
    private static String readText(File file) throws IOException {
        try (InputStream in = new FileInputStream(file)) { return new String(readBytes(in),StandardCharsets.UTF_8); }
    }
    private static void writeText(File file, String value) throws IOException {
        try (OutputStream out = new FileOutputStream(file)) { out.write(value.getBytes(StandardCharsets.UTF_8)); }
    }
    private static void delete(File file) {
        if (Files.isSymbolicLink(file.toPath())) { file.delete(); return; }
        File[] list = file.listFiles(); if (list != null) for(File child:list) delete(child);
        file.delete();
    }
}
