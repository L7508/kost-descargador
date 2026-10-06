# Descargador Android, prototipo nativo

La app tiene su propio formulario y guarda el resultado en Descargas de Android. Ubuntu sigue procesando los enlaces, debe permanecer encendido y estar en el mismo Wi-Fi. No es aún una app independiente.

## Compilación

Abre esta carpeta en Android Studio con Android SDK 35, sincroniza Gradle y genera un APK de depuración en Build > Build APK(s). Requiere Android 10 o posterior. No hay APK incluido: este entorno no tiene SDK ni Gradle, por lo que no se pudo compilar ni probar en un teléfono.

## Uso

En Ubuntu, ejecuta `.venv/bin/python server.py --lan`. En la app pega la dirección completa que imprime el servidor, reemplazando `IP_DE_UBUNTU` por su dirección Wi-Fi y conservando `?key=...`. Pega el enlace público de YouTube y elige Video o Audio MP3. La dirección del servidor se guarda en las preferencias privadas; al reiniciarlo tendrás que pegar la nueva clave.

Usa el servidor solo dentro de una red privada; la comunicación local es HTTP y no ofrece protección de transporte. No expongas el puerto 8000 a Internet.
