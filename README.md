<h1 align="center">
  <img src="https://raw.githubusercontent.com/devicons/devicon/master/icons/spring/spring-original.svg" 
       width="45"
       style="vertical-align: middle; margin-right: 10px;" />
  <span style="vertical-align: middle;">
    SPRING BOOT STARTER TEMPLATE
  </span>
</h1>

<h3 align="center">
A Production-Ready Spring Boot Boilerplate  
Secure • Scalable • Clean • Environment-Aware • Simple
</h3>

<p align="center">
Stop rewriting authentication, security, and configuration logic in every new project.  
Start with a solid, production-grade backend foundation. A modern backend foundation with JWT, OAuth2, Rate Limiting, and Dev/Prod profiles.  
so you never start from scratch again.
</p>
<p align="center">

<img src="https://img.shields.io/github/last-commit/yuosef33/Spring-boot-starter-template?color=blue&style=flat" />
<img src="https://img.shields.io/github/languages/top/yuosef33/Spring-boot-starter-template?style=flat" />
<img src="https://img.shields.io/github/languages/count/yuosef33/Spring-boot-starter-template?style=flat" />
<img src="https://img.shields.io/badge/Java-21-red?style=flat&logo=openjdk" />
<img src="https://img.shields.io/badge/Spring%20Boot-3.x-brightgreen?style=flat&logo=springboot" />
</p>

---

## 📖 Overview

This project is a **modern Spring Boot starter template** designed to eliminate repetitive setup work and enforce best practices from day one.

Instead of rebuilding:

- Security configuration  
- JWT authentication  
- OAuth2 login (Google)
- Role management  
- Exception handling  
- Rate limiting  
- Environment profiles  


It is built to be:

- 🔐 Secure by default  
- 🧱 Architecturally clean  
- 🌍 Environment-aware (Dev / Prod separation)  
- 🚀 Easy to extend  
- 🧩 Free of business logic  

You can plug this into any new project and immediately focus on real application features.

---

## ⚡ Quick Setup
By default dev profile is active and on dev profile we use h2 DB so you can just clone and run the project direct with out any issues 
Clone and run locally in seconds:

```bash
# Clone the repository
git clone https://github.com/yuosef33/Spring-boot-starter-template.git

# Navigate into project directory
cd Spring-boot-starter-template

# Run the application (dev profile by default)
./mvnw spring-boot:run
```

Application runs at:

```
http://localhost:8080
```

Swagger (dev profile only):

```
http://localhost:8080/swagger-ui.html
```

For Windows (PowerShell):

```powershell
mvnw.cmd spring-boot:run
```

Postman collection link 
```
https://www.postman.com/me4444-5137/spring-boot-starter-template/collection/39139361-bd03fc7a-8e53-460b-8df5-59f0e22e061b?action=share&source=copy-link&creator=39139361
```

---

## ✨ Core Features

### 🔐 Authentication & Security

- JWT Authentication (Access + Refresh Tokens)
- Refresh token stored in database with real logout
- OAuth2 Google login fully integrated with JWT
- Role-based authorization (USER / ADMIN)
- Stateless security configuration
- Bucket4j rate limiting (per IP)
- Production-grade security headers
- Profile-based CORS configuration

### 🏗 Architecture

- Clean layered structure
- DTO + Mapper separation
- Global ApiResponse wrapper
- Centralized exception handling
- Entity auditing (createdAt, updatedAt, createdBy)
- Dev and Prod security configurations

### 🌍 Environment Support

- H2 (dev profile)
- PostgreSQL (prod profile)
- Swagger enabled in dev only
- Actuator monitoring endpoints
- Environment variable driven configuration

### 🐳 Infrastructure

- Docker Compose for PostgreSQL
- Production-safe defaults
- Ready for container deployment

---

## 🏗️ Project Structure

```
com.yuosef.cloudbasedlabexaminationplatform
│
├── config
│   ├── JWT
│   ├── Bucket4J
│   ├── SecurityConfig (dev)
│   ├── SecurityConfigProd (prod)
│   └── SwaggerConfig
│
├── controller
├── service
│   └── impl
├── repository
├── model
│   ├── dto
│   └── mapper
```

---

## 🐘 Run with PostgreSQL (Production Profile)

Start PostgreSQL container:

```bash
docker-compose up -d
```

Run application with production profile:

```bash
./mvnw spring-boot:run -Dspring-boot.run.profiles=prod
```

---

## 🔑 Environment Variables

### Required for Production

| Variable | Description |
|----------|------------|
| `JWT_SECRET_KEY` | JWT signing secret (minimum 32 characters recommended) |
| `DB_URL` | PostgreSQL JDBC URL |
| `DB_USERNAME` | Database username |
| `DB_PASSWORD` | Database password |
| `GOOGLE_CLIENT_ID` | Google OAuth2 Client ID |
| `GOOGLE_CLIENT_SECRET` | Google OAuth2 Client Secret |
| `ALLOWED_ORIGINS` | Allowed frontend origins |

---

## 🔐 Authentication Flow

### Local Authentication

1. Register user
2. Login → receive access + refresh token
3. Use access token in `Authorization: Bearer <token>`
4. Refresh token when expired
5. Logout → refresh token removed from database

### Google OAuth2 Authentication

1. Redirect to `/oauth2/authorization/google`
2. Authenticate with Google
3. Receive short-lived exchange code
4. Exchange code for JWT tokens
5. Continue using stateless JWT authentication

No session-based authentication. Everything remains fully stateless.

---

## 🚦 Rate Limiting

| Endpoint | Limit |
|----------|--------|
| `/auth/**` | 5 requests per minute per IP |
| All other endpoints | 100 requests per minute per IP |

Designed for easy upgrade to Redis-backed implementation for multi-instance environments.

---

## 📊 Actuator

Available endpoints:

- `/actuator/health`
- `/actuator/info`

Dev profile:
- Full exposure

Prod profile:
- Restricted exposure (health + info only)

---

## 🌍 Profiles

Default profile:

```yaml
spring:
  profiles:
    active: dev
```

Switch to production using:

```bash
SPRING_PROFILES_ACTIVE=prod
```

---

## 🎯 Design Principles

- Secure by default  
- No business logic included  
- Clean separation of concerns  
- Easy to extend  
- No over-engineering  
- Production-ready configuration  

---


## 📝 License

This project is licensed under the MIT License — see the [LICENSE](LICENSE) file for details.

---

## 🤝 Contributing

Pull requests are welcome.  
If this template saved you time, consider giving it a ⭐.

---
