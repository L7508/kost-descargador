# Fase de prueba: descarga en el teléfono con Termux

Esta fase usa la instalación de yt-dlp, Deno y FFmpeg que ya funcionó en Termux. La app se conecta al servidor en `127.0.0.1`, el mismo teléfono. No necesita Ubuntu ni Wi-Fi compartido. Sí necesita Termux abierto y el servidor ejecutándose. Aún no es una APK autónoma.

## Archivos

- `termux/server.py` y `termux/Descargador.html`: copia ambos a la carpeta personal de Termux (`~`). Si descargas estos archivos por separado en el teléfono, en Termux usa `termux-setup-storage` y luego `cp ~/storage/downloads/server.py ~/` y `cp ~/storage/downloads/Descargador.html ~/`.
- `android/MainActivity.java`: sustituye el contenido del ÚNICO `app/src/main/java/com/kost/descargador/MainActivity.java` del proyecto `downloader apk V1`. No dejes otra clase pública MainActivity en la carpeta Java.

## Uso

1. En Termux, ejecuta `python ~/server.py --phone`. La terminal imprimirá una URL `http://127.0.0.1:8000/?key=...`. No compartas la clave. Deja Termux ejecutándose (sin cerrarlo ni deslizarlo para eliminarlo).
2. Compila la app en Android Studio (`Build > Make Project`) e instálala en el teléfono. Si Android limita Termux en segundo plano, desactiva la optimización de batería para Termux durante la prueba.
3. Pega en el primer campo de la app la URL completa que imprimió Termux. Pega la URL pública de YouTube en el segundo campo; elige video o audio MP3 y pulsa `Descargar con Termux o Ubuntu`.
4. El archivo debe aparecer en Descargas del teléfono. El botón de prueba de biblioteca interna sigue allí solo para diagnóstico y puede dar 403; no lo uses en esta prueba.

Cada reinicio del servidor crea una clave nueva. La variante `--phone` atiende únicamente en el propio teléfono (127.0.0.1); `--lan` en Ubuntu sigue funcionando para comparar. El video que funcionó directamente en Termux es la primera prueba recomendada. La API devuelve archivos de hasta 200 MB; la app espera hasta cinco minutos antes de declarar un fallo de lectura.
