# Instrucciones reproducibles — Kost Descargador

Corte: 29 de octubre de 2026. Estas instrucciones documentan las rutas existentes; los comandos no se han vuelto a ejecutar sobre un teléfono durante esta recopilación. [Estado de cada variante](BITACORA.md) · [Pruebas y errores](PRUEBAS_Y_PENDIENTES.md).

## 1. Obtener fuentes e identificar la variante

En Ubuntu, para una copia nueva:

```bash
git clone https://github.com/L7508/kost-descargador.git
cd kost-descargador
git status
git log -1 --oneline
git branch --show-current
```

Si ya tienes una copia, conserva o confirma tus cambios antes de cambiar de rama. No sustituyas tu copia funcional por un ZIP sin respaldo.

- `main`: organización original y documentación consolidada.
- `experimental/motor-integrado`: app independiente nueva y flujo de entrega de APK.

Para abrir el motor independiente desde una copia limpia:

```bash
git fetch origin
git switch experimental/motor-integrado
```

Si esa rama no existe localmente, Git normalmente crea el seguimiento de `origin/experimental/motor-integrado`. No uses `reset --hard` para resolver diferencias. Abre **la carpeta de una app concreta**, no todas a la vez, en Android Studio.

| Variante | Carpeta que se abre | Paquete Android |
| --- | --- | --- |
| Motor integrado experimental | `apps/android-motor-integrado` en la rama experimental | `com.kost.descargador.interno` |
| Termux automático | `apps/android-termux-automatico` | `com.kost.descargador.termux` |
| Servidor Android | `apps/android-servidor` | `com.kost.descargador` |
| Diagnóstico interno anterior | `apps/android-interno-diagnostico` | `com.kost.descargador` |

Las dos últimas comparten paquete y pueden reemplazarse al instalar. La ruta de V1 dentro de `archivo` está incompleta y no es la base activa.

## 2. Primera prueba recomendada: APK integrada ya publicada

