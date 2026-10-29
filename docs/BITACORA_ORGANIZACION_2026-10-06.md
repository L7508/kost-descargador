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
