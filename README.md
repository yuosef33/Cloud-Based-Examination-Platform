# ☁️ Cloud-Based Lab Examination Platform

> **Individual Graduation Project** — A full-stack cloud platform that automates the entire lifecycle of practical lab examinations, from real-time VM provisioning to automated file collection, built with Spring Boot, React, and AWS.

🔗 **Frontend Repository:** [Graduation-Project-Frontend](https://github.com/yuosef33/Graduation-Project-Frontend)

---

## 📌 Overview

Traditional practical lab exams require physical machines, manual setup, and on-site supervision. This platform eliminates all of that.

When a student clicks **"Attend Exam"**, the system automatically provisions a dedicated cloud VM (Windows or Linux) just for them — accessible directly in the browser via VNC. When the exam ends, the platform automatically stops all VMs, and the instructor can trigger file collection with one click — pulling every student's submission from their VM and storing it securely in AWS S3.

---

## 🎯 Key Features

- 🖥️ **Real-Time VM Provisioning** — Dedicated AWS EC2 instance per student, launched automatically on exam join
- 🌐 **Browser-Based VM Access** — Full desktop interaction via VNC over WebSockets (no client software needed)
- 🪟🐧 **Multi-OS Support** — Windows and Linux exam environments from custom AMI templates
- 📁 **Automated File Collection** — Post-exam parallel file collection from all student VMs using AWS SSM (Windows) and Ansible (Linux)
- ☁️ **S3 Storage** — All student submissions organized and stored in AWS S3 (`labs/{labId}/{studentId}/`)
- ⏱️ **Lab Scheduling** — Automated lab start/finish lifecycle with Spring TaskScheduler
- 🔐 **Secure Authentication** — JWT, refresh token rotation, and Google OAuth2
- 👥 **Role-Based Access** — Student and Admin roles with protected routes
- 📊 **Admin Dashboard** — Create lab templates, schedule labs, monitor status, collect and download student files
- 🔄 **VM Lifecycle Management** — Automated cleanup scheduler for terminated and waiting VMs

---

## 🏗️ System Architecture

```
┌─────────────────────────────────────────────────────────┐
│                     React Frontend                       │
│         VNC Screen │ Countdown Timer │ Admin Dashboard   │
└──────────────────────────┬──────────────────────────────┘
                           │ REST API (JWT)
┌──────────────────────────▼──────────────────────────────┐
│                  Spring Boot Backend                      │
│                                                           │
│  ┌─────────────┐  ┌──────────────┐  ┌────────────────┐  │
│  │ Lab Scheduler│  │ File Collection│  │  VM Lifecycle  │  │
│  │ (TaskScheduler)│  │  Service     │  │   Manager      │  │
│  └─────────────┘  └──────────────┘  └────────────────┘  │
└──────────────────────────┬──────────────────────────────┘
                           │ AWS SDK
┌──────────────────────────▼──────────────────────────────┐
│                        AWS                               │
│                                                           │
│   EC2 Instances    │    S3 Bucket    │    SSM Agent      │
│  (Student VMs)     │  (Submissions)  │  (File Collect)   │
└─────────────────────────────────────────────────────────┘
```

---

## 🔄 Exam Lifecycle

```
Admin creates Lab Template
        ↓
   Boots base VM → configures via VNC → saves as AMI
        ↓
Admin schedules Lab (start time + duration + template)
        ↓
   Scheduler auto-starts lab at scheduled time
        ↓
Student clicks "Attend Exam"
        ↓
   Backend provisions dedicated EC2 from AMI
   Student gets VNC access in browser
        ↓
   Countdown timer runs → exam ends
        ↓
   All VMs stopped → status: WAITING
        ↓
Admin clicks "Collect Files"
        ↓
   VMs restarted in parallel (CompletableFuture thread pool)
   Windows → AWS SSM runs PowerShell → uploads to S3
   Linux   → Ansible playbook runs   → uploads to S3
        ↓
   VMs terminated → files available for download
```

---

## 🛠️ Tech Stack

### Backend
| Technology | Usage |
|---|---|
| Java 21 + Spring Boot 3 | Core backend framework |
| Spring Security | Authentication & authorization |
| JWT + OAuth2 (Google) | Secure authentication |
| Spring TaskScheduler | Lab lifecycle automation |
| AWS SDK (EC2, SSM, S3) | Cloud infrastructure management |
| Terraform | Infrastructure as Code (base template provisioning) |
| Ansible | Linux VM file collection automation |
| MySQL | Primary database |
| Docker | Local development environment |
| Swagger / OpenAPI | API documentation |
| Bucket4j | Rate limiting |

### Frontend
| Technology | Usage |
|---|---|
| React 19 + Vite | Frontend framework |
| react-vnc | Browser-based VNC client |
| Tailwind CSS v4 | Styling |
| react-router-dom v7 | Client-side routing |
| Formik + Yup | Form handling & validation |
| Axios | HTTP client |

### AWS Services
| Service | Usage |
|---|---|
| EC2 | Student VM instances |
| AMI | Lab environment templates |
| S3 | Student file submissions storage |
| SSM (Systems Manager) | Remote command execution on Windows VMs |
| IAM | Role-based access for EC2 instances |

---

## 🖥️ Base VM Configuration

### Windows AMI Setup
- TigerVNC Server with auto-start on boot
- Websockify for WebSocket-to-VNC bridge (port 6080)
- Virtual Display Driver configured at 1920×1080
- Resolution scheduled task to persist across reboots
- AWS SSM Agent enabled with IAM role for S3 access
- Hibernate enabled for faster VM resume

### Linux (Ubuntu) AMI Setup
- XFCE desktop environment
- TigerVNC standalone server (port 5901)
- Websockify bridge (port 6080)
- Both services configured as systemd services for auto-start
- AWS SSM Agent via snap
- AWS CLI v2 installed
- Ansible-ready for post-exam file collection

---

## 📁 File Collection System

After an exam ends, the admin triggers file collection with a single click:

**Windows VMs** — AWS SSM sends a PowerShell script:
```powershell
aws s3 cp $sourcePath s3://bucket/labs/{labId}/{studentId}/ --recursive
```

**Linux VMs** — Ansible playbook runs via SSH:
```yaml
- name: Upload student files to S3
  command: aws s3 cp {{ source_dir }} s3://bucket/labs/{{ lab_id }}/{{ student_id }}/ --recursive
```

All VMs are processed **simultaneously** using Java's `CompletableFuture` thread pool — total collection time equals the time for one VM, not all VMs combined.

---

## 🔐 Authentication Flow

```
Local Login → JWT Access Token + Refresh Token
Google OAuth2 → OAuth2 callback → JWT issued
Refresh Token → Auto-rotation on every refresh
Role: ADMIN → /admin routes
Role: USER  → /home + /exam routes
```

---

## 📂 Project Structure

```
src/
├── config/          — Security, AWS, Async, Swagger, OAuth2
│   ├── JWT/         — Token filter and handler
│   ├── Bucket4J/    — Rate limiting
│   └── OnRunTimeCode/ — Startup data initialization
├── controller/      — Auth, Lab, Student, Business endpoints
├── models/          — Entities + DTOs + Mappers
├── repository/      — JPA DAOs
└── services/
    ├── Impl/        — Business logic
    │   ├── TerraformService.java      — EC2 provisioning via AWS SDK
    │   ├── FileCollectionService.java — Parallel file collection
    │   ├── S3Service.java             — File listing + presigned URLs
    │   ├── LabServiceImpl.java        — Lab management
    │   └── schedule/
    │       ├── LabScheduler.java      — Lab lifecycle automation
    │       └── VmCleanupScheduler.java — VM cleanup
    └── terraform-template/            — Terraform IaC files
```

---

## ⚙️ Environment Variables

```yaml
AWS_ACCESS_KEY=your-access-key
AWS_SECRET_KEY=your-secret-key
AWS_REGION=eu-central-1
JWT_SECRET_KEY=your-jwt-secret
GOOGLE_CLIENT_ID=your-google-client-id
GOOGLE_CLIENT_SECRET=your-google-client-secret
DB_URL=your-mysql-url
DB_USERNAME=your-db-username
DB_PASSWORD=your-db-password
```

---

## 🚀 Running Locally

```bash
# Clone the repository
git clone https://github.com/yuosef33/Cloud-Based-Examintation-Platform.git

# Start MySQL via Docker
docker-compose up -d

# Set environment variables and run
./mvnw spring-boot:run
```

Frontend setup: [Graduation-Project-Frontend](https://github.com/yuosef33/Graduation-Project-Frontend)

---

## 📸 API Documentation

Swagger UI available at: `http://localhost:8080/swagger-ui.html`

---

## 👤 Author

**Yuosef** — Individual Graduation Project

[![LinkedIn](https://img.shields.io/badge/LinkedIn-Connect-blue)](https://linkedin.com)
[![GitHub](https://img.shields.io/badge/GitHub-Follow-black)](https://github.com/yuosef33)

---

## 📄 License

This project is licensed under the MIT License — see the [LICENSE](LICENSE) file for details.
