# Matriz de pruebas, errores y pendientes

Corte documental: 29 de octubre de 2026. **Los casos indicados como pendientes son un plan, no pruebas realizadas.** [Bitácora](BITACORA.md) · [Instrucciones](INSTRUCCIONES.md).

## Resultados disponibles

| ID | Prueba / entorno | Resultado conservado | Tipo de evidencia |
| --- | --- | --- | --- |
| H01 | yt-dlp Ubuntu antes de actualizar | HTTP 403 | Historial reportado |
| H02 | Ubuntu con venv y yt-dlp 2026.08.19 | Descarga reportada como correcta | Historial reportado |
| H03 | App con servidor Ubuntu | Archivo guardado en Descargas, reportado | Historial reportado |
| H04 | yt-dlp directo en Termux | Descarga correcta reportada | Historial reportado |
| H05 | App con servidor Termux, Ubuntu apagado | Descarga correcta reportada | Historial reportado; distinta de RUN_COMMAND |
| H06 | Biblioteca interna 2.0.2 | HTTP 403 persistente | Logs y reportes históricos |
| H07 | Formato `best[height<=720]/best` | Un enlace devolvió Requested format not available | Reporte histórico |
| H08 | pm grant con primera instalación Termux | Unknown permission | Salida de Android reproducida en chat |
| H09 | pm grant tras cambio de instalación | Sin error, permiso considerado concedido | Salida/reportado; comprobar operación completa aparte |
| H10 | Preparación automática | Tres instalaciones pip simultáneas | Listado de procesos histórico |
| H11 | Dos archivos de pruebas anteriores | Reproducción incorrecta; pistas no verificadas | Reporte del usuario; origen exacto no identificado |
| H12 | yt-dlp activo unos 30 minutos | No se confirmó avance ni resultado correcto | Proceso y tiempo reportados |
| H13 | YTDLnis en el teléfono | Tres videos correctos con imagen y audio | Confirmación del usuario |
| H14 | Motor integrado de Kost: construcción local | Compiló, Lint 0 errores / 4 advertencias | Registro de construcción del 17/10 |
| H15 | Motor integrado: GitHub Actions | Compilación y publicación exitosas | Run 39082989523 |
| H16 | Motor integrado: descarga en teléfono | **SIN CONFIRMACIÓN RECUPERADA** | Pendiente |
| H17 | REA: estructura de firma sobre entrega GitHub | Estructura v2 y coherencia de digest observadas | Inspección estática; no autenticidad |
| H18 | REA/JADX: métodos del APK | Bloqueado por error de ps | Salida capability_unavailable |

## Catálogo de errores

