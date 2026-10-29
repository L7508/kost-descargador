# Fuentes, decisiones y alcance de la recopilación

Fecha: 29 de octubre de 2026. Esta documentación es una reconstrucción técnica del proyecto, no una exportación íntegra de conversaciones privadas.

## Fuentes utilizadas

1. Conversación del proyecto disponible en esta sesión: pruebas, mensajes de terminal, entregas, resultados del usuario y acciones del asistente.
2. Recuperación de contexto de conversaciones anteriores, centrada exclusivamente en Kost y el descargador. Una consulta adicional falló; otra confirmó que no se recuperó un resultado de la APK integrada posterior a la entrega. No se presenta la recuperación como exhaustiva.
3. `main` en el commit base `64b38cbfe0c73511d81f7e8a1ee809d9da88d1ee`: README, apps, scripts, instrucciones y bitácora de organización.
4. Rama [experimental/motor-integrado](https://github.com/L7508/kost-descargador/tree/experimental/motor-integrado), inspeccionada al corte documental; commit de registro `8619ac5814a6a3e1159888132c62ac6a796a673e` y entrega de fuentes `e0e16a435824be0f3552656323dc0fec01bc21a2`.
5. [GitHub Actions 39082989523](https://github.com/L7508/kost-descargador/actions/runs/39082989523) y [entrega experimental](https://github.com/L7508/kost-descargador/releases/tag/kost-motor-interno-e0e16a4), verificados en la sesión de publicación.
6. Resultados de REA 6.8.0 del 28/10 y procedimiento registrado en [REA.md](REA.md).

Los resúmenes históricos sirven para conservar antecedentes, pero tienen menos detalle que un log contemporáneo. Si una fecha o causalidad no queda fijada, se indica expresamente. Los mensajes antiguos «aquí no hay SDK» describen el entorno de aquella entrega; no invalidan la construcción posterior del motor nuevo con SDK/JDK preparados.

## Fuentes técnicas de referencia

- yt-dlp: https://github.com/yt-dlp/yt-dlp ; https://github.com/yt-dlp/yt-dlp/wiki/EJS ; https://github.com/yt-dlp/yt-dlp/wiki/FAQ .
- Biblioteca anterior: https://github.com/ffmpegkit-maintained/yt-dlp-android . La oferta Pro se investigó como alternativa; no hay compra confirmada.
- Termux y RUN_COMMAND: https://github.com/termux/termux-app ; https://github.com/termux/termux-app/wiki/RUN_COMMAND-Intent .
- Termux:API: https://github.com/termux/termux-api .
- YTDLnis: https://github.com/deniscerri/ytdlnis . Revisión histórica de código `d5beded4ec3c66d589fb1005ba3dd14959575c07`.
- Paquetes YTDLnis: https://github.com/deniscerri/ytdlnis-packages . Revisión histórica `c32745216eab2c643643374bb1153fc018086665`.
- REA: https://github.com/morluto/rea ; https://github.com/morluto/rea/blob/main/docs/android-analysis.md .

Son referencias para volver a verificar requisitos vigentes. Sus ramas principales, precios y procedimientos pueden cambiar; no se afirma haber auditado hoy cada dependencia externa. Las versiones del experimento se documentan como versiones fijadas entonces.

## Decisiones registradas

| Decisión | Motivo | Límite |
| --- | --- | --- |
| Conservar servidor Termux | Hay resultado funcional histórico reportado | No equivale a resolver dependencia externa |
| Interfaz mínima | Preferencia del usuario: URL y descarga, con estado | Mantener información suficiente para diagnosticar |
| Argumentos separados | Conservar URL completa y no interpretarla como shell | Validación y manejo de procesos deben probarse |
| App experimental con otro paquete | Probar motor nuevo conservando variantes anteriores | No se fusionó con main como app |
| Investigar YTDLnis | Usuario confirmó tres videos correctos | No atribuir mejora exclusivamente a curl-cffi |
| Posponer compra Pro | Falta demostrar capacidad concreta que resuelve el fallo | No se asegura pago único ni suscripción |
| GitHub como entrega | Descargas de archivos del chat fallaron reiteradamente | La causa de aquel fallo no se identificó |
| REA por CLI | Permite una evaluación sin registrar un servidor nuevo | Análisis de métodos bloqueado por ps |
| Publicar esta recopilación en main | Facilitar acceso central a bitácora e instrucciones | Solo documentación; no fusiona código experimental |

## Datos que se omiten de la publicación

No se incorporan documentos personales ajenos, nombres/correos privados, seriales ADB, direcciones IP concretas, cookies, claves del servidor, tokens, credenciales de proxy, keystores ni logs crudos con URLs firmadas. Las rutas de trabajo del usuario se expresan como carpetas del repositorio o marcadores.

No se ha cambiado la versión del archivo maestro general ni cerrado su Etapa 2. No se ha repetido una descarga, instalado un APK en el teléfono ni validado un respaldo durante esta actualización documental.

## Cómo resolver discrepancias

- Para comportamiento de una variante, prevalece el código identificado por rama/commit; el historial explica cómo se llegó allí.
- Para éxito real, prevalece una prueba de usuario o dispositivo con evidencia sobre una descripción del asistente o una compilación.
- Si un registro histórico y un archivo actual difieren, conservar ambos con fecha y contexto; no borrar el fallo anterior.
- No convertir una hipótesis de TLS, JavaScript, IP, cookies o PO Token en causa confirmada sin una comparación reproducible.
