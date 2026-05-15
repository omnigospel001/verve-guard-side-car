# Verve Guard Fraud Detection Sidecar

## Table of Contents

- [Overview](#overview)
- [Project Purpose](#project-purpose)
- [Architecture](#architecture)
- [Technologies](#technologies)
- [Prerequisites](#prerequisites)
- [Installation](#installation)
- [Configuration](#configuration)
- [Running the Application](#running-the-application)
- [API Documentation](#api-documentation)
- [Database Schema](#database-schema)
- [Monitoring and Observability](#monitoring-and-observability)
- [Development](#development)
- [Testing](#testing)
- [Deployment](#deployment)
- [Troubleshooting](#troubleshooting)
- [Contributing](#contributing)
- [License](#license)

## Overview

Verve Guard Fraud Detection Sidecar is a Spring Boot microservice designed to detect and prevent fraudulent transactions. Operating as a sidecar service, it intercepts transaction requests, analyzes them against multiple fraud detection patterns, and returns risk assessments to determine whether transactions should be authorized or rejected.

The service is built with enterprise-grade security, scalability, and monitoring capabilities to ensure robust fraud detection in high-throughput payment environments.

## Project Purpose

The Verve Guard Fraud Detection Sidecar serves the following core purposes:

- **Real-time Fraud Detection**: Analyzes incoming transactions in real-time against configured fraud rules and patterns.
- **Risk Assessment**: Assigns risk scores to transactions based on multiple detection algorithms and heuristics.
- **Transaction Authorization**: Provides authorization decisions to prevent fraudulent transactions from being processed.
- **Resilience**: Employs circuit breaker patterns and retry mechanisms to ensure service continuity.
- **Distributed Tracing**: Tracks transaction flows across microservices for debugging and monitoring.
- **Caching**: Maintains high performance through distributed caching with Redis.
- **Event-Driven Architecture**: Publishes fraud detection events through Kafka for downstream processing.
- **Audit and Compliance**: Maintains comprehensive audit logs and security records for compliance requirements.

## Architecture

### High-Level Design

```
┌─────────────────────────────────────────────────────────────────┐
│                    API Gateway / Proxy                          │
└────────────────────────┬────────────────────────────────────────┘
                         │
┌────────────────────────▼────────────────────────────────────────┐
│           Verve Guard Fraud Detection Sidecar                    │
│                                                                  │
│  ┌──────────────────────────────────────────────────────────┐  │
│  │ REST Controller Layer                                    │  │
│  │ - Transaction Validation                                │  │
│  │ - Authentication & Authorization                        │  │
│  └────────────────┬─────────────────────────────────────────┘  │
│                   │                                             │
│  ┌────────────────▼─────────────────────────────────────────┐  │
│  │ Business Logic Layer                                     │  │
│  │ - Fraud Detection Engine                                │  │
│  │ - Risk Scoring                                          │  │
│  │ - Pattern Analysis                                      │  │
│  └────────────────┬─────────────────────────────────────────┘  │
│                   │                                             │
│  ┌────────────────▼─────────────────────────────────────────┐  │
│  │ Data Access Layer                                        │  │
│  │ - JPA Repository                                        │  │
│  │ - Cache Layer                                           │  │
│  │ - Event Publishing                                      │  │
│  └──────────────────────────────────────────────────────────┘  │
└────────────────────────────────────────────────────────────────┘
         │                  │                    │
         ▼                  ▼                    ▼
    ┌─────────┐      ┌──────────┐        ┌────────────┐
    │  MySQL  │      │  Redis   │        │   Kafka    │
    │Database │      │  Cache   │        │Event Queue │
    └─────────┘      └──────────┘        └────────────┘
```

### Service Components

- **REST API Layer**: Handles incoming HTTP requests for fraud detection and transaction analysis.
- **Fraud Detection Engine**: Core business logic for pattern matching and risk assessment.
- **Data Persistence Layer**: Manages transaction records, fraud patterns, and historical data.
- **Cache Layer**: Redis-based caching for frequently accessed data and session management.
- **Event Streaming**: Kafka integration for publishing fraud events and alerts.
- **Security Module**: JWT-based authentication, role-based access control, and input validation.
- **Monitoring Module**: Distributed tracing, metrics collection, and health checks.

## Technologies

### Core Framework

| Technology | Version | Purpose |
|------------|---------|---------|
| Spring Boot | 4.0.3 | Framework for building microservices |
| Spring Cloud | 2025.1.0 | Cloud-native application development |
| Java | 25 | Programming language |

### Data Management

| Technology | Purpose |
|------------|---------|
| Spring Data JPA | Object-relational mapping and database abstraction |
| MySQL | Relational database for persistent data storage |
| Flyway | Database migration and versioning |
| Redis | In-memory caching and session management |
| Jedis | Redis client for Java |

### Message Queue

| Technology | Purpose |
|------------|---------|
| Apache Kafka | Event streaming and message processing |
| Spring Kafka | Kafka integration with Spring |

### Security

| Technology | Version | Purpose |
|------------|---------|---------|
| Spring Security | Latest | Authentication and authorization |
| JWT (JJWT) | 0.11.5 | JSON Web Token handling |
| Jackson | Latest | JSON serialization/deserialization |

### Resilience and Monitoring

| Technology | Purpose |
|------------|---------|
| Resilience4j | Circuit breaker and fault tolerance patterns |
| Micrometer | Metrics collection and monitoring |
| Spring Boot Actuator | Application monitoring endpoints |
| Brave | Distributed tracing framework |
| Zipkin | Distributed tracing backend |

### API Documentation

| Technology | Version | Purpose |
|------------|---------|---------|
| SpringDoc OpenAPI | 3.0.2 | OpenAPI 3.0 documentation and Swagger UI |

### Development Tools

| Technology | Purpose |
|------------|---------|
| Maven | Dependency management and build automation |
| Lombok | Reduces boilerplate code with annotations |
| UserAgentUtils | User agent parsing and analysis |

### Infrastructure

| Technology | Purpose |
|------------|---------|
| Docker | Containerization |
| Docker Compose | Multi-container orchestration |
| Grafana | Metrics visualization and dashboards |
| phpMyAdmin | MySQL database management UI |
| Redis Insight | Redis management and monitoring |

## Prerequisites

### System Requirements

- **Java**: JDK 25 or later
- **Maven**: 3.6.0 or later
- **Docker**: 20.10 or later (for containerized deployment)
- **Docker Compose**: 2.0 or later (for running development environment)

### External Services

- **MySQL**: 8.0 or later (or use Docker Compose)
- **Redis**: 7.0 or later (or use Docker Compose)
- **Apache Kafka**: Latest version (or use Docker Compose)

### Network Requirements

- Ensure that ports 8080, 3306, 6379, 9092, 9411, 3000, and 5540 are available on your system or modify the docker-compose.yaml accordingly.

## Installation

### Using Docker Compose (Recommended for Development)

1. Clone the repository:
```bash
git clone https://github.com/omnigospel001/verve-guard-side-car.git
cd verve-guard-side-car
```

2. Start all services using Docker Compose:
```bash
docker-compose up -d
```

This will start:
- MySQL database
- Redis cache
- Apache Kafka and Zookeeper
- Zipkin (tracing backend)
- Grafana (monitoring)
- phpMyAdmin (database UI)
- Redis Insight (Redis UI)

### Manual Installation

1. Clone the repository:
```bash
git clone https://github.com/omnigospel001/verve-guard-side-car.git
cd verve-guard-side-car
```

2. Ensure MySQL and Redis are running on your system.

3. Configure the database connection in `application.properties` or `application.yml`.

4. Build the project:
```bash
./mvnw clean package
```

5. Run the application:
```bash
./mvnw spring-boot:run
```

## Configuration

### Application Properties

Create or update `src/main/resources/application.yml` with the following configurations:

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/verve_guard
    username: appuser
    password: apppass
    driver-class-name: com.mysql.cj.jdbc.Driver
  
  jpa:
    hibernate:
      ddl-auto: validate
    show-sql: false
    properties:
      hibernate:
        dialect: org.hibernate.dialect.MySQLDialect
  
  redis:
    host: localhost
    port: 6379
    timeout: 2000ms
    jedis:
      pool:
        max-active: 8
        max-idle: 8
        min-idle: 0
  
  kafka:
    bootstrap-servers: localhost:9092
    producer:
      key-serializer: org.apache.kafka.common.serialization.StringSerializer
      value-serializer: org.springframework.kafka.support.serializer.JsonSerializer
    consumer:
      bootstrap-servers: localhost:9092
      group-id: verve-guard-group
  
  mail:
    host: smtp.gmail.com
    port: 587
    username: your-email@gmail.com
    password: your-app-password
    properties:
      mail:
        smtp:
          auth: true
          starttls:
            enable: true
            required: true
  
  security:
    jwt:
      secret: your-secret-key-here
      expiration: 86400000  # 24 hours in milliseconds

server:
  port: 8080
  servlet:
    context-path: /api/v1

management:
  endpoints:
    web:
      exposure:
        include: health,metrics,prometheus,info
  tracing:
    sampling:
      probability: 1.0
  zipkin:
    tracing:
      endpoint: http://localhost:9411/api/v2/spans
```

### Database Migration

Flyway automatically manages database migrations. Place migration scripts in `src/main/resources/db/migration/` following the naming convention: `V{version}__{description}.sql`

### Security Configuration

- Update JWT secret in application properties
- Configure OAuth2/SAML providers if required
- Set up role-based access control policies
- Configure CORS if accessing from multiple origins

## Running the Application

### Using Docker Compose

```bash
# Start all services
docker-compose up -d

# View logs
docker-compose logs -f verve-guard

# Stop services
docker-compose down
```

### Using Maven

```bash
# Development mode with hot reload
./mvnw spring-boot:run

# With specific profile (dev, test, prod)
./mvnw spring-boot:run -Dspring-boot.run.arguments="--spring.profiles.active=dev"
```

### Using Java

```bash
# After building the project
java -jar target/verve-guard-0.0.1-SNAPSHOT.jar
```

## API Documentation

### Swagger UI

Once the application is running, access the interactive API documentation:

```
http://localhost:8080/swagger-ui.html
```

### OpenAPI JSON Schema

```
http://localhost:8080/v3/api-docs
```

### Key Endpoints

#### Transaction Analysis

- **POST** `/api/v1/transactions/analyze` - Analyze a transaction for fraud
  - Request: Transaction details (amount, merchant, customer, etc.)
  - Response: Risk score and authorization decision

#### Health Check

- **GET** `/api/v1/health` - Service health status
- **GET** `/actuator/health` - Spring Boot health endpoint

#### Metrics

- **GET** `/actuator/metrics` - Application metrics
- **GET** `/actuator/prometheus` - Prometheus metrics format

## Database Schema

### Core Tables

#### transactions
Stores transaction records and fraud analysis results.

```sql
CREATE TABLE transactions (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  transaction_id VARCHAR(255) UNIQUE NOT NULL,
  merchant_id BIGINT NOT NULL,
  customer_id BIGINT NOT NULL,
  amount DECIMAL(15, 2) NOT NULL,
  currency VARCHAR(3) NOT NULL,
  risk_score DECIMAL(5, 2),
  is_fraudulent BOOLEAN DEFAULT FALSE,
  authorization_status VARCHAR(50),
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  INDEX idx_customer (customer_id),
  INDEX idx_merchant (merchant_id),
  INDEX idx_created_at (created_at)
);
```

#### fraud_rules
Stores configurable fraud detection rules and patterns.

```sql
CREATE TABLE fraud_rules (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  rule_name VARCHAR(255) NOT NULL,
  rule_type VARCHAR(50) NOT NULL,
  threshold DECIMAL(5, 2),
  is_active BOOLEAN DEFAULT TRUE,
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);
```

#### audit_logs
Records all security-related events for compliance.

```sql
CREATE TABLE audit_logs (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  user_id BIGINT,
  action VARCHAR(255) NOT NULL,
  resource_type VARCHAR(100),
  resource_id BIGINT,
  status VARCHAR(50),
  details LONGTEXT,
  ip_address VARCHAR(45),
  user_agent VARCHAR(500),
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  INDEX idx_user (user_id),
  INDEX idx_created_at (created_at)
);
```

## Monitoring and Observability

### Health Checks

Monitor application health through Spring Boot Actuator:

```bash
curl http://localhost:8080/actuator/health
```

### Distributed Tracing

Track requests across services using Zipkin:

```
http://localhost:9411
```

### Metrics Dashboard

View real-time metrics and performance data using Grafana:

```
http://localhost:3000
```

Default credentials: `admin` / `admin`

### Logging

Configure logging levels in application.yml:

```yaml
logging:
  level:
    root: INFO
    com.verve.guard: DEBUG
    org.springframework.web: DEBUG
    org.hibernate.SQL: DEBUG
    org.hibernate.type.descriptor.sql.BasicBinder: TRACE
  pattern: "%d{yyyy-MM-dd HH:mm:ss} - %msg%n"
  file: logs/application.log
```

### Performance Monitoring

- Monitor heap memory and garbage collection
- Track request latency and throughput
- Monitor database connection pool status
- Observe Redis cache hit rates

## Development

### Project Structure

```
src/
├── main/
│   ├── java/com/verve/guard/
│   │   ├── controller/       # REST API controllers
│   │   ├── service/          # Business logic services
│   │   ├── repository/       # Data access layer
│   │   ├── entity/           # JPA entities
│   │   ├── dto/              # Data transfer objects
│   │   ├── security/         # Security configuration and JWT
│   │   ├── exception/        # Custom exceptions
│   │   ├── config/           # Spring configuration classes
│   │   ├── util/             # Utility classes
│   │   └── listener/         # Event listeners
│   └── resources/
│       ├── application.yml   # Application configuration
│       ├── db/migration/     # Flyway migrations
│       └── static/           # Static resources
└── test/
    ├── java/com/verve/guard/
    │   ├── controller/       # Controller tests
    │   ├── service/          # Service tests
    │   └── integration/      # Integration tests
    └── resources/
        └── application-test.yml
```

### Code Style

- Follow Google Java Style Guide
- Use meaningful variable and method names
- Add JavaDoc comments for public methods and classes
- Keep methods focused and concise (under 30 lines preferred)
- Use optional and streams instead of null checks

### Git Workflow

1. Create feature branch: `git checkout -b feature/description`
2. Make changes with atomic commits
3. Write descriptive commit messages
4. Push to repository and create pull request
5. Ensure all tests pass before merging

## Testing

### Running Tests

```bash
# Run all tests
./mvnw test

# Run specific test class
./mvnw test -Dtest=TransactionServiceTest

# Run with coverage
./mvnw test jacoco:report

# Run integration tests
./mvnw test -Dgroups=integration
```

### Test Structure

- **Unit Tests**: Test individual components in isolation
- **Integration Tests**: Test multiple components working together
- **Performance Tests**: Validate system performance under load

### Test Coverage

Aim for at least 80% code coverage. View coverage reports in `target/site/jacoco/index.html`

## Deployment

### Docker Image Build

```bash
# Build the Docker image
./mvnw spring-boot:build-image

# Tag and push to registry
docker tag verve-guard:latest your-registry/verve-guard:latest
docker push your-registry/verve-guard:latest
```

### Kubernetes Deployment

Update the deployment manifest with appropriate environment variables and resource limits, then apply:

```bash
kubectl apply -f k8s/deployment.yaml
```

### Environment-Specific Configuration

Create separate application profiles:
- `application-dev.yml` - Development configuration
- `application-test.yml` - Test configuration
- `application-prod.yml` - Production configuration

### Database Migrations

Ensure database migrations run automatically on startup or manually execute:

```bash
./mvnw flyway:migrate
```

### Production Checklist

- Enable HTTPS/TLS
- Configure environment variables for sensitive data
- Set up database backups
- Configure log aggregation
- Enable distributed tracing
- Set up monitoring and alerting
- Configure rate limiting
- Enable API authentication
- Test disaster recovery procedures

## Troubleshooting

### Common Issues

#### Database Connection Error
```
Error: Unable to connect to database
```
**Solution**: 
- Verify MySQL is running
- Check database credentials in application.yml
- Ensure MySQL port 3306 is accessible

#### Redis Connection Error
```
Error: Cannot connect to Redis
```
**Solution**:
- Verify Redis is running
- Check Redis configuration in application.yml
- Ensure Redis port 6379 is accessible
- Check firewall rules

#### Kafka Connection Error
```
Error: Cannot connect to Kafka broker
```
**Solution**:
- Verify Kafka and Zookeeper are running
- Check Kafka bootstrap servers configuration
- Verify network connectivity to Kafka broker

#### JWT Token Expired
```
Error: JWT token expired
```
**Solution**:
- Generate new token
- Check JWT expiration configuration
- Verify system time is synchronized

#### Memory Issues
```
java.lang.OutOfMemoryError: Java heap space
```
**Solution**:
- Increase JVM heap size: `java -Xmx2048m -jar application.jar`
- Review cache configuration and eviction policies
- Optimize database queries

### Debug Mode

Enable debug logging:

```bash
./mvnw spring-boot:run -Dspring-boot.run.arguments="--debug"
```

### Log Analysis

View application logs:

```bash
# Real-time logs
tail -f logs/application.log

# Search for errors
grep ERROR logs/application.log

# View last 100 lines
tail -100 logs/application.log
```

## Contributing

### Getting Started

1. Fork the repository
2. Clone your fork locally
3. Create a feature branch
4. Make your changes
5. Write tests for new functionality
6. Ensure all tests pass
7. Submit a pull request

### Pull Request Requirements

- Clear description of changes
- All tests passing
- Code coverage maintained or improved
- No breaking changes without discussion
- Updated documentation if applicable

### Code Review Process

- Minimum one approval required
- All automated checks must pass
- Meaningful feedback expected
- Respectful and constructive discussion

## License

This project is provided as-is for internal use. Refer to the LICENSE file for complete details.

---

**Last Updated**: May 15, 2026
**Version**: 0.0.1-SNAPSHOT
**Default Branch**: verve-guard

For questions or support, refer to project documentation or contact the development team.
