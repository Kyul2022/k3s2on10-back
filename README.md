# k3s2on10-back

A **Spring Boot** REST API (Java 21) for managing a book library, backed by **MySQL**, containerized with Docker, and deployed to a **k3s** cluster through a **Jenkins** pipeline running natively on Kubernetes.

This repo is the **application backend** of the project. It pairs with [`DevOpsPortfolio1st-k8sInfra`](https://github.com/Kyul2022/DevOpsPortfolio1st-k8sInfra), which holds the manifests tracked by ArgoCD for a GitOps deployment of the same service.

## Tech stack

- **Java 21** / **Spring Boot 4** (`spring-boot-starter-data-jpa`, `spring-boot-starter-webmvc`)
- **MySQL** (via `mysql-connector-j`) in production, **H2** in-memory for tests (`test` profile)
- **Lombok**
- **Maven** (wrapper `mvnw` included)
- **Docker** (multi-stage build)
- **Jenkins** (pipeline as code, ephemeral Kubernetes agents)
- **Kaniko** (daemonless image builds)
- **SonarQube** (`sonar-project.properties` for static analysis)

## Project structure

```
src/main/java/com/example/k3s_2on10/
├── K3s2on10Application.java        # Spring Boot entry point
├── controllers/BookController.java # /api/books REST endpoints
├── model/Book.java                 # JPA entity (id, title, author, year)
└── repositories/BookRepository.java# JpaRepository<Book, Long>

manifests/          # K8s manifests for a direct deployment (kubectl apply)
Dockerfile           # multi-stage build (Maven -> JRE 21)
Jenkinsfile          # CI/CD pipeline
```

## API

Base path: `/api/books`

| Method | Endpoint          | Description                       |
|--------|--------------------|------------------------------------|
| GET    | `/api/books`       | List all books (404 if empty)     |
| POST   | `/api/books`       | Create a book                     |
| DELETE | `/api/books/{id}`  | Delete a book by id                |

CORS is fully open (`@CrossOrigin(origins = "*")`) to ease testing from an external frontend.

## Configuration

The application reads its MySQL connection settings from environment variables (with sensible local defaults):

| Variable      | Default      |
|---------------|--------------|
| `DB_HOST`     | `localhost`  |
| `DB_PORT`     | `3306`       |
| `DB_NAME`     | `mydb`       |
| `DB_USER`     | `root`       |
| `DB_PASSWORD` | `pwd`        |
| `SERVER_PORT` | `8080`       |

`spring.jpa.hibernate.ddl-auto=update`: the schema is auto-synced at startup.

## Run locally

```bash
./mvnw spring-boot:run
```

Or build the jar:

```bash
./mvnw package -DskipTests
java -jar target/*.jar
```

## Tests

```bash
./mvnw test
```

Tests run under the `test` profile against an **in-memory H2 database** (`application-test.properties`), so no MySQL instance is required to run them.

## Docker

Multi-stage build: Maven compilation inside a `maven:3.9-eclipse-temurin-21` container, then a lightweight final image based on `eclipse-temurin:21-jre`.

```bash
docker build -t k3s2on10-back .
docker run -p 8080:8080 k3s2on10-back
```

## CI/CD — Jenkinsfile

The pipeline runs entirely on **ephemeral Kubernetes agents** (no static Jenkins agent):

1. **Build image**: a pod with a **Kaniko** container builds the image from the `Dockerfile` (authenticating to Docker Hub via a secret mounted as a volume) and pushes it to `kyul1234/k3s2on10:latest`.
2. **Deploy to a pod**: a pod running `alpine/k8s`, using the `k3s2on10` `ServiceAccount`, sequentially applies the manifests in the `manifests/` folder (`config`, `secrets`, `pvc`, `mysql`, `backend`) to the `k3s2on10` namespace.

> The `manifests/` folder corresponds to an **imperative** deployment path (`kubectl apply` triggered by Jenkins), distinct from the `dev/` folder in the `DevOpsPortfolio1st-k8sInfra` repo, which describes the **declarative / GitOps** deployment path driven by ArgoCD (`argocd` namespace → `myapp`, image tagged by commit SHA).

## Relation to the infrastructure repo

This backend is packaged as a Docker image published to Docker Hub, then deployed to the k3s cluster either:
- directly by this Jenkins pipeline (`k3s2on10` namespace), or
- through ArgoCD, which continuously syncs the manifests from [`DevOpsPortfolio1st-k8sInfra`](https://github.com/Kyul2022/DevOpsPortfolio1st-k8sInfra) (`myapp` namespace).