1. En el teléfono abre la [entrega experimental](https://github.com/L7508/kost-descargador/releases/tag/kost-motor-interno-e0e16a4).
2. En **Assets**, descarga `Kost_motor_interno_v01.apk`. El ZIP es para las fuentes, no para instalar en Android.
3. Instala **Kost motor interno**. Android puede pedir permiso para instalar desde el navegador. Conserva las otras variantes.
4. Pega una URL pública HTTPS de YouTube; empieza con un video corto que ya haya funcionado en YTDLnis, en el mismo teléfono y red.
5. Pulsa Descargar una sola vez. Registra cada fase y el resultado final.
6. Busca el archivo en **Descargas/Kost** y comprueba imagen, sonido y duración. Que otro reproductor no lo indexe no significa necesariamente que el archivo no exista.
7. Si hay error, mantén pulsado el estado para exportar el registro. Guárdalo antes de iniciar otro intento: el siguiente reemplaza `motor.log`.

### Qué hace esta versión según su código

- Android 10 o superior y ABI ARM64; compileSdk 36, targetSdk 35, JDK 17 para la construcción.
- Extrae Python y FFmpeg en el espacio privado. No usa el servidor Ubuntu, `RUN_COMMAND`, pkg ni pip para preparar el teléfono.
- Ejecuta yt-dlp como lista de argumentos mediante ProcessBuilder; valida dominio/HTTPS e introduce `--` antes de la URL.
- Comprueba Python/curl-cffi y ejecuta una prueba mínima de QuickJS.
- Declara QuickJS y permite obtener EJS desde GitHub. Necesita acceso a Internet, también a GitHub cuando descarga ese componente.
- Selecciona video hasta 720p más audio y puede combinar en MKV. No promete MP4 universal ni compatibilidad de códecs con todos los reproductores.
- Si la primera ruta falla con 403, prueba Chrome mediante curl-cffi; ambas rutas comparten un presupuesto aproximado de 600 segundos de descarga.
- Comprueba pistas con ffprobe y publica por MediaStore. No decodifica cada cuadro ni garantiza por sí sola la reproducción correcta.
- Usa un servicio en primer plano con notificación, bloqueo de intentos simultáneos mientras vive el servicio y botón Cancelar.

Las fases adicionales de preparación, comprobación, ffprobe y copia a Descargas no están incluidas en los 600 segundos de las rutas de descarga. El comportamiento ante muerte del proceso o cierre forzado requiere pruebas específicas.

### Construir el motor nuevo

En Android Studio configura JDK 17 y SDK 36. La versión verificada usó AGP 8.13.2 y wrapper Gradle 8.13, no el wrapper 9.3 de otros proyectos históricos. No copies configuraciones Java 25 de otra carpeta.

Desde `apps/android-motor-integrado`:

```bash
./gradlew :app:clean :app:assembleDebug :app:lintDebug
```

Salida: `app/build/outputs/apk/debug/app-debug.apk`. Las fuentes incluyen el ejecutable yt-dlp fijado y su SHA-256. Las dependencias se resuelven desde Google/Maven Central; el SDK y las dependencias deben estar accesibles. `local.properties` identifica el SDK de tu equipo y no se sube a Git.

La firma es **debug**. El APK construido en otro equipo puede tener una clave distinta. Si Android rechaza una actualización, identifica primero qué paquete tienes instalado; reinstalar solo la variante experimental puede borrar sus datos y logs. No desinstales Termux para resolver una incompatibilidad de firma de Kost.

## 3. Ruta histórica funcional: servidor Ubuntu

Desde la raíz del repositorio:

```bash
cd servidores/ubuntu
python3 -m venv .venv
.venv/bin/python -m pip install -U 'yt-dlp[default]'
.venv/bin/python -m yt_dlp --version
ffmpeg -version
.venv/bin/python server.py --lan
```

El comando de instalación con `-U` obtiene la versión disponible en ese momento. El valor histórico que resolvió el primer 403 fue `2026.08.19`; registrar el resultado real, no asumir que seguirá siendo esa versión ni que ese arreglo resolverá cualquier 403 futuro.

El servidor imprime la dirección y una clave nueva para el acceso LAN. Utiliza la IP local real de Ubuntu y conserva `?key=...` al configurar el cliente. Ubuntu debe estar encendido y en la misma red. Puerto 8000. No publiques la clave ni abras este servidor HTTP a Internet.

Para aislar un fallo del servidor, desde esa carpeta y con una URL autorizada:

```bash
.venv/bin/python -m yt_dlp -v --no-playlist -f 'bv*+ba/b' 'URL_PUBLICA_DEL_VIDEO'
```

Reemplaza todo el texto `URL_PUBLICA_DEL_VIDEO`; las comillas conservan `&` y otros caracteres. Si la terminal también falla, la interfaz Android no basta para resolver ese fallo. No mezcles un `python3` global antiguo con el Python del entorno actualizado.

## 4. Ruta histórica funcional: servidor Termux

Esta ruta conserva Termux como motor. Tener su pantalla cerrada no elimina la dependencia del proceso de Termux.

Preparación en Termux, con autorización de almacenamiento:

```bash
termux-setup-storage
pkg update
pkg install python ffmpeg deno
python -m pip install -U 'yt-dlp[default]'
python -m yt_dlp --version
deno --version
ffmpeg -version
```

No actualices el propio pip sustituyendo a ciegas el administrado por Termux. No ejecutes varias instalaciones pip en paralelo. El conjunto exacto que funcionó antes debe registrarse con sus versiones.

Copia juntos `server.py` y `Descargador.html` de `servidores/termux-corregido` a una carpeta del teléfono accesible desde Termux. Desde esa carpeta:

```bash
python server.py --phone
```

Usa en el mismo teléfono la dirección `127.0.0.1:8000` con la clave que imprime esa variante. No reutilices la clave de otro arranque. El servidor de Ubuntu solo acepta `--lan`; `--phone` corresponde a las variantes Termux que lo implementan.

La descarga directa puede aislarse con:

```bash
python -m yt_dlp -v --no-playlist -f 'bv*+ba/b' -P "$HOME/storage/downloads" 'URL_PUBLICA_DEL_VIDEO'
```

`$HOME` aquí es la variable existente de Termux; no se redefine. Conservar las comillas de la ruta y de la URL. Esta prueba es un procedimiento, no un nuevo resultado exitoso.

## 5. Ruta automática: RUN_COMMAND

### Preparación única

1. Identifica Termux y su procedencia. La instalación usada en las pruebas cambió de una variante sin RUN_COMMAND a otra obtenida desde F-Droid. No generalizar sobre todas las ediciones de Play Store ni mezclar firmas de complementos.
2. Ejecuta `termux-setup-storage` y acepta el permiso.
3. Edita `~/.termux/termux.properties` para dejar una sola línea activa:

```properties
allow-external-apps=true
```

4. Ejecuta `termux-reload-settings` o reinicia Termux.
5. Instala `apps/android-termux-automatico` y concede a **Descargador con Termux** el permiso para ejecutar comandos.

La app no puede concederse permisos Android ni habilitar por sí sola `allow-external-apps`. `pkg` y `pip` no agregan permisos declarados en la APK de Termux.

### ADB si el permiso no aparece

En Ubuntu, con depuración autorizada y conexión ya establecida:

```bash
"$HOME/Android/Sdk/platform-tools/adb" devices
"$HOME/Android/Sdk/platform-tools/adb" -s 'SERIAL_ACTUAL' shell pm grant com.kost.descargador.termux com.termux.permission.RUN_COMMAND
```

Sustituye `SERIAL_ACTUAL` por el dispositivo correcto del listado actual. No copies los identificadores inalámbricos antiguos del chat. Si tu SDK está en otra ruta, consulta `sdk.dir` en `local.properties`.

- `more than one device/emulator`: falta seleccionar dispositivo con `-s`.
- `Unknown permission`: revisar qué declara Termux; no es un problema que se arregle repitiendo pm grant o instalando yt-dlp.
- Antes de reinstalar Termux, respaldar su entorno y verificar el respaldo; desinstalarlo puede borrar herramientas y archivos privados.

Para identificar la instalación:

```bash
"$HOME/Android/Sdk/platform-tools/adb" -s 'SERIAL_ACTUAL' shell dumpsys package com.termux | rg 'RUN_COMMAND|versionName'
```

### Comportamiento y tiempos de la variante automática

El script fijo recibe la URL como `$1`. Comprueba Python, FFmpeg y Deno; si faltan, intenta instalarlos. El intento de actualización del paquete principal usa `timeout 90s python -m pip install --no-deps -U yt-dlp`, conservando la instalación existente si la actualización falla y yt-dlp ya está presente.

La descarga usa `timeout -k 10s 600s yt-dlp`, dos reintentos y espera de socket de 20 segundos. **El timeout de 600 segundos no abarca pkg, pip, ffprobe ni el escaneo.** Se comprueban las pistas antes de declarar éxito. El formato `bv*+ba/b` no coincide con el límite de 720p del motor nuevo.

Si está disponible `termux-media-scan`, el script intenta escanear el resultado. Requiere paquete `termux-api` y complemento **Termux:API** compatible; no es condición para que el archivo exista en Descargas.

## 6. Descarga aparentemente detenida

En Termux:

```bash
ps -ef | grep -E '[p]kg|[a]pt|[p]ip|[y]t-dlp|[f]fmpeg'
ls -lh "$HOME/storage/downloads"
```

El espacio es `ls -lh`, no `ls - lh`. Para ver un proceso concreto utiliza un PID del listado actual:

```bash
ps -p PID_ACTUAL -o pid,etime,stat,args
```

Que exista un proceso no demuestra que el archivo avance. Compara tamaño/fecha de un mismo `.part`, salida y tiempo transcurrido. Si necesitas cancelar manualmente, identifica primero el proceso y ejecuta `kill PID_ACTUAL`; vuelve a consultar el listado. No reutilices los PID 16245, 19605, 21731 o 21017 del historial.

En la variante automática, restablece la marca manteniendo pulsado el **estado** solo después de verificar que no queda una descarga o instalación activa. En el motor integrado nuevo utiliza su botón Cancelar; no presupongas que comparte el mismo mecanismo de restablecimiento.

## 7. Comprobar archivos y registrar pruebas

Si tienes ffprobe en el equipo donde está el archivo:

```bash
ffprobe -v error -show_entries format=duration,size:stream=codec_type,codec_name -of json '/ruta/al/archivo'
```

La presencia de audio/video se complementa con reproducción real. No identifiques el contenido solo por la extensión. MKV, WebM y MP4 son contenedores; un reproductor puede no soportar alguno de sus códecs.

Usa la [plantilla de prueba](PRUEBAS_Y_PENDIENTES.md#plantilla-para-cada-ejecución). Conserva el registro antes de reintentar. Antes de publicarlo, retira cookies, claves del servidor, credenciales de proxy, tokens y enlaces firmados temporales. No subir `cookies.txt`, keystores ni `local.properties` al repositorio.

## 8. REA

El procedimiento está en [REA.md](REA.md). Se ha probado la CLI, no una integración MCP completa. El siguiente paso para analizar métodos requiere resolver el fallo de procps/ps en un entorno compatible y comprobar JADX/JDK. No se debe sustituir la comprobación de propiedad de procesos para saltarse el fallo. La inspección estática no reproduce tráfico ni prueba descargas.

## 9. Git y documentación

```bash
git status
git diff --check
git diff -- docs README.md
git add docs README.md
git commit -m 'Documenta resultados verificados de Kost'
git push origin NOMBRE_DE_LA_RAMA
```

Reemplaza el nombre de rama; no pegues el marcador literal. Añade solo archivos revisados. Registrar un nuevo resultado debe actualizar la bitácora y su caso de prueba, manteniendo los intentos fallidos anteriores. No fusionar automáticamente el motor experimental con la copia funcional.
