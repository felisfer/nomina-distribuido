# Frontend Next.js - Sistema de Nómina

Aplicación frontend en Next.js del Sistema de Nómina desarrollado en Next.js.

## Requisitos

- Node.js 20+
- Backend ejecutándose (por defecto en `http://localhost:8080`)

Las instrucciones para levantar el backend se encuentran en su respectivo módulo, revisar [/backend/README.md](/backend/README.md).

## Configuración

Variables de entorno:

```env
BACKEND_API_URL=http://localhost:8080
PORT=3000
ALLOWED_HOSTNAMES=localhost
```

## Ejecutar

### Entorno local

```bash
pnpm install
pnpm run dev
```

Abrir: `http://localhost:3000` (o el puerto que asigne Next.js).

### Con Docker

```bash
docker build -t frontend-nomina:latest --target dev .
docker run -d -p 3000:3000 --name frontend-nomina frontend-nomina:latest
```

## Rutas principales

- `/login`
- `/register`
- `/dashboard`
- `/dashboard/empleados/[rfc]/calcular-nomina`
- `/dashboard/empleados/[rfc]/nominas`
