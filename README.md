# DeliveryGo — Módulo Comercios y Catálogo (Grupo 07)

API REST en Spring Boot que administra comercios, sucursales, categorías y productos, y expone el catálogo que consumen el **Portal Web** y **Pedidos**. Valida los tokens JWT que emite el módulo **Usuarios**.

- Detalle de cada caso de uso, decisiones (DM-xx) y pruebas paso a paso: [`doc/CAMBIOS.md`](doc/CAMBIOS.md)
- Especificación OpenAPI: [`doc/openapi.yaml`](doc/openapi.yaml), o en vivo en `http://<host>:8082/swagger-ui/index.html`

## 1. Arranque

Requisitos: **Java 21**. Maven no hace falta: viene el wrapper `mvnw`.

```bash
cd demo
./mvnw clean spring-boot:run          # Linux / macOS / Git Bash
mvnw.cmd clean spring-boot:run        # Windows (cmd o PowerShell)
```

Está listo cuando la consola dice `Started DemoApplication`. Para comprobarlo:

```bash
curl http://localhost:8082/api/v1/status      # {"status":"ok"}
```

La base es **H2 en memoria**: cada arranque la crea de cero y carga los datos de prueba de [`data.sql`](demo/src/main/resources/data.sql). Si una prueba cambió datos, reiniciar la app los deja como al principio. La consola de H2 está en `/h2-console` (JDBC URL `jdbc:h2:mem:deliverygo`, usuario `sa`, sin contraseña).

Tests: `./mvnw test`. También regeneran `doc/openapi.yaml`.

Para dejar un jar y correrlo en otra máquina:

```bash
./mvnw clean package -DskipTests
java -jar target/demo-0.0.1-SNAPSHOT.jar
```

## 2. Configuración

Todo se configura con variables de entorno. Si no se definen, se usan los valores por defecto de `application.properties`.

| Variable | Para qué | Por defecto |
| --- | --- | --- |
| `SERVER_PORT` | Puerto HTTP | `8082` |
| `JWT_SECRET` | Clave HMAC compartida con Usuarios (ver sección 3). Mínimo 32 bytes: si es más corta, la app no arranca | Clave de desarrollo `DeliveryGo-Clave-Secreta-JWT-2026-UADE-API` |
| `CORS_ALLOWED_ORIGINS` | Orígenes del front (Portal) habilitados, separados por coma | `http://localhost:5173,http://localhost:5174` |
| `OPENAPI_SERVER_URL` | URL que usa Swagger para "Try it out" | `http://localhost:8082` |
| `LOG_CONSULTAS` | Una línea en la consola por cada request a `/api/` (ver sección 5) | `true` |

### Entre máquinas virtuales

Supongamos que el catálogo corre en la VM `192.168.56.10` y el Portal en `192.168.56.20:5173`:

```bash
# En la VM del catálogo
export SERVER_PORT=8082
export JWT_SECRET='la-misma-clave-que-usa-Usuarios-de-32-bytes-o-mas'
export CORS_ALLOWED_ORIGINS='http://192.168.56.20:5173'
export OPENAPI_SERVER_URL='http://192.168.56.10:8082'
cd demo && ./mvnw spring-boot:run
```

En PowerShell: `$env:JWT_SECRET='...'`, y lo mismo con las demás.

Chequeos:

- La app escucha en todas las interfaces. Desde otra VM: `curl http://192.168.56.10:8082/api/v1/status`.
- El firewall de la VM tiene que dejar pasar el puerto (por ejemplo `sudo ufw allow 8082/tcp`, o una regla de entrada en Windows).
- **CORS** solo afecta al navegador, es decir, al Portal. Pedidos llama de servidor a servidor y no lo necesita. Si el Portal recibe un error de CORS, su origen exacto (esquema, IP y puerto) tiene que estar en `CORS_ALLOWED_ORIGINS`.
- Sin `OPENAPI_SERVER_URL`, el "Try it out" de Swagger abierto desde otra máquina llama a `localhost` de esa máquina, no al catálogo.

## 3. JWT

El catálogo **no emite tokens**: valida los que firma Usuarios. Para que un token sirva:

| Qué | Valor |
| --- | --- |
| Algoritmo | `HS256` |
| Clave | La misma en Usuarios y en el catálogo (`JWT_SECRET`), de 32 bytes o más |
| Header HTTP | `Authorization: Bearer <token>` |
| Claim `sub` | Identificador del usuario. **Es el dueño de los comercios que registra** |
| Claim `role` | `ADMIN`, `COMERCIO` o cualquier otro (por ejemplo `CLIENTE`) |
| Claim `exp` | Opcional. Si está y ya pasó, el token se rechaza |

