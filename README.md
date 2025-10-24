# 🌐 GlobalBooks SOA & Microservices Project

**License:** MIT  
**Technologies:** Java, Spring Boot, Docker, PostgreSQL, RabbitMQ

---

## 📖 Project Overview

The **GlobalBooks Inc. SOA Migration Project** demonstrates the transformation of a legacy **monolithic order-processing system** into a modern **Service-Oriented Architecture (SOA)** with multiple independent microservices.  

This project implements **enterprise integration patterns**, **security standards**, and **containerized deployments**—showcasing best practices in building scalable, modular, and maintainable enterprise software.

---

## 🏗️ Architecture Overview

The system is decomposed into **four autonomous services**:

### 1️⃣ Catalog Service (SOAP / Java)
- **Technology:** JAX-WS, Java WAR  
- **Security:** WS-Security (UsernameToken)  
- **Features:** Book catalog management, search functionality  
- **Port:** `8081`

### 2️⃣ Orders Service (REST / Spring Boot)
- **Technology:** Spring Boot, REST API  
- **Security:** OAuth2 + JWT tokens  
- **Features:** Order management, status tracking  
- **Port:** `8082`

### 3️⃣ Payment Service (REST / Spring Boot)
- **Technology:** Spring Boot, REST API  
- **Security:** OAuth2 + JWT tokens  
- **Features:** Payment processing and transaction management  
- **Port:** `8083`

### 4️⃣ Shipping Service (REST / Spring Boot)
- **Technology:** Spring Boot, REST API  
- **Security:** OAuth2 + JWT tokens  
- **Features:** Shipping cost calculation and logistics  
- **Port:** `8084`

---

## 🛠️ Technical Stack

| Layer | Technology |
|-------|-------------|
| **Language** | Java 17+ |
| **Frameworks** | Spring Boot 3.1+, JAX-WS |
| **Database** | PostgreSQL 15+ |
| **Messaging** | RabbitMQ |
| **Orchestration** | Apache ODE (BPEL) |
| **Security** | WS-Security, OAuth2, JWT, Spring Security, BCrypt |
| **Infrastructure** | Docker, Docker Compose, UDDI Registry |
| **API Docs** | Swagger / OpenAPI |

---

## 📂 Project Structure
Global-Books/
├── catalog-service/ # SOAP-based service
├── orders-service/ # REST-based service
├── payment-service/ # REST-based service
├── shipping-service/ # REST-based service
├── docker-compose.yml # Multi-service orchestration
├── init-databases.sql # Database initialization
└── README.md

---

## 🚀 Quick Start with Docker

```bash
# Clone the repository
git clone https://github.com/NavvAbhishek/Global-Books.git
cd Global-Books

# Start all services
docker-compose up -d

# Initialize databases
docker-compose exec postgres psql -U postgres -d postgres -f /docker-entrypoint-initdb.d/init-databases.sql

---

Verify services:
    curl http://localhost:8081/ws/v1/catalog?wsdl   # Catalog (SOAP)
    curl http://localhost:8082/api/v1/orders        # Orders (REST)
    curl http://localhost:8083/api/v1/payments      # Payments (REST)
    curl http://localhost:8084/api/v1/shipping      # Shipping (REST)

---

🔐 Authentication & Security
SOAP Services (Catalog)

Use WS-Security headers:
    <soapenv:Header>
      <wsse:Security xmlns:wsse="http://docs.oasis-open.org/wss/...">
        <wsse:UsernameToken>
            <wsse:Username>admin</wsse:Username>
            <wsse:Password Type="#PasswordText">admin123</wsse:Password>
        </wsse:UsernameToken>
      </wsse:Security>
    </soapenv:Header>

---

REST Services (Orders, Payments, Shipping)
# Get JWT Token
curl -X POST http://localhost:8082/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"admin","password":"admin123"}'

Use the token in subsequent requests:
curl -X GET http://localhost:8082/api/v1/orders \
  -H "Authorization: Bearer <token>"

---

📚 API Documentation
| Service      | URL                                        | Docs       |
| ------------ | ------------------------------------------ | ---------- |
| **Catalog**  | `http://localhost:8081/ws/v1/catalog?wsdl` | WSDL       |
| **Orders**   | `http://localhost:8082/api/v1`             | Swagger UI |
| **Payment**  | `http://localhost:8083/api/v1`             | Swagger UI |
| **Shipping** | `http://localhost:8084/api/v1`             | Swagger UI |

---

🧪 Testing

1. SOAP UI for Catalog Service testing
2. Postman for REST services
3. JUnit for automated unit testing

    # Run all tests
    mvn test

---

🩺 Monitoring & Health Checks

| Service  | Endpoint           |
| -------- | ------------------ |
| Catalog  | `/health`          |
| Orders   | `/actuator/health` |
| Payment  | `/actuator/health` |
| Shipping | `/actuator/health` |

Includes Spring Boot Actuator, database connectivity checks, and structured logging via Logback.

---

☁️ Deployment Options

1. Local: Docker Compose
2. Cloud: AWS / Azure / GCP
3. Scalability: Kubernetes orchestration, service replication
4. Storage: Managed PostgreSQL (RDS / Cloud SQL)

---

🤝 Contributing

1. Fork the repository
2. Create a new branch
3. Commit your changes
4. Push and submit a Pull Request

Please ensure:
    * Code follows Java best practices
    * Unit tests are added
    * Documentation is updated
    * Docker compatibility is preserved

---

🙏 Acknowledgments

* Spring Boot Community
* Apache Software Foundation
* OASIS WS Standards
* Docker Community

---

Built with ❤️ for CCS3341 – SOA & Microservices Coursework.