| ID | Síntoma | Qué sabemos | Acción / estado |
| --- | --- | --- | --- |
| E01 | HTTP 403 en Ubuntu antiguo | Cambió tras actualizar entorno yt-dlp | Históricamente resuelto allí; no extrapolar a Android |
| E02 | HTTP 403 en biblioteca interna | Versión integrada antigua; JS/TLS son hipótesis | Causa única pendiente; probar motor nuevo y recoger log |
| E03 | Requested format not available | La selección solicitada no estaba disponible | Inspeccionar formatos del enlace; no confundir con 403 |
| E04 | HTTP 504 servidor | Hubo espera/fallo del servidor | No hay causa definitiva preservada; revisar log y prueba directa |
| E05 | HTTP 422 servidor | Se observó respuesta de error | Guardar cuerpo/log; no atribuir sin evidencia a la URL o a YouTube |
| E06 | ConnectionAbortedError 103 | Conexión abortada en registro histórico | No basta para concluir fallo del extractor; correlacionar cliente/servidor |
| E07 | Unknown permission RUN_COMMAND | Android no conocía el permiso en esa instalación | Identificar APK de Termux; no se agrega con pkg/pip |
| E08 | adb: more than one device/emulator | Había dos conexiones | Seleccionar serial actual |
| E09 | adb no encontrado | No estaba en PATH de Ubuntu | Usar ruta real del SDK |
| E10 | Errores 107 y 100 | Usuario reportó que pkg update los resolvió | Sin causa precisa documentada por número |
| E11 | Preparando Termux sin terminar | pip simultáneo y compilación de dependencias observados | Actualización acotada y guardas preparadas; revalidar |
| E12 | Botón/estado en curso atascado | La respuesta RUN_COMMAND podía no regresar | Comprobar procesos antes de borrar marca; prueba de recuperación pendiente |
| E13 | Archivos de reproducción incorrecta | No se determinaron sus pistas ni intento de origen | No contar como éxito; ffprobe + reproducción |
| E14 | Descarga >30 min | Proceso activo sin avance demostrado | Timeout añadido al código; probar su efecto |
| E15 | Plataforma android-37 ausente | Configuración solicitaba SDK no instalado | Revisar carpeta/SDK, no cambiar todas las variantes a ciegas |
| E16 | dependencies() en settings.gradle | Dependencias en archivo incorrecto | Corrección histórica; verificar build.gradle de app |
| E17 | Java 25 / major version 69 | Incompatible con Gradle 8.13 de la prueba | JDK 17 en motor nuevo; no replicar criterios Java 25 de otra app |
| E18 | JDK sin capacidades JAVA_COMPILER | Un runtime sin herramientas no bastó para Gradle | Se utilizó JDK completo en construcción exitosa |
| E19 | Error de certificados durante construcción alojada | Falló confianza TLS de un JDK temporal | Corregido con almacén de confianza del entorno; no desactivar TLS |
| E20 | Lint WrongConstant | Comparación de permiso con literal | Código corregido usando PERMISSION_GRANTED |
| E21 | Advertencia nativa 16 KB en FFmpeg 0.17.2 | Se inspeccionó alineación | Se usó 0.18.1; última construcción limpia sin esa advertencia |
| E22 | ChatGPT no descarga APK/ZIP | Falló para usuario por varias vías | Entrega alternativa GitHub publicada; causa del servicio desconocida |
| E23 | Copia local temporal APK truncada durante REA | Faltaba fin de directorio ZIP | Descarga GitHub completa inspeccionada; no implica fallo del APK publicado |
| E24 | REA ps: fatal library error, lookup self | No pudo comprobar procesos en entorno alojado | Analizar métodos en entorno compatible; no eludir control |

Los identificadores E de esta tabla pertenecen a esta matriz del descargador; no sustituyen E11/E12 de documentos generales antiguos de Kost.

## Pendientes priorizados y criterio para cerrarlos

| ID | Prioridad | Pendiente | Se cierra cuando |
| --- | --- | --- | --- |
| P01 | P0 | Probar APK integrada en teléfono | Registrar versión, dispositivo, URL y resultado; imagen/audio/duración correctos en archivo final |
| P02 | P0 | Identificar el primer fallo real del motor integrado | Exportar log completo de un único intento y relacionarlo con fase, proceso y código |
| P03 | P0 | Preservar la ruta funcional de Termux | Identificar copia/commit y procedimiento que el usuario puede repetir; comprobar restauración del respaldo |
| P04 | P1 | Confirmar QuickJS/EJS y curl-cffi en ejecución real | Log muestre comprobaciones y ruta efectivamente usada; no solo presencia de archivos |
| P05 | P1 | Comparar mismo enlace con YTDLnis | Misma red/teléfono, versiones identificadas y resultados registrados; sin afirmar causalidad por un solo caso |
| P06 | P1 | Validar timeout, doble pulsación y cancelación | Una tarea activa, retorno de estado y ausencia de procesos huérfanos observados |
| P07 | P1 | Revisar MediaStore, pistas y reproducción | Archivo final accesible y reproducible; documentar galería/códecs por separado |
| P08 | P1 | Verificar versión final RUN_COMMAND | Instalación y retorno del resultado correctos, sin instalaciones repetidas ni estado atascado |
| P09 | P2 | Análisis de métodos con REA/JADX | Herramientas operativas, evidencia de clases/rutas relevantes y comparación reproducible |
| P10 | P2 | Determinar actualización del motor integrado | Diseñar y probar una política; actualmente la versión yt-dlp está fijada y no usa pip en el teléfono |
| P11 | P2 | Resolver si es necesario PO Token/cookies | Solo después del log real; no configurarlos como requisito sin evidencia |
| P12 | P2 | Evaluar biblioteca Pro si aporta algo comprobable | Identificar capacidad faltante y condiciones verificadas; no consta compra |
| P13 | P2 | Preparar distribución estable | Firma y actualizaciones persistentes, avisos/licencias de componentes y pruebas de dispositivos |
| P14 | Abierto | Cerrar etapa funcional del descargador | Cumplir criterios acordados con pruebas repetidas; esta recopilación no la cierra |
| P15 | Abierto | Analizar Etapa 2 general de futuras apps | Verificar su diagrama y requisitos en el archivo maestro; permanece EN DISEÑO |

