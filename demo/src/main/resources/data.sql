-- Datos de prueba: se cargan en cada arranque, después de que Hibernate crea las tablas
-- (spring.jpa.defer-datasource-initialization=true). La base H2 es en memoria y arranca vacía.
-- Sin ids fijos: las relaciones se resuelven por CUIT o por nombre para no chocar con la identidad.
-- Nombres y precios tomados del boceto de pantallas (PRIMER_VISTAZO_INTERFAZ).

-- Categorías del catálogo global (DM-02). Hacen falta para publicar productos (RN-03),
-- porque su ABM (CU-08) es solo para ADMIN
INSERT INTO categorias (nombre) VALUES ('Hamburguesas');
INSERT INTO categorias (nombre) VALUES ('Pizzas');
INSERT INTO categorias (nombre) VALUES ('Bebidas');
INSERT INTO categorias (nombre) VALUES ('Postres');
INSERT INTO categorias (nombre) VALUES ('Vegano');
INSERT INTO categorias (nombre) VALUES ('Sin TACC');

-- Comercios: tres activos y uno INACTIVO para probar RN-02 y RN-05
INSERT INTO comercios (cuit, razon_social, direccion, rubro, estado)
VALUES ('30712345678', 'La Esquina SRL', 'Av. Corrientes 1450, CABA', 'Hamburguesería', 'ACTIVO');
INSERT INTO comercios (cuit, razon_social, direccion, rubro, estado)
VALUES ('30712345679', 'Don Pietro SRL', 'Av. Cabildo 2100, CABA', 'Pizzería', 'ACTIVO');
INSERT INTO comercios (cuit, razon_social, direccion, rubro, estado)
VALUES ('30712345680', 'Helados del Sur SA', 'Av. Pueyrredón 900, CABA', 'Heladería', 'ACTIVO');
INSERT INTO comercios (cuit, razon_social, direccion, rubro, estado)
VALUES ('30712345681', 'Sushi Cerrado SRL', 'Av. Callao 300, CABA', 'Sushi', 'INACTIVO');

-- Sucursales (REL-01)
INSERT INTO sucursales (direccion, comercio_id)
SELECT 'Av. Corrientes 1450, CABA', id FROM comercios WHERE cuit = '30712345678';
INSERT INTO sucursales (direccion, comercio_id)
SELECT 'Av. Santa Fe 2800, CABA', id FROM comercios WHERE cuit = '30712345678';
INSERT INTO sucursales (direccion, comercio_id)
SELECT 'Av. Rivadavia 6200, CABA', id FROM comercios WHERE cuit = '30712345678';
INSERT INTO sucursales (direccion, comercio_id)
SELECT 'Av. Cabildo 2100, CABA', id FROM comercios WHERE cuit = '30712345679';

-- Productos (REL-02). Casos para probar las reglas:
--   Brownie de chocolate y Flan casero: disponible = false (no salen en el catálogo, RN-05)
--   Fainá: estado INACTIVO, dado de baja con CU-09 (no sale en el catálogo, RN-05)
--   Roll California: de un comercio INACTIVO (no sale en el catálogo y no se puede modificar, RN-02)
INSERT INTO productos (nombre, descripcion, precio, disponible, estado, comercio_id)
SELECT 'Hamburguesa clásica', 'Carne, queso, lechuga y tomate.', 8500.00, TRUE, 'ACTIVO', id FROM comercios WHERE cuit = '30712345678';
INSERT INTO productos (nombre, descripcion, precio, disponible, estado, comercio_id)
SELECT 'Doble cheddar', 'Doble carne y doble queso cheddar.', 10500.00, TRUE, 'ACTIVO', id FROM comercios WHERE cuit = '30712345678';
INSERT INTO productos (nombre, descripcion, precio, disponible, estado, comercio_id)
SELECT 'Hamburguesa de lentejas', 'Medallón de lentejas y vegetales.', 8000.00, TRUE, 'ACTIVO', id FROM comercios WHERE cuit = '30712345678';
INSERT INTO productos (nombre, descripcion, precio, disponible, estado, comercio_id)
SELECT 'Limonada de la casa', 'Limón fresco y un toque de menta.', 3200.00, TRUE, 'ACTIVO', id FROM comercios WHERE cuit = '30712345678';
INSERT INTO productos (nombre, descripcion, precio, disponible, estado, comercio_id)
SELECT 'Agua mineral', 'Agua sin gas, botella de 500 ml.', 2000.00, TRUE, 'ACTIVO', id FROM comercios WHERE cuit = '30712345678';
INSERT INTO productos (nombre, descripcion, precio, disponible, estado, comercio_id)
SELECT 'Brownie de chocolate', 'Chocolate semiamargo y nueces.', 4200.00, FALSE, 'ACTIVO', id FROM comercios WHERE cuit = '30712345678';
INSERT INTO productos (nombre, descripcion, precio, disponible, estado, comercio_id)
SELECT 'Gaseosa cola', 'Gaseosa cola, botella de 500 ml.', 2800.00, TRUE, 'ACTIVO', id FROM comercios WHERE cuit = '30712345679';
INSERT INTO productos (nombre, descripcion, precio, disponible, estado, comercio_id)
SELECT 'Tiramisú', 'Crema, café y cacao en una porción.', 5500.00, TRUE, 'ACTIVO', id FROM comercios WHERE cuit = '30712345679';
INSERT INTO productos (nombre, descripcion, precio, disponible, estado, comercio_id)
SELECT 'Flan casero', 'Flan de vainilla con dulce de leche.', 3800.00, FALSE, 'ACTIVO', id FROM comercios WHERE cuit = '30712345679';
INSERT INTO productos (nombre, descripcion, precio, disponible, estado, comercio_id)
SELECT 'Fainá', 'Porción de fainá al horno.', 1500.00, TRUE, 'INACTIVO', id FROM comercios WHERE cuit = '30712345679';
INSERT INTO productos (nombre, descripcion, precio, disponible, estado, comercio_id)
SELECT 'Helado de chocolate', 'Chocolate artesanal, pote de 250 g.', 6000.00, TRUE, 'ACTIVO', id FROM comercios WHERE cuit = '30712345680';
INSERT INTO productos (nombre, descripcion, precio, disponible, estado, comercio_id)
SELECT 'Helado de frutilla al agua', 'Frutillas naturales, pote de 250 g.', 5800.00, TRUE, 'ACTIVO', id FROM comercios WHERE cuit = '30712345680';
INSERT INTO productos (nombre, descripcion, precio, disponible, estado, comercio_id)
SELECT 'Roll California', 'Ocho piezas de roll California.', 7200.00, TRUE, 'ACTIVO', id FROM comercios WHERE cuit = '30712345681';

