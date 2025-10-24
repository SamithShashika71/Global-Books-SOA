GlobalBooks SOA & Microservices Project
License: MIT Java Spring Boot Docker

📖 Project Overview
GlobalBooks Inc. SOA migration project - transforming a legacy monolithic order-processing system into a modern Service-Oriented Architecture (SOA) with four autonomous services. This project demonstrates enterprise-grade SOA patterns, security implementations, and microservices best practices.

🏗️ Architecture

🚀 Services
1. Catalog Service (SOAP/Java)
Technology: Java WAR, JAX-WS, SOAP
Security: WS-Security with UsernameToken
Features: Book catalog management, search functionality
Port: 8081
2. Orders Service (REST/Spring Boot)
Technology: Spring Boot, REST API
Security: OAuth2 with JWT tokens
Features: Order management, status tracking
Port: 8082
3. Payment Service (REST/Spring Boot)
Technology: Spring Boot, REST API
Security: OAuth2 with JWT tokens
Features: Payment processing, transaction management
Port: 8083
4. Shipping Service (REST/Spring Boot)
Technology: Spring Boot, REST API
Security: OAuth2 with JWT tokens
Features: Shipping calculation, logistics management
Port: 8084
🛠️ Technical Stack
Backend Technologies
Java 17+ - Primary programming language
Spring Boot 3.1+ - REST services framework
JAX-WS - SOAP web services
PostgreSQL - Primary database
RabbitMQ - Message broker for async communication
Apache ODE - BPEL orchestration engine
Security
WS-Security - SOAP services authentication
OAuth2 + JWT - REST services authentication
Spring Security - Security framework
BCrypt - Password encryption
Infrastructure
Docker & Docker Compose - Containerization
UDDI Registry - Service discovery
Swagger/OpenAPI - REST API documentation
📁 Project Structure
Global-Books/
├── catalog-service/          # SOAP-based catalog service
│   ├── src/main/java/       # Java source code
│   ├── src/main/resources/  # WSDL, configuration files
│   └── pom.xml             # Maven dependencies
├── orders-service/          # REST-based orders service
│   ├── src/main/java/       # Spring Boot application
│   ├── src/main/resources/  # Application properties
│   └── pom.xml             # Maven dependencies
├── payment-service/         # REST-based payment service
│   ├── src/main/java/       # Spring Boot application
│   ├── src/main/resources/  # Application properties
│   └── pom.xml             # Maven dependencies
├── shipping-service/        # REST-based shipping service
│   ├── src/main/java/       # Spring Boot application
│   ├── src/main/resources/  # Application properties
│   └── pom.xml             # Maven dependencies
├── docker-compose.yml       # Multi-service orchestration
├── init-databases.sql       # Database initialization
└── README.md               # This file
🚀 Getting Started
Prerequisites
Java 17+
Maven 3.8+
Docker & Docker Compose
PostgreSQL 15+
RabbitMQ 3.12+
Quick Start with Docker
Clone the repository

git clone https://github.com/NavvAbhishek/Global-Books.git
cd Global-Books
Start all services

docker-compose up -d
Initialize databases

docker-compose exec postgres psql -U postgres -d postgres -f /docker-entrypoint-initdb.d/init-databases.sql
Verify services are running

curl http://localhost:8081/ws/v1/catalog?wsdl  # Catalog Service WSDL
curl http://localhost:8082/api/v1/orders       # Orders Service
curl http://localhost:8083/api/v1/payments     # Payment Service
curl http://localhost:8084/api/v1/shipping     # Shipping Service
Manual Setup
1. Database Setup
-- Create databases
CREATE DATABASE catalog_db;
CREATE DATABASE orders_db;
CREATE DATABASE payments_db;
CREATE DATABASE shipping_db;

-- Create users
CREATE USER catalog_user WITH PASSWORD 'catalog_pass';
CREATE USER orders_user WITH PASSWORD 'orders_pass';
CREATE USER payments_user WITH PASSWORD 'payments_pass';
CREATE USER shipping_user WITH PASSWORD 'shipping_pass';

-- Grant permissions
GRANT ALL PRIVILEGES ON DATABASE catalog_db TO catalog_user;
GRANT ALL PRIVILEGES ON DATABASE orders_db TO orders_user;
GRANT ALL PRIVILEGES ON DATABASE payments_db TO payments_user;
GRANT ALL PRIVILEGES ON DATABASE shipping_db TO shipping_user;
2. Start Individual Services
Catalog Service (SOAP)

cd catalog-service
mvn clean package
java -jar target/catalog-service-1.0.0.jar
Orders Service (REST)

cd orders-service
mvn clean package
java -jar target/orders-service-1.0.0.jar
Payment Service (REST)

cd payment-service
mvn clean package
java -jar target/payment-service-1.0.0.jar
Shipping Service (REST)

