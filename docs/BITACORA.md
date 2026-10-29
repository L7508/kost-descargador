# Bitácora consolidada — Proyecto de Kost / Kost Descargador

**Corte documental:** 29 de octubre de 2026, America/Mexico_City.
**Estado funcional:** EN PRUEBAS. La APK autosuficiente no se considera concluida.
**Repositorio:** https://github.com/L7508/kost-descargador

## Cómo leer esta bitácora

Se consolida el historial de conversaciones disponible, las recuperaciones de conversaciones anteriores y las fuentes del repositorio. No es una transcripción íntegra ni garantiza acceso a todos los chats de la cuenta. [Fuentes y límites](FUENTES_Y_ALCANCE.md).

- **REPORTADO POR EL USUARIO:** resultado observado en su equipo; no repetido aquí.
- **VERIFICADO EN FUENTES:** comportamiento presente en el código; no equivale a prueba en teléfono.
- **VERIFICADO EN CONSTRUCCIÓN:** compilación, inspección o publicación con evidencia identificable.
- **PREPARADO / EN PRUEBAS:** implementación entregada cuya ejecución real no está confirmada.
- **PENDIENTE / HIPÓTESIS:** propuesta, explicación posible o prueba sin resultado.

Solo se registran como concluidas las acciones concretas efectivamente realizadas. Una implementación o una compilación correcta no concluyen por sí solas una etapa funcional. La falta de un registro recuperado no demuestra que el usuario nunca haya ejecutado una prueba.

## 1. Objetivo y reglas acordadas

Construir un descargador Android tipo Snaptube, con una interfaz sencilla para introducir una URL y descargar video/audio. La preferencia posterior es una APK que funcione sin servidor Ubuntu y sin depender de la aplicación Termux. Mientras se desarrolla, conservar la versión que funciona mediante servidor Termux.

Reglas del archivo maestro «Proyecto de kost»:

1. Registrar cada paso concluido con código, instrucciones y datos suficientes para recrearlo.
2. Distinguir los pasos implementados de los probados y concluidos; no repetir como avances tareas aún no realizadas.
3. Mantener pendientes y errores identificables, con el resultado de cada nueva prueba.
4. Se usaron históricamente versiones como `0.01`, `0.11.2` y `0.11.3`; las instrucciones evolucionaron. Esta recopilación usa fecha de corte y no inventa una nueva versión del archivo maestro ni recalcula sus etapas.
5. La Etapa 2 del proyecto general de aplicaciones permanece **EN DISEÑO** según la instrucción del usuario. No se cierra por haber creado una app o esta documentación.

El proyecto general contemplaba autenticación, recuperación de contraseña, MFA, cifrado, roles, UI adaptable, navegación, perfiles, modo claro/oscuro, notificaciones, filtros, sincronización, caché/offline, APIs, nube y respaldos. Son antecedentes de la base de futuras apps; no consta que esas funciones estén implementadas en el descargador. La investigación de términos de Spotify fue otra entrada de documentación, no una integración realizada en Kost.

## 2. Mapa de variantes y estado

| Ruta | Ubicación | Dependencia | Evidencia y estado |
| --- | --- | --- | --- |
| Servidor Ubuntu | `servidores/ubuntu` y `apps/android-servidor` | PC y red local | Descargas históricas reportadas como correctas tras actualizar yt-dlp. No repetidas en esta recopilación. |
| Servidor Termux | `servidores/termux-base`, `servidores/termux-corregido`; antecedente `archivo/fase-termux-servidor` | Termux en el teléfono | Descarga directa y desde app con Ubuntu apagado reportadas en el historial. Conservar copia funcional. |
| Termux automático | `apps/android-termux-automatico` | Termux + RUN_COMMAND | Código preparado; hubo permisos, instalaciones simultáneas y esperas. No asumir validación final de esta variante. |
| Biblioteca interna anterior | `apps/android-interno-diagnostico` | Python integrado de biblioteca 2.0.2 | HTTP 403 reportado. Hipótesis JavaScript/versión/TLS sin causa única demostrada. |
| Motor integrado nuevo | `apps/android-motor-integrado` en rama `experimental/motor-integrado` | Herramientas incluidas en APK ARM64 | Compilado y publicado. Descarga y reproducción en teléfono aún sin confirmación recuperada. |
| REA | [Evaluación](REA.md) | Herramientas del equipo de desarrollo | Inspección estática parcial realizada; análisis de métodos bloqueado por `ps`. |