Respuestas: sin token, o con un token mal firmado o vencido, **401**. Con un token válido pero un rol que no alcanza, **403**. Las dos tienen el mismo formato JSON que el resto de los errores.

### Qué puede hacer cada rol

- **Sin token:** catálogo, detalle de producto, verificación de disponibilidad, categorías, comercio por id y sucursales (todos son `GET`).
- **`COMERCIO`:** registrar comercios. Sobre **sus propios comercios**, los que registró él (`sub` = responsable), también modificarlos, publicar productos, cambiar precio, disponibilidad y estado, y administrar sucursales. Sobre un comercio de otro usuario responde **403** "El comercio N no pertenece al usuario autenticado", aunque el rol sea correcto.
- **`ADMIN`:** todo lo anterior sobre cualquier comercio, más el ABM de categorías. Al registrar un comercio puede indicar el dueño con `"usuarioResponsable": "<sub>"`.

### Tokens de prueba

Están firmados con la clave de desarrollo y no vencen. Si se define otra `JWT_SECRET`, dejan de servir.

| Usuario (`sub`) | Rol | Dueño de | Token |
| --- | --- | --- | --- |
| `prueba-admin` | ADMIN | — | `eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiJwcnVlYmEtYWRtaW4iLCJyb2xlIjoiQURNSU4ifQ.jXvvRRZMakLjcEvXkDGB8wnlzHTW7Y5171IWoIOKuFg` |
| `prueba-comercio` | COMERCIO | 1 La Esquina, 4 Sushi Cerrado | `eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiJwcnVlYmEtY29tZXJjaW8iLCJyb2xlIjoiQ09NRVJDSU8ifQ.qve-eikFzYL1osRC0I9wM53gLvPc56HXWSYB4SmPAMA` |
| `prueba-comercio-2` | COMERCIO | 2 Don Pietro, 3 Helados del Sur | `eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiJwcnVlYmEtY29tZXJjaW8tMiIsInJvbGUiOiJDT01FUkNJTyJ9.Xz1vR2OJT51raJ7FF5y3ssZRjxQqdrjCKcQRoVSvtmE` |

Para armar otro token con otra clave o con otro `sub`: en https://jwt.io elegir HS256, poner el payload (por ejemplo `{"sub":"mi-usuario","role":"COMERCIO"}`) y la clave en "your-256-bit-secret". También alcanza este snippet de Python, sin librerías:

```python
import base64, hashlib, hmac, json
clave = b"DeliveryGo-Clave-Secreta-JWT-2026-UADE-API"          # = JWT_SECRET
b64 = lambda b: base64.urlsafe_b64encode(b).rstrip(b"=").decode()
h = b64(json.dumps({"alg": "HS256", "typ": "JWT"}, separators=(",", ":")).encode())
p = b64(json.dumps({"sub": "mi-usuario", "role": "COMERCIO"}, separators=(",", ":")).encode())
print(f"{h}.{p}." + b64(hmac.new(clave, f"{h}.{p}".encode(), hashlib.sha256).digest()))
```

En Swagger: botón **Authorize**, pegar el token **sin** la palabra `Bearer`.

## 4. Ejemplos de consumo

En los ejemplos, `CAT=http://192.168.56.10:8082/api/v1`. En PowerShell usar `curl.exe` (no el alias `curl`). Todos los errores responden así:

```json
{"timestamp":"2026-11-24T13:38:13","status":403,"error":"FORBIDDEN","message":"El comercio 1 no pertenece al usuario autenticado","path":"/api/v1/productos/2/disponibilidad"}
```

### Portal Web

