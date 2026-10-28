# Cambios en la rama GerGia: interfaces de controladores, Swagger y los 10 CU

Esta rama documenta en Swagger/OpenAPI los **10 casos de uso** del módulo Comercios y Catálogo, en **16 endpoints**, y deja **implementada la lógica de los 10**, con tests.

La fuente de verdad sigue siendo el documento de la Primera Entrega. Lo nuevo se numera a continuación de lo que ya había: API-06 en adelante y DM-06 en adelante.

## Cómo verlo

1. Levantar la app: `cd demo` y después `./mvnw clean spring-boot:run`. Si el IDE avisa que cambió el `pom.xml`, recargar Maven.
2. Abrir **http://localhost:8080/swagger-ui/index.html**. La base arranca con los datos de prueba de `data.sql` (ver más abajo).
3. La especificación en YAML está en [`docs/openapi.yml`](openapi.yml). También se puede ver en vivo en `http://localhost:8080/v3/api-docs.yaml`, o pegarla en [editor.swagger.io](https://editor.swagger.io).
4. Tests: `./mvnw test`.

Para probar los endpoints protegidos hace falta un token JWT. Se carga con el botón **Authorize** de Swagger, y hay dos de prueba en la sección siguiente.

## Paso a paso para probar

### Preparación

1. `cd demo` y `./mvnw clean spring-boot:run`. Cuando la consola diga `Started DemoApplication`, está lista.
2. Abrir http://localhost:8080/swagger-ui/index.html.
3. Cada vez que arranca, la base se carga desde cero con los datos de `data.sql`. Si una prueba cambió algo, reiniciar la app lo deja como al principio.

### Tokens de prueba

El módulo Usuarios todavía no existe, así que estos tokens están firmados con la clave de `JwtService`. No vencen.

- **ADMIN**:
  `eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiJwcnVlYmEtYWRtaW4iLCJyb2xlIjoiQURNSU4ifQ.jXvvRRZMakLjcEvXkDGB8wnlzHTW7Y5171IWoIOKuFg`
- **COMERCIO**:
  `eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiJwcnVlYmEtY29tZXJjaW8iLCJyb2xlIjoiQ09NRVJDSU8ifQ.qve-eikFzYL1osRC0I9wM53gLvPc56HXWSYB4SmPAMA`

En Swagger: botón **Authorize** (arriba a la derecha) → pegar el token solo, sin la palabra "Bearer" → Authorize → Close. Para cambiar de rol: Authorize → Logout → pegar el otro.

### Datos con los que arranca la base

| id | Comercio | Para qué sirve |
|---|---|---|
| 1 | La Esquina | Comercio activo con 3 sucursales |
| 2 | Don Pietro | Comercio activo con 1 sucursal |
| 3 | Helados del Sur | Comercio activo |
| 4 | Sushi Cerrado SRL | **INACTIVO**: para probar RN-02 y RN-05 |

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
| 15 | CU-07 | COMERCIO | `DELETE /api/v1/comercios/{comercioId}/sucursales/{id}` | comercioId `2`, id `1` | 404: la sucursal 1 no es de Don Pietro |
| 16 | CU-08 | COMERCIO | `POST /api/v1/categorias` | `{"nombre":"Wraps"}` | **403**: el rol no alcanza (DM-15) |
| 17 | CU-08 | ADMIN | `POST /api/v1/categorias` | `{"nombre":"Wraps"}` | 201 |
| 18 | CU-08 | ADMIN | `DELETE /api/v1/categorias/{id}` | id `1` | 409: tiene productos asociados (DM-13) |
| 19 | CU-10 | COMERCIO | `PUT /api/v1/comercios/{id}` | id `1`, `{"razonSocial":"La Esquina Burgers","direccion":"Av. Siempre Viva 742","rubro":"Hamburguesería"}` | 200 con la razón social nueva; el CUIT no cambia |

Todos los errores tienen el mismo formato (`ErrorResponseDTO`): `timestamp`, `status`, `error`, `message` y `path`.

### Sin la app levantada

- `./mvnw test` corre los 59 tests. Tiene que terminar en `BUILD SUCCESS`.
- `docs/openapi.yml` se puede pegar en https://editor.swagger.io para ver la documentación sin correr nada.

## Endpoints

| API | Verbo y ruta | CU | Token |
| --- | --- | --- | --- |
| API-01 | `POST /api/v1/comercios` | CU-01 Registrar comercio | ADMIN o COMERCIO |
| API-02 | `POST /api/v1/productos` | CU-02 Publicar producto | Sí |
| API-03 | `PATCH /api/v1/productos/{id}/precio` | CU-03 Modificar precio | Sí |
| API-04 | `PATCH /api/v1/productos/{id}/disponibilidad` | CU-04 Cambiar disponibilidad | Sí |
| API-05 | `GET /api/v1/catalogo` | CU-05 Buscar catálogo | No |
| API-06 | `GET /api/v1/productos/{id}` | CU-06 Consultar detalle de producto | No |
| API-07 | `GET /api/v1/comercios/{comercioId}/sucursales` | CU-07 Administrar sucursales | No |
| API-08 | `POST /api/v1/comercios/{comercioId}/sucursales` | CU-07 | ADMIN o COMERCIO |
| API-09 | `PUT` y `DELETE /api/v1/comercios/{comercioId}/sucursales/{id}` | CU-07 | ADMIN o COMERCIO |
| API-10 | `GET /api/v1/categorias` | CU-08 Administrar categorías | No |
| API-11 | `POST /api/v1/categorias`, `PUT` y `DELETE /api/v1/categorias/{id}` | CU-08 | ADMIN |
| API-12 | `PATCH /api/v1/productos/{id}/estado` | CU-09 Desactivar/reactivar producto | Sí |
| API-13 | `PUT /api/v1/comercios/{id}` | CU-10 Modificar info. de comercio | ADMIN o COMERCIO |

"Sí" significa cualquier usuario autenticado, porque la regla general de `SecurityConfig` es `anyRequest().authenticated()`.

**Roles:** el token trae el rol en el claim `role`, y `JwtAuthenticationFilter` lo convierte en `ROLE_<rol>`. Los valores que usa el código son `ADMIN` y `COMERCIO`. El actor "Comerciante" de la Primera Entrega corresponde al rol técnico `COMERCIO`.

## Qué se agregó

- **Dependencia** `springdoc-openapi-starter-webmvc-ui` 2.8.17 en el `pom.xml`.
- **`Config/OpenApiConfig`**: título y descripción de la API, y el esquema `bearerAuth` del token.
- **Interfaces `*Api` en `Controller/`**: `ComercioApi`, `ProductoApi`, `CatalogoApi`, `SucursalApi` y `CategoriaApi`. Cada método tiene la ruta, el verbo, `@Operation` y un `@ApiResponse` por código, todo trazado a CU, RN y DM. **Lo que ven en Swagger sale de acá.**
- **Controladores** que implementan esas interfaces y solo delegan en los Services.
- **Services** (`Service/`) con la lógica de los 10 CU (ver "CU implementados").
- **DTOs de entrada nuevos**: `ModificarComercioRequestDTO` (sin CUIT, por RN-01), `SucursalRequestDTO`, `CategoriaRequestDTO` y `CambiarEstadoProductoRequestDTO`.
- **`Producto.estado`** (`"ACTIVO"` / `"INACTIVO"`) para la baja lógica de CU-09, también expuesto en `ProductoResponseDTO`. Ver DM-12.
- **`GlobalExceptionHandler`**: los errores de validación, JSON mal formado y parámetro de tipo incorrecto ahora responden con `ErrorResponseDTO` (DM-08). También traduce la nueva `CategoriaEnUsoException` a 409 (DM-13).
- **`docs/openapi.yml`**: la especificación en YAML. **Se regenera sola** con `./mvnw test`: el test `OpenApiYamlTest` le pide la especificación a springdoc y la escribe en el archivo. Si cambió un endpoint, el YAML queda modificado y listo para commitear. Para que salga siempre igual, `OpenApiConfig` fija la URL del servidor (`http://localhost:8080`) y `application.properties` ordena las claves (`springdoc.writer-with-order-by-keys=true`).

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
| CU-08 Administrar categorías | `CategoriaService` | **Listar:** ordenado por nombre; es público. **Crear y modificar:** solo ADMIN (lo exige `SecurityConfig`); modificar una categoría inexistente da 404. **Borrar:** 404 si no existe, y **409** si algún producto la usa, incluidos los INACTIVOS (DM-13, RN-03). Se agregaron `ProductoRepository.existsByCategoriasId` y la excepción `CategoriaEnUsoException`. | `CategoriaServiceTest` (7) |
| CU-09 Desactivar/reactivar producto | `ProductoService.cambiarEstado` | Mismo paso común que CU-03 (404 / 409). `activo: false` pasa `estado` a INACTIVO y `activo: true` lo vuelve a ACTIVO (DM-12). No toca `disponible`. Un producto INACTIVO deja de salir en el detalle y en el catálogo (RN-05), pero no se borra. | `ProductoServiceTest` (4) |
| CU-10 Modificar info. de comercio | `ComercioService.modificarComercio` | Busca el comercio con `findById`: si no existe, 404. Actualiza razón social, dirección y rubro y devuelve DTO-02. El CUIT no cambia (RN-01): no está en el DTO, y si alguien lo manda en el JSON se ignora. La Primera Entrega asocia CU-10 solo con RN-01, así que también se puede modificar un comercio inactivo. | `ComercioServiceTest` (2) |

El token ya no es parámetro de `registrarComercio`: lo valida `SecurityConfig` antes de llegar al controlador (DM-06).

En total hay **59 tests**: 54 de los Services (unitarios con Mockito, salvo los de CU-05, que son de integración contra H2), 3 de `DatosDePruebaTest`, `OpenApiYamlTest` y el de arranque de la aplicación.

## Cambios en la seguridad (`Secutiry/SecurityConfig.java`)

- **Rutas de Swagger públicas**: `/swagger-ui/**`, `/v3/api-docs/**` y `/v3/api-docs.yaml`.
- **`/error` público.** Antes, cualquier error 500 se veía como **403**: Spring redirige internamente a `/error` y esa ruta estaba bloqueada, así que un error del servidor parecía un problema de permisos.
- **Categorías**: el `GET` es público y el resto es solo para ADMIN (DM-02).
- **Sucursales**: el `DELETE` se permite a ADMIN y COMERCIO. Antes todo `DELETE` bajo `/comercios` era solo para ADMIN, y el comerciante no podía borrar sus propias sucursales. La regla específica va antes que la general.
- **401 y 403 con `ErrorResponseDTO` (DM-15)**: `JsonAuthenticationEntryPoint` responde **401** cuando falta el token, es inválido o está vencido, y `JsonAccessDeniedHandler` responde **403** cuando el token es válido pero el rol no alcanza. Se registran en `SecurityConfig` con `.exceptionHandling(...)`. Antes los dos casos daban 403 con el formato propio de Spring. En Swagger, los endpoints protegidos documentan el 401.
- **CORS con `PATCH`**: la lista de métodos permitidos no incluía `PATCH`, así que el front (`localhost:5173`) no podía llamar a API-03, API-04 ni API-12.

## Decisiones nuevas

| ID | Decisión | Motivo |
| --- | --- | --- |
| DM-11 | La interfaz de cada controlador se documenta con springdoc/OpenAPI: interfaces `*Api` anotadas que el controlador implementa, y la especificación se exporta a `docs/openapi.yml`. | Lo pidió el docente. Al estar en el código no se desactualiza, y deja la anotación separada de la implementación. |
| DM-12 | `Producto` tiene un `estado` String controlado (`ACTIVO` / `INACTIVO`), distinto de `disponible`. | CU-09 necesita una baja lógica, distinta de la pausa de CU-04. Sigue el mismo criterio que `Comercio.estado` (DM-04), y el paso 6 de CU-02 ya preveía `estado = "ACTIVO"`. Por RN-05, el catálogo y el detalle muestran solo productos ACTIVOS. |
| DM-13 | Borrar una categoría con productos asociados responde 409. | Esos productos quedarían sin categoría, en contra de RN-03. |
| DM-14 | `/error` es público en `SecurityConfig`. | Sin eso, los errores 500 llegan al cliente como 403. |
| DM-15 | Sin token válido responde 401; con token válido pero rol insuficiente, 403. Ambos con `ErrorResponseDTO`. | Es la distinción estándar de HTTP: el cliente sabe si tiene que loguearse (401) o si no tiene permiso (403). Completa DM-08. |

## Pendiente

Nada de lo planificado para esta rama. Antes de subir algo que cambie un endpoint, correr `./mvnw test` para que `docs/openapi.yml` quede actualizado.
