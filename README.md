# Kost Descargador

Código del descargador reorganizado a partir de `repaso.zip`, el 6 de octubre de 2026. Es un repositorio con proyectos independientes; abre cada app por separado en Android Studio.

## Documentación consolidada — 29 de octubre de 2026

- [Bitácora completa y estado de las variantes](docs/BITACORA.md).
- [Instrucciones de instalación, pruebas y diagnóstico](docs/INSTRUCCIONES.md).
- [Pruebas realizadas, errores y pendientes](docs/PRUEBAS_Y_PENDIENTES.md).
- [Evaluación parcial de REA](docs/REA.md).
- [Fuentes, decisiones y límites de esta recopilación](docs/FUENTES_Y_ALCANCE.md).

**APK independiente: EN PRUEBAS.** Se compiló y publicó en [esta entrega experimental](https://github.com/L7508/kost-descargador/releases/tag/kost-motor-interno-e0e16a4), pero no se ha recuperado confirmación de descarga correcta en el teléfono. Sus fuentes están en [experimental/motor-integrado](https://github.com/L7508/kost-descargador/tree/experimental/motor-integrado); esta rama main contiene la documentación consolidada y las variantes originales.

## Carpetas

| Ruta | Uso | Estado |
| --- | --- | --- |
| `apps/android-termux-automatico` | Envía una URL a Termux mediante RUN_COMMAND | Base para continuar; requiere Termux |
| `apps/android-interno-diagnostico` | Prueba biblioteca interna y conexión a servidor | Diagnóstico; el HTTP 403 no se considera resuelto |
| `apps/android-servidor` | Interfaz Android conectada a servidor Python | Versión anterior |
| `servidores/ubuntu` | Servidor local de Ubuntu | Código conservado |
| `servidores/termux-base` | Servidor y página HTML para Termux | Versión anterior |
| `servidores/termux-corregido` | Servidor con cambios para diagnosticar el 504 | Variante posterior |
| `archivo` | Fuentes anteriores y parches extraídos de ZIP | Consulta histórica; no son el proyecto activo |
| `docs` | Bitácora, procedencia e instrucciones Git | Documentación de esta organización |

## Continuar con Android

Abre `apps/android-termux-automatico` en Android Studio. Las instrucciones funcionales están en `LEEME_PRUEBA.md` dentro de esa carpeta. Conserva los permisos y la preparación de Termux allí descritos.

Los proyectos conservan su configuración de SDK, plugins, dependencias e identificadores Android. Android Studio generará `local.properties` para tu equipo. No subas ese archivo. Los wrappers Gradle y sus JAR están incluidos; en Linux los scripts `gradlew` tienen permiso de ejecución.

Las otras dos apps usan el mismo applicationId `com.kost.descargador` y pueden reemplazarse al instalarse. La app automática usa `com.kost.descargador.termux`. La carpeta V1 en `archivo` carece de MainActivity.java; no se presenta como proyecto completo.

## Servidores

Ejecuta los comandos desde la carpeta del servidor elegido. Usa un entorno Python nuevo; el del respaldo no es portable.

```bash
python3 -m venv .venv
.venv/bin/python -m pip install -U 'yt-dlp[default]'
```

FFmpeg debe estar instalado en el equipo. Para Ubuntu: `.venv/bin/python server.py --lan`. Para los servidores Termux: `python server.py --phone`, con las herramientas de Termux ya preparadas. La página HTML de la variante base permanece junto a su servidor. Consulta la configuración original en `archivo/fase-termux-servidor/README.md`.

Los README originales se conservan como antecedentes. Cuando mencionen rutas antiguas o que no había un APK, describen el paquete de aquel momento; usa este índice para las nuevas rutas.

## Git

Consulta `docs/GIT.md`. El repositorio ya está publicado en GitHub; la descripción del paquete original y su organización del 6 de octubre se conserva en `docs/BITACORA_ORGANIZACION_2026-10-06.md`. Mantén fuera de Git credenciales, configuraciones locales y archivos personales.

## Alcance de la revisión

Se comprobó la integridad del respaldo, la sintaxis de los servidores Python y la correspondencia de las fuentes copiadas. En aquella organización no se compilaron las apps ni se probaron descargas en un teléfono. Posteriormente se compiló y publicó el motor nuevo en la rama experimental; el estado funcional sigue EN PRUEBAS. Véase la bitácora consolidada para distinguir ambos momentos.
