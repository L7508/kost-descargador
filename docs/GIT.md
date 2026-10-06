# Iniciar el repositorio

Descomprime el paquete y abre una terminal dentro de `kost-descargador`.

```bash
git init -b main
git add .
git status --short
git diff --cached --stat
git commit -m "Organiza fuentes del descargador Kost"
```

Si Git pide identidad, configura tu nombre y correo en este repositorio y repite el commit:

```bash
git config user.name "Tu nombre"
git config user.email "Tu correo"
```

Para publicarlo, crea un repositorio vacío en tu proveedor y usa su URL real:

```bash
git remote add origin URL_DE_TU_REPOSITORIO
git push -u origin main
```

## Trabajo diario

```bash
git status
git add .
git diff --cached
git commit -m "Describe el cambio realizado"
git push
```

La carpeta `archivo` contiene antecedentes entregados juntos en este respaldo. No representan commits o ramas anteriores. Los APK generados van fuera de Git; si quieres distribuirlos, puedes adjuntarlos a una publicación de tu proveedor.

Se ignoran cachés, builds, local.properties, entornos Python, descargas, cookies y claves de firma. El JAR del wrapper de Gradle sí se guarda. No se añadió una licencia: el dueño del proyecto debe elegirla antes de conceder permisos de distribución.
