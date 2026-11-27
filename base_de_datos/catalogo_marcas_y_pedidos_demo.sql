\encoding UTF8
-- =====================================================================
-- CHERO.SAC | Catálogo de marcas + pedidos de demostración
-- Aplicar sobre chero_sac DESPUÉS de migrar_desde_original.sql y
-- ampliar_catalogo_36.sql.
-- Seguro para reejecutar: no reinicia la base ni duplica los productos
-- o pedidos de muestra. Los pedidos DEMO NO descuentan stock.
-- =====================================================================
\set ON_ERROR_STOP on

BEGIN;

-- Marca los pedidos de muestra para identificarlos en el panel del dueño.
ALTER TABLE pedidos
    ADD COLUMN IF NOT EXISTS es_demo BOOLEAN NOT NULL DEFAULT FALSE;

-- ---------------------------------------------------------------------
-- 1. Nombres comerciales más claros y fotografías locales referenciales
-- ---------------------------------------------------------------------
UPDATE productos SET nombre_producto='ASUS TUF Gaming F15 RTX 4060 15.6 pulgadas',
    descripcion='Laptop gamer ASUS TUF Gaming F15 con NVIDIA GeForce RTX 4060, pantalla Full HD de alta frecuencia, 16 GB de RAM y SSD de 1 TB. Imagen referencial.' ,
    imagen_url='/img/productos/foto-laptop-gamer.jpg'
WHERE nombre_producto='Laptop Gamer 15.6 pulgadas RTX 4060';

UPDATE productos SET nombre_producto='Lenovo IdeaPad Slim 5 14 pulgadas Core i5',
    descripcion='Portátil Lenovo IdeaPad Slim 5 de 14 pulgadas, Intel Core i5, 16 GB de RAM y SSD de 512 GB para estudio y trabajo. Imagen referencial.',
    imagen_url='/img/productos/foto-laptop-oficina.jpg'
WHERE nombre_producto='Laptop Ultrabook 14 pulgadas Core i5';

UPDATE productos SET nombre_producto='PC Gamer Ryzen 7 5700X con RTX 4060 y RGB',
    descripcion='PC armada con AMD Ryzen 7 5700X, NVIDIA GeForce RTX 4060, 32 GB de RAM, SSD de 1 TB y gabinete con iluminación RGB. Imagen referencial.',
    imagen_url='/img/productos/foto-pc-gamer-rgb.jpg'
WHERE nombre_producto='PC Gamer Ryzen 7 con RGB';

UPDATE productos SET nombre_producto='PC compacta Intel Core i5-12400 para oficina',
    descripcion='Computadora compacta para productividad con Intel Core i5-12400, 16 GB de RAM y SSD de 512 GB. Imagen referencial.',
    imagen_url='/img/productos/foto-pc-oficina.jpg'
WHERE nombre_producto='PC Compacta Core i5 para oficina';

UPDATE productos SET nombre_producto='LG UltraWide 29WQ600 29 pulgadas IPS',
    descripcion='Monitor LG UltraWide de 29 pulgadas con panel IPS, formato panorámico y conectividad para trabajo multitarea. Imagen referencial.',
    imagen_url='/img/productos/foto-monitor-ultrawide.jpg'
WHERE nombre_producto='Monitor UltraWide 29 pulgadas IPS';

UPDATE productos SET nombre_producto='Samsung Odyssey G5 27 pulgadas 165Hz',
    descripcion='Monitor curvo Samsung Odyssey G5 de 27 pulgadas y 165 Hz, orientado a juegos y entretenimiento. Imagen referencial.',
    imagen_url='/img/productos/foto-monitor-gamer.jpg'
WHERE nombre_producto='Monitor Gamer Curvo 27 pulgadas 165Hz';

UPDATE productos SET nombre_producto='Logitech Pebble Keys 2 K380s',
    descripcion='Teclado compacto inalámbrico Logitech Pebble Keys 2 K380s, silencioso y compatible con varios dispositivos. Imagen referencial.',
    imagen_url='/img/productos/foto-teclado-compacto.jpg'
WHERE nombre_producto='Teclado inalámbrico compacto';

