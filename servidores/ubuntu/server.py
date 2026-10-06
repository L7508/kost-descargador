"""Servidor local: enlaces directos en el navegador y conversión MP3 de archivos propios."""
from http.server import BaseHTTPRequestHandler, ThreadingHTTPServer
from pathlib import Path
import json
import subprocess
import tempfile
import shutil
import sys
import importlib.util
import re
import secrets
from urllib.parse import urlparse, parse_qs

YOUTUBE_HOSTS = {'youtube.com', 'www.youtube.com', 'm.youtube.com', 'youtu.be', 'www.youtu.be', 'youtube-nocookie.com', 'www.youtube-nocookie.com'}

ROOT = Path(__file__).resolve().parent
MAX = 200 * 1024 * 1024

class Handler(BaseHTTPRequestHandler):
    def authorized(self):
        if not self.server.access_token:
            return True
        if self.path.startswith('/api/'):
            given = self.headers.get('X-Access-Token', '')
        else:
            given = parse_qs(urlparse(self.path).query).get('key', [''])[0]
        return secrets.compare_digest(given, self.server.access_token)

    def do_GET(self):
        if not self.authorized():
            return self.send_error(403, 'Enlace de acceso no válido')
        if self.path == '/api/status':
            try:
                result = subprocess.run([sys.executable, '-m', 'yt_dlp', '--version'], capture_output=True, timeout=5)
                version = result.stdout.decode(errors='replace').strip() if result.returncode == 0 else 'no disponible'
            except (OSError, subprocess.TimeoutExpired):
                version = 'no disponible'
            data = json.dumps({'python': sys.executable, 'yt_dlp': version, 'ffmpeg': bool(shutil.which('ffmpeg'))}).encode()
            self.send_response(200)
            self.send_header('Content-Type', 'application/json')
            self.send_header('Cache-Control', 'no-store')
            self.send_header('Content-Length', str(len(data)))
            self.end_headers()
            self.wfile.write(data)
            return
        if self.path != '/':
            if urlparse(self.path).path == '/':
                pass
            else:
                self.send_error(404)
                return
        data = (ROOT / 'Descargador.html').read_bytes()
        self.send_response(200)
        self.send_header('Content-Type', 'text/html; charset=utf-8')
        self.send_header('Cache-Control', 'no-store')
        self.send_header('Referrer-Policy', 'no-referrer')
        self.send_header('Content-Length', str(len(data)))
        self.end_headers()
        self.wfile.write(data)

    def fail(self, status, message):
        data = json.dumps({'error': message}).encode()
        self.send_response(status)
        self.send_header('Content-Type', 'application/json')
        self.send_header('Content-Length', str(len(data)))
        self.end_headers()
        self.wfile.write(data)

    def do_POST(self):
        if not self.authorized():
            return self.fail(403, 'Acceso no autorizado')
        if self.path == '/api/youtube':
            return self.youtube()
        if self.path != '/api/audio':
            return self.fail(404, 'Ruta desconocida')
        try:
            length = int(self.headers.get('Content-Length', '0'))
        except ValueError:
            length = 0
        if length <= 0 or length > MAX:
            return self.fail(413, 'Archivo vacío o superior a 200 MB')
        if self.headers.get('Content-Type') != 'application/octet-stream':
            return self.fail(415, 'Tipo de solicitud no admitido')
        with tempfile.TemporaryDirectory() as tmp:
            source = Path(tmp) / 'input'
            output = Path(tmp) / 'audio.mp3'
            with source.open('wb') as target:
                remaining = length
                while remaining:
                    chunk = self.rfile.read(min(1024 * 1024, remaining))
                    if not chunk:
                        return self.fail(400, 'Carga incompleta')
                    target.write(chunk)
                    remaining -= len(chunk)
            try:
                result = subprocess.run(['ffmpeg', '-nostdin', '-v', 'error', '-y', '-i', str(source),
                                         '-vn', '-map', '0:a:0', '-codec:a', 'libmp3lame', '-b:a', '192k',
                                         str(output)], capture_output=True, timeout=180, check=False)
            except (OSError, subprocess.TimeoutExpired):
                return self.fail(500, 'FFmpeg no está disponible o tardó demasiado')
            if result.returncode or not output.exists():
                return self.fail(422, 'El archivo no contiene audio compatible')
            self.send_response(200)
            self.send_header('Content-Type', 'audio/mpeg')
            self.send_header('Content-Disposition', 'attachment; filename="audio.mp3"')
            self.send_header('Content-Length', str(output.stat().st_size))
            self.end_headers()
            try:
                with output.open('rb') as stream:
                    shutil.copyfileobj(stream, self.wfile)
            except BrokenPipeError:
                pass

    def youtube(self):
        try:
            length = int(self.headers.get('Content-Length', '0'))
            if not 0 < length < 2048:
                return self.fail(413, 'Solicitud demasiado grande')
            payload = json.loads(self.rfile.read(length))
            url, mode = payload['url'], payload['mode']
            parsed = urlparse(url)
            if parsed.scheme != 'https' or parsed.hostname not in YOUTUBE_HOSTS or not parsed.path or mode not in ('audio', 'video'):
                return self.fail(400, 'Enlace de YouTube o formato no válido')
        except (ValueError, KeyError, TypeError):
            return self.fail(400, 'Solicitud no válida')
        if importlib.util.find_spec('yt_dlp') is None:
            return self.fail(503, 'Falta yt-dlp. En una terminal ejecuta: python3 -m pip install -U yt-dlp (Windows: py -m pip install -U yt-dlp). Reinicia el servidor.')
        if shutil.which('ffmpeg') is None:
            return self.fail(503, 'Falta FFmpeg. Instálalo, comprueba que ffmpeg -version funcione y reinicia el servidor.')
        with tempfile.TemporaryDirectory() as tmp:
            command = [sys.executable, '-m', 'yt_dlp', '--no-config', '--no-playlist',
                       '--no-progress', '--max-filesize', '200M',
                       '--paths', tmp, '-o', 'media.%(ext)s']
            if mode == 'audio':
                command += ['-f', 'bestaudio/best', '-x', '--audio-format', 'mp3']
            else:
                command += ['-f', 'bv*+ba/b', '--merge-output-format', 'mp4']
            try:
                completed = subprocess.run(command + ['--', url], capture_output=True, timeout=240, check=False)
            except subprocess.TimeoutExpired:
                return self.fail(504, 'La descarga tardó demasiado')
            if completed.returncode:
                detail = completed.stderr.decode('utf-8', errors='replace')
                errors = [line for line in detail.splitlines() if 'ERROR:' in line]
                reason = errors[-1].split('ERROR:', 1)[-1].strip() if errors else 'Revisa la terminal donde ejecutaste el servidor.'
                reason = re.sub(r'https?://\S+', '[URL]', reason)[:350]
                print('yt-dlp error:', detail[-1500:], file=sys.stderr)
                return self.fail(422, 'yt-dlp: ' + reason)
            files = [p for p in Path(tmp).glob('media.*') if p.is_file()]
            if len(files) != 1 or files[0].stat().st_size > MAX:
                return self.fail(422, 'No se obtuvo un único archivo dentro del límite de 200 MB')
            output = files[0]
            self.send_response(200)
            self.send_header('Content-Type', 'audio/mpeg' if output.suffix == '.mp3' else 'application/octet-stream')
            self.send_header('Content-Disposition', 'attachment; filename="descarga' + output.suffix + '"')
            self.send_header('Content-Length', str(output.stat().st_size))
            self.end_headers()
            try:
                with output.open('rb') as stream:
                    shutil.copyfileobj(stream, self.wfile)
            except BrokenPipeError:
                pass

if __name__ == '__main__':
    lan = '--lan' in sys.argv[1:]
    if any(arg != '--lan' for arg in sys.argv[1:]):
        raise SystemExit('Uso: python server.py [--lan]')
    token = secrets.token_urlsafe(24) if lan else ''
    print('Abre http://127.0.0.1:8000' if not lan else
          'En Android (mismo Wi-Fi): http://IP_DE_UBUNTU:8000/?key=' + token,
          flush=True)
    print('Python del servidor:', sys.executable, flush=True)
    server = ThreadingHTTPServer(('0.0.0.0' if lan else '127.0.0.1', 8000), Handler)
    server.access_token = token
    server.serve_forever()
