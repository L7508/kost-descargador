# Bitácora — organización para Git

Fecha: 2026-10-06. Paso concluido: reorganización de las fuentes del respaldo repaso.zip.

- Se renombraron las carpetas de las tres apps, los servidores y los antecedentes con nombres descriptivos sin espacios.
- Se cambiaron únicamente los nombres visibles de proyecto en settings.gradle. Los paquetes Android y el comportamiento del código se conservaron.
- Se extrajeron los paquetes históricos para consultar sus fuentes sin mantener ZIP anidados en Git. Se omitieron copias idénticas de los mismos paquetes.
- Se conservaron scripts y JAR de Gradle; se asignó permiso de ejecución a gradlew.
- Se agregaron README principal, guía Git, .gitignore, .gitattributes y registro de procedencia.
- Se excluyeron entornos Python, cachés, carpetas de compilación, APK, configuraciones del equipo y un documento PDF ajeno al proyecto. El respaldo original permanece intacto.
- Se verificaron integridad ZIP, fuentes copiadas, sintaxis Python e inclusión de archivos mediante Git en un repositorio temporal.

Pendiente: compilar las apps, probarlas en Android y continuar el diagnóstico del motor interno. La APK independiente y las etapas funcionales no se marcan como concluidas.

Observaciones preservadas: V1 está incompleta; android-servidor conserva minSdk 29 y targetSdk 28, que deben revisarse antes de compilar; el límite de 10 minutos de la versión automática cubre yt-dlp, no toda la preparación. No se corrigieron estos puntos en esta tarea de organización.

## 2026-10-17 — Preparación de motor integrado experimental

Estado funcional: **EN PRUEBAS**. No se da por resuelto el 403 ni por concluida la APK autosuficiente.

Pasos realizados:

- Se revisaron las fuentes públicas de YTDLnis y sus paquetes; la consola usa componentes de Termux, y las descargas arrancan Python con ProcessBuilder en el entorno de la aplicación.
- Se agregó `apps/android-motor-integrado` como proyecto independiente, paquete `com.kost.descargador.interno`. Las versiones anteriores se conservaron sin cambios.
- Se implementó extracción del entorno nativo, comprobación de Python/curl-cffi/QuickJS, ejecución de yt-dlp con argumentos separados y verificación final con ffprobe.
- Se añadió un servicio en primer plano, cancelación, límite de diez minutos para las rutas de descarga y exclusión de intentos simultáneos mientras vive el servicio.
- Se añadió publicación por MediaStore en Descargas/Kost y exportación del registro manteniendo pulsado el estado.
- Se fijó yt-dlp 2026.08.19; el ejecutable se verificó contra SHA2-256SUMS de su lanzamiento oficial y la app comprueba el hash antes de usarlo.
- Se comprobó en el AAR de Maven que Python incluye curl-cffi, certificados y QuickJS para ARM64; los enlaces simbólicos del archivo de Python permanecen dentro del paquete.
- Se documentaron procedencia y licencias del nuevo módulo en TERCEROS.md y LICENSE.

Pendiente: instalar en el teléfono, verificar reproducción con imagen y audio, revisar cancelación y confirmar el resultado de las rutas sin y con impersonación. La presencia de pistas detectada por ffprobe no prueba por sí sola la reproducción completa.

Verificación de construcción concluida el 2026-10-17:

- Se preparó un SDK Android 36 y un JDK 17 temporal y se ejecutó `./gradlew :app:clean :app:assembleDebug :app:lintDebug`: **BUILD SUCCESSFUL**, 48 tareas ejecutadas.
- Lint terminó con **0 errores y 4 advertencias**: target 35 frente a 36, versión posterior de Gradle, versión posterior de Commons Compress y exclusión intencional de x86/ChromeOS. No quedaron advertencias de alineación de bibliotecas nativas.
- Se usó FFmpeg 0.18.1 y se comprobó su alineación de 16 KB. Se verificaron la firma APK, la integridad ZIP y la presencia de Python, QuickJS, FFmpeg y ffprobe ARM64.
- Se volvió a verificar el SHA-256 del ejecutable yt-dlp dentro del APK.

Esto concluye la preparación y compilación de la prueba, no la validación de descargas en Android. No se probó ningún teléfono ni se publicaron cambios remotos en GitHub.

## 2026-10-17 — Entrega mediante GitHub concluida

- Se publicó el código en la rama `experimental/motor-integrado`, sin modificar `main`.
- GitHub Actions completó con éxito compilación, Lint y publicación: https://github.com/L7508/kost-descargador/actions/runs/39082989523 .
- Se publicó una entrega experimental con APK, ZIP de fuentes y SHA256SUMS: https://github.com/L7508/kost-descargador/releases/tag/kost-motor-interno-e0e16a4 .
- El commit de la entrega es `e0e16a435824be0f3552656323dc0fec01bc21a2`. El APK se recompiló y firmó en GitHub; el ZIP corresponde a ese commit.

La entrega de archivos queda concluida. La instalación, reproducción y descarga en el teléfono continúan EN PRUEBAS; no se declara resuelto el HTTP 403.
