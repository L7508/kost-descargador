# Descargador con Termux: interfaz simple (prototipo)

La interfaz contiene un campo para el enlace, un botón Descargar y mensajes de estado. Requiere Termux instalado: este prototipo no es una APK independiente. Se instala con `applicationId` distinto de la versión anterior para conservarla.

## Lo que hace la app

- Solicita al usuario el permiso `com.termux.permission.RUN_COMMAND` y envía a Termux un comando con `RUN_COMMAND`. Termux también exige que el usuario active una sola vez `allow-external-apps=true` en `~/.termux/termux.properties`.
- Comprueba al iniciar una descarga si faltan Python, FFmpeg o Deno. Solo en ese caso intenta `pkg update -y` y `pkg install -y python ffmpeg deno`. Android podría requerir que se abra Termux si la instalación necesita intervención; los permisos de almacenamiento no pueden concederse en silencio.
- En cada descarga intenta actualizar el paquete principal con `timeout 90s python -m pip install --no-deps -U yt-dlp`. Las dependencias opcionales no se compilan en cada pulsación. El extractor usa `--remote-components ejs:github` para solicitar los componentes JavaScript cuando se necesiten. Se requiere conexión a GitHub desde el teléfono. Si falla la actualización pero ya existe yt-dlp, intenta descargar con esa instalación e informa del fallo; si yt-dlp falta, detiene la descarga.
- Valida que el enlace sea HTTPS y que el host sea youtube.com, uno de sus subdominios o youtu.be. Lo envía como un único argumento `$1` a un script fijo con `"$1"`; el texto del enlace no se interpreta como código shell.
- Usa la carpeta principal Descargas y un nombre de archivo restringido (`%(title)s_%(id)s.%(ext)s` con `--restrict-filenames`). El tipo resultante puede ser webm o mp4, según el video.
- Si `termux-media-scan` está disponible, intenta pedir un escaneo al terminar. Esta herramienta requiere el paquete `termux-api` y la aplicación complementaria Termux:API instalada de la misma procedencia y firmada de forma compatible. El escaneo no es requisito para que el archivo exista en Descargas.
- Muestra el inicio, la finalización y algunos errores. No informa el porcentaje de avance en esta versión.
- Mantiene el botón disponible, pero si ya existe una descarga en curso, una nueva pulsación muestra el estado sin iniciar otro proceso. Si Termux nunca devuelve un resultado y confirmaste con `ps -ef` que ya no quedan procesos de descarga o actualización, mantén presionado el texto de estado para borrar esa marca.
- Antes de declarar éxito, comprueba con `ffprobe` que el archivo exista, no esté vacío y tenga al menos una pista de video y una de audio. Si falta una pista, informa el fallo y conserva el archivo para poder inspeccionarlo.
- Cada petición tiene un máximo total de 10 minutos, `--socket-timeout 20`, dos reintentos normales y dos para fragmentos. Si supera el límite, informa el tiempo agotado y conserva cualquier archivo parcial para diagnóstico.

## Preparación una sola vez en el teléfono

1. Conserva tu instalación de Termux, que ya consiguió descargar. Para una instalación nueva, comprueba la documentación vigente de Termux: F-Droid y el repositorio oficial de GitHub son las fuentes habituales de su rama principal. Existe también una rama de Play Store, por lo que no es exacto afirmar que toda instalación de Play Store esté desactualizada. No mezcles complementos descargados de fuentes con firmas distintas.
2. En Termux, ejecuta `termux-setup-storage` y concede acceso a archivos cuando Android lo pida. Verifica que puedas guardar en `/storage/emulated/0/Download`.
3. Edita `~/.termux/termux.properties` y deja una sola línea `allow-external-apps=true`; después ejecuta `termux-reload-settings` o reinicia Termux.
4. Abre este proyecto en Android Studio, sincroniza Gradle e instala con Run. Al pulsar Descargar, acepta el permiso «Ejecutar comandos en Termux». Si no aparece el aviso, consíguelo en Ajustes de Android > Aplicaciones > Descargador con Termux > Permisos adicionales.

No uses `pkg upgrade` en cada descarga: modifica todo el entorno, puede pedir intervención y no es necesario para actualizar yt-dlp. Si la instalación automática de paquetes no funciona, en Termux ejecuta una vez `pkg update && pkg install python ffmpeg deno` y `python -m pip install -U 'yt-dlp[default]'`.

## Prueba

Pega un enlace público de YouTube y pulsa Descargar. El enlace se recibe como un único argumento, aunque contenga `&`. Al terminar, revisa la carpeta Descargas. Si la app informa error de código o no termina, comparte el mensaje en pantalla y consulta Termux. No compartas cookies, claves ni archivos privados.

Esta fuente no pudo compilarse ni probarse en un teléfono desde este entorno, pues aquí no hay Android SDK. El resultado automático de `RUN_COMMAND` requiere Termux >= 0.109. La marca interna de preparación se registra tras una descarga correcta; las herramientas se comprueban de nuevo en cada descarga sin repetir su instalación cuando están presentes.