cd shipping-service
mvn clean package
java -jar target/shipping-service-1.0.0.jar
🔐 Authentication & Security
SOAP Services (Catalog)
<soapenv:Header>
    <wsse:Security xmlns:wsse="http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-secext-1.0.xsd">
        <wsse:UsernameToken>
            <wsse:Username>admin</wsse:Username>
            <wsse:Password Type="#PasswordText">admin123</wsse:Password>
        </wsse:UsernameToken>
    </wsse:Security>
</soapenv:Header>
REST Services (Orders, Payments, Shipping)
# 1. Login to get JWT token
curl -X POST http://localhost:8082/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username": "admin", "password": "admin123"}'

# 2. Use token for protected endpoints
curl -X GET http://localhost:8082/api/v1/orders \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
📚 API Documentation
Catalog Service (SOAP)
WSDL: http://localhost:8081/ws/v1/catalog?wsdl
Endpoint: http://localhost:8081/ws/v1/catalog
Operations: searchBooks, getBookDetails, addBook, updateBook
Orders Service (REST)
Base URL: http://localhost:8082/api/v1
Swagger UI: http://localhost:8082/swagger-ui.html
Endpoints:
POST /auth/login - Authentication
GET /orders - List orders
POST /orders - Create order
GET /orders/{id} - Get order details
Payment Service (REST)
Base URL: http://localhost:8083/api/v1
Swagger UI: http://localhost:8083/swagger-ui.html
Endpoints:
POST /auth/login - Authentication
POST /payments/process - Process payment
GET /payments/{id} - Get payment status
Shipping Service (REST)
Base URL: http://localhost:8084/api/v1
Swagger UI: http://localhost:8084/swagger-ui.html
Endpoints:
POST /auth/login - Authentication
POST /shipping/calculate - Calculate shipping
GET /shipping/{id} - Get shipping details
🧪 Testing
SOAP UI Testing (Catalog Service)
Import WSDL: http://localhost:8081/ws/v1/catalog?wsdl
Configure WS-Security authentication
Test operations with sample data
Postman Testing (REST Services)
# Import Postman collection (if provided)
# Or use manual requests:

# 1. Orders Service Test
curl -X POST http://localhost:8082/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username": "admin", "password": "admin123"}'

# 2. Create Order Test
curl -X POST http://localhost:8082/api/v1/orders \
  -H "Authorization: Bearer <token>" \
  -H "Content-Type: application/json" \
  -d '{"customerId": 1, "items": [{"bookId": 123, "quantity": 2}]}'
Unit Testing
# Run all tests
mvn test

# Run specific service tests
cd orders-service && mvn test
cd catalog-service && mvn test
🔧 Configuration
Environment Variables
# Database Configuration
DB_HOST=localhost
DB_PORT=5432
CATALOG_DB_NAME=catalog_db
ORDERS_DB_NAME=orders_db

# Security Configuration
JWT_SECRET=mySecretKey123456789012345678901234567890
JWT_EXPIRATION=3600

# RabbitMQ Configuration
RABBITMQ_HOST=localhost
RABBITMQ_PORT=5672
RABBITMQ_USERNAME=guest
RABBITMQ_PASSWORD=guest
Service Ports
Catalog Service: 8081
Orders Service: 8082
Payment Service: 8083
Shipping Service: 8084
PostgreSQL: 5432
RabbitMQ: 5672, 15672 (Management UI)
📊 Monitoring & Health Checks
Health Check Endpoints
Catalog Service: http://localhost:8081/health
Orders Service: http://localhost:8082/actuator/health
Payment Service: http://localhost:8083/actuator/health
Shipping Service: http://localhost:8084/actuator/health
Metrics & Monitoring
Spring Boot Actuator: Metrics and monitoring for REST services
Custom Health Checks: Database connectivity, external service health
Logging: Structured logging with logback configuration
🚀 Deployment
Docker Deployment
# Build and deploy all services
docker-compose up --build -d

# Scale specific services
docker-compose up --scale orders-service=3 -d

# View logs
docker-compose logs -f orders-service
Cloud Deployment (AWS/Azure/GCP)
Container orchestration with Kubernetes
Database services (RDS, Cloud SQL)
Message queuing (Amazon MQ, Service Bus)
Load balancing and auto-scaling
🤝 Contributing
Fork the repository
Create a feature branch (git checkout -b feature/amazing-feature)
Commit your changes (git commit -m 'Add amazing feature')
Push to the branch (git push origin feature/amazing-feature)
Open a Pull Request
Development Guidelines
Follow Java coding standards
Write unit tests for new features
Update documentation for API changes
Ensure Docker compatibility
📄 License
This project is licensed under the MIT License - see the LICENSE file for details.

👥 Team
NavvAbhishek - Lead Developer & Architect
GlobalBooks Architecture Team - System Design & Review
📞 Support
For questions and support:

Issues: GitHub Issues
Documentation: Project Wiki
Email: support@globalbooks.com
🙏 Acknowledgments
Spring Boot Community
Apache Software Foundation
OASIS Web Services Standards
Docker Community
Built with ❤️ for CCS3341 SOA & Microservices Coursework
