# CHERO.SAC en Vercel (Java 17 + Tomcat 10.1 + PostgreSQL)

Este proyecto usa `Dockerfile.vercel` para ejecutarse como contenedor HTTP en Vercel Container Runtime. El runtime de contenedores de Vercel se encuentra en beta; revisa los límites y el consumo de tu cuenta antes de publicar.

## 1. Qué necesitas

- Una cuenta de GitHub.
- Una cuenta de Vercel conectada a GitHub.
- Una base PostgreSQL accesible desde Internet. Puedes usar Neon u otro proveedor compatible.
- No uses `localhost` como host de PostgreSQL en producción: `localhost` apuntaría al propio contenedor de Vercel.

## 2. Crear la base PostgreSQL remota

1. Crea un proyecto PostgreSQL en Neon.
2. Copia el host, puerto, base de datos, usuario y contraseña. Para la aplicación, utiliza el endpoint pooled de Neon si está disponible.
3. Exporta tu base local `chero_sac` desde PowerShell (requiere PostgreSQL 18 instalado):

```powershell
& "C:\Program Files\PostgreSQL\18\bin\pg_dump.exe" -h 127.0.0.1 -U postgres -d chero_sac --no-owner --no-acl --format=plain -f ".\chero_sac_neon.sql"
```

4. Importa el respaldo en la base vacía de Neon. Reemplaza los valores entre `< >` por los datos de Neon:

```powershell
& "C:\Program Files\PostgreSQL\18\bin\psql.exe" -h <HOST_NEON> -p 5432 -U <USUARIO_NEON> -d <BASE_NEON> -W -v ON_ERROR_STOP=1 -f ".\chero_sac_neon.sql"
```

No ejecutes en Neon el archivo `chero_sac_completo.sql` si ya contiene `DROP DATABASE` / `CREATE DATABASE` y `\c`. El respaldo de `pg_dump` anterior es el método recomendado para copiar el estado actual completo (tablas y datos). Mantén el archivo `chero_sac_neon.sql` fuera de GitHub; ya está excluido por `.gitignore`.

## 3. Subir el proyecto a GitHub

1. Crea un repositorio, por ejemplo `chero-sac`.
2. Sube el contenido de esta carpeta (la carpeta donde están `pom.xml`, `Dockerfile.vercel` y `vercel.json`). No subas `.env`, respaldos de base de datos ni contraseñas.
3. Asegúrate de que `Dockerfile.vercel` esté en la raíz del repositorio, no dentro de `src`.

## 4. Crear el proyecto en Vercel

1. En Vercel, pulsa **Add New → Project** e importa el repositorio GitHub.
2. Vercel debe detectar `Dockerfile.vercel` / el preset **Container**. El archivo `vercel.json` también declara `framework: container`.
3. Deja el Root Directory en la raíz del repositorio (donde está `pom.xml`).
4. Antes de desplegar, abre **Project → Settings → Environment Variables** y añade estas variables para Production (y Preview, si las necesitas):

| Variable | Valor |
|---|---|
| `PORT` | `8080` |
| `CHERO_DB_URL` | `jdbc:postgresql://<HOST_NEON>:5432/<BASE_NEON>?sslmode=require` |
| `CHERO_DB_USER` | usuario PostgreSQL de Neon |
| `CHERO_DB_PASSWORD` | contraseña PostgreSQL de Neon |

`CHERO_DB_URL` debe comenzar con `jdbc:postgresql://`. No pegues la URI estándar `postgresql://` sin convertirla al formato JDBC. No coloques la contraseña dentro de `CHERO_DB_URL`; usa `CHERO_DB_PASSWORD`.

5. Despliega el proyecto. El Dockerfile compila el WAR con Maven y Java 17, lo instala como `ROOT.war` en Tomcat 10.1 y arranca Tomcat escuchando en el valor de `PORT`.
6. Al terminar, abre la URL de producción de Vercel. La aplicación se publica en la raíz del dominio (`/`), por lo que no debes añadir `/CheroSacWeb` a la URL.

## 5. Rutas de la aplicación

- Tienda: `/`
- Catálogo: `/catalogo`
- Inicio de sesión: `/login`
- Seguimiento: `/seguimiento`
- Pedidos del administrador: `/admin/pedidos`
- Inventario: `/admin/inventario`
- Postventa: `/admin/devoluciones`
- Reportes: `/admin/reportes`

## 6. Seguridad antes de publicar

- Cambia las contraseñas iniciales de las cuentas administrativas y no publiques las credenciales de demostración.
- Usa contraseñas únicas y largas para Neon y Vercel; nunca las subas a GitHub.
- Si el repositorio será público, revisa los comentarios y archivos SQL/LEEME para retirar credenciales de ejemplo.
- Los comprobantes actuales son documentos internos demostrativos; no son comprobantes tributarios autorizados por SUNAT.

## 7. Limitaciones importantes de Vercel Container Runtime

- El runtime de contenedores de Vercel está en beta y usa el modelo de consumo de contenedores/Fluid Compute. Revisa los límites y costos aplicables a tu cuenta.
- Las instancias pueden apagarse cuando quedan inactivas y volver a arrancar. El sistema de archivos y la memoria del contenedor no son almacenamiento persistente; por eso la base de datos debe estar fuera del contenedor.
- La aplicación utiliza sesiones HTTP en memoria de Tomcat (por ejemplo, carrito y autenticación). En un entorno que puede escalar a varias instancias o reiniciarse, esas sesiones no están garantizadas. Para uso real/producción, migra las sesiones a un almacén compartido (por ejemplo PostgreSQL con Spring Session JDBC o Redis) y prueba el flujo completo.
- La publicación no equivale a una integración SUNAT. La emisión real requiere el proceso tributario y proveedor/servicio autorizado correspondiente.

## 8. Pruebas posteriores al despliegue

1. Abre `/` y revisa que cargue el catálogo e imágenes.
2. Inicia sesión en el panel administrativo con credenciales nuevas.
3. Revisa `/admin/pedidos` y `/admin/inventario`.
4. Realiza un pedido de prueba y confirma que se guarde en PostgreSQL remoto.
5. Revisa los logs de Deployment → Runtime Logs si aparece HTTP 500 o un error de conexión JDBC.

No consideres la aplicación lista para uso comercial hasta probar autenticación, compras, sesiones, PDF y conexión remota bajo la URL pública.
