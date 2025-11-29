# Sevika Task Management System

A comprehensive Task Management REST API built with Spring Boot demonstrating software engineering best practices and design principles.

## 🎯 Features

- RESTful API for Task Management
- User Authentication & Authorization (JWT)
- Role-Based Access Control (RBAC)
- PostgreSQL Database Integration
- Complete CRUD Operations
- API Documentation (Swagger/OpenAPI)
- Docker Support
- Comprehensive Testing

## 🏗️ Architecture & Design Patterns

### Clean Architecture (Layered Architecture)
```
├── Controller Layer (REST API)
├── Service Layer (Business Logic)
├── Repository Layer (Data Access)
└── Domain Layer (Entities & DTOs)
```

### Design Principles Implemented
- **SOLID Principles**
  - Single Responsibility Principle
  - Open/Closed Principle
  - Liskov Substitution Principle
  - Interface Segregation Principle
  - Dependency Inversion Principle
- **Domain-Driven Design (DDD)**
- **Repository Pattern**
- **DTO Pattern**
- **Builder Pattern**
- **Factory Pattern**

### Design Patterns
- Repository Pattern
- Service Layer Pattern
- DTO Pattern (with MapStruct)
- Builder Pattern (Lombok)
- Strategy Pattern (Authentication)
- Chain of Responsibility (Filter Chain)

## 🛠️ Technology Stack

- **Java 17**
- **Spring Boot 3.2.0**
- **Spring Security** (JWT Authentication)
- **Spring Data JPA**
- **PostgreSQL**
- **Flyway** (Database Migrations)
- **Lombok** (Boilerplate Reduction)
- **MapStruct** (Object Mapping)
- **SpringDoc OpenAPI** (API Documentation)
- **TestContainers** (Integration Testing)
- **JUnit 5 & Mockito** (Testing)
- **Docker & Docker Compose**

## 📦 Project Structure

```
online.sevika.tm/
├── config/              # Configuration classes
├── controller/          # REST Controllers
├── dto/                 # Data Transfer Objects
├── entity/              # JPA Entities
├── repository/          # JPA Repositories
├── service/             # Business Logic
│   ├── impl/           # Service Implementations
│   └── interfaces/     # Service Interfaces
├── security/            # Security Configuration
│   ├── jwt/            # JWT Implementation
│   └── filter/         # Security Filters
├── exception/           # Custom Exceptions & Handlers
├── mapper/              # MapStruct Mappers
├── aspect/              # AOP Aspects
└── util/                # Utility Classes
```

## 🚀 Getting Started

### Prerequisites
- Java 17+
- Maven 3.8+
- Docker & Docker Compose
- PostgreSQL (or use Docker)

### Running with Docker

```bash
# Build and start all services
docker-compose up -d

# View logs
docker-compose logs -f app

# Stop services
docker-compose down
```

The application will be available at `http://localhost:8080`

### Running Locally

1. **Start PostgreSQL**
```bash
docker run -d \
  --name postgres-db \
  -e POSTGRES_DB=sevika_task_db \
  -e POSTGRES_USER=sevika \
  -e POSTGRES_PASSWORD=sevika123 \
  -p 5432:5432 \
  postgres:16
```

2. **Build the application**
```bash
mvn clean install
```

3. **Run the application**
```bash
mvn spring-boot:run
```

Or using the JAR:
```bash
java -jar target/sevika-task-management-1.0.0.jar
```

## 📚 API Documentation

Once the application is running, access:

- **Swagger UI**: http://localhost:8080/swagger-ui.html
- **OpenAPI JSON**: http://localhost:8080/v3/api-docs
- **Health Check**: http://localhost:8080/actuator/health

## 🔐 Authentication

### Register a new user
```bash
POST /api/auth/register
{
  "username": "john",
  "email": "john@example.com",
  "password": "Password123!",
  "firstName": "John",
  "lastName": "Doe"
}
```

### Login
```bash
POST /api/auth/login
{
  "username": "john",
  "password": "Password123!"
}
```

Returns a JWT token to be used in subsequent requests:
```
Authorization: Bearer <your-jwt-token>
```

## 📝 API Endpoints

### Authentication
- `POST /api/auth/register` - Register new user
- `POST /api/auth/login` - Login and get JWT token

### Users
- `GET /api/users` - Get all users (ADMIN only)
- `GET /api/users/{id}` - Get user by ID
- `GET /api/users/me` - Get current authenticated user
- `PUT /api/users/{id}` - Update user
- `DELETE /api/users/{id}` - Delete user (ADMIN only)

### Projects
- `POST /api/projects` - Create project
- `GET /api/projects` - Get all projects
- `GET /api/projects/{id}` - Get project by ID
- `GET /api/projects/my-projects` - Get user's owned projects
- `PUT /api/projects/{id}` - Update project
- `DELETE /api/projects/{id}` - Delete project

