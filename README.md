# BK Inventory Management API

Microservicio de gestión de inventario desarrollado con Spring Boot que permite administrar el stock de productos, procesar compras y mantener un historial de operaciones.

## 📋 Tabla de Contenidos

- [Características](#-características)
- [Tecnologías](#-tecnologías)
- [Requisitos Previos](#-requisitos-previos)
- [Instalación](#-instalación)
- [Configuración](#-configuración)
- [Uso](#-uso)
- [API Endpoints](#-api-endpoints)
- [Documentación API](#-documentación-api)
- [Estructura del Proyecto](#-estructura-del-proyecto)
- [Arquitectura](#-arquitectura)

## ✨ Características

- ✅ **Gestión de Inventario**: Consulta y actualización de cantidades disponibles de productos
- ✅ **Procesamiento de Compras**: Validación de stock y actualización automática de inventario
- ✅ **Integración con Microservicio de Productos**: Obtención de información de productos desde servicio externo
- ✅ **Historial de Operaciones**: Registro automático de todas las transacciones de inventario
- ✅ **Eventos Asíncronos**: Sistema de eventos para notificaciones de cambios en inventario
- ✅ **Documentación OpenAPI/Swagger**: Documentación interactiva de la API
- ✅ **Validación de Datos**: Validación de requests con Jakarta Validation
- ✅ **Manejo de Transacciones**: Operaciones transaccionales para garantizar consistencia de datos

## 🛠 Tecnologías

- **Java 21**
- **Spring Boot 3.5.7**
- **Spring Data JPA**: Persistencia de datos
- **PostgreSQL**: Base de datos relacional
- **Spring WebFlux**: Cliente HTTP reactivo (WebClient)
- **Lombok**: Reducción de código boilerplate
- **OpenAPI 3 / Swagger**: Documentación de API
- **Jakarta Validation**: Validación de datos
- **Maven**: Gestión de dependencias

## 📦 Requisitos Previos

- Java 21 o superior
- Maven 3.6+
- PostgreSQL 12+
- Microservicio de productos ejecutándose (opcional para desarrollo)

## 🚀 Instalación

1. **Clonar el repositorio**
   ```bash
   git clone <repository-url>
   cd bk-inventory
   ```

2. **Compilar el proyecto**
   ```bash
   mvn clean install
   ```

3. **Ejecutar la aplicación**
   ```bash
   mvn spring-boot:run
   ```

La aplicación estará disponible en `http://localhost:9090`

## ⚙️ Configuración

### Base de Datos PostgreSQL

Edita el archivo `src/main/resources/application.properties` con tus credenciales:

```properties
# PostgreSQL Database Configuration
spring.datasource.url=jdbc:postgresql://localhost:5432/linktic
spring.datasource.username=tu_usuario
spring.datasource.password=tu_contraseña
spring.datasource.driver-class-name=org.postgresql.Driver

# JPA/Hibernate Configuration
spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true
spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.PostgreSQLDialect
```

### Microservicio de Productos

Configura la URL del microservicio de productos:

```properties
# Product Service Configuration
product.service.url=http://localhost:8080/api/v1/products
```

### Puerto del Servidor

Por defecto, la aplicación corre en el puerto `9090`. Puedes cambiarlo en `application.properties`:

```properties
server.port=9090
```

## 📖 Uso

### Health Check

Verifica que la API esté funcionando:

```bash
curl http://localhost:9090/api/v1/inventory/health
```

### Consultar Cantidad Disponible

```bash
curl http://localhost:9090/api/v1/inventory/products/1/quantity
```

### Actualizar Inventario

```bash
curl -X PUT http://localhost:9090/api/v1/inventory/products \
  -H "Content-Type: application/json" \
  -d '{
    "productId": 1,
    "quantity": 100
  }'
```

### Realizar una Compra

```bash
curl -X POST http://localhost:9090/api/v1/orders \
  -H "Content-Type: application/json" \
  -d '{
    "productId": 1,
    "quantity": 5
  }'
```

## 🔌 API Endpoints

### Inventory Management (`/api/v1/inventory`)

| Método | Endpoint | Descripción |
|--------|----------|-------------|
| GET | `/health` | Health check de la API |
| GET | `/products/{productId}/quantity` | Consultar cantidad disponible de un producto |
| PUT | `/products` | Actualizar cantidad disponible de un producto |

### Orders Management (`/api/v1/orders`)

| Método | Endpoint | Descripción |
|--------|----------|-------------|
| POST | `/` | Realizar compra de producto |

### Ejemplos de Request/Response

#### Consultar Cantidad Disponible

**Request:**
```http
GET /api/v1/inventory/products/1/quantity
```

**Response:**
```json
{
  "productId": 1,
  "availableQuantity": 50,
  "product": {
    "id": 1,
    "name": "producto 1",
    "price": 15000.00,
    "description": "descripcion de producto"
  }
}
```

#### Realizar Compra

**Request:**
```http
POST /api/v1/orders
Content-Type: application/json

{
  "productId": 1,
  "quantity": 5
}
```

**Response:**
```json
{
  "productId": 1,
  "product": {
    "id": 1,
    "name": "producto 1",
    "price": 15000.00,
    "description": "descripcion de producto"
  },
  "quantity": 5,
  "unitPrice": 15000.00,
  "subtotal": 75000.00,
  "total": 75000.00,
  "remainingQuantity": 45
}
```

## 📚 Documentación API

La documentación interactiva de la API está disponible mediante Swagger UI:

- **Swagger UI**: http://localhost:9090/swagger-ui.html
- **OpenAPI JSON**: http://localhost:9090/api-docs

## 📁 Estructura del Proyecto

```
src/main/java/com/example/demo/
├── client/              # Clientes HTTP para servicios externos
│   └── ProductClient.java
├── config/              # Configuraciones
│   ├── OpenApiConfig.java
│   └── WebClientConfig.java
├── controller/         # Controladores REST
│   ├── InventoryControllerV1.java
│   └── OrdersControllerV1.java
├── dto/                # Data Transfer Objects
│   ├── InventoryResponseDTO.java
│   ├── ProductDTO.java
│   ├── PurchaseRequestDTO.java
│   ├── PurchaseResponseDTO.java
│   └── ...
├── event/              # Eventos de dominio
│   └── InventoryChangeEvent.java
├── listener/           # Listeners de eventos
│   └── InventoryChangeListener.java
├── model/              # Entidades JPA
│   ├── InventoryModel.java
│   └── PurchaseHistoryModel.java
├── repository/         # Repositorios JPA
│   ├── InventoryRepo.java
│   └── PurchaseHistoryRepo.java
└── service/            # Lógica de negocio
    ├── InventoryServiceV1.java
    └── OrdersServiceV1.java
```

## 🏗 Arquitectura

### Capas de la Aplicación

1. **Controller Layer**: Maneja las peticiones HTTP y valida los DTOs
2. **Service Layer**: Contiene la lógica de negocio
3. **Repository Layer**: Acceso a datos mediante Spring Data JPA
4. **Model Layer**: Entidades JPA que representan las tablas de la base de datos
5. **Client Layer**: Comunicación con microservicios externos

### Flujo de una Compra

1. Cliente envía request con `productId` y `quantity`
2. Controller valida el request
3. Service obtiene información del producto desde el microservicio
4. Service verifica disponibilidad en inventario
5. Service actualiza el inventario (reduce cantidad)
6. Service registra la operación en el historial
7. Service emite evento de cambio de inventario
8. Controller retorna información de la compra

### Sistema de Eventos

La aplicación utiliza eventos asíncronos para notificar cambios en el inventario:

- **InventoryChangeEvent**: Se emite cuando cambia el inventario
- **InventoryChangeListener**: Procesa los eventos de forma asíncrona

## 🔒 Validaciones

- **PurchaseRequestDTO**: 
  - `productId`: Obligatorio (NotNull)
  - `quantity`: Obligatorio, mínimo 1 (NotNull, Min(1))

- **UpdateInventoryRequestDTO**:
  - `productId`: Obligatorio
  - `quantity`: Obligatorio, no puede ser negativo (Min(0))

## 🧪 Testing

Para ejecutar los tests:

```bash
mvn test
```

## 📝 Notas Adicionales

- La aplicación utiliza `spring.jpa.hibernate.ddl-auto=update` para crear/actualizar automáticamente las tablas
- Los eventos se procesan de forma asíncrona para no bloquear las operaciones principales
- El historial de compras se registra automáticamente en cada operación de inventario
- La comunicación con el microservicio de productos utiliza WebClient (reactivo)

## 👥 Contribución

1. Fork el proyecto
2. Crea una rama para tu feature (`git checkout -b feature/AmazingFeature`)
3. Commit tus cambios (`git commit -m 'Add some AmazingFeature'`)
4. Push a la rama (`git push origin feature/AmazingFeature`)
5. Abre un Pull Request

## 📄 Licencia

Este proyecto está bajo la Licencia Apache 2.0.

## 📧 Contacto

Para más información, contacta al equipo de desarrollo.

---

**Desarrollado con ❤️ usando Spring Boot**