```bash
# Catálogo: solo productos ACTIVOS, disponibles y de comercios activos. Todos los filtros son opcionales
curl "$CAT/catalogo?texto=hamburguesa&categoriaId=1&categoriaId=5&precioMax=9000&page=0&size=20"
# -> {"items":[{"id":1,"nombre":"Hamburguesa clásica","precio":8500.00,"disponible":true,"estado":"ACTIVO","comercioId":1,"categorias":[...]}],"page":0,"size":20,"total":2}

curl "$CAT/productos/1"                  # detalle; 404 si está oculto
curl "$CAT/categorias"                   # para los filtros
curl "$CAT/comercios/1"                  # datos del comercio
curl "$CAT/comercios/1/sucursales"

# Panel del comerciante: sus comercios y la edición de sus productos
curl -H "Authorization: Bearer $TOKEN" "$CAT/comercios/mios"
curl -X PATCH -H "Authorization: Bearer $TOKEN" -H "Content-Type: application/json" \
     -d '{"nuevoPrecio":9200}' "$CAT/productos/1/precio"
curl -X PATCH -H "Authorization: Bearer $TOKEN" -H "Content-Type: application/json" \
     -d '{"disponible":false}' "$CAT/productos/1/disponibilidad"
curl -X POST  -H "Authorization: Bearer $TOKEN" -H "Content-Type: application/json" \
     -d '{"nombre":"Papas fritas","descripcion":"Porción","precio":4500,"categoriaIds":[1],"comercioId":1}' \
     "$CAT/productos"
```

Desde el navegador (fetch), el Portal puede identificarse con el header `X-Modulo`, ya habilitado en CORS:

```js
const r = await fetch(`${CAT}/catalogo?size=20`, { headers: { "X-Modulo": "portal" } });
const { items, total } = await r.json();
```

### Pedidos

Pedidos tiene que consultar el catálogo **al crear el pedido y otra vez al confirmarlo**, porque entre un momento y otro el comercio puede pausar un producto, darlo de baja o cambiarle el precio:

```bash
curl -H "X-Modulo: pedidos" "$CAT/productos/disponibilidad?ids=1,2,6"
```

```json
{
  "todosDisponibles": false,
  "productos": [
    {"productoId":1,"nombre":"Hamburguesa clásica","precio":8500.00,"comercioId":1,"disponible":true,"motivo":"DISPONIBLE"},
    {"productoId":2,"nombre":"Doble cheddar","precio":10500.00,"comercioId":1,"disponible":true,"motivo":"DISPONIBLE"},
    {"productoId":6,"nombre":"Brownie de chocolate","precio":4200.00,"comercioId":1,"disponible":false,"motivo":"NO_DISPONIBLE"}
  ]
}
```

| `motivo` | Qué pasó | Qué debería hacer Pedidos |
| --- | --- | --- |
| `DISPONIBLE` | Se puede pedir | Seguir, con el `precio` que devuelve (es el vigente) |
| `NO_DISPONIBLE` | El comercio lo pausó (CU-04) | Rechazar o pedir al cliente que lo saque del pedido |
| `PRODUCTO_INACTIVO` | Dado de baja (CU-09) | Igual |
| `COMERCIO_INACTIVO` | El comercio está inactivo | Rechazar el pedido |
| `INEXISTENTE` | El id no existe | Rechazar el pedido |

Es público (sin token), acepta hasta 100 ids (`ids=1,2,3` o `ids=1&ids=2`) y devuelve un ítem por id, en el orden pedido y sin repetidos. Sin `ids` o con un id no numérico responde 400.

## 5. Demostrar la integración

Con `LOG_CONSULTAS=true` (por defecto), cada consulta deja una línea en la consola del catálogo con la IP de quien llama y el módulo que se identifica en `X-Modulo`:

```
INTEGRACION : GET /api/v1/productos/disponibilidad?ids=1,2 -> 200 (12 ms) desde 192.168.56.30 modulo=pedidos
INTEGRACION : GET /api/v1/catalogo?size=20 -> 200 (9 ms) desde 192.168.56.20 modulo=portal
```

Guion para la demo de disponibilidad:

1. En el Portal, armar un pedido con los productos 1 y 2 (La Esquina). En la consola del catálogo aparece la consulta de Pedidos.
2. Con el token de `prueba-comercio`, pausar el producto 2: `PATCH /productos/2/disponibilidad` con `{"disponible":false}`.
3. Confirmar el pedido. Pedidos vuelve a consultar, recibe `todosDisponibles: false` con `motivo: NO_DISPONIBLE` y rechaza la confirmación. El producto 2 ya no está en el catálogo del Portal.
4. Con el token de `prueba-comercio-2`, intentar volver a habilitarlo. Responde **403**: no es su comercio.
5. Habilitarlo con `prueba-comercio`. El pedido se puede confirmar.

El mismo flujo corre automatizado en [`FlujoPedidoDisponibilidadTest`](demo/src/test/java/com/catalogodeliverygo/demo/Integracion/FlujoPedidoDisponibilidadTest.java), y los casos de comercio ajeno en [`PropiedadDelComercioTest`](demo/src/test/java/com/catalogodeliverygo/demo/Secutiry/PropiedadDelComercioTest.java).