### Tasks
- `POST /api/tasks` - Create task
- `GET /api/tasks` - Get all tasks (with filters)
- `GET /api/tasks/{id}` - Get task by ID
- `GET /api/tasks/my-tasks` - Get current user's tasks
- `GET /api/tasks/overdue` - Get overdue tasks
- `GET /api/tasks/project/{projectId}` - Get tasks by project
- `GET /api/tasks/assignee/{userId}` - Get tasks assigned to user
- `PUT /api/tasks/{id}` - Update task
- `PATCH /api/tasks/{id}/status` - Update task status
- `DELETE /api/tasks/{id}` - Delete task

### Teams
- `POST /api/teams` - Create team
- `GET /api/teams` - Get all teams
- `GET /api/teams/{id}` - Get team by ID
- `GET /api/teams/my-teams` - Get user's owned teams
- `GET /api/teams/memberships` - Get user's team memberships
- `PUT /api/teams/{id}` - Update team
- `DELETE /api/teams/{id}` - Delete team
- `GET /api/teams/{id}/members` - Get team members
- `POST /api/teams/{id}/members` - Add team member
- `DELETE /api/teams/{teamId}/members/{memberId}` - Remove team member
- `PATCH /api/teams/{teamId}/members/{memberId}/role` - Update member role

### Permissions
- `POST /api/permissions/projects/{projectId}/grant` - Grant project permission
- `DELETE /api/permissions/projects/{projectId}/revoke` - Revoke project permission
- `GET /api/permissions/projects/{projectId}` - Get project permissions
- `POST /api/permissions/tasks/{taskId}/grant` - Grant task permission
- `DELETE /api/permissions/tasks/{taskId}/revoke` - Revoke task permission
- `GET /api/permissions/tasks/{taskId}` - Get task permissions

### Subscriptions
- `POST /api/subscriptions` - Create subscription
- `POST /api/subscriptions/verify-payment` - Verify payment
- `GET /api/subscriptions/active` - Get active subscription
- `GET /api/subscriptions` - Get all user subscriptions
- `GET /api/subscriptions/{id}` - Get subscription by ID
- `DELETE /api/subscriptions/{id}` - Cancel subscription
- `GET /api/subscriptions/payments` - Get payment history

### Subscription Plans
- `POST /api/subscription-plans` - Create plan (ADMIN only)
- `GET /api/subscription-plans` - Get all plans
- `GET /api/subscription-plans/active` - Get active plans
- `GET /api/subscription-plans/{id}` - Get plan by ID
- `PUT /api/subscription-plans/{id}` - Update plan (ADMIN only)
- `DELETE /api/subscription-plans/{id}` - Delete plan (ADMIN only)

### Audit Logs
- `GET /api/audit-logs` - Get all audit logs (ADMIN only)
- `GET /api/audit-logs/entity/{entityType}/{entityId}` - Get logs by entity
- `GET /api/audit-logs/user/{userId}` - Get logs by user
- `GET /api/audit-logs/recent` - Get recent activity
- `GET /api/audit-logs/entity/{entityType}/{entityId}/count` - Get entity log count
- `GET /api/audit-logs/user/{userId}/count` - Get user log count

## 🧪 Testing

### Run all tests
```bash
mvn test
```

### Run with coverage
```bash
mvn clean test jacoco:report
```

Coverage report will be available at `target/site/jacoco/index.html`

## 🏗️ Database Schema

### Users Table
- id (UUID, PK)
- username (unique)
- email (unique)
- password (encrypted)
- first_name
- last_name
- role (ADMIN, USER)
- created_at
- updated_at

### Projects Table
- id (UUID, PK)
- name
- description
- owner_id (FK -> Users)
- status (ACTIVE, COMPLETED, ARCHIVED)
- created_at
- updated_at

### Tasks Table
- id (UUID, PK)
- title
- description
- status (TODO, IN_PROGRESS, DONE)
- priority (LOW, MEDIUM, HIGH)
- project_id (FK -> Projects)
- assigned_to (FK -> Users)
- created_by (FK -> Users)
- due_date
- created_at
- updated_at

## 🔧 Configuration

### Application Profiles
- `dev` - Development profile
- `prod` - Production profile
- `test` - Testing profile

Set active profile:
```bash
mvn spring-boot:run -Dspring-boot.run.profiles=dev
```

## 📊 Monitoring

Health check endpoint:
```bash
curl http://localhost:8080/actuator/health
```

Metrics endpoint:
```bash
curl http://localhost:8080/actuator/metrics
```

## 🛡️ Security Features

- Password encryption (BCrypt)
- JWT token-based authentication
- Role-based authorization
- Method-level security
- CORS configuration
- Request validation
- SQL injection prevention

## 📄 License

This project is licensed under the MIT License.

## 👥 Contributing

Contributions are welcome! Please feel free to submit a Pull Request.

## 📧 Contact

For questions or feedback, please contact: info@sevika.online