UPDATE productos SET nombre_producto='HyperX Alloy Origins Core RGB TKL',
    descripcion='Teclado mecánico HyperX Alloy Origins Core TKL con iluminación RGB y conexión USB. Imagen referencial.',
    imagen_url='/img/productos/foto-teclado-rgb.jpg'
WHERE nombre_producto='Teclado mecánico RGB TKL';

UPDATE productos SET nombre_producto='Logitech MX Master 3S inalámbrico',
    descripcion='Mouse inalámbrico Logitech MX Master 3S, sensor de alta precisión, botones configurables y diseño ergonómico. Imagen referencial.',
    imagen_url='/img/productos/foto-mouse-ergonomico.jpg'
WHERE nombre_producto='Mouse ergonómico inalámbrico';

UPDATE productos SET nombre_producto='Logitech G502 HERO 25K',
    descripcion='Mouse gamer Logitech G502 HERO con sensor de hasta 25 600 DPI, botones programables y peso ajustable. Imagen referencial.',
    imagen_url='/img/productos/foto-mouse-gamer.jpg'
WHERE nombre_producto='Mouse Gamer RGB 12K DPI';

UPDATE productos SET nombre_producto='HyperX Cloud III Gaming Headset',
    descripcion='Audífonos gamer HyperX Cloud III con micrófono, almohadillas cómodas y sonido para juegos y comunicación. Imagen referencial.',
    imagen_url='/img/productos/foto-headset-gamer.jpg'
WHERE nombre_producto='Headset Gamer RGB con micrófono';

UPDATE productos SET nombre_producto='Sony WH-CH720N inalámbricos',
    descripcion='Audífonos Sony WH-CH720N inalámbricos de diadema, con cancelación de ruido y batería recargable. Imagen referencial.',
    imagen_url='/img/productos/foto-audifonos-wireless.jpg'
WHERE nombre_producto='Audífonos inalámbricos Over-Ear';

UPDATE productos SET nombre_producto='Logitech C920s Pro Full HD',
    descripcion='Webcam Logitech C920s Pro Full HD 1080p para videollamadas, clases y transmisión. Imagen referencial.',
    imagen_url='/img/productos/foto-webcam-fullhd.jpg'
WHERE nombre_producto='Webcam Full HD 1080p';

UPDATE productos SET nombre_producto='FIFINE K669B USB',
    descripcion='Micrófono USB FIFINE K669B de condensador con soporte de escritorio para videollamadas y streaming. Imagen referencial.',
    imagen_url='/img/productos/foto-microfono-usb.jpg'
WHERE nombre_producto='Micrófono USB de condensador';

UPDATE productos SET nombre_producto='Xbox Wireless Controller Carbon Black',
    descripcion='Control inalámbrico Xbox Carbon Black compatible con PC y dispositivos compatibles. Imagen referencial.',
    imagen_url='/img/productos/foto-control-wireless.jpg'
WHERE nombre_producto='Control inalámbrico para PC';

UPDATE productos SET nombre_producto='TP-Link Archer AX55 Wi-Fi 6',
    descripcion='Router TP-Link Archer AX55 Wi-Fi 6 de doble banda con puertos Gigabit para el hogar y la oficina. Imagen referencial.',
    imagen_url='/img/productos/foto-router-wifi6.jpg'
WHERE nombre_producto='Router Wi-Fi 6 doble banda';

UPDATE productos SET nombre_producto='ASUS TUF Gaming B550M-PLUS AM4',
    descripcion='Placa madre ASUS TUF Gaming B550M-PLUS para procesadores AMD Ryzen con socket AM4. Imagen referencial.',
    imagen_url='/img/productos/foto-motherboard-asus.jpg'
WHERE nombre_producto='Placa madre B550M AM4';

UPDATE productos SET nombre_producto='MSI GeForce RTX 4060 Ventus 2X 8GB',
    descripcion='Tarjeta gráfica MSI GeForce RTX 4060 Ventus 2X de 8 GB GDDR6, compatible con tecnologías de trazado de rayos y escalado de imagen. Imagen referencial.',
    imagen_url='/img/productos/foto-gpu-geforce.jpg'