## Próximas pruebas propuestas — todavía no realizadas aquí

| Caso | Procedimiento | Evidencia mínima |
| --- | --- | --- |
| T01 | Descargar un video público corto con la APK integrada | Estado, log, tamaño, duración, imagen y sonido |
| T02 | Repetir con enlace que contiene `&list=...` | URL recibida íntegra y una sola descarga por `--no-playlist` |
| T03 | Pulsar Descargar dos veces durante un intento | No aparecen dos tareas; interfaz conserva estado coherente |
| T04 | Cancelar un intento activo | Proceso termina; no se publica un archivo incompleto como éxito |
| T05 | Observar un fallo o timeout real | Error final, tiempo y ausencia de bloqueo permanente; no provocar acceso no autorizado |
| T06 | Salir y volver a la pantalla durante descarga | Notificación/servicio y estado coherentes; registrar efecto de restricciones de batería |
| T07 | Comprobar un resultado con ffprobe y reproductor | Pistas presentes más reproducción de inicio, mitad y final |
| T08 | Comparar mismo video con YTDLnis | Versión exacta de ambas apps y condiciones equivalentes |
| T09 | Probar RUN_COMMAND con preparación ya hecha | No reinstala paquetes existentes, devuelve resultado y guarda archivo |
| T10 | REA en entorno con ps operativo | Resultado de inspect-android-package y ruta/método relevante, sin ejecutar el APK |

No declarar probado un fallback si la primera ruta ya funcionó y nunca llegó a ejecutarse. No repetir instalaciones o descargas en paralelo para obtener más evidencia.

## Plantilla para cada ejecución

```text
ID de ejecución:
Fecha, hora y zona:
Caso de prueba:
Variante, paquete, versión, rama y commit:
Android / ABI / dispositivo (sin serial público):
Red de prueba (sin credenciales ni IP privada innecesaria):
Versión yt-dlp, Python, JS y FFmpeg observadas:
URL pública de prueba o identificador controlado:
Configuración especial (cookies/proxy/PO Token, si realmente se usó):
Pasos ejecutados:
Resultado esperado:
Resultado observado y fase donde terminó:
Código/error exacto:
Tiempo transcurrido:
Archivo: nombre, tamaño, contenedor y duración:
ffprobe: audio / video / códecs:
Reproducción: inicio / mitad / final / sonido:
Galería/Descargas:
Ruta realmente ejecutada (normal / curl-cffi / otra):
Registro adjunto, previamente revisado para retirar secretos:
Estado: OK / FALLÓ / INCONCLUSO:
Conclusión sustentada y siguiente paso:
```

Para cerrar un caso no basta con «no mostró error» salvo que el caso mida justamente la aceptación de un comando. Para cerrar una descarga hay que verificar el resultado final.
