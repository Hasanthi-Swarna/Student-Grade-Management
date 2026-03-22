# Student Grade Management System

A production-ready RESTful API for managing student records, grades, and academic analytics. Built with Spring Boot, secured with JWT authentication, and containerised with Docker.

---

## Tech Stack

| Layer | Technology |
|---|---|
| Backend | Java 17, Spring Boot 3.2 |
| Database | PostgreSQL 16 + Spring Data JPA |
| Security | Spring Security + JWT (JJWT) |
| Documentation | SpringDoc OpenAPI (Swagger UI) |
| Export | CSV via HttpServletResponse |
| Testing | JUnit 5, MockMvc, H2 in-memory |
| Containerisation | Docker, docker-compose |
| CI/CD | GitHub Actions |

---

## Features

- **JWT Authentication** with two roles: `TEACHER` (full access) and `STUDENT` (read-only, own records)
- **Full CRUD** for students and grades with Bean Validation
- **GPA computation** — auto-calculated from grades, letter grade assigned on save
- **Analytics endpoint** — class average, highest/lowest scores, top performer, subject-wise breakdown
- **CSV export** — download grade report per student
- **Swagger UI** — interactive API docs at `/swagger-ui.html`
- **11 JUnit tests** covering auth, CRUD, role enforcement, and analytics
- **CI pipeline** — tests + Docker build on every push via GitHub Actions

---

## Getting Started

### Option 1 — Docker (recommended, no Java install needed)

```bash
git clone https://github.com/Hasanthi-Swarna/student-grade-management.git
cd student-grade-management
docker-compose up --build
```

API available at: `http://localhost:8080`  
Swagger UI at: `http://localhost:8080/swagger-ui.html`

### Option 2 — Run locally

**Prerequisites:** Java 17, Maven 3.9+, PostgreSQL 16

```bash
# 1. Create the database
psql -U postgres -c "CREATE DATABASE gradedb;"

# 2. Clone and run
git clone https://github.com/Hasanthi-Swarna/student-grade-management.git
cd student-grade-management
mvn spring-boot:run
```

### Run tests

```bash
mvn clean test -Dspring.profiles.active=test
```

Tests use H2 in-memory DB — no PostgreSQL needed.

---

## API Reference

### Authentication

| Method | Endpoint | Role | Description |
|---|---|---|---|
| POST | `/api/auth/register` | Public | Register new user |
| POST | `/api/auth/login` | Public | Login, returns JWT token |

### Students

| Method | Endpoint | Role | Description |
|---|---|---|---|
| GET | `/api/students` | TEACHER, STUDENT | Get all students |
| GET | `/api/students/{id}` | TEACHER, STUDENT | Get student by ID |
| POST | `/api/students` | TEACHER | Create student |
| PUT | `/api/students/{id}` | TEACHER | Update student |
| DELETE | `/api/students/{id}` | TEACHER | Delete student |

### Grades

| Method | Endpoint | Role | Description |
|---|---|---|---|
| GET | `/api/grades/student/{id}` | TEACHER, STUDENT | Get grades for student |
| POST | `/api/grades/student/{id}` | TEACHER | Add grade |
| PUT | `/api/grades/{gradeId}` | TEACHER | Update grade |
| DELETE | `/api/grades/{gradeId}` | TEACHER | Delete grade |
| GET | `/api/grades/student/{id}/export/csv` | TEACHER, STUDENT | Download CSV report |

### Analytics

| Method | Endpoint | Role | Description |
|---|---|---|---|
| GET | `/api/analytics/class` | TEACHER, STUDENT | Class-wide stats |
| GET | `/api/analytics/student/{id}/gpa` | TEACHER, STUDENT | Student GPA |

---

## Quick API Walkthrough

```bash
# 1. Register a teacher
curl -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{"name":"Hasanthi","email":"teacher@uni.com","password":"pass123","role":"TEACHER"}'

# 2. Login and get token
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"teacher@uni.com","password":"pass123"}'
# → {"token":"eyJ...","role":"TEACHER","name":"Hasanthi"}

# 3. Add a student (use token from step 2)
curl -X POST http://localhost:8080/api/students \
  -H "Authorization: Bearer <token>" \
  -H "Content-Type: application/json" \
  -d '{"name":"Alice Kumar","rollNumber":"CS001","department":"Computer Science","year":2}'

# 4. Add a grade
curl -X POST http://localhost:8080/api/grades/student/1 \
  -H "Authorization: Bearer <token>" \
  -H "Content-Type: application/json" \
  -d '{"subject":"Mathematics","marks":87.5}'
# → {"id":1,"subject":"Mathematics","marks":87.5,"grade":"A"}

# 5. Get analytics
curl http://localhost:8080/api/analytics/class \
  -H "Authorization: Bearer <token>"
```

---

## Grade Calculation

| Marks | Letter Grade |
|---|---|
| 90 – 100 | A+ |
| 80 – 89 | A |
| 70 – 79 | B |
| 60 – 69 | C |
| 50 – 59 | D |
| Below 50 | F |

---

## Project Structure

```
src/main/java/com/hasanthi/grades/
├── GradeManagementApplication.java   # Entry point
├── controller/
│   ├── AuthController.java           # Register + login
│   ├── StudentController.java        # Student CRUD
│   ├── GradeController.java          # Grade CRUD + CSV export
│   └── AnalyticsController.java      # Class analytics + GPA
├── model/
│   ├── User.java                     # TEACHER / STUDENT roles
│   ├── Student.java                  # Student entity
│   └── Grade.java                    # Grade entity (auto letter grade)
├── repository/
│   └── Repositories.java             # JPA repos + analytics queries
├── security/
│   ├── JwtUtils.java                 # Token generation + validation
│   └── SecurityConfig.java          # Filter chain + role rules
└── exception/
    └── ExceptionConfig.java          # Global handler + Swagger config

src/test/java/com/hasanthi/grades/
└── GradeManagementApplicationTests.java   # 11 integration tests

.github/workflows/
└── ci.yml                            # Run tests + build Docker on push
```

---

## Environment Variables

| Variable | Default | Description |
|---|---|---|
| `DB_URL` | `jdbc:postgresql://localhost:5432/gradedb` | PostgreSQL connection URL |
| `DB_USERNAME` | `postgres` | Database username |
| `DB_PASSWORD` | `postgres` | Database password |
| `JWT_SECRET` | (base64 key) | JWT signing secret — change in production |

---

## License

MIT License — free to use, modify, and distribute with attribution.
