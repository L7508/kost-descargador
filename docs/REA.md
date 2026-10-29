# Evaluación de REA para Kost — 2026-10-28

Estado: evaluación parcial; la descarga de Kost sigue sin validarse en el teléfono en esta sesión.

## Pasos realizados

- Se consultaron el repositorio oficial https://github.com/morluto/rea y su guía Android https://github.com/morluto/rea/blob/main/docs/android-analysis.md . El proyecto declara licencia MIT.
- Se ejecutó la CLI de `rea-agents` 6.8.0 mediante npx, con Node 24.22.1. No se registró un servidor MCP ni se instalaron instrucciones/skills de REA en el agente.
- Se obtuvo JADX headless MCP 0.7.1 de la URL indicada en la guía y se verificó su SHA-256: `6e5eacf500b64292bfb73c49797c1958f6ee44646e43e868039ae7feb573ff75`.
- Se descargó el APK de la entrega `kost-motor-interno-e0e16a4` directamente desde GitHub. Tamaño: 91151081 bytes; SHA-256: `67160ae9e47660418b83200964c57220ace9c7a1794f48e30aa885087902111a`.
- `inspect-apk-signature` leyó una estructura v2 con certificado Android Debug y un resumen de contenido que coincide con el APK. REA declara expresamente que esta operación NO verifica criptográficamente la firma ni la autenticidad del productor. No ejecutó el APK.
- Se intentó `inspect-android-package` con JADX. No se completó: el control de propiedad de procesos de REA encontró un fallo de `ps -axo pid=,ppid=,pgid=,uid=,stat=,command=`: `fatal library error, lookup self`. No se desactivó ni sustituyó ese control.

## Aplicación prevista

Usar REA como herramienta de investigación en el equipo de desarrollo: inspeccionar clases y métodos de APK, seguir la inicialización de Python/yt-dlp y comparar componentes con una versión identificada de YTDLnis. Su presencia no resuelve automáticamente un HTTP 403. El análisis dinámico requiere un dispositivo o emulador accesible con ADB; el análisis profundo de bibliotecas nativas necesita un proveedor apropiado, como Ghidra.

## Reproducción

```bash
npx --yes rea-agents@6.8.0 --version
npx --yes rea-agents@6.8.0 inspect-apk-signature /ruta/al/Kost.apk --json
REA_JADX_MCP_JAR=/ruta/al/jadx-headless-mcp-0.7.1-all.jar npx --yes rea-agents@6.8.0 inspect-android-package /ruta/al/Kost.apk --json
```

Antes del último comando, comprobar un JDK completo y un `ps` de procps que funcione en ese entorno. La prueba de clases/métodos permanece pendiente. No se cambiaron las fuentes Android, ni se recompiló o publicó una nueva APK en esta evaluación.
