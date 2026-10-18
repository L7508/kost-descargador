# Procedencia y licencias — experimento de motor interno

El código nuevo de este módulo se distribuye bajo GPL-3.0 (ver LICENSE), para mantener una base compatible con los paquetes utilizados. No se modifican las licencias de las otras carpetas del repositorio.

No se ha copiado el gestor de ejecución de YTDLnis. Se escribió una implementación Java mínima, después de estudiar su arquitectura.

## Referencias examinadas

- YTDLnis: https://github.com/deniscerri/ytdlnis — commit d5beded4ec3c66d589fb1005ba3dd14959575c07, GPL-3.0. `RuntimeManager.kt`, `PackageBase.kt`, `ZipUtils.kt` y `app/build.gradle`.
- Paquetes YTDLnis: https://github.com/deniscerri/ytdlnis-packages — commit c32745216eab2c643643374bb1153fc018086665, GPL-3.0. `scripts/build_python.sh` describe cómo se empaqueta Python y curl-cffi para Android.

## Dependencias fijadas

- `io.github.deniscerri:ytdlnis_packages.python:3.14.6`: paquete Android de Python, curl-cffi para ARM64 y QuickJS. Código y scripts de construcción en el repositorio anterior. Los componentes incluidos conservan sus propias licencias; Python usa PSF y curl-cffi MIT, entre otras.
- `io.github.junkfood02.youtubedl-android:ffmpeg:0.18.1`: https://github.com/JunkFood02/youtubedl-android . Revisar también las licencias y configuración de los componentes FFmpeg antes de distribuir una versión pública.
- `org.apache.commons:commons-compress:1.27.1`: Apache-2.0, https://commons.apache.org/proper/commons-compress/ .
- yt-dlp `2026.08.19`: ejecutable Python multiplataforma oficial, https://github.com/yt-dlp/yt-dlp/releases/tag/2026.08.19 . Su distribución conserva las licencias de yt-dlp y dependencias; ver https://github.com/yt-dlp/yt-dlp/blob/2026.08.19/LICENSE .

La aplicación verifica en cada preparación el SHA-256 del ejecutable contra el archivo `yt-dlp.sha256`. Se comprobó inicialmente con SHA2-256SUMS del lanzamiento oficial. El archivo no se actualiza silenciosamente.

`--remote-components ejs:github` autoriza a yt-dlp a obtener su componente JavaScript oficial por red. Esta prueba necesita Internet y puede fallar si GitHub o YouTube no están accesibles. No activa un servidor de PO Token. Primero prueba el motor sin impersonación forzada; si recibe HTTP 403, reintenta con `--impersonate chrome` y curl-cffi dentro del mismo límite total de diez minutos.

Esta es una prueba local. Antes de publicar binarios a terceros, completar los avisos de los componentes transitivos y el suministro del código fuente correspondiente, incluidos los binarios nativos. No se afirma que esta lista sea una auditoría completa de distribución.