WHERE nombre_producto='Tarjeta gráfica RTX 4060 8GB';

UPDATE productos SET nombre_producto='AMD Ryzen 7 5700X 8 núcleos / 16 hilos',
    descripcion='Procesador AMD Ryzen 7 5700X para socket AM4, 8 núcleos y 16 hilos. Imagen referencial.',
    imagen_url='/img/productos/foto-amd-ryzen.jpg'
WHERE nombre_producto='Procesador AMD Ryzen 7 5700X';

UPDATE productos SET nombre_producto='Kingston FURY Beast DDR5 32GB (2x16GB)',
    descripcion='Kit Kingston FURY Beast DDR5 de 32 GB (2 x 16 GB) para plataformas compatibles. Imagen referencial.',
    imagen_url='/img/productos/foto-ram-kingston.jpg'
WHERE nombre_producto='Kit RAM DDR5 32GB (2x16GB)';

UPDATE productos SET nombre_producto='Kingston NV2 NVMe 1TB PCIe 4.0',
    descripcion='Unidad SSD Kingston NV2 M.2 NVMe de 1 TB con interfaz PCIe 4.0. Imagen referencial.',
    imagen_url='/img/productos/foto-ssd-wd.jpg'
WHERE nombre_producto='SSD NVMe 1TB PCIe 4.0';

UPDATE productos SET nombre_producto='WD My Passport SSD 1TB USB-C',
    descripcion='Unidad externa WD My Passport SSD de 1 TB con conexión USB-C para respaldos y transporte de archivos. Imagen referencial.',
    imagen_url='/img/productos/foto-ssd-wd.jpg'
WHERE nombre_producto='SSD externo USB-C 1TB';

UPDATE productos SET nombre_producto='Corsair RM750e 750W 80 Plus Gold',
    descripcion='Fuente de alimentación Corsair RM750e de 750 W, modular y con certificación 80 Plus Gold. Imagen referencial.',
    imagen_url='/img/productos/foto-fuente-corsair.jpg'
WHERE nombre_producto='Fuente modular 750W 80+ Gold';

UPDATE productos SET nombre_producto='NZXT H5 Flow RGB Mid Tower',
    descripcion='Gabinete NZXT H5 Flow Mid Tower con panel lateral transparente y ventilación optimizada. Imagen referencial.',
    imagen_url='/img/productos/foto-gabinete-rgb.jpg'
WHERE nombre_producto='Gabinete Gamer Mid Tower RGB';

UPDATE productos SET nombre_producto='DeepCool AK400 Digital',
    descripcion='Disipador de aire DeepCool AK400 Digital para procesadores compatibles. Imagen referencial.',
    imagen_url='/img/productos/foto-cooler-cpu.jpg'
WHERE nombre_producto='Cooler CPU torre ARGB';

UPDATE productos SET nombre_producto='Corsair iCUE H100i RGB ELITE 240mm',
    descripcion='Sistema de refrigeración líquida Corsair iCUE H100i RGB ELITE con radiador de 240 mm. Imagen referencial.',
    imagen_url='/img/productos/foto-aio-corsair.jpg'
WHERE nombre_producto='Refrigeración líquida 240mm';

UPDATE productos SET nombre_producto='UGREEN Hub USB-C 8 en 1',
    descripcion='Adaptador UGREEN USB-C multipuerto 8 en 1 con HDMI, USB-A y lector de tarjetas. Imagen referencial.',
    imagen_url='/img/productos/dock-usbc-8en1.svg'
WHERE nombre_producto='Dock USB-C 8 en 1';

UPDATE productos SET nombre_producto='Cooler Master NotePal X150R',
    descripcion='Base refrigeradora Cooler Master para laptop con inclinación ergonómica y ventilación auxiliar. Imagen referencial.',
    imagen_url='/img/productos/base-refrigeradora-laptop.svg'
WHERE nombre_producto='Base refrigeradora para laptop';

