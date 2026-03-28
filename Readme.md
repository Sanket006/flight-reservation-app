# ✈️ Flight Reservation System

<div align="center">

![Java](https://img.shields.io/badge/Java-ED8B00?style=for-the-badge&logo=openjdk&logoColor=white)
![Spring Boot](https://img.shields.io/badge/Spring_Boot-6DB33F?style=for-the-badge&logo=springboot&logoColor=white)
![React](https://img.shields.io/badge/React-61DAFB?style=for-the-badge&logo=react&logoColor=black)
![Vite](https://img.shields.io/badge/Vite-646CFF?style=for-the-badge&logo=vite&logoColor=white)
![MySQL](https://img.shields.io/badge/MySQL-4479A1?style=for-the-badge&logo=mysql&logoColor=white)
![Apache](https://img.shields.io/badge/Apache2-D22128?style=for-the-badge&logo=apache&logoColor=white)
![Docker](https://img.shields.io/badge/Docker-2496ED?style=for-the-badge&logo=docker&logoColor=white)
![Kubernetes](https://img.shields.io/badge/Kubernetes-326CE5?style=for-the-badge&logo=kubernetes&logoColor=white)

*A full-stack Flight Reservation web application — Spring Boot REST API + React frontend + MySQL, deployed on Linux with production-grade containerization*

</div>

---

## 📌 Overview

A full-stack **Flight Reservation System** that allows users to search, book, and manage flight reservations. The backend is a **Spring Boot REST API** connected to a **MySQL** database, and the frontend is a **React (Vite)** app served via **Apache2**.

This project demonstrates end-to-end application deployment on Linux — from database provisioning to backend packaging with Maven and frontend build with Vite — and has been extended with **Docker multi-stage builds** (achieving 60% smaller images) and **Kubernetes orchestration** with HPA for production scalability.

---

## 🏗️ Architecture

```
  Browser
     │
     ▼
┌─────────────────────────────────────────────┐
│              Linux Server                    │
│                                             │
│  ┌──────────────────────────────────────┐   │
│  │         Apache2 Web Server           │   │
│  │         Port: 80                     │   │
│  │  Serves React build (/var/www/html)  │   │
│  └────────────────┬─────────────────────┘   │
│                   │ API calls               │
│  ┌────────────────▼─────────────────────┐   │
│  │      Spring Boot REST API            │   │
│  │      Port: 8080                      │   │
│  │      JAR via Maven build             │   │
│  └────────────────┬─────────────────────┘   │
│                   │ JDBC                    │
│  ┌────────────────▼─────────────────────┐   │
│  │           MySQL Server               │   │
│  │           Port: 3306                 │   │
│  │           Database: flightdb         │   │
│  └──────────────────────────────────────┘   │
└─────────────────────────────────────────────┘
```

---

## 📁 Repository Structure

```
Flight-reservation/
├── FlightReservationSystem/        # Spring Boot backend
│   ├── src/
│   │   └── main/
│   │       ├── java/               # Controllers, services, models, repos
│   │       └── resources/
│   │           └── application.properties
│   └── pom.xml                     # Maven build config
├── frontend/                       # React + Vite frontend
│   ├── src/
│   │   ├── components/
│   │   └── App.jsx
│   ├── package.json
│   └── vite.config.js
└── README.md
```

---

## 🛠️ Tech Stack

| Layer | Technology |
|---|---|
| Backend | Java 17 · Spring Boot · Spring Data JPA · Maven |
| Frontend | React 18 · Vite · Axios |
| Database | MySQL Server |
| Web Server | Apache2 (serves React build) |
| Containerization | Docker (multi-stage builds — 60% smaller images) |
| Orchestration | Kubernetes + HPA (production deployment) |

---

## ⚙️ Environment Variables

| Variable | Description | Example |
|---|---|---|
| `DATASOURCE_URL` | MySQL JDBC connection URL | `jdbc:mysql://localhost:3306/flightdb` |
| `DATASOURCE_USER` | MySQL username | `linux` |
| `DATASOURCE_PASSWORD` | MySQL password | `your_password` |
| `FRONTEND_URL` | Allowed CORS origin | `http://localhost:80` |
| `VITE_API_URL` | Backend API URL for frontend | `http://localhost:8080` |

> ⚠️ Never hardcode credentials. Always use environment variables or a secrets manager in production.

---

## 🚀 Deployment Guide (Linux)

### Prerequisites

```bash
# Update system packages
apt update -y
```

---

### Step 1 — Set Up MySQL Database

```bash
# Install MySQL
apt install mysql-server -y
mysql_secure_installation

# Login and create DB + user
mysql -uroot -p
```

```sql
CREATE USER 'linux'@'localhost' IDENTIFIED BY 'your_secure_password';
GRANT ALL PRIVILEGES ON *.* TO 'linux'@'localhost';
FLUSH PRIVILEGES;
CREATE DATABASE flightdb;
EXIT;
```

---

### Step 2 — Deploy Spring Boot Backend

```bash
# Install Java 17 and Maven
apt install openjdk-17-jdk maven -y

# Clone the repository
git clone https://github.com/Sanket006/flight-reservation-microservices.git
cd flight-reservation-microservices/FlightReservationSystem

# Set environment variables
export DATASOURCE_URL="jdbc:mysql://localhost:3306/flightdb"
export DATASOURCE_USER="linux"
export DATASOURCE_PASSWORD="your_secure_password"
export FRONTEND_URL="http://localhost:80"

# Build and run
mvn clean package -DskipTests
java -jar target/flight*.jar
```

Backend runs at: `http://localhost:8080`

---

### Step 3 — Deploy React Frontend

> Open a **new terminal tab** for this step.

```bash
cd flight-reservation-microservices/frontend

# Install Node.js
apt install nodejs npm -y

# Set backend API URL
export VITE_API_URL=http://localhost:8080

# Install dependencies and build
npm install
npm run build

# Serve via Apache2
apt install apache2 -y
cp -r dist/* /var/www/html/
systemctl start apache2
systemctl enable apache2
```

Frontend runs at: `http://localhost:80`

---

### Step 4 — Verify Everything Is Running

```bash
# Check backend is up
curl http://localhost:8080/actuator/health

# Check Apache2 is serving frontend
curl http://localhost:80

# Check MySQL is running
systemctl status mysql
```

---

## 🐳 Docker Deployment (Multi-Stage Build)

A multi-stage Dockerfile reduces the final image size by **60%** by excluding Maven, source code, and build tools from the runtime image.

```dockerfile
# ─── Stage 1: Build ─────────────────────────────────────
FROM maven:3.9-eclipse-temurin-17 AS builder
WORKDIR /app
COPY pom.xml .
RUN mvn dependency:go-offline
COPY src ./src
RUN mvn clean package -DskipTests

# ─── Stage 2: Runtime (lean image) ──────────────────────
FROM eclipse-temurin:17-jre-alpine AS runtime
WORKDIR /app
COPY --from=builder /app/target/flight*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
```

```bash
# Build and run
docker build -t flight-reservation-backend:latest .
docker run -d -p 8080:8080 \
  -e DATASOURCE_URL="jdbc:mysql://<db-host>:3306/flightdb" \
  -e DATASOURCE_USER="linux" \
  -e DATASOURCE_PASSWORD="your_password" \
  flight-reservation-backend:latest
```

---

## ☸️ Kubernetes Deployment (Production)

For production, the app is deployed on **Kubernetes with HPA** for automatic scaling under traffic load.

```bash
# Deploy all K8s resources
kubectl apply -f k8s/

# Verify
kubectl get pods -n flight-app
kubectl get hpa  -n flight-app

# Rolling update (zero downtime)
kubectl set image deployment/flight-backend \
  flight-backend=<registry>/flight-backend:v2.0 \
  -n flight-app
kubectl rollout status deployment/flight-backend -n flight-app
```

---

## 📊 Impact

| Metric | Before | After |
|---|---|---|
| Docker image size | ~450 MB | ~180 MB (**60% reduction**) |
| Deployment downtime | Manual restart | **Zero-downtime** rolling update |
| Scaling under load | Manual | **Auto** via HPA |
| Infra provisioning | ~2 hrs manual | **~8 min** via Terraform |

---

## 🔗 Related Projects

- [flight-reservation-aws-infra](https://github.com/Sanket006/flight-reservation-aws-infra) — Terraform IaC provisions EKS, RDS MySQL, and S3 static site on AWS 
- [jenkins-cicd-pipelines](https://github.com/Sanket006/jenkins-cicd-pipelines) — Jenkins pipeline that builds the Docker image and deploys to K8s
- [terraform-aws-iac](https://github.com/Sanket006/terraform-aws-iac) — Terraform modules for provisioning the AWS infrastructure this runs on

---

## 👨‍💻 Author

**Sanket Ajay Chopade** — DevOps Engineer

[![LinkedIn](https://img.shields.io/badge/LinkedIn-0077B5?style=flat&logo=linkedin&logoColor=white)](https://www.linkedin.com/in/sanketchopade07)
[![GitHub](https://img.shields.io/badge/GitHub-181717?style=flat&logo=github&logoColor=white)](https://github.com/Sanket006)
