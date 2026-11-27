\encoding UTF8
-- CHERO.SAC - Catálogo ampliado a 36 productos.
-- Ejecutar conectado a chero_sac DESPUÉS de migrar_desde_original.sql.
-- No borra ni reinicia las tablas existentes. Puede ejecutarse más de una vez.
BEGIN;

-- Sustituir las tarjetas gráficas antiguas por imágenes locales específicas del producto.
UPDATE productos SET imagen_url='/img/productos/lenovo-ideapad-slim-3-foto.png' WHERE nombre_producto='Laptop Lenovo IdeaPad Slim 3';
UPDATE productos SET imagen_url='/img/productos/asus-vivobook-15-foto.png' WHERE nombre_producto='Laptop ASUS Vivobook 15';
UPDATE productos SET imagen_url='/img/productos/pc-gamer-rtx-3060-foto.png' WHERE nombre_producto='PC de escritorio Gamer Ryzen 5 / RTX 3060';
UPDATE productos SET imagen_url='/img/productos/hp-all-in-one-24-foto.svg' WHERE nombre_producto='All in One HP 24"';
UPDATE productos SET imagen_url='/img/productos/monitor-165hz-foto.png' WHERE nombre_producto='Monitor Gamer 24" 165 Hz';
UPDATE productos SET imagen_url='/img/productos/teclado-mecanico-foto.png' WHERE nombre_producto='Teclado mecanico Redragon Kumara';
UPDATE productos SET imagen_url='/img/productos/mouse-g203-foto.png' WHERE nombre_producto='Mouse Logitech G203 Lightsync';
UPDATE productos SET imagen_url='/img/productos/ram-ddr4-16gb-foto.svg' WHERE nombre_producto='Memoria RAM Kingston Fury 16 GB DDR4 3200 MHz';
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

-- Registrar el stock inicial de los productos incorporados al Kardex sin duplicar movimientos.
INSERT INTO movimientos_inventario (id_producto, tipo_movimiento, cantidad, referencia_codigo)
SELECT p.id_producto, 'AJUSTE', p.stock_disponible, 'CATALOGO_INICIAL_V2'
FROM productos p
WHERE p.nombre_producto IN (
    'Laptop Gamer 15.6 pulgadas RTX 4060',
    'Laptop Ultrabook 14 pulgadas Core i5',
    'PC Gamer Ryzen 7 con RGB',
    'PC Compacta Core i5 para oficina',
    'Monitor UltraWide 29 pulgadas IPS',
    'Monitor Gamer Curvo 27 pulgadas 165Hz',
    'Teclado inalámbrico compacto',
    'Teclado mecánico RGB TKL',
    'Mouse ergonómico inalámbrico',
    'Mouse Gamer RGB 12K DPI',
    'Headset Gamer RGB con micrófono',
    'Audífonos inalámbricos Over-Ear',
    'Webcam Full HD 1080p',
    'Micrófono USB de condensador',
    'Control inalámbrico para PC',
    'Router Wi-Fi 6 doble banda',
    'Placa madre B550M AM4',
    'Tarjeta gráfica RTX 4060 8GB',
    'Procesador AMD Ryzen 7 5700X',
    'Kit RAM DDR5 32GB (2x16GB)',
    'SSD NVMe 1TB PCIe 4.0',
    'SSD externo USB-C 1TB',
    'Fuente modular 750W 80+ Gold',
    'Gabinete Gamer Mid Tower RGB',
    'Cooler CPU torre ARGB',
    'Refrigeración líquida 240mm',
    'Dock USB-C 8 en 1',
    'Base refrigeradora para laptop'
)
AND p.stock_disponible > 0
AND NOT EXISTS (
    SELECT 1 FROM movimientos_inventario m
    WHERE m.id_producto = p.id_producto AND m.referencia_codigo = 'CATALOGO_INICIAL_V2'
);
COMMIT;