-- Ajuste de imágenes de algunos productos originales para que el catálogo mantenga
-- una presentación homogénea. Se conservan sus identificadores y precios.
UPDATE productos SET imagen_url='/img/productos/foto-laptop-oficina.jpg'
WHERE nombre_producto='Laptop ASUS Vivobook 15';
UPDATE productos SET imagen_url='/img/productos/foto-pc-gamer-rgb.jpg'
WHERE nombre_producto='PC de escritorio Gamer Ryzen 5 / RTX 3060';
UPDATE productos SET imagen_url='/img/productos/foto-monitor-gamer.jpg'
WHERE nombre_producto='Monitor Gamer 24" 165 Hz';
UPDATE productos SET imagen_url='/img/productos/foto-teclado-rgb.jpg'
WHERE nombre_producto='Teclado mecanico Redragon Kumara';
UPDATE productos SET imagen_url='/img/productos/foto-mouse-gamer.jpg'
WHERE nombre_producto='Mouse Logitech G203 Lightsync';
UPDATE productos SET imagen_url='/img/productos/foto-ram-kingston.jpg'
WHERE nombre_producto='Memoria RAM Kingston Fury 16 GB DDR4 3200 MHz';

-- ---------------------------------------------------------------------
-- 2. Doce productos adicionales de marcas y modelos variados.
--    Total esperado: 48 productos activos; el catálogo muestra 16 por página.
-- ---------------------------------------------------------------------
INSERT INTO productos (id_categoria, nombre_producto, descripcion, imagen_url, precio,
                       costo_unitario, stock_disponible, stock_defectuoso)
SELECT c.id_categoria, v.nombre, v.descripcion, v.imagen, v.precio, v.costo, v.stock, 0
FROM (VALUES
    ('Componentes', 'AMD Ryzen 5 5600 6 núcleos / 12 hilos', 'Procesador AMD Ryzen 5 5600 para socket AM4, 6 núcleos y 12 hilos. Imagen referencial.', '/img/productos/foto-amd-ryzen.jpg', 569.00, 390.00, 14),
    ('Componentes', 'AMD Ryzen 7 7800X3D 8 núcleos / 16 hilos', 'Procesador AMD Ryzen 7 7800X3D para socket AM5, 8 núcleos y 16 hilos con tecnología 3D V-Cache. Imagen referencial.', '/img/productos/foto-amd-ryzen.jpg', 1899.00, 1510.00, 5),
    ('Componentes', 'Intel Core i5-13400F 10 núcleos / 16 hilos', 'Procesador Intel Core i5-13400F para socket LGA1700, 10 núcleos híbridos y 16 hilos. Imagen referencial.', '/img/productos/foto-intel-core.jpg', 899.00, 690.00, 10),
    ('Componentes', 'Intel Core i7-14700K 20 núcleos / 28 hilos', 'Procesador Intel Core i7-14700K para socket LGA1700, 20 núcleos y 28 hilos. Imagen referencial.', '/img/productos/foto-intel-core.jpg', 2199.00, 1790.00, 4),
    ('Componentes', 'Intel Core i5-12400F 6 núcleos / 12 hilos', 'Procesador Intel Core i5-12400F para socket LGA1700, 6 núcleos y 12 hilos. Imagen referencial.', '/img/productos/foto-intel-core.jpg', 699.00, 520.00, 12),
    ('Componentes', 'MSI GeForce RTX 4070 SUPER Ventus 2X 12GB', 'Tarjeta gráfica MSI GeForce RTX 4070 SUPER Ventus 2X de 12 GB GDDR6X. Imagen referencial.', '/img/productos/foto-gpu-geforce.jpg', 2999.00, 2510.00, 5),
    ('Componentes', 'Sapphire Pulse Radeon RX 7800 XT 16GB', 'Tarjeta gráfica Sapphire Pulse AMD Radeon RX 7800 XT de 16 GB GDDR6. Imagen referencial.', '/img/productos/foto-gpu-radeon.jpg', 2799.00, 2290.00, 4),
    ('Componentes', 'ASUS TUF Gaming B650-PLUS WIFI AM5', 'Placa madre ASUS TUF Gaming B650-PLUS WIFI para procesadores AMD Ryzen socket AM5 y memoria DDR5. Imagen referencial.', '/img/productos/foto-motherboard-asus.jpg', 999.00, 790.00, 6),
    ('Componentes', 'MSI PRO B760M-A WIFI DDR5', 'Placa madre MSI PRO B760M-A WIFI DDR5 para procesadores Intel compatibles con socket LGA1700. Imagen referencial.', '/img/productos/foto-motherboard-msi.jpg', 899.00, 710.00, 6),
    ('Componentes', 'Corsair Vengeance RGB DDR5 32GB 6000MHz', 'Kit Corsair Vengeance RGB DDR5 de 32 GB (2 x 16 GB), velocidad de referencia 6000 MHz. Imagen referencial.', '/img/productos/foto-ram-corsair.jpg', 629.00, 470.00, 11),
    ('Componentes', 'Samsung 990 PRO NVMe 1TB PCIe 4.0', 'Unidad SSD Samsung 990 PRO M.2 NVMe de 1 TB con interfaz PCIe 4.0. Imagen referencial.', '/img/productos/foto-ssd-samsung.jpg', 569.00, 430.00, 9),
    ('Periféricos', 'Samsung Odyssey G6 27 pulgadas 240Hz', 'Monitor Samsung Odyssey G6 de 27 pulgadas con frecuencia de actualización de hasta 240 Hz. Imagen referencial.', '/img/productos/foto-monitor-odyssey.jpg', 1499.00, 1160.00, 5)
) AS v(categoria, nombre, descripcion, imagen, precio, costo, stock)
JOIN categorias c ON lower(c.nombre_categoria) = lower(v.categoria)
WHERE NOT EXISTS (SELECT 1 FROM productos p WHERE lower(p.nombre_producto) = lower(v.nombre));