-- Producto ↔ Categoría (REL-03, DM-03): cada producto con al menos una categoría (RN-03)
INSERT INTO producto_categoria (producto_id, categoria_id)
SELECT p.id, c.id FROM productos p, categorias c WHERE p.nombre = 'Hamburguesa clásica' AND c.nombre = 'Hamburguesas';
INSERT INTO producto_categoria (producto_id, categoria_id)
SELECT p.id, c.id FROM productos p, categorias c WHERE p.nombre = 'Doble cheddar' AND c.nombre = 'Hamburguesas';
INSERT INTO producto_categoria (producto_id, categoria_id)
SELECT p.id, c.id FROM productos p, categorias c WHERE p.nombre = 'Hamburguesa de lentejas' AND c.nombre IN ('Hamburguesas', 'Vegano');
INSERT INTO producto_categoria (producto_id, categoria_id)
SELECT p.id, c.id FROM productos p, categorias c WHERE p.nombre = 'Limonada de la casa' AND c.nombre IN ('Bebidas', 'Vegano');
INSERT INTO producto_categoria (producto_id, categoria_id)
SELECT p.id, c.id FROM productos p, categorias c WHERE p.nombre = 'Agua mineral' AND c.nombre IN ('Bebidas', 'Sin TACC');
INSERT INTO producto_categoria (producto_id, categoria_id)
SELECT p.id, c.id FROM productos p, categorias c WHERE p.nombre = 'Brownie de chocolate' AND c.nombre = 'Postres';
INSERT INTO producto_categoria (producto_id, categoria_id)
SELECT p.id, c.id FROM productos p, categorias c WHERE p.nombre = 'Gaseosa cola' AND c.nombre = 'Bebidas';
INSERT INTO producto_categoria (producto_id, categoria_id)
SELECT p.id, c.id FROM productos p, categorias c WHERE p.nombre = 'Tiramisú' AND c.nombre = 'Postres';
INSERT INTO producto_categoria (producto_id, categoria_id)
SELECT p.id, c.id FROM productos p, categorias c WHERE p.nombre = 'Flan casero' AND c.nombre IN ('Postres', 'Sin TACC');
INSERT INTO producto_categoria (producto_id, categoria_id)
SELECT p.id, c.id FROM productos p, categorias c WHERE p.nombre = 'Fainá' AND c.nombre IN ('Pizzas', 'Vegano');
INSERT INTO producto_categoria (producto_id, categoria_id)
SELECT p.id, c.id FROM productos p, categorias c WHERE p.nombre = 'Helado de chocolate' AND c.nombre IN ('Postres', 'Sin TACC');
INSERT INTO producto_categoria (producto_id, categoria_id)
SELECT p.id, c.id FROM productos p, categorias c WHERE p.nombre = 'Helado de frutilla al agua' AND c.nombre IN ('Postres', 'Vegano');
INSERT INTO producto_categoria (producto_id, categoria_id)
SELECT p.id, c.id FROM productos p, categorias c WHERE p.nombre = 'Roll California' AND c.nombre = 'Sin TACC';