**Importante:** el código del motor nuevo está en [la rama experimental](https://github.com/L7508/kost-descargador/tree/experimental/motor-integrado), no se integra automáticamente al descargar `main`. Esta actualización de documentación no fusiona las apps.

## 3. Cronología reconstruida

### 15–17 de septiembre de 2026 — Archivo maestro y base de futuras apps

Se reorganizaron instrucciones y versiones del archivo maestro, se pidió un diagrama general y se añadió la regla de no marcar la Etapa 2 como concluida. Son antecedentes documentales. No se utiliza el avance del descargador para declarar terminadas aquellas etapas.

### 22–25 de septiembre de 2026 — Ubuntu y primeras apps Android

**Historial reportado:** inicialmente yt-dlp `2026.03.17` devolvía HTTP 403 incluso desde Ubuntu. Se creó un entorno virtual y se instaló `yt-dlp[default]`; la versión registrada después fue `2026.08.19`. FFmpeg registrado: `7.0.2-static`.

Se reportaron posteriormente descargas correctas del servidor Ubuntu y desde la app Android guardando en Descargas. El endpoint utilizado era `POST /api/youtube`, puerto 8000. Antes se observaron respuestas 504 y 422; no hay evidencia suficiente para asignar a cada una una única causa definitiva.

Se pasó a Termux en el teléfono: se reportó funcionamiento directo de yt-dlp y luego desde la app conectada al servidor Termux, con Ubuntu apagado. Este éxito corresponde al **servidor Termux**, no demuestra que la APK interna o RUN_COMMAND automático estuvieran resueltos.

En Android Studio hubo un SDK solicitado `android-37` no encontrado y un `dependencies()` colocado en `settings.gradle`; se corrigieron configuraciones y se registró una construcción exitosa de 31 tareas. También se observó una inconsistencia minSdk 29 / targetSdk 28 en una variante histórica. No todas las carpetas anteriores quedaron uniformadas.

### Desde septiembre, antes de la organización del 6 de octubre — APK interna y cuatro rutas

**Fallo reportado:** `dev.ffmpegkit-maintained:yt-dlp-android:2.0.2` ejecutaba yt-dlp dentro de la app, pero devolvía HTTP 403; se registró `YtDlpException`, con referencia histórica a `MainActivity.java:117`. Se observó yt-dlp `2026.06.09` dentro de esa APK y no se encontró un ejecutable de motor JavaScript compatible en aquella inspección.

Se preparó una prueba con marcadores para Referer, curl-cffi, cookies y proxy. La ruta gratuita de aquella biblioteca no incorporaba curl-cffi; cookies y proxy requerían datos del usuario. Las rutas podían omitirse si faltaban datos y el ensayo se detenía al conseguir una descarga. No se conserva un resultado concluyente que atribuya el 403 a una sola ruta.

**Corrección de una afirmación anterior:** que Referer falle no demuestra que comprar Pro lo resuelva. La exclusividad de curl-cffi se refería a esa oferta de biblioteca, no a todas las implementaciones Android. Tampoco `pip install` garantiza por sí solo que sus dependencias nativas funcionen en Termux.

### Fecha exacta no fijada — Automatización RUN_COMMAND y bloqueos

Se solicitó una interfaz con URL, botón y estado; la URL debía llegar íntegra aunque contuviera `&`. Se implementó el paso de argumentos a un script fijo, sin construir órdenes a partir del texto libre del enlace.

- La primera instalación de Termux, reportada por el usuario como procedente de Play Store y versión `3.0.9`, no registraba `com.termux.permission.RUN_COMMAND`: Android devolvía `Unknown permission`.
- El usuario informó reinstalación desde F-Droid y una versión mostrada como `1.45.0`. Se conserva ese dato como reporte, no como identificación técnica verificada del paquete.
- Con la nueva instalación, `pm grant` terminó sin error; se configuraron `allow-external-apps=true` y el acceso al almacenamiento.
- Se reportaron errores 107 y 100, resueltos por el usuario con `pkg update`. No se infiere su causa precisa solo por esos números.
- Un listado de procesos mostró tres `python -m pip install -U yt-dlp[default]` simultáneos, además de entornos de compilación. Explica actividad de instalación concurrente, no prueba avance de un video.
- Se prepararon límites a la actualización, prevención de doble intento y una forma de restablecer el estado solo después de comprobar que no quedan procesos activos.
- El usuario reportó dos archivos con reproducción incorrecta: uno aparentemente solo audio y otro sin reproducción útil. No se comprobó qué intento produjo cada archivo ni se guardaron resultados ffprobe de aquellos dos archivos.
- Se observó un proceso yt-dlp PID `21017` con una URL completa, tras unos 30 minutos según el usuario. El proceso activo no demostraba avance; no se confirmó descarga válida de ese intento.
- Se prepararon verificación ffprobe, dos reintentos y límite de 600 segundos a yt-dlp. El código conserva parciales para diagnóstico en esa variante.

No se asignan fechas exactas a estos subintentos cuando el historial disponible no las permite. Los PID y seriales antiguos no se deben reutilizar en comandos actuales.

### 6 de octubre de 2026 — Organización y Git

Se reorganizó `repaso.zip` en tres apps, servidores, antecedentes y documentación; se excluyeron entornos virtuales, cachés, configuraciones locales y archivos ajenos. El registro original se conserva en [BITACORA_ORGANIZACION_2026-10-06.md](BITACORA_ORGANIZACION_2026-10-06.md).

El usuario reportó rama `main`, árbol limpio y commit `64b38cb`, «Organiza fuentes del descargador Kost». Se instalaron o prepararon Git y `gh`, tras el error inicial de comando no encontrado. También se pidió revisar datos sensibles antes de publicar.

La carpeta histórica `archivo/android-interno-v1-incompleto` no es un proyecto completo. La mera organización no resolvió el 403 ni compiló todas las variantes.

### 8–17 de octubre de 2026 — Referencia funcional YTDLnis

El usuario confirmó una descarga correcta con YTDLnis y después un total de tres videos correctos, visibles en galería y en la app. La sección Registros estaba vacía. La consola mostraba procesos del paquete `com.deniscerri.ytdl`, `libpython.so` y rutas privadas que desaparecían al finalizar.

**Observación y alcance:** esos procesos apuntan a ejecución de Python dentro del entorno de YTDLnis; su parecido visual con Termux no demostraba dependencia de la app Termux.

El 17 de octubre se revisaron las fuentes públicas de YTDLnis y sus paquetes. Se identificaron componentes de terminal de Termux, ejecución mediante `ProcessBuilder`, paquetes Python y curl-cffi ARM64, configuración de certificados y bibliotecas, y motores JavaScript opcionales. Esto proporcionó una arquitectura de referencia; no demostró qué componente eliminaba nuestro 403.

Se consultó una oferta Pro de una biblioteca de terceros. Al revisarla entonces anunciaba 24 USD individual; la periodicidad no se confirmó. No consta compra ni integración de esa versión. No confundir esa oferta con una edición de pago del proyecto base yt-dlp.

### 17 de octubre de 2026 — Nuevo motor integrado de Kost

**Preparación y construcción verificadas:** nuevo módulo independiente, paquete `com.kost.descargador.interno`, Android 10+, ARM64. Dependencias fijadas: paquete Python `3.14.6`, FFmpeg `0.18.1`, Commons Compress `1.27.1`; ejecutable yt-dlp `2026.08.19`, verificado por hash.

El código incorpora QuickJS, EJS obtenido por red cuando se necesita, servicio en primer plano, cancelación, verificación ffprobe, exportación de log y publicación por MediaStore en `Descargas/Kost`. Primera ruta sin impersonación forzada; ante un 403, segunda ruta `--impersonate chrome`, dentro del mismo presupuesto de diez minutos para descarga. No configura un servidor de PO Token.

Se corrigieron problemas de construcción del entorno: selección inicial de Java 25 incompatible con Gradle 8.13, necesidad de JDK con compilador y certificados del entorno. Lint señaló una constante de permiso mal expresada; se corrigió. Se actualizó FFmpeg desde 0.17.2 a 0.18.1 tras detectar alineación nativa de 16 KB. La construcción limpia final registró **48 tareas, BUILD SUCCESSFUL, 0 errores y 4 advertencias de Lint**. No fueron pruebas de descarga en un dispositivo.

### 17 de octubre de 2026 — Entrega por GitHub

Los enlaces de archivos del chat fallaron repetidamente para el usuario, incluso tras volver a adjuntar y guardar las copias. Se cambió la entrega a GitHub por indicación del usuario.

Se publicó `experimental/motor-integrado` y GitHub Actions compiló y publicó correctamente:

- [Ejecución 39082989523](https://github.com/L7508/kost-descargador/actions/runs/39082989523).
- [Entrega experimental kost-motor-interno-e0e16a4](https://github.com/L7508/kost-descargador/releases/tag/kost-motor-interno-e0e16a4).
- Commit de fuentes de la entrega: `e0e16a435824be0f3552656323dc0fec01bc21a2`.
- Registro posterior en esa rama: `8619ac5814a6a3e1159888132c62ac6a796a673e`.

La ejecución terminó con éxito, incluidos los pasos de preparar archivos y publicar. La subida quedó concluida. **No se ha recuperado una confirmación posterior de instalación/descarga de esa APK por el usuario.**

Las fechas de GitHub pueden mostrarse como 18 de octubre UTC; la conversación y esta cronología usan 17 de octubre en Ciudad de México.

### 28 de octubre de 2026 — REA 6.8.0

Se ejecutó la CLI de REA con Node `24.22.1`; se descargó y verificó JADX headless MCP `0.7.1`. No se registró un nuevo servidor MCP ni se instalaron skills del agente.

Una copia local temporal del APK estaba truncada; REA detectó ausencia del final del ZIP. Se descargó de nuevo la entrega desde GitHub: 91,151,081 bytes, SHA-256 `67160ae9e47660418b83200964c57220ace9c7a1794f48e30aa885087902111a`. Esto no prueba que el archivo servido originalmente al usuario estuviera truncado ni explica por sí solo el error de descarga de ChatGPT.

La lectura estática observó estructura v2 y certificado Android Debug; el resumen de contenido coincidía. Esa operación de REA **no valida criptográficamente la firma ni la autenticidad del autor**. El análisis de clases/métodos quedó bloqueado al fallar `ps` con `fatal library error, lookup self`. No se desactivó el control de propiedad de procesos. [Procedimiento y límites](REA.md).

### 29 de octubre de 2026 — Consolidación documental

Se recopilaron conversaciones recuperables y fuentes de `main` y `experimental/motor-integrado`. Se prepararon esta bitácora, instrucciones por variante, matriz de errores/pruebas y fuentes. Se conservó el registro original de organización. Esta tarea no ejecuta descargas ni cambia código Android.

## 4. Correcciones que deben mantenerse

| Afirmación que apareció | Interpretación que se conserva |
| --- | --- |
| «Pro resolverá el 403» | No demostrado. Una compra no sustituye el diagnóstico. |
| «curl-cffi solo existe pagando en Android» | Incorrecto como afirmación general; YTDLnis tiene otra implementación. |
| «Todas las versiones de Play Store están desactualizadas» | Generalización incorrecta. El hecho comprobado de aquel caso fue `Unknown permission`. |
| «Termux cerrado» = «sin Termux» | La automatización requiere Termux instalado y un proceso suyo, aunque su pantalla no esté abierta. |
| «10 minutos por petición» | En la variante Termux, el timeout del código cubre yt-dlp, no toda la preparación, pip, ffprobe o escaneo. En el motor nuevo cubre las rutas de descarga; hay etapas adicionales. |
| «Aparece en galería» = «video válido» | Hay que comprobar pistas y reproducción real, además del archivo publicado. |
| «Firma leída por REA» = «autenticidad verificada» | Incorrecto: su inspección realizada es estructural y de coherencia de digest. |
| «Compila» = «ya descarga» | Incorrecto: falta prueba en el teléfono. |

## 5. Continuación

1. Seguir [INSTRUCCIONES.md](INSTRUCCIONES.md) para elegir la variante correcta y obtener registros.
2. Ejecutar los casos prioritarios de [PRUEBAS_Y_PENDIENTES.md](PRUEBAS_Y_PENDIENTES.md).
3. Registrar cada ejecución con versión/commit, URL de prueba, entorno, resultado y evidencia; actualizar solo las filas que cambien de estado.

La Etapa 2 general sigue EN DISEÑO y el motor independiente sigue EN PRUEBAS.
