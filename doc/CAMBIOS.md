# Cambios en la rama GerGia: interfaces de controladores, Swagger y los 10 CU

Esta rama documenta en Swagger/OpenAPI los **10 casos de uso** del módulo Comercios y Catálogo, en **16 endpoints**, y deja **implementada la lógica de los 10**, con tests.

La fuente de verdad sigue siendo el documento de la Primera Entrega. Lo nuevo se numera a continuación de lo que ya había: API-06 en adelante y DM-06 en adelante.

## Cómo verlo

1. Levantar la app: `cd demo` y después `./mvnw clean spring-boot:run`. Si el IDE avisa que cambió el `pom.xml`, recargar Maven.
2. Abrir **http://localhost:8082/swagger-ui/index.html**. La base arranca con los datos de prueba de `data.sql` (ver más abajo).
3. La especificación en YAML está en [`doc/openapi.yaml`](openapi.yaml). También se puede ver en vivo en `http://localhost:8082/v3/api-docs.yaml`, o pegarla en [editor.swagger.io](https://editor.swagger.io).
4. Tests: `./mvnw test`.

La app escucha en el puerto **8082** por defecto. Tres variables de entorno permiten cambiar la configuración sin tocar el código; si no se definen, se usan los valores por defecto de `application.properties`:

| Variable | Para qué | Valor por defecto |
| --- | --- | --- |
| `SERVER_PORT` | Puerto HTTP | `8082` |
| `JWT_SECRET` | Clave HMAC con la que el módulo Usuarios firma los tokens (DM-17). Mínimo 32 bytes: si es más corta, la app no arranca | La clave de desarrollo, con la que funcionan los tokens de prueba de abajo |
| `CORS_ALLOWED_ORIGINS` | Orígenes del front habilitados, separados por coma (DM-19) | `http://localhost:5173,http://localhost:5174` |

Por ejemplo: `SERVER_PORT=9090 JWT_SECRET=una-clave-de-al-menos-32-bytes-123 ./mvnw spring-boot:run`.

Para probar los endpoints protegidos hace falta un token JWT. Se carga con el botón **Authorize** de Swagger, y hay tres de prueba en la sección siguiente. El arranque entre máquinas virtuales, la configuración del JWT y ejemplos de consumo para Portal y Pedidos están en el [`README.md`](../README.md).

## Paso a paso para probar

### Preparación

1. `cd demo` y `./mvnw clean spring-boot:run`. Cuando la consola diga `Started DemoApplication`, está lista.
2. Abrir http://localhost:8082/swagger-ui/index.html.
3. Cada vez que arranca, la base se carga desde cero con los datos de `data.sql`. Si una prueba cambió algo, reiniciar la app lo deja como al principio.

### Tokens de prueba

El módulo Usuarios todavía no existe, así que estos tokens están firmados con la clave de desarrollo, el valor por defecto de `app.jwt.secret` (DM-17). Si se define otra `JWT_SECRET`, dejan de servir. No vencen.

- **ADMIN**:
  `eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiJwcnVlYmEtYWRtaW4iLCJyb2xlIjoiQURNSU4ifQ.jXvvRRZMakLjcEvXkDGB8wnlzHTW7Y5171IWoIOKuFg`
- **COMERCIO** (`prueba-comercio`, dueño de La Esquina y Sushi Cerrado):
  `eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiJwcnVlYmEtY29tZXJjaW8iLCJyb2xlIjoiQ09NRVJDSU8ifQ.qve-eikFzYL1osRC0I9wM53gLvPc56HXWSYB4SmPAMA`
- **COMERCIO 2** (`prueba-comercio-2`, dueño de Don Pietro y Helados del Sur):
  `eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiJwcnVlYmEtY29tZXJjaW8tMiIsInJvbGUiOiJDT01FUkNJTyJ9.Xz1vR2OJT51raJ7FF5y3ssZRjxQqdrjCKcQRoVSvtmE`

Un COMERCIO solo puede modificar sus propios comercios, y los productos y sucursales de esos comercios (DM-21). En la tabla de abajo, "COMERCIO" es el primero salvo que diga "COMERCIO 2".

En Swagger: botón **Authorize** (arriba a la derecha) → pegar el token solo, sin la palabra "Bearer" → Authorize → Close. Para cambiar de rol: Authorize → Logout → pegar el otro.

### Datos con los que arranca la base

| id | Comercio | Responsable (`sub`) | Para qué sirve |
|---|---|---|---|
| 1 | La Esquina | `prueba-comercio` | Comercio activo con 3 sucursales |
| 2 | Don Pietro | `prueba-comercio-2` | Comercio activo con 1 sucursal |
| 3 | Helados del Sur | `prueba-comercio-2` | Comercio activo |
| 4 | Sushi Cerrado SRL | `prueba-comercio` | **INACTIVO**: para probar RN-02 y RN-05 |

Productos útiles: **1** Hamburguesa clásica (normal), **6** Brownie (no disponible), **10** Fainá (INACTIVO), **13** Roll California (de un comercio inactivo).

Categorías: 1 Hamburguesas, 2 Pizzas, 3 Bebidas, 4 Postres, 5 Vegano, 6 Sin TACC.

### Pruebas por caso de uso

Seguir el orden de la tabla. Algunos pasos cambian datos que usa el paso siguiente.

| # | CU | Token | Endpoint en Swagger | Qué cargar | Qué tiene que pasar |
|---|---|---|---|---|---|
| 1 | CU-05 | ninguno | `GET /api/v1/catalogo` | nada | 200, `total: 9`. No aparecen Brownie, Fainá ni Roll California (RN-05) |
| 2 | CU-05 | ninguno | `GET /api/v1/catalogo` | categoriaId `4` y `6` (botón "Add item") | 200, 4 productos; Helado de chocolate aparece una sola vez |
| 3 | CU-05 | ninguno | `GET /api/v1/catalogo` | precioMin `5000`, precioMax `1000` | 400 "precioMin no puede ser mayor que precioMax" |
| 4 | CU-06 | ninguno | `GET /api/v1/productos/{id}` | id `10` | 404: está INACTIVO, se comporta como si no existiera |
| 5 | Seguridad | ninguno | `PATCH /api/v1/productos/{id}/precio` | id `1` | **401** "Se requiere un token válido…" |
| 6 | CU-01 | COMERCIO | `POST /api/v1/comercios` | `{"cuit":"30712345678","razonSocial":"Otra","direccion":"X 1","rubro":"Comidas"}` | 409: el CUIT ya existe (RN-01) |
| 7 | CU-02 | COMERCIO | `POST /api/v1/productos` | `{"nombre":"Papas fritas","descripcion":"Porción","precio":4500,"categoriaIds":[1],"comercioId":1}` | 201, `estado: ACTIVO` |
| 8 | CU-02 | COMERCIO | `POST /api/v1/productos` | lo mismo, con `"comercioId":4` | 404: el comercio está inactivo (RN-02) |
| 9 | CU-03 | COMERCIO | `PATCH /api/v1/productos/{id}/precio` | id `2`, `{"nuevoPrecio":9999}` | 200 con el precio nuevo |
| 10 | CU-03 | COMERCIO | `PATCH /api/v1/productos/{id}/precio` | id `13`, `{"nuevoPrecio":1}` | 409: el comercio del producto está inactivo |
| 11 | CU-04 | COMERCIO | `PATCH /api/v1/productos/{id}/disponibilidad` | id `2`, `{"disponible":false}` | 200; si se repite el paso 1, ya no aparece Doble cheddar |
| 12 | CU-09 | COMERCIO | `PATCH /api/v1/productos/{id}/estado` | id `1`, `{"activo":false}` | 200 `estado: INACTIVO`; `GET /productos/1` ahora da 404 |
| 13 | CU-07 | ninguno | `GET /api/v1/comercios/{comercioId}/sucursales` | comercioId `1` | 200 con 3 sucursales |
| 14 | CU-07 | COMERCIO | `POST /api/v1/comercios/{comercioId}/sucursales` | comercioId `4`, `{"direccion":"Calle 1"}` | 409: el comercio está inactivo |
| 15 | CU-07 | COMERCIO 2 | `DELETE /api/v1/comercios/{comercioId}/sucursales/{id}` | comercioId `2`, id `1` | 404: la sucursal 1 no es de Don Pietro. Con el token COMERCIO da 403, porque Don Pietro no es suyo (DM-21) |
| 16 | CU-08 | COMERCIO | `POST /api/v1/categorias` | `{"nombre":"Wraps"}` | **403**: el rol no alcanza (DM-15) |
| 17 | CU-08 | ADMIN | `POST /api/v1/categorias` | `{"nombre":"Wraps"}` | 201 |
| 18 | CU-08 | ADMIN | `DELETE /api/v1/categorias/{id}` | id `1` | 409: tiene productos asociados (DM-13) |
| 19 | CU-10 | COMERCIO | `PUT /api/v1/comercios/{id}` | id `1`, `{"razonSocial":"La Esquina Burgers","direccion":"Av. Siempre Viva 742","rubro":"Hamburguesería"}` | 200 con la razón social nueva; el CUIT no cambia |
| 20 | API-14 | ninguno | `GET /api/v1/comercios/{id}` | id `4`; después id `999` | 200 con `estado: INACTIVO` (DM-18); con `999`, 404 |
| 21 | CU-08 | ADMIN | `POST /api/v1/categorias` | `{"nombre":"  bebidas "}` | 409: ya existe "Bebidas", sin distinguir mayúsculas ni espacios (DM-20) |
| 22 | DM-21 | COMERCIO 2 | `PATCH /api/v1/productos/{id}/precio` | id `1`, `{"nuevoPrecio":1}` | **403** "El comercio 1 no pertenece al usuario autenticado"; el precio no cambia |
| 23 | DM-21 | COMERCIO 2 | `PUT /api/v1/comercios/{id}` | id `1`, cualquier cuerpo válido | **403**; con id `2` da 200 |
| 24 | API-15 | COMERCIO 2 | `GET /api/v1/comercios/mios` | nada | 200 con Don Pietro y Helados del Sur |
| 25 | API-16 | ninguno | `GET /api/v1/productos/disponibilidad` | ids `1,6,10,13,999` | 200, `todosDisponibles: false`; motivos DISPONIBLE, NO_DISPONIBLE, PRODUCTO_INACTIVO, COMERCIO_INACTIVO e INEXISTENTE |
| 26 | API-16 | COMERCIO | `PATCH /productos/2/disponibilidad` con `{"disponible":false}`, y repetir el paso 25 con ids `1,2` | | El producto 2 pasa a `NO_DISPONIBLE`: Pedidos no puede confirmar un pedido que lo tenga |

Todos los errores tienen el mismo formato (`ErrorResponseDTO`): `timestamp`, `status`, `error`, `message` y `path`.

### Sin la app levantada

- `./mvnw test` corre los 130 tests. Tiene que terminar en `BUILD SUCCESS`.
- `doc/openapi.yaml` se puede pegar en https://editor.swagger.io para ver la documentación sin correr nada.

## Endpoints

| API | Verbo y ruta | CU | Token |
| --- | --- | --- | --- |
| API-01 | `POST /api/v1/comercios` | CU-01 Registrar comercio | ADMIN o COMERCIO |
| API-02 | `POST /api/v1/productos` | CU-02 Publicar producto | ADMIN o COMERCIO |
| API-03 | `PATCH /api/v1/productos/{id}/precio` | CU-03 Modificar precio | ADMIN o COMERCIO |
| API-04 | `PATCH /api/v1/productos/{id}/disponibilidad` | CU-04 Cambiar disponibilidad | ADMIN o COMERCIO |
| API-05 | `GET /api/v1/catalogo` | CU-05 Buscar catálogo | No |
| API-06 | `GET /api/v1/productos/{id}` | CU-06 Consultar detalle de producto | No |
| API-07 | `GET /api/v1/comercios/{comercioId}/sucursales` | CU-07 Administrar sucursales | No |
| API-08 | `POST /api/v1/comercios/{comercioId}/sucursales` | CU-07 | ADMIN o COMERCIO |
| API-09 | `PUT` y `DELETE /api/v1/comercios/{comercioId}/sucursales/{id}` | CU-07 | ADMIN o COMERCIO |
| API-10 | `GET /api/v1/categorias` | CU-08 Administrar categorías | No |
| API-11 | `POST /api/v1/categorias`, `PUT` y `DELETE /api/v1/categorias/{id}` | CU-08 | ADMIN |
| API-12 | `PATCH /api/v1/productos/{id}/estado` | CU-09 Desactivar/reactivar producto | ADMIN o COMERCIO |
| API-13 | `PUT /api/v1/comercios/{id}` | CU-10 Modificar info. de comercio | ADMIN o COMERCIO |
| API-14 | `GET /api/v1/comercios/{id}` | Consultar comercio, para otros módulos (INT-03) | No |
| API-15 | `GET /api/v1/comercios/mios` | Comercios del usuario del token, para el Portal (DM-21) | ADMIN o COMERCIO |
| API-16 | `GET /api/v1/productos/disponibilidad?ids=1,2` | Verificar disponibilidad y precio vigente, para Pedidos (INT-02, DM-22) | No |

Las escrituras sobre productos exigen ADMIN o COMERCIO (DM-16). Antes alcanzaba con cualquier usuario autenticado, porque caían en la regla general `anyRequest().authenticated()`, que sigue al final de `SecurityConfig` para las rutas sin regla propia.

**Roles:** el token trae el rol en el claim `role`, y `JwtAuthenticationFilter` lo convierte en `ROLE_<rol>`. Los valores que usa el código son `ADMIN` y `COMERCIO`; un token con otro rol (por ejemplo `CLIENTE`) recibe 403 en los endpoints que piden rol. El actor "Comerciante" de la Primera Entrega corresponde al rol técnico `COMERCIO`.

## Qué se agregó

- **Dependencia** `springdoc-openapi-starter-webmvc-ui` 2.8.17 en el `pom.xml`.
- **`Config/OpenApiConfig`**: título y descripción de la API, y el esquema `bearerAuth` del token.
- **Interfaces `*Api` en `Controller/`**: `ComercioApi`, `ProductoApi`, `CatalogoApi`, `SucursalApi` y `CategoriaApi`. Cada método tiene la ruta, el verbo, `@Operation` y un `@ApiResponse` por código, todo trazado a CU, RN y DM. **Lo que ven en Swagger sale de acá.**
- **Controladores** que implementan esas interfaces y solo delegan en los Services.
- **Services** (`Service/`) con la lógica de los 10 CU (ver "CU implementados").
- **DTOs de entrada nuevos**: `ModificarComercioRequestDTO` (sin CUIT, por RN-01), `SucursalRequestDTO`, `CategoriaRequestDTO` y `CambiarEstadoProductoRequestDTO`.
- **`Producto.estado`** (`"ACTIVO"` / `"INACTIVO"`) para la baja lógica de CU-09, también expuesto en `ProductoResponseDTO`. Ver DM-12.
- **`GlobalExceptionHandler`**: los errores de validación, JSON mal formado y parámetro de tipo incorrecto ahora responden con `ErrorResponseDTO` (DM-08). También traduce la nueva `CategoriaEnUsoException` a 409 (DM-13).
- **`doc/openapi.yaml`**: la especificación en YAML. **Se regenera sola** con `./mvnw test`: el test `OpenApiYamlTest` le pide la especificación a springdoc y la escribe en el archivo. Si cambió un endpoint, el YAML queda modificado y listo para commitear. Para que salga siempre igual, `OpenApiConfig` fija la URL del servidor (`http://localhost:8082`) y `application.properties` ordena las claves (`springdoc.writer-with-order-by-keys=true`).

## Base para implementar los Services

- **`data.sql` con datos de prueba** (`demo/src/main/resources/`): se carga en cada arranque, después de que Hibernate crea las tablas (`spring.jpa.defer-datasource-initialization=true` en `application.properties`). Carga 6 categorías, 4 comercios, 4 sucursales y 13 productos, tomados del boceto de pantallas. Incluye casos para probar las reglas:

  | Dato | Para probar |
  | --- | --- |
  | Sushi Cerrado SRL, comercio INACTIVO, con el producto Roll California | RN-02 y RN-05 |
  | Fainá, producto INACTIVO | CU-09 y RN-05 |
  | Brownie de chocolate y Flan casero, con `disponible = false` | RN-05 |

  Las categorías hacen falta para publicar productos (RN-03).
- **`Mapper/DtoMapper`**: convierte entidades en DTOs de salida (`toComercioResponse`, `toProductoResponse`, `toCatalogoResponse`, `toCategoriaDTO`, `toSucursalDTO`). Así cada Service no repite el armado de DTOs.
- **`@Transactional`** en los cinco Services: a nivel de clase para las escrituras, con `readOnly = true` en las lecturas.
- **Test `DatosDePruebaTest`**: verifica que `data.sql` cargue completo y que cada producto tenga al menos una categoría (RN-03).

## CU implementados

| CU | Service | Qué hace | Tests |
| --- | --- | --- | --- |
| CU-01 Registrar comercio | `ComercioService.registrarComercio` | Busca el CUIT con `findByCuit` (REP-01): si existe, 409 (RN-01). Si no, guarda el comercio con estado ACTIVO y devuelve DTO-02. Si dos altas simultáneas usan el mismo CUIT, la restricción unique de la base también se informa como 409, no como 500. | `ComercioServiceTest` (3) |
| CU-02 Publicar producto | `ProductoService.publicarProducto` | Busca el comercio con `findByIdAndEstado(id, "ACTIVO")` (REP-02): si no existe o está inactivo, 404 (RN-02, DM-09). Busca las categorías con `findAllById` (REP-06): si falta alguna, 400 con los ids que no existen (RN-03); los ids repetidos cuentan una vez. Guarda el producto ACTIVO y disponible (REP-05) y devuelve DTO-04. | `ProductoServiceTest` (4) |
| CU-03 Modificar precio | `ProductoService.modificarPrecio` | Busca el producto con `findById` (REP-03): si no existe, 404. Verifica su comercio con `findByIdAndEstado` (REP-02): si está inactivo, 409 (RN-02, DM-09). Actualiza el precio (REP-05) y devuelve DTO-04. El precio > 0 (RN-04) lo valida el DTO. Ese paso común está en `buscarProductoDeComercioActivo`, que reusan CU-04 y CU-09. | `ProductoServiceTest` (3) |
| CU-04 Cambiar disponibilidad | `ProductoService.cambiarDisponibilidad` | Mismo paso común que CU-03 (404 si el producto no existe, 409 si su comercio está inactivo). Actualiza `disponible` (REP-05) y devuelve DTO-04. El campo es obligatorio en el DTO, así que un cuerpo vacío da 400 (DM-10). Un producto no disponible deja de salir en el catálogo (RN-05). | `ProductoServiceTest` (3) |
| CU-05 Buscar catálogo | `CatalogoService.buscarCatalogo` | Valida los criterios (DM-07): `precioMin` y `precioMax` no negativos, `precioMin ≤ precioMax`, `page ≥ 0` y `size` entre 1 y 100; si no, 400. Busca con `ProductoRepository.findByCriterios` (REP-04), que combina los filtros de `ProductoSpecifications` con `JpaSpecificationExecutor`. Siempre aplica RN-05 (producto ACTIVO y disponible, comercio ACTIVO), y suma un filtro por cada criterio informado: `texto` en nombre, descripción y razón social (sin distinguir mayúsculas), `categoriaId` repetible (con `distinct`, para no duplicar productos con varias categorías), `comercioId` y rango de precio. Ordena por nombre y pagina. Sin resultados devuelve 200 con la lista vacía. | `CatalogoServiceTest` (14, contra los datos de `data.sql`) |
| CU-06 Consultar detalle de producto | `ProductoService.obtenerProducto` | Devuelve el producto solo si está ACTIVO, disponible y su comercio está ACTIVO (RN-05). En cualquier otro caso responde 404 con el mismo mensaje que si no existiera, para no revelar productos ocultos. Endpoint público. | `ProductoServiceTest` (5) |
| CU-07 Administrar sucursales | `SucursalService` | **Listar:** el comercio tiene que existir (404); es una lectura pública. **Crear, modificar y borrar:** el comercio tiene que existir (404) y estar ACTIVO (409, RN-02). Para modificar y borrar, la sucursal tiene que pertenecer a ese comercio; si es de otro, responde 404 (REL-01). Se agregó `SucursalRepository.findByComercioId`. | `SucursalServiceTest` (9) |
| CU-08 Administrar categorías | `CategoriaService` | **Listar:** ordenado por nombre; es público. **Crear y modificar:** solo ADMIN (lo exige `SecurityConfig`); modificar una categoría inexistente da 404. Un nombre que ya existe da **409**, sin distinguir mayúsculas ni espacios exteriores; al modificar no cuenta la propia categoría, así que se puede guardar con el mismo nombre o cambiando solo mayúsculas (DM-20). El nombre se guarda sin espacios exteriores. Se agregaron `CategoriaRepository.existsByNombreIgnoreCase` y `existsByNombreIgnoreCaseAndIdNot`. **Borrar:** 404 si no existe, y **409** si algún producto la usa, incluidos los INACTIVOS (DM-13, RN-03). Se agregaron `ProductoRepository.existsByCategoriasId` y la excepción `CategoriaEnUsoException`. | `CategoriaServiceTest` (12) y `CategoriaRepositoryTest` (2, contra los datos de `data.sql`) |
| CU-09 Desactivar/reactivar producto | `ProductoService.cambiarEstado` | Mismo paso común que CU-03 (404 / 409). `activo: false` pasa `estado` a INACTIVO y `activo: true` lo vuelve a ACTIVO (DM-12). No toca `disponible`. Un producto INACTIVO deja de salir en el detalle y en el catálogo (RN-05), pero no se borra. | `ProductoServiceTest` (4) |
| CU-10 Modificar info. de comercio | `ComercioService.modificarComercio` | Busca el comercio con `findById`: si no existe, 404. Actualiza razón social, dirección y rubro y devuelve DTO-02. El CUIT no cambia (RN-01): no está en el DTO, si alguien lo manda en el JSON se ignora, y `Comercio` no tiene `setCuit`. La Primera Entrega asocia CU-10 solo con RN-01, así que también se puede modificar un comercio inactivo. | `ComercioServiceTest` (2) |

**API-14** (`ComercioService.obtenerComercio`, de solo lectura): busca el comercio con `findById` y devuelve DTO-02 (id, CUIT, razón social y estado). Si no existe, 404. A diferencia del catálogo, no filtra por estado: un comercio INACTIVO responde 200 con su estado (DM-18), porque Opiniones y Analítica lo usan para validar el comercio (INT-03). Tests: `ComercioServiceTest` (3) y `ComercioControllerTest` (4).

El token ya no es parámetro de `registrarComercio`: lo valida `SecurityConfig` antes de llegar al controlador (DM-06).

En total hay **130 tests**: 77 de los Services (unitarios con Mockito, salvo los de CU-05, que son de integración contra H2), 2 de `CategoriaRepositoryTest`, 3 de `DatosDePruebaTest`, `OpenApiYamlTest`, el de arranque de la aplicación, 4 de `JwtServiceTest`, 6 de `ControlDeAccesoTest`, 13 de `SeguridadProductosTest`, 12 de `PropiedadDelComercioTest`, 4 de `ComercioControllerTest`, 1 de `StatusControllerTest` y 6 de `FlujoPedidoDisponibilidadTest`. Los que usan MockMvc generan sus tokens con jjwt y la misma clave de `app.jwt.secret`.

## Cambios en la seguridad (`Secutiry/SecurityConfig.java`)

- **Rutas de Swagger públicas**: `/swagger-ui/**`, `/v3/api-docs/**` y `/v3/api-docs.yaml`.
- **`/error` público.** Antes, cualquier error 500 se veía como **403**: Spring redirige internamente a `/error` y esa ruta estaba bloqueada, así que un error del servidor parecía un problema de permisos.
- **Categorías**: el `GET` es público y el resto es solo para ADMIN (DM-02).
- **Sucursales**: el `DELETE` se permite a ADMIN y COMERCIO. Antes todo `DELETE` bajo `/comercios` era solo para ADMIN, y el comerciante no podía borrar sus propias sucursales. La regla específica va antes que la general.
- **401 y 403 con `ErrorResponseDTO` (DM-15)**: `JsonAuthenticationEntryPoint` responde **401** cuando falta el token, es inválido o está vencido, y `JsonAccessDeniedHandler` responde **403** cuando el token es válido pero el rol no alcanza. Se registran en `SecurityConfig` con `.exceptionHandling(...)`. Antes los dos casos daban 403 con el formato propio de Spring. En Swagger, los endpoints protegidos documentan el 401.
- **CORS con `PATCH`**: la lista de métodos permitidos no incluía `PATCH`, así que el front (`localhost:5173`) no podía llamar a API-03, API-04 ni API-12.
- **Escrituras de productos solo para ADMIN o COMERCIO (DM-16)**: `POST`, `PATCH`, `PUT` y `DELETE` sobre `/api/v1/productos/**`. Los `GET` siguen públicos. Las reglas van antes de `anyRequest()`. Sin token responde 401; con otro rol, 403.
- **Clave JWT por configuración (DM-17)**: `JwtService` recibe la clave por constructor desde `app.jwt.secret` (`JWT_SECRET`) en vez de tenerla como constante. Si tiene menos de 32 bytes, la app no arranca y lo dice en el mensaje. Los claims que lee no cambian: `sub` y `role`.
- **`GET /api/v1/comercios/{id}` público (DM-18)**: ya lo cubría la regla de `GET /api/v1/comercios/**`.
- **CORS configurable (DM-19)**: los orígenes salen de `app.cors.allowed-origins` (`CORS_ALLOWED_ORIGINS`); por defecto, `localhost:5173` y `localhost:5174`. Métodos y headers, igual que antes.

## Decisiones nuevas

| ID | Decisión | Motivo |
| --- | --- | --- |
| DM-11 | La interfaz de cada controlador se documenta con springdoc/OpenAPI: interfaces `*Api` anotadas que el controlador implementa, y la especificación se exporta a `doc/openapi.yaml`. | Lo pidió el docente. Al estar en el código no se desactualiza, y deja la anotación separada de la implementación. |
| DM-12 | `Producto` tiene un `estado` String controlado (`ACTIVO` / `INACTIVO`), distinto de `disponible`. | CU-09 necesita una baja lógica, distinta de la pausa de CU-04. Sigue el mismo criterio que `Comercio.estado` (DM-04), y el paso 6 de CU-02 ya preveía `estado = "ACTIVO"`. Por RN-05, el catálogo y el detalle muestran solo productos ACTIVOS. |
| DM-13 | Borrar una categoría con productos asociados responde 409. | Esos productos quedarían sin categoría, en contra de RN-03. |
| DM-14 | `/error` es público en `SecurityConfig`. | Sin eso, los errores 500 llegan al cliente como 403. |
| DM-15 | Sin token válido responde 401; con token válido pero rol insuficiente, 403. Ambos con `ErrorResponseDTO`. | Es la distinción estándar de HTTP: el cliente sabe si tiene que loguearse (401) o si no tiene permiso (403). Completa DM-08. |
| DM-16 | Publicar, modificar precio, cambiar disponibilidad y desactivar productos (CU-02, CU-03, CU-04, CU-09) exige rol ADMIN o COMERCIO. | Con `anyRequest().authenticated()` cualquier usuario logueado, por ejemplo un cliente, podía cambiar productos. Sigue el mismo criterio que comercios y sucursales. |
| DM-17 | La clave del JWT se toma de `app.jwt.secret` (variable `JWT_SECRET`), con un mínimo de 32 bytes. El valor por defecto es la clave de desarrollo. | La clave la define el módulo Usuarios y no puede quedar fija en el código de cada entorno. El mínimo es el que exige HS256; con el valor por defecto siguen andando los tokens de prueba. |
| DM-18 | `GET /api/v1/comercios/{id}` (API-14) es público y devuelve también comercios INACTIVOS, con su estado. | Lo consumen otros módulos (Opiniones, Analítica, INT-03) para validar un comercio: necesitan distinguir "no existe" (404) de "existe pero está inactivo" (200 con `estado`). El catálogo sigue ocultando los inactivos (RN-05). |
| DM-19 | Los orígenes de CORS se configuran con `app.cors.allowed-origins` (variable `CORS_ALLOWED_ORIGINS`), separados por coma. | Cada entorno tiene su propia URL de front; así no hay que recompilar para cambiarla. |
| DM-20 | El nombre de una categoría es único sin distinguir mayúsculas ni espacios exteriores: crear o modificar con un nombre que ya existe responde 409 (`RecursoDuplicadoException`). Al modificar no cuenta la propia categoría. El nombre se guarda sin espacios exteriores. | Dos categorías "Bebidas" y "bebidas " confunden el filtro del catálogo (CU-05) y la elección de categorías al publicar (RN-03). Es un conflicto con el estado actual, igual que el CUIT duplicado (RN-01). Guardar el nombre recortado hace que la comparación valga también contra lo ya guardado. |

## Devolución del docente: propiedad, integración y disponibilidad

| Punto | Qué se hizo |
| --- | --- |
| **Seguridad**: un comercio solo modifica lo suyo | `Comercio.usuarioResponsable` guarda el `sub` del token de quien lo registró. `Secutiry/ControlDeAcceso` lo compara con el usuario autenticado en todas las escrituras: CU-02, CU-03, CU-04, CU-07 (crear, modificar y borrar), CU-09 y CU-10. Si no coincide, **403**, aunque el rol sea COMERCIO. ADMIN puede todo. Tests: `ControlDeAccesoTest` (6), `PropiedadDelComercioTest` (12, MockMvc con tokens reales) y casos nuevos en los tests de los Services. |
| **Integración**: Portal y Pedidos consultan el catálogo real | `Config/RegistroDeConsultasFilter` deja en la consola una línea por request, con IP, ruta, status y el módulo que se identifica en `X-Modulo` (DM-23). Swagger usa `OPENAPI_SERVER_URL` para que "Try it out" funcione desde otra máquina virtual (DM-24). `X-Modulo` está habilitado en CORS. Contratos y ejemplos para cada módulo en el [`README.md`](../README.md). |
| **Disponibilidad** y su impacto en el pedido | API-16 (`GET /api/v1/productos/disponibilidad?ids=...`) dice, para cada producto, si se puede pedir, por qué no y el precio vigente (DM-22). Pedidos lo llama al crear y al confirmar. `Integracion/FlujoPedidoDisponibilidadTest` (6) prueba el flujo: se crea el pedido, el comercio pausa un producto, la confirmación falla y el producto sale del catálogo del Portal; al reactivarlo se puede confirmar otra vez. |
| **Git**: commit de disponibilidad en una rama lateral | Se revisaron todas las ramas del remoto (`GerGia`, `TotoRizzi`, `TomPedre`, `main`): no hay ningún commit fuera de `TomPedre`. El único commit cuyo mensaje habla de disponibilidad es `cc6dcd0` (lógica de CU-04), y ya está en `TomPedre` y en `main`. `TomPedre` tiene commits que todavía no están en `main` y se integran con un PR. |
| **Documentación** | `README.md` nuevo: arranque, variables, despliegue entre VMs, JWT (claims, clave, tokens de prueba y cómo generar otros) y ejemplos de consumo. |

### Decisiones nuevas

| ID | Decisión | Motivo |
| --- | --- | --- |
| DM-21 | Cada comercio tiene un `usuarioResponsable`: el `sub` del token de quien lo registró con rol COMERCIO, o el que indique un ADMIN en el alta (si no indica ninguno, queda sin responsable y solo lo administra un ADMIN). Las escrituras sobre el comercio, sus productos y sus sucursales exigen ser ese usuario o ADMIN; si no, 403. La propiedad se revisa después de la existencia (404) y antes del estado (409). `GET /api/v1/comercios/mios` (API-15) lista los del usuario. | Tener el rol correcto no debe permitir modificar cualquier comercio. Se usa el `sub` porque Usuarios ya lo emite: no hace falta un claim nuevo ni que Usuarios conozca los ids del catálogo. Se revisa antes del estado para no informarle a un tercero si el comercio ajeno está activo. |
| DM-22 | `GET /api/v1/productos/disponibilidad?ids=...` (API-16), público, devuelve por producto `disponible`, `motivo` (`DISPONIBLE`, `NO_DISPONIBLE`, `PRODUCTO_INACTIVO`, `COMERCIO_INACTIVO`, `INEXISTENTE`) y el precio vigente, más `todosDisponibles`. Hasta 100 ids. | CU-06 responde 404 por cualquier producto oculto, y Pedidos no puede distinguir "no existe" de "se pausó". Con una sola llamada por pedido valida todos los ítems al crear y al confirmar, y cobra el precio actual. |
| DM-23 | Un filtro registra cada request a `/api/` (logger `INTEGRACION`) con IP, módulo (`X-Modulo`, saneado), ruta y status. Se apaga con `LOG_CONSULTAS=false`. | Es la evidencia, del lado del catálogo, de que los otros módulos consultan los datos reales y no un mock. |
| DM-24 | La URL del servidor en OpenAPI sale de `app.openapi.server-url` (`OPENAPI_SERVER_URL`), por defecto `http://localhost:8082`. | Fija, Swagger abierto desde otra VM llamaba al `localhost` de esa VM. El valor por defecto mantiene estable `doc/openapi.yaml`. |

## Pendiente

Del lado del catálogo, nada. Para cerrar la integración, **Portal y Pedidos tienen que apuntar a la URL real del catálogo** (no a datos de ejemplo), y Pedidos tiene que llamar a API-16 al crear y al confirmar un pedido. Antes de subir algo que cambie un endpoint, correr `./mvnw test` para que `doc/openapi.yaml` quede actualizado.