-- Kardex de alta inicial solo para productos que todavía no tienen este registro.
INSERT INTO movimientos_inventario (id_producto, tipo_movimiento, cantidad, referencia_codigo)
SELECT p.id_producto, 'AJUSTE', p.stock_disponible, 'CATALOGO_MARCAS_2026'
FROM productos p
WHERE p.nombre_producto IN (
    'AMD Ryzen 5 5600 6 núcleos / 12 hilos',
    'AMD Ryzen 7 7800X3D 8 núcleos / 16 hilos',
    'Intel Core i5-13400F 10 núcleos / 16 hilos',
    'Intel Core i7-14700K 20 núcleos / 28 hilos',
    'Intel Core i5-12400F 6 núcleos / 12 hilos',
    'MSI GeForce RTX 4070 SUPER Ventus 2X 12GB',
    'Sapphire Pulse Radeon RX 7800 XT 16GB',
    'ASUS TUF Gaming B650-PLUS WIFI AM5',
    'MSI PRO B760M-A WIFI DDR5',
    'Corsair Vengeance RGB DDR5 32GB 6000MHz',
    'Samsung 990 PRO NVMe 1TB PCIe 4.0',
    'Samsung Odyssey G6 27 pulgadas 240Hz'
)
AND p.stock_disponible > 0
AND NOT EXISTS (
    SELECT 1 FROM movimientos_inventario m
    WHERE m.id_producto=p.id_producto AND m.referencia_codigo='CATALOGO_MARCAS_2026'
);

-- ---------------------------------------------------------------------
-- 3. Clientes ficticios para exposición académica.
-- ---------------------------------------------------------------------
INSERT INTO clientes (tipo_documento, numero_documento, nombre_razon_social, apellido,
                      telefono, correo, direccion, password_hash, tiene_cuenta)
