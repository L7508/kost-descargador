Corrección del 504 en la fase Termux del teléfono.

Sustituye el archivo ~/Proyect/termux/server.py por el server.py de este paquete.
Deja Descargador.html y MainActivity.java como están.
Detén el servidor anterior con Ctrl+C y, desde ~/Proyect/termux, ejecuta:
  python server.py --phone
Copia la NUEVA dirección ?key=... a la app Android y repite el mismo video.

El servidor ahora usa para video la selección automática que funcionó en Termux.
Si vuelve a dar 504, copia el mensaje completo que muestra la app y las últimas
líneas de Termux (sin incluir claves ni URLs firmadas).
