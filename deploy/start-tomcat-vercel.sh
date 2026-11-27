#!/bin/sh
set -eu

APP_PORT="${PORT:-8080}"
SERVER_XML="${CATALINA_HOME:-/usr/local/tomcat}/conf/server.xml"

if [ ! -f "$SERVER_XML" ]; then
  echo "No se encontró la configuración de Tomcat: $SERVER_XML" >&2
  exit 1
fi

case "$APP_PORT" in
  ''|*[!0-9]*) echo "PORT debe ser un número de puerto válido; recibido: $APP_PORT" >&2; exit 1 ;;
esac

# Cambia únicamente el puerto del Connector HTTP de Tomcat (no el shutdown port).
sed -i "s/port=\"8080\"/port=\"${APP_PORT}\"/" "$SERVER_XML"

exec "${CATALINA_HOME:-/usr/local/tomcat}/bin/catalina.sh" run