VALUES
('DNI','70451231','Juan','Pérez','999100201','juan.perez@demo.cherosac.local','Av. Demo 101, Lima',NULL,FALSE),
('DNI','71452362','María','Torres','999100202','maria.torres@demo.cherosac.local','Jr. Demo 202, Lima',NULL,FALSE),
('DNI','72563473','Carlos','Rojas','999100203','carlos.rojas@demo.cherosac.local','Av. Tecnología 303, Lima',NULL,FALSE),
('DNI','73674584','Lucía','Gómez','999100204','lucia.gomez@demo.cherosac.local','Calle Hardware 404, Lima',NULL,FALSE),
('DNI','74785695','Diego','Castillo','999100205','diego.castillo@demo.cherosac.local','Jr. Circuito 505, Lima',NULL,FALSE),
('DNI','75896706','Ana','Morales','999100206','ana.morales@demo.cherosac.local','Av. Procesadores 606, Lima',NULL,FALSE),
('DNI','76907817','Luis','Vargas','999100207','luis.vargas@demo.cherosac.local','Calle Gamer 707, Lima',NULL,FALSE),
('DNI','77018928','Valeria','Campos','999100208','valeria.campos@demo.cherosac.local','Av. Periféricos 808, Lima',NULL,FALSE),
('DNI','78029039','José','Luján','999100209','jose.lujan@demo.cherosac.local','Jr. Componentes 909, Lima',NULL,FALSE),
('DNI','79130140','Mariana','Ríos','999100210','mariana.rios@demo.cherosac.local','Av. Sistemas 100, Lima',NULL,FALSE)
ON CONFLICT (tipo_documento, numero_documento) DO NOTHING;

-- La etiqueta DEMO queda en una columna propia. Los pedidos de muestra no
-- descuentan inventario y la lógica Java evita reponer stock al cancelarlos.
CREATE TEMP TABLE tmp_demo_pedidos (
    codigo VARCHAR(20) PRIMARY KEY,
    dni VARCHAR(8) NOT NULL,
    metodo VARCHAR(30) NOT NULL,
    estado VARCHAR(15) NOT NULL,
    horas_atras INTEGER NOT NULL
) ON COMMIT DROP;

INSERT INTO tmp_demo_pedidos VALUES
('CH-2026-900001','70451231','Yape/Plin','PENDIENTE',2),
('CH-2026-900002','71452362','Tarjeta','PENDIENTE',5),
('CH-2026-900003','72563473','Transferencia','CONFIRMADO',24),
('CH-2026-900004','73674584','Tarjeta','CONFIRMADO',32),
('CH-2026-900005','74785695','Yape/Plin','ENVIADO',48),
('CH-2026-900006','75896706','Efectivo','ENVIADO',66),
('CH-2026-900007','76907817','Tarjeta','ENTREGADO',80),
('CH-2026-900008','77018928','Transferencia','ENTREGADO',96),
('CH-2026-900009','78029039','Yape/Plin','ENTREGADO',132),
('CH-2026-900010','79130140','Efectivo','CANCELADO',40);

CREATE TEMP TABLE tmp_demo_lineas (
    codigo VARCHAR(20) NOT NULL,
    producto VARCHAR(150) NOT NULL,
    cantidad INTEGER NOT NULL CHECK (cantidad > 0),
    PRIMARY KEY (codigo, producto)
) ON COMMIT DROP;

INSERT INTO tmp_demo_lineas VALUES
('CH-2026-900001','AMD Ryzen 5 5600 6 núcleos / 12 hilos',1),
('CH-2026-900001','MSI GeForce RTX 4060 Ventus 2X 8GB',1),
('CH-2026-900002','Laptop Lenovo IdeaPad Slim 3',1),
('CH-2026-900002','LG UltraWide 29WQ600 29 pulgadas IPS',1),
('CH-2026-900003','Intel Core i5-13400F 10 núcleos / 16 hilos',1),
('CH-2026-900003','MSI PRO B760M-A WIFI DDR5',1),
('CH-2026-900004','AMD Ryzen 7 7800X3D 8 núcleos / 16 hilos',1),
('CH-2026-900004','ASUS TUF Gaming B650-PLUS WIFI AM5',1),
('CH-2026-900004','Kingston FURY Beast DDR5 32GB (2x16GB)',1),
('CH-2026-900005','MSI GeForce RTX 4070 SUPER Ventus 2X 12GB',1),
('CH-2026-900005','Samsung 990 PRO NVMe 1TB PCIe 4.0',1),
('CH-2026-900006','HyperX Cloud III Gaming Headset',1),
('CH-2026-900006','Logitech G502 HERO 25K',1),
('CH-2026-900007','ASUS TUF Gaming F15 RTX 4060 15.6 pulgadas',1),
('CH-2026-900007','Samsung Odyssey G6 27 pulgadas 240Hz',1),
('CH-2026-900008','PC Gamer Ryzen 7 5700X con RTX 4060 y RGB',1),
('CH-2026-900008','TP-Link Archer AX55 Wi-Fi 6',1),
('CH-2026-900009','Intel Core i7-14700K 20 núcleos / 28 hilos',1),
('CH-2026-900009','MSI PRO B760M-A WIFI DDR5',1),
('CH-2026-900009','Corsair Vengeance RGB DDR5 32GB 6000MHz',1),
('CH-2026-900010','Corsair Vengeance RGB DDR5 32GB 6000MHz',1),
('CH-2026-900010','Corsair RM750e 750W 80 Plus Gold',1);

