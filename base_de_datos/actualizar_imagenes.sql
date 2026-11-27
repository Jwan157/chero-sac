-- Cambia las imágenes de demostración a archivos locales (no borra datos).
-- Ejecutar conectado a la base chero_sac.

UPDATE productos SET imagen_url = '/img/productos/lenovo-ideapad-slim-3.png' WHERE imagen_url = 'https://placehold.co/600x400?text=Lenovo+IdeaPad+Slim+3';
UPDATE productos SET imagen_url = '/img/productos/asus-vivobook-15.png' WHERE imagen_url = 'https://placehold.co/600x400?text=ASUS+Vivobook+15';
UPDATE productos SET imagen_url = '/img/productos/pc-gamer-rtx-3060.png' WHERE imagen_url = 'https://placehold.co/600x400?text=PC+Gamer+RTX+3060';
UPDATE productos SET imagen_url = '/img/productos/hp-all-in-one-24.png' WHERE imagen_url = 'https://placehold.co/600x400?text=HP+All+in+One+24';
UPDATE productos SET imagen_url = '/img/productos/monitor-165hz.png' WHERE imagen_url = 'https://placehold.co/600x400?text=Monitor+165Hz';
UPDATE productos SET imagen_url = '/img/productos/teclado-mecanico.png' WHERE imagen_url = 'https://placehold.co/600x400?text=Teclado+Mecanico';
UPDATE productos SET imagen_url = '/img/productos/mouse-g203.png' WHERE imagen_url = 'https://placehold.co/600x400?text=Mouse+G203';
UPDATE productos SET imagen_url = '/img/productos/ram-ddr4-16gb.png' WHERE imagen_url = 'https://placehold.co/600x400?text=RAM+DDR4+16GB';

-- Sustituir las tarjetas gráficas antiguas por imágenes locales específicas del producto.
UPDATE productos SET imagen_url='/img/productos/lenovo-ideapad-slim-3-foto.png' WHERE nombre_producto='Laptop Lenovo IdeaPad Slim 3';
UPDATE productos SET imagen_url='/img/productos/asus-vivobook-15-foto.png' WHERE nombre_producto='Laptop ASUS Vivobook 15';
UPDATE productos SET imagen_url='/img/productos/pc-gamer-rtx-3060-foto.png' WHERE nombre_producto='PC de escritorio Gamer Ryzen 5 / RTX 3060';
UPDATE productos SET imagen_url='/img/productos/hp-all-in-one-24-foto.svg' WHERE nombre_producto='All in One HP 24"';
UPDATE productos SET imagen_url='/img/productos/monitor-165hz-foto.png' WHERE nombre_producto='Monitor Gamer 24" 165 Hz';
UPDATE productos SET imagen_url='/img/productos/teclado-mecanico-foto.png' WHERE nombre_producto='Teclado mecanico Redragon Kumara';
UPDATE productos SET imagen_url='/img/productos/mouse-g203-foto.png' WHERE nombre_producto='Mouse Logitech G203 Lightsync';
UPDATE productos SET imagen_url='/img/productos/ram-ddr4-16gb-foto.svg' WHERE nombre_producto='Memoria RAM Kingston Fury 16 GB DDR4 3200 MHz';
