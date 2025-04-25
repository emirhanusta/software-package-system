# Repository API (Spring Boot)

This is a developer-friendly package repository system written in Java using Spring Boot.  
It allows uploading and downloading custom packages with metadata and versioning support.

---

##  Features

-  Upload and download packages
-  File system & object storage support (strategy pattern)
-  MinIO integration (S3-compatible)
-  PostgreSQL persistence
-  Dockerized and ready to run via `docker-compose`
-  Repsy integration for Maven & Docker Registry

---

##  Tech Stack

- Java 21 + Spring Boot
- PostgreSQL
- MinIO
- Docker + Docker Compose
- Maven (with custom libraries hosted on Repsy)

---

##  Quick Start

### 1. Clone the project

```
git clone https://github.com/emirhanusta/software-package-system
cd software-package-system
```
### 2. Run using Docker Compose

```
docker-compose up
```

### 3. Access the API
-  API: `http://localhost:8080`
-  MinIO: `http://localhost:9000`

## ⚙️ Configuration

### Environment Variables

| Variable                     | Default                 | Description                                                             |
|------------------------------|-------------------------|-------------------------------------------------------------------------|
| `SPRING_DATASOURCE_URL`      | -                       | PostgreSQL JDBC URL (e.g. `jdbc:postgresql://db:5432/package_systemDB`) |
| `SPRING_DATASOURCE_USERNAME` | `postgres`              | PostgreSQL username                                                     |
| `SPRING_DATASOURCE_PASSWORD` | `postgres`              | PostgreSQL password                                                     |
| `STORAGE_STRATEGY`           | `file-system`           | Storage strategy (`file-system` or `object-storage`)                    |
| `STORAGE_FS_BASE_PATH`       | `uploads`               | Base path for file system storage                                       |
| `MINIO_ENDPOINT`             | `http://localhost:9000` | MinIO server URL                                                        |
| `MINIO_ACCESS_KEY`           | `minioadmin`            | MinIO access key                                                        |
| `MINIO_SECRET_KEY`           | `minioadmin`            | MinIO secret key                                                        |
| `MINIO_BUCKET`               | `repsy-packages`        | MinIO bucket name                                                       |

#### Example YAML Configuration
```yml
spring:
  datasource:
    url: jdbc:postgresql://db:5432/package_systemDB
    username: postgres
    password: postgres

storage:
  strategy: file-system # or object-storage
  fs:
    base-path: uploads
  obj:
    endpoint: http://minio:9000
    access-key: minioadmin
    secret-key: minioadmin
    bucket: repsy-packages

```

##  API Endpoints

### 1. Upload a package

```
POST /{packageName}/{version}
Content-Type: multipart/form-data
```

#### Request Body
- `file`: your `.rep` package.
- `meta`: your `meta.json` file.

#### Example Request

```
curl -X POST http://localhost:8080/api/my-lib/1.0.0 \
  -F "meta=@meta.json" \
  -F "package=@package.rep"
```

### 2. Download a file

```
GET /{packageName}/{version}/{fileName}
```

#### Example Request

```
curl -X GET http://localhost:8080/api/my-lib/1.0.0/package.rep
```

### Custom Storage Libraries
This project uses strategy-based storage libraries hosted on Repsy:
- `com.repsy:storage-core`
- `com.repsy:storage-file-system`
- `com.repsy:storage-object`