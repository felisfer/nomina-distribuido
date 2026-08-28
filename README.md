# Sistema de Nómina - Arquitectura Distribuida

Sistema de gestión de nómina, con **backend API REST** (Spring Boot) y **frontend web** (Next.js) desacoplados, más una base de datos PostgreSQL como tercer servicio.

## Características

- Arquitectura distribuida: base de datos, backend y frontend como servicios independientes
- Autenticación con JWT (verificada en `middleware.ts` del frontend)
- Gestión de empleados y cálculo automático de nóminas (ISR, salario neto)
- PostgreSQL (contenedor propio, con script de inicialización `database/init.sql`)
- 226 pruebas unitarias en backend, 99% de cobertura
- CI con GitHub Actions (`frontend-ci-cd.yaml` para frontend, `backend-ci-cd.yaml` para backend)
- Dockerizado por completo (docker-compose con 3 servicios)

## Estructura

```
mx-uaemex-isii-distributed/
├── .github/workflows/   # CI: build. test y deploy en GHCR
├── backend/             # API REST en Java + Spring Boot
│   ├── src/              # capas: config, logic, persistence, presentation
│   └── docs/              # manual de usuario, diagramas UML, imágenes
├── frontend/             # Next.js + TypeScript (pnpm)
│   ├── app/                # páginas y rutas API (auth, empleado, nomina)
│   ├── lib/                 # auth.ts, backend.ts (cliente hacia la API)
│   └── middleware.ts       # protección de rutas vía JWT
├── database/             # init.sql (esquema inicial de Postgres)
└── docker-compose.yml
```

## Cómo ejecutarlo

### Opción A: Docker Compose (recomendada)

1. Crea un `.env` en la raíz:

```env
# Backend
BACKEND_PORT=3000
BACKEND_DB_USERNAME=postgres
BACKEND_DB_PASSWORD=123
BACKEND_DB_NAME=nomina
BACKEND_DB_PORT=5432
BACKEND_JWT_SECRET=p4zDJcrnZxKJ5iHpsIa3r4AE0P2GAtv0bhfbyrP8vv7UtH223l
BACKEND_JWT_EXPIRATION_MS=86400000

# Frontend
FRONTEND_PORT=3001
ALLOWED_HOSTNAMES=localhost # e.g. 192.168.100.34,host-name.local,mi-dominio.com

# General
ENVIRONMENT_TARGET=dev # dev/prod
```

Debido a la configuración por defecto de Next.js es necesario específicar el hostname autorizado mediante la variable de entorno ALLOWED_HOSTNAMES cuando se use herramientas como tunnels o ngrok.
Para entornos de desarollo se puede usar `localhost`, `127.0.0.1`, el hostname del equipo `*.local` o un dominio `mi-sitio.com`.
En entornos productivos se debe de usar bajo un certificado TLS configurado mediante un proxy como NGINX.

2. Levanta los servicios:

```bash
docker-compose up -d
```

3. Accede:
   - Backend: http://localhost:3000
   - Frontend: http://localhost:3001

Los tres servicios (`database`, `backend`, `frontend`) se comunican entre sí por nombre de servicio dentro de la red de Docker (por ejemplo, el backend se conecta a `jdbc:postgresql://database:${BACKEND_DB_PORT}/...` y el frontend llama al backend en `http://backend:${BACKEND_PORT}`).

### Opción B: Local (sin Docker)

**Base de datos:** levanta un PostgreSQL local y ejecuta `database/init.sql`.

**Backend:**
```bash
cd backend
export JWT_SECRET="tu-secreto-jwt"
export JWT_EXPIRATION_MS=86400000
export DB_URL="jdbc:postgresql://localhost:5432/nomina"
export DB_USERNAME="postgres"
export DB_PASSWORD="tu-password"
export PORT=3000
export SPRING_PROFILES_ACTIVE=dev
mvn spring-boot:run
```

**Frontend:**
```bash
cd frontend
pnpm install
pnpm dev
```

> Configura `BACKEND_API_URL` (variable de entorno del frontend) apuntando a `http://localhost:3000` si el backend corre en otro puerto.

## Endpoints principales

| Método | Ruta | Protegido | Descripción |
|---|---|---|---|
| POST | `/auth/register` | No | Registrar empleado |
| POST | `/auth/login` | No | Login (JWT) |
| GET | `/empleado/` | Sí | Listar empleados |
| GET | `/nomina/?rfc={rfc}` | Sí | Nóminas de un empleado |
| POST | `/nomina/` | Sí | Crear nómina |
| DELETE | `/nomina/{id}` | Sí | Eliminar nómina |

Ejemplo rápido:

```bash
curl -X POST http://localhost:3000/auth/login \
  -H "Content-Type: application/json" \
  -d '{"correo":"admin@example.com","password":"Admin123456*"}'
```

## Stack

**Backend:** Java 17, Spring Boot 4, Spring Security, JWT, PostgreSQL, JUnit 5 + Mockito, Maven, Docker.
**Frontend:** Next.js, TypeScript, React, middleware de autenticación, pnpm, Docker.
**Base de datos:** PostgreSQL 17 (Alpine), inicializada con `database/init.sql`.

## Pruebas

```bash
cd backend
mvn clean test jacoco:report
# Reporte en backend/target/site/jacoco/index.html
```

*Uso académico - Versión 0.0.1-SNAPSHOT (ago. 2026).*
