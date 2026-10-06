# Diagnóstico de la biblioteca interna

Proyecto de prueba basado en `copy.zip`. Mantiene la descarga con Termux/Ubuntu. En la prueba interna usa la selección automática de formatos de yt-dlp, añade `--verbose` y registra el error completo en Logcat bajo la etiqueta `DescargadorDiagnostico`.

1. En Ubuntu, descomprime este ZIP en una carpeta nueva y abre esa carpeta como proyecto en Android Studio.
2. Sincroniza Gradle, conecta el teléfono y pulsa Run para instalar esta versión de diagnóstico.
3. Cierra Termux. En la app pega una URL pública de YouTube en el campo del enlace. Toca `Diagnóstico de descarga interna`.
4. En Android Studio abre View > Tool Windows > Logcat. Selecciona el teléfono y la app `com.kost.descargador`. Busca `DescargadorDiagnostico` y copia los mensajes de la prueba, ocultando claves y URLs de `googlevideo.com` antes de compartirlos.

La descarga con Termux sigue usando su botón habitual. Esta prueba no demuestra todavía una APK independiente: depende del resultado en el teléfono. Si Gradle muestra un error al compilar, comparte el error completo de Build Output.
