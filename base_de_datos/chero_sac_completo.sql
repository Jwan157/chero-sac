-- Instalación nueva con psql: psql -U postgres -d postgres -f chero_sac_completo.sql
-- Para actualizar la versión anterior, use migrar_desde_original.sql.
\set ON_ERROR_STOP on
SELECT 'CREATE DATABASE chero_sac WITH ENCODING ''UTF8'' TEMPLATE template0'
WHERE NOT EXISTS (SELECT 1 FROM pg_database WHERE datname = 'chero_sac') \gexec
\connect chero_sac
BEGIN;

-- Secuencia para generar el correlativo de los codigos CH-YYYY-XXXXXX
CREATE SEQUENCE seq_codigo_pedido START 1;

-- =====================================================================
-- 1. USUARIOS (administradores del sistema)
-- =====================================================================
CREATE TABLE usuarios (
    id_usuario        SERIAL PRIMARY KEY,
    nombre_completo   VARCHAR(120) NOT NULL,
    nombre_usuario    VARCHAR(50)  NOT NULL UNIQUE,
    correo            VARCHAR(120) NOT NULL UNIQUE,
    password_hash     VARCHAR(100) NOT NULL,
    rol               VARCHAR(20)  NOT NULL DEFAULT 'ADMIN'
                      CHECK (rol IN ('DUENO', 'ADMIN')),
    activo            BOOLEAN      NOT NULL DEFAULT TRUE,
    fecha_creacion    TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    ultimo_acceso     TIMESTAMP
);

-- =====================================================================
-- 2. CLIENTES (compra sin registro previo; se identifica por documento)
-- =====================================================================
CREATE TABLE clientes (
    id_cliente           SERIAL PRIMARY KEY,
    tipo_documento       VARCHAR(3)   NOT NULL CHECK (tipo_documento IN ('DNI', 'RUC')),
    numero_documento     VARCHAR(11)  NOT NULL,
    nombre_razon_social  VARCHAR(150) NOT NULL,
    apellido             VARCHAR(80),
    telefono             VARCHAR(15),
    correo               VARCHAR(120),
    direccion            VARCHAR(200) NOT NULL,
    password_hash        VARCHAR(100),
    tiene_cuenta         BOOLEAN NOT NULL DEFAULT FALSE,
    hash_activacion      VARCHAR(64),
    CONSTRAINT chk_cuenta_clave CHECK (tiene_cuenta = (password_hash IS NOT NULL)),
    fecha_registro       TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uq_cliente_documento UNIQUE (tipo_documento, numero_documento),
    CONSTRAINT chk_formato_documento CHECK (
        (tipo_documento = 'DNI' AND numero_documento ~ '^[0-9]{8}$') OR
        (tipo_documento = 'RUC' AND numero_documento ~ '^(10|20)[0-9]{9}$')
    )
);

-- =====================================================================
-- 3. CATEGORIAS
-- =====================================================================
CREATE TABLE categorias (
    id_categoria      SERIAL PRIMARY KEY,
    nombre_categoria  VARCHAR(60)  NOT NULL UNIQUE,
    descripcion       VARCHAR(200),
    activo            BOOLEAN      NOT NULL DEFAULT TRUE
);