DO $$
DECLARE faltantes TEXT;
BEGIN
    SELECT string_agg(DISTINCT l.producto, ', ')
      INTO faltantes
      FROM tmp_demo_lineas l
      LEFT JOIN productos p ON lower(p.nombre_producto)=lower(l.producto)
     WHERE p.id_producto IS NULL;
    IF faltantes IS NOT NULL THEN
        RAISE EXCEPTION 'No se encontraron productos requeridos por los pedidos DEMO: %', faltantes;
    END IF;
END $$;

WITH totales AS (
    SELECT d.codigo, d.dni, d.metodo, d.estado, d.horas_atras,
           SUM(p.precio * l.cantidad)::NUMERIC(10,2) AS total
    FROM tmp_demo_pedidos d
    JOIN tmp_demo_lineas l ON l.codigo=d.codigo
    JOIN productos p ON lower(p.nombre_producto)=lower(l.producto)
    GROUP BY d.codigo, d.dni, d.metodo, d.estado, d.horas_atras
)
INSERT INTO pedidos (codigo_seguimiento, id_cliente, id_metodo_pago, id_usuario,
                     fecha_pedido, estado, subtotal, igv, total, observaciones, es_demo)
SELECT t.codigo, c.id_cliente, mp.id_metodo_pago,
       COALESCE(
           (SELECT u.id_usuario FROM usuarios u WHERE u.rol='DUENO' AND u.activo ORDER BY u.id_usuario LIMIT 1),
           (SELECT u.id_usuario FROM usuarios u WHERE u.activo ORDER BY u.id_usuario LIMIT 1)
       ),
       CURRENT_TIMESTAMP - make_interval(hours => t.horas_atras),
       t.estado,
       ROUND(t.total / 1.18, 2),
       t.total - ROUND(t.total / 1.18, 2),
       t.total,
       'DEMO: Registro de muestra para exposición académica. No representa una venta real ni modifica el stock.',
       TRUE
FROM totales t
JOIN clientes c ON c.tipo_documento='DNI' AND c.numero_documento=t.dni
JOIN metodos_pago mp ON mp.tipo_pago=t.metodo
ON CONFLICT (codigo_seguimiento) DO NOTHING;

INSERT INTO detalle_pedidos (id_pedido, id_producto, cantidad, precio_unitario, costo_unitario)
SELECT pe.id_pedido, p.id_producto, l.cantidad, p.precio, p.costo_unitario
FROM tmp_demo_lineas l
JOIN pedidos pe ON pe.codigo_seguimiento=l.codigo AND pe.es_demo=TRUE
JOIN productos p ON lower(p.nombre_producto)=lower(l.producto)
WHERE NOT EXISTS (
    SELECT 1 FROM detalle_pedidos d
    WHERE d.id_pedido=pe.id_pedido AND d.id_producto=p.id_producto
);

COMMIT;

-- Comprobación final: debe mostrar 10 pedidos de demostración y sus estados.
SELECT estado, COUNT(*) AS cantidad
FROM pedidos
WHERE es_demo=TRUE
GROUP BY estado
ORDER BY CASE estado
    WHEN 'PENDIENTE' THEN 1 WHEN 'CONFIRMADO' THEN 2 WHEN 'ENVIADO' THEN 3
    WHEN 'ENTREGADO' THEN 4 WHEN 'CANCELADO' THEN 5 ELSE 6 END;
