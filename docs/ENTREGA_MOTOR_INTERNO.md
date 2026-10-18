# Kost motor interno — prueba v0.1

APK experimental ARM64 para Android 10 o superior. Incluye Python, yt-dlp, QuickJS, curl-cffi y FFmpeg. No requiere la aplicación Termux ni un servidor Ubuntu. Se instala como `com.kost.descargador.interno`, separada de las variantes anteriores.

Descarga `Kost_motor_interno_v01.apk` en el teléfono para instalar. El ZIP contiene el repositorio completo y el nuevo proyecto en `apps/android-motor-integrado`.

Pega un enlace HTTPS de YouTube y pulsa Descargar. Si hay HTTP 403, intenta la ruta curl-cffi con impersonación Chrome. Verifica video y audio con ffprobe y publica en Descargas/Kost. Mantén pulsado el estado para exportar el registro en caso de error.

La compilación y Lint deben pasar antes de publicar estos archivos. Esto no confirma que las descargas funcionen en tu teléfono: el estado funcional permanece EN PRUEBAS. La firma del APK es debug y esta entrega es para pruebas. Una recompilación puede usar una firma debug diferente; si Android rechaza una actualización, desinstala solo esta app experimental antes de instalar la nueva entrega.

Consulta `apps/android-motor-integrado/TERCEROS.md` y `LICENSE` para procedencia y licencias. Los binarios nativos se obtienen de las dependencias de Maven indicadas allí.