-- =====================================================================
-- 4. PRODUCTOS (stock de venta separado del stock defectuoso)
--    Los precios incluyen IGV (18%).
-- =====================================================================
CREATE TABLE productos (
    id_producto           SERIAL PRIMARY KEY,
    id_categoria          INT           NOT NULL REFERENCES categorias(id_categoria)
                                        ON UPDATE CASCADE,
    nombre_producto       VARCHAR(150)  NOT NULL,
    descripcion           TEXT,
    imagen_url            VARCHAR(300),
    precio                NUMERIC(10,2) NOT NULL CHECK (precio > 0),
    costo_unitario        NUMERIC(10,2) CHECK (costo_unitario >= 0),
    stock_disponible      INT           NOT NULL DEFAULT 0 CHECK (stock_disponible >= 0),
    stock_defectuoso      INT           NOT NULL DEFAULT 0 CHECK (stock_defectuoso >= 0),
    activo                BOOLEAN       NOT NULL DEFAULT TRUE,
    fecha_registro        TIMESTAMP     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    fecha_actualizacion   TIMESTAMP     NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- =====================================================================
-- 5. METODOS DE PAGO
-- =====================================================================
CREATE TABLE metodos_pago (
    id_metodo_pago  SERIAL PRIMARY KEY,
    tipo_pago       VARCHAR(30) NOT NULL UNIQUE
                    CHECK (tipo_pago IN ('Efectivo', 'Tarjeta', 'Transferencia', 'Yape/Plin')),
    activo          BOOLEAN     NOT NULL DEFAULT TRUE
);

-- =====================================================================
-- 6. PEDIDOS
-- =====================================================================
CREATE TABLE pedidos (
    id_pedido            SERIAL PRIMARY KEY,
    codigo_seguimiento   VARCHAR(20)  NOT NULL UNIQUE,
    id_cliente           INT          NOT NULL REFERENCES clientes(id_cliente),
    id_metodo_pago       INT          NOT NULL REFERENCES metodos_pago(id_metodo_pago),
    id_usuario           INT          REFERENCES usuarios(id_usuario),
    fecha_pedido         TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    estado               VARCHAR(15)  NOT NULL DEFAULT 'PENDIENTE'
                         CHECK (estado IN ('PENDIENTE', 'CONFIRMADO', 'ENVIADO', 'ENTREGADO', 'CANCELADO')),
    subtotal             NUMERIC(10,2) NOT NULL CHECK (subtotal >= 0),
    igv                  NUMERIC(10,2) NOT NULL CHECK (igv >= 0),
    total                NUMERIC(10,2) NOT NULL CHECK (total >= 0),
    observaciones        TEXT,
    es_demo              BOOLEAN NOT NULL DEFAULT FALSE
);

-- =====================================================================
-- 7. DETALLE DE PEDIDOS (resuelve la relacion N:M Pedido-Producto)
-- =====================================================================
CREATE TABLE detalle_pedidos (
    id_detalle       SERIAL PRIMARY KEY,
    id_pedido        INT           NOT NULL REFERENCES pedidos(id_pedido) ON DELETE CASCADE,
    id_producto      INT           NOT NULL REFERENCES productos(id_producto),
    cantidad         INT           NOT NULL CHECK (cantidad > 0),
    precio_unitario  NUMERIC(10,2) NOT NULL CHECK (precio_unitario >= 0),
    costo_unitario   NUMERIC(10,2) CHECK (costo_unitario >= 0),
    CONSTRAINT uq_pedido_producto UNIQUE (id_pedido, id_producto)
);

-- =====================================================================
-- 8. COMPROBANTES ELECTRONICOS (boleta, factura y nota de credito)
--    Simulacion local: hash_sunat es un hash SHA-256 interno,
--    no se envia a los servidores de SUNAT.
-- =====================================================================
CREATE TABLE comprobantes_electronicos (
    id_comprobante        SERIAL PRIMARY KEY,
    id_pedido             INT           NOT NULL REFERENCES pedidos(id_pedido),
    tipo_comprobante      VARCHAR(15)   NOT NULL
                          CHECK (tipo_comprobante IN ('BOLETA', 'FACTURA', 'NOTA_CREDITO')),
    serie                 VARCHAR(4)    NOT NULL CHECK (serie IN ('B001', 'F001', 'BC01', 'FC01')),
    numero_correlativo    INT           NOT NULL CHECK (numero_correlativo > 0),
    ruc_dni_cliente       VARCHAR(11)   NOT NULL,
    razon_social_cliente  VARCHAR(150)  NOT NULL,
    subtotal              NUMERIC(10,2) NOT NULL CHECK (subtotal >= 0),
    igv_18                NUMERIC(10,2) NOT NULL CHECK (igv_18 >= 0),
    total                 NUMERIC(10,2) NOT NULL CHECK (total >= 0),
    hash_sunat            VARCHAR(64)   NOT NULL,
    estado_sunat          VARCHAR(10)   NOT NULL DEFAULT 'ACEPTADO'
                          CHECK (estado_sunat IN ('ACEPTADO', 'ANULADO')),
    fecha_emision         TIMESTAMP     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uq_serie_correlativo UNIQUE (serie, numero_correlativo),
    CONSTRAINT chk_serie_tipo CHECK (
        (tipo_comprobante = 'BOLETA'       AND serie = 'B001') OR
        (tipo_comprobante = 'FACTURA'      AND serie = 'F001') OR
        (tipo_comprobante = 'NOTA_CREDITO' AND serie IN ('BC01', 'FC01'))
    )
);

-- =====================================================================
-- 9. SOLICITUDES DE DEVOLUCION (postventa solicitada por el cliente)
-- =====================================================================
CREATE TABLE solicitudes_devolucion (
    id_solicitud        SERIAL PRIMARY KEY,
    id_pedido           INT          NOT NULL REFERENCES pedidos(id_pedido),
    id_producto         INT          NOT NULL REFERENCES productos(id_producto),
    cantidad_devuelta   INT          NOT NULL CHECK (cantidad_devuelta > 0),
    tipo_solicitud      VARCHAR(12)  NOT NULL
                        CHECK (tipo_solicitud IN ('DEVOLUCION', 'CAMBIO', 'GARANTIA')),
    motivo              TEXT         NOT NULL,
    estado              VARCHAR(10)  NOT NULL DEFAULT 'PENDIENTE'
                        CHECK (estado IN ('PENDIENTE', 'APROBADO', 'RECHAZADO')),
    condicion_producto  VARCHAR(12)
                        CHECK (condicion_producto IN ('BUEN_ESTADO', 'DEFECTUOSO')),
    id_usuario          INT          REFERENCES usuarios(id_usuario),
    respuesta_admin     TEXT,
    fecha_solicitud     TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    fecha_resolucion    TIMESTAMP,
    CONSTRAINT chk_condicion_aprobada CHECK (
        estado <> 'APROBADO' OR condicion_producto IS NOT NULL
    )
);

-- =====================================================================
-- 10. MOVIMIENTOS DE INVENTARIO (Kardex)
--     cantidad: positiva en entradas y salidas; AJUSTE puede ser negativo.
-- =====================================================================
CREATE TABLE movimientos_inventario (
    id_movimiento     SERIAL PRIMARY KEY,
    id_producto       INT         NOT NULL REFERENCES productos(id_producto),
    tipo_movimiento   VARCHAR(32) NOT NULL
                      CHECK (tipo_movimiento IN ('SALIDA_VENTA', 'INGRESO_DEVOLUCION_OK',
                                                 'INGRESO_DEFECTUOSO', 'REPARACION_OK', 'BAJA_MERMA', 'AJUSTE')),
    cantidad          INT         NOT NULL,
    referencia_codigo VARCHAR(40),
    fecha_movimiento  TIMESTAMP   NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT chk_cantidad_movimiento CHECK (tipo_movimiento = 'AJUSTE' OR cantidad > 0)
);

-- =====================================================================
-- 11. HISTORIAL DE ACCIONES (auditoria de administradores)
-- =====================================================================
CREATE TABLE historial_acciones (
    id_historial   SERIAL PRIMARY KEY,
    id_usuario     INT         NOT NULL REFERENCES usuarios(id_usuario),
    accion         VARCHAR(40) NOT NULL,
    detalle        TEXT,
    fecha_accion   TIMESTAMP   NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- =====================================================================
-- INDICES
-- =====================================================================
CREATE INDEX idx_productos_categoria     ON productos(id_categoria);
CREATE INDEX idx_pedidos_fecha          ON pedidos(fecha_pedido);
CREATE INDEX idx_pedidos_estado         ON pedidos(estado);
CREATE INDEX idx_detalle_producto       ON detalle_pedidos(id_producto);
CREATE INDEX idx_comprobantes_pedido    ON comprobantes_electronicos(id_pedido);
CREATE INDEX idx_solicitudes_pedido     ON solicitudes_devolucion(id_pedido);
CREATE INDEX idx_solicitudes_estado     ON solicitudes_devolucion(estado);
CREATE INDEX idx_movimientos_producto   ON movimientos_inventario(id_producto);
CREATE INDEX idx_historial_usuario      ON historial_acciones(id_usuario);

-- =====================================================================
-- DATOS INICIALES
-- =====================================================================

-- Cuentas de demostración de la instalación nueva.
INSERT INTO usuarios (nombre_completo, nombre_usuario, correo, password_hash, rol) VALUES
('Dueño Chero SAC','dueno','dueno@example.test','$2a$11$vanWcJFIgkx.Rz9E0VRcfOS34Ebr7AII8DRBdnoZtF901Gk4QYoB.','DUENO'),
('Empleado de ventas','vendedor1','vendedor1@example.test','$2a$11$7prJ1rB/8jWFHTDEONlZguEqIc8T/mJiSsyh1sdvByjWltDft3IUS','ADMIN');
INSERT INTO clientes (tipo_documento, numero_documento, nombre_razon_social, apellido, correo,
                      direccion, tiene_cuenta, password_hash) VALUES
('DNI','76543210','Ana','Torres','cliente@example.test','Av. de prueba 120',TRUE,'$2a$11$np1DLCBFtO2FVj8JKlDXferR6gvd/ZOc65vM0y52WQYscbRT3cdQq');

-- Categorias de hardware
INSERT INTO categorias (nombre_categoria, descripcion) VALUES
('Laptops',      'Laptops para estudio, oficina y uso personal'),
('Computadoras', 'Equipos de escritorio y todo en uno'),
('Periféricos',  'Monitores, teclados, mouse y accesorios'),
('Componentes',  'Memorias RAM, almacenamiento y componentes internos');

-- Metodos de pago
INSERT INTO metodos_pago (tipo_pago) VALUES
('Efectivo'), ('Tarjeta'), ('Transferencia'), ('Yape/Plin');

-- Productos de referencia (precios con IGV incluido)
INSERT INTO productos (id_categoria, nombre_producto, descripcion, imagen_url, precio, stock_disponible, stock_defectuoso) VALUES
(1, 'Laptop Lenovo IdeaPad Slim 3',
    'AMD Ryzen 5, 16 GB RAM, SSD 512 GB, pantalla 15.6" FHD',
    '/img/productos/lenovo-ideapad-slim-3-foto.png', 2399.00, 12, 0),
(1, 'Laptop ASUS Vivobook 15',
    'Intel Core i5, 8 GB RAM, SSD 512 GB, pantalla 15.6" FHD',
    '/img/productos/asus-vivobook-15-foto.png', 2099.90, 8, 0),
(2, 'PC de escritorio Gamer Ryzen 5 / RTX 3060',
    'Ryzen 5 5600, 16 GB RAM, RTX 3060 12 GB, SSD 1 TB',
    '/img/productos/pc-gamer-rtx-3060-foto.png', 3899.00, 4, 0),
(2, 'All in One HP 24"',
    'Pantalla 23.8" FHD, Intel Core i5, 8 GB RAM, SSD 256 GB',
    '/img/productos/hp-all-in-one-24-foto.svg', 2299.00, 3, 0),
(3, 'Monitor Gamer 24" 165 Hz',
    'Panel IPS 24" FHD, 165 Hz, 1 ms',
    '/img/productos/monitor-165hz-foto.png', 649.00, 15, 0),
(3, 'Teclado mecanico Redragon Kumara',
    'Teclado mecanico TKL con switches rojos y retroiluminacion',
    '/img/productos/teclado-mecanico-foto.png', 189.90, 25, 0),
(3, 'Mouse Logitech G203 Lightsync',
    'Mouse optico con 6 botones y iluminacion RGB',
    '/img/productos/mouse-g203-foto.png', 99.00, 28, 2),
(4, 'Memoria RAM Kingston Fury 16 GB DDR4 3200 MHz',
    'Modulo de memoria DDR4 de 16 GB a 3200 MHz',
    '/img/productos/ram-ddr4-16gb-foto.svg', 219.00, 20, 0);

-- Costos unitarios de demostración en soles, sin IGV.
UPDATE productos p SET costo_unitario = v.costo
FROM (VALUES (1,1500.00),(2,1320.00),(3,2400.00),(4,1400.00),(5,380.00),
             (6,105.00),(7,55.00),(8,120.00)) AS v(id,costo) WHERE p.id_producto=v.id;

-- Catálogo ampliado: 28 productos adicionales (total esperado: 36).
-- Se usa nombre_producto para que el bloque sea seguro al ejecutarse más de una vez.
INSERT INTO productos (id_categoria, nombre_producto, descripcion, imagen_url, precio,
                       stock_disponible, stock_defectuoso, costo_unitario)
SELECT c.id_categoria, v.nombre, v.descripcion, v.imagen, v.precio, v.stock, 0, v.costo
FROM (VALUES
    ('Laptops', 'Laptop Gamer 15.6 pulgadas RTX 4060', 'Equipo portátil para gaming y creación de contenido, pantalla Full HD 144 Hz, 16 GB RAM y SSD 1 TB.', '/img/productos/laptop-gamer-156.png', 4699.00, 6, 3590.00),
    ('Laptops', 'Laptop Ultrabook 14 pulgadas Core i5', 'Portátil liviana para estudio y oficina, 16 GB RAM, SSD 512 GB y pantalla Full HD.', '/img/productos/laptop-ultrabook-14.png', 3299.00, 8, 2460.00),
    ('Computadoras', 'PC Gamer Ryzen 7 con RGB', 'Computadora de escritorio Ryzen 7, gráficos RTX 4060, 32 GB RAM y SSD 1 TB.', '/img/productos/pc-gamer-ryzen7-rgb.png', 5299.00, 4, 4080.00),
    ('Computadoras', 'PC Compacta Core i5 para oficina', 'Equipo compacto para productividad, Intel Core i5, 16 GB RAM y SSD 512 GB.', '/img/productos/pc-compacta-i5.png', 2599.00, 5, 1920.00),
    ('Periféricos', 'Monitor UltraWide 29 pulgadas IPS', 'Monitor panorámico IPS de 29 pulgadas para trabajo multitarea y entretenimiento.', '/img/productos/monitor-ultrawide-29.png', 899.00, 9, 625.00),
    ('Periféricos', 'Monitor Gamer Curvo 27 pulgadas 165Hz', 'Panel curvo Full HD, frecuencia de 165 Hz y respuesta rápida para gaming.', '/img/productos/monitor-curvo-27-165.png', 1099.00, 7, 780.00),
    ('Periféricos', 'Teclado inalámbrico compacto', 'Teclado compacto inalámbrico de perfil bajo, ideal para escritorio y movilidad.', '/img/productos/teclado-compacto-wireless.png', 149.00, 18, 82.00),
    ('Periféricos', 'Teclado mecánico RGB TKL', 'Teclado mecánico tenkeyless con retroiluminación RGB y conexión USB.', '/img/productos/teclado-mecanico-rgb-tkl.png', 239.90, 16, 154.00),
    ('Periféricos', 'Mouse ergonómico inalámbrico', 'Mouse ergonómico inalámbrico con botones laterales y sensor de alta precisión.', '/img/productos/mouse-ergonomico-wireless.png', 139.00, 20, 74.00),
    ('Periféricos', 'Mouse Gamer RGB 12K DPI', 'Mouse gamer con iluminación RGB, botones programables y sensor de hasta 12000 DPI.', '/img/productos/mouse-gamer-rgb.png', 169.00, 22, 94.00),
    ('Periféricos', 'Headset Gamer RGB con micrófono', 'Auriculares circumaurales para gaming con micrófono flexible y sonido estéreo.', '/img/productos/headset-gamer-rgb.png', 299.00, 12, 188.00),
    ('Periféricos', 'Audífonos inalámbricos Over-Ear', 'Audífonos inalámbricos de diadema acolchada y batería recargable.', '/img/productos/audifonos-wireless.png', 329.00, 13, 208.00),
    ('Periféricos', 'Webcam Full HD 1080p', 'Cámara web Full HD con micrófono integrado y clip universal para monitor.', '/img/productos/webcam-fullhd.png', 189.00, 10, 118.00),
    ('Periféricos', 'Micrófono USB de condensador', 'Micrófono USB de condensador con soporte de escritorio para streaming y videollamadas.', '/img/productos/microfono-usb.png', 249.00, 10, 158.00),
    ('Periféricos', 'Control inalámbrico para PC', 'Mando inalámbrico compatible con PC, joysticks dobles y vibración.', '/img/productos/control-wireless-pc.png', 219.00, 14, 138.00),
    ('Periféricos', 'Router Wi-Fi 6 doble banda', 'Router Wi-Fi 6 de doble banda con cuatro antenas y puertos Gigabit.', '/img/productos/router-wifi6.png', 429.00, 8, 288.00),
    ('Componentes', 'Placa madre B550M AM4', 'Placa madre microATX con chipset B550, socket AM4 y ranura M.2 NVMe.', '/img/productos/placa-madre-b550m.svg', 499.00, 7, 328.00),
    ('Componentes', 'Tarjeta gráfica RTX 4060 8GB', 'Tarjeta gráfica de 8 GB GDDR6 para gaming Full HD y creación de contenido.', '/img/productos/tarjeta-grafica-rtx4060.svg', 1999.00, 4, 1570.00),
    ('Componentes', 'Procesador AMD Ryzen 7 5700X', 'Procesador de 8 núcleos y 16 hilos para equipos de alto rendimiento.', '/img/productos/procesador-ryzen7.svg', 899.00, 6, 648.00),
    ('Componentes', 'Kit RAM DDR5 32GB (2x16GB)', 'Memoria DDR5 en kit de 32 GB para plataformas compatibles.', '/img/productos/ram-ddr5-32gb.svg', 499.00, 10, 318.00),
    ('Componentes', 'SSD NVMe 1TB PCIe 4.0', 'Unidad de almacenamiento M.2 NVMe de 1 TB con interfaz PCIe 4.0.', '/img/productos/ssd-nvme-1tb.svg', 329.00, 15, 204.00),
    ('Componentes', 'SSD externo USB-C 1TB', 'Almacenamiento portátil de 1 TB con conexión USB-C para respaldos y transferencia.', '/img/productos/ssd-externo-1tb.svg', 379.00, 8, 238.00),
    ('Componentes', 'Fuente modular 750W 80+ Gold', 'Fuente de poder modular de 750 W con certificación 80 Plus Gold.', '/img/productos/fuente-750w-gold.svg', 599.00, 5, 418.00),
    ('Componentes', 'Gabinete Gamer Mid Tower RGB', 'Gabinete ATX mid tower con panel lateral transparente y ventiladores RGB.', '/img/productos/gabinete-mid-tower.svg', 399.00, 7, 258.00),
    ('Componentes', 'Cooler CPU torre ARGB', 'Disipador de aire tipo torre con ventilador ARGB para procesadores compatibles.', '/img/productos/cooler-torre-argb.svg', 189.00, 8, 113.00),
    ('Componentes', 'Refrigeración líquida 240mm', 'Sistema AIO de refrigeración líquida con radiador de 240 mm y dos ventiladores.', '/img/productos/refrigeracion-liquida-240.svg', 349.00, 5, 222.00),
    ('Periféricos', 'Dock USB-C 8 en 1', 'Hub USB-C multipuerto con HDMI, USB-A, lector de tarjetas y carga pass-through.', '/img/productos/dock-usbc-8en1.svg', 229.00, 9, 148.00),
    ('Periféricos', 'Base refrigeradora para laptop', 'Base inclinable con ventiladores y conexión USB para mejorar el flujo de aire del portátil.', '/img/productos/base-refrigeradora-laptop.svg', 119.00, 12, 68.00)
) AS v(categoria, nombre, descripcion, imagen, precio, stock, costo)
JOIN categorias c ON lower(c.nombre_categoria) = lower(v.categoria)
WHERE NOT EXISTS (
    SELECT 1 FROM productos p WHERE lower(p.nombre_producto) = lower(v.nombre)
);

-- Kardex inicial: refleja el stock cargado en la tabla productos
INSERT INTO movimientos_inventario (id_producto, tipo_movimiento, cantidad, referencia_codigo)
SELECT id_producto, 'AJUSTE', stock_disponible, 'CARGA_INICIAL'
FROM productos WHERE stock_disponible > 0;

INSERT INTO movimientos_inventario (id_producto, tipo_movimiento, cantidad, referencia_codigo)
SELECT id_producto, 'INGRESO_DEFECTUOSO', stock_defectuoso, 'CARGA_INICIAL'
FROM productos WHERE stock_defectuoso > 0;

INSERT INTO historial_acciones (id_usuario, accion, detalle) VALUES
(1, 'CARGA_INICIAL', 'Creacion de la base de datos chero_sac y datos de prueba');
COMMIT;
