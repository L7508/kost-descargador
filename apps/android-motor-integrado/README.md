# Kost — motor integrado experimental

Proyecto independiente para Android Studio. No necesita Termux instalado ni Ubuntu como servidor. No modifica las apps existentes. Estado: EN PRUEBAS, descarga en teléfono aún no verificada.

## Probar

1. Abrir esta carpeta en Android Studio y dejar sincronizar Gradle. Requiere JDK 17, SDK 36 y conexión a Maven Central/Google.
2. Instalar con Run en un teléfono ARM64, Android 10 o superior. Se identifica como **Kost motor interno**, paquete `com.kost.descargador.interno`.
3. Pegar un enlace HTTPS de YouTube y pulsar Descargar una vez. Se puede salir de la pantalla; un servicio en primer plano mantiene la tarea con notificación. Cancelar detiene el proceso activo.
4. La primera preparación extrae el paquete de Python y FFmpeg. Las siguientes reutilizan esa instalación. No ejecuta pkg ni pip.
5. El resultado pasa por ffprobe: debe contener pistas de audio y video. Después se copia mediante MediaStore a **Descargas/Kost**. Su aparición en una galería depende de cómo esa app indexe Descargas y de los códecs soportados.

## Diagnóstico

La pantalla muestra preparación, comprobación de herramientas, descarga, verificación y publicación. Una sola tarea a la vez; límite de 10 minutos para yt-dlp. El registro privado completo se conserva hasta el siguiente intento en `no_backup/motor/motor.log`. Mantener pulsado el estado permite guardarlo como texto desde el selector de Android. Puede contener la URL y direcciones temporales del video: revisar antes de compartir o subir a Git.

No se solicitan cookies ni credenciales. La URL se valida y se pasa como argumento separado, después de `--`. No se interpreta con un shell.

## Motor

Python 3.14.6 del paquete de YTDLnis; incluye curl-cffi para ARM64 y QuickJS. FFmpeg/ffprobe se incluyen como dependencias nativas. Se usa yt-dlp 2026.08.19, verificado con SHA-256. QuickJS se declara con `--js-runtimes`; EJS puede descargarse desde GitHub. No se habilita PO Token. Si la primera ruta devuelve HTTP 403, se reintenta con impersonación Chrome mediante curl-cffi, sin ampliar el límite total de diez minutos. El fallo HTTP 403 puede persistir: esta prueba establece primero una base reproducible.

Se selecciona video hasta 720p con audio, se combinan pistas en MKV si es necesario y se guardan nombres restringidos. ffprobe comprueba presencia de pistas, no decodifica cada cuadro del archivo: la reproducción real debe comprobarse en el teléfono.

## Compilar por terminal

```bash
./gradlew :app:assembleDebug :app:lintDebug
```

Salida: `app/build/outputs/apk/debug/app-debug.apk`. La firma debug es solo para pruebas. Consultar TERCEROS.md y LICENSE antes de distribuir públicamente.

## Validación de esta entrega

Se ejecutó una construcción limpia con Android SDK 36 y JDK 17: `:app:clean :app:assembleDebug :app:lintDebug`, BUILD SUCCESSFUL. Lint: 0 errores y 4 advertencias sobre versiones posteriores y el alcance ARM64. La firma debug y la integridad del APK se verificaron. No se ha probado la ejecución de estas bibliotecas ni una descarga en un teléfono. Instalar y descargar un video sigue siendo una comprobación necesaria.
