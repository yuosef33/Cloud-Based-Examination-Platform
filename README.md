# ☁️ Cloud-Based Lab Examination Platform

> **Individual Graduation Project** — A full-stack cloud platform that automates the entire lifecycle of practical lab examinations, from real-time VM provisioning to automated file collection, built with Spring Boot, React, and AWS.

🔗 **Frontend Repository:** [Graduation-Project-Frontend](https://github.com/yuosef33/Graduation-Project-Frontend)

---

## 📌 Overview

Traditional practical lab exams are constrained by physical infrastructure — they require dedicated lab machines, manual environment setup before every session, on-site supervision, and a tedious process of collecting student work at the end. Scaling this to hundreds of students is a logistical nightmare.

This platform solves all of that by moving the entire lab exam experience to the cloud. Instructors create reusable lab environments by configuring a base VM — installing any required software, tools, or IDEs — and saving it as an AWS AMI template. They then schedule lab sessions with a start time, duration, and the environment template. That's it.

On the student side, the experience is seamless. When a student clicks **"Attend Exam"**, the backend automatically provisions a **dedicated AWS EC2 instance** just for them — spun up from the instructor's template in seconds. The student interacts with a full Windows or Linux desktop directly in their browser via VNC over WebSockets, with no software to install and no setup required. A countdown timer tracks the remaining time, and the exam finishes automatically when time runs out.

When the exam ends, all student VMs are stopped immediately — freezing their work at that exact moment. The instructor then clicks **"Collect Files"** from the admin dashboard. The platform restarts all VMs in parallel, uses **AWS SSM** to run PowerShell scripts on Windows machines and **Ansible playbooks** on Linux machines, uploads every student's submission to an organized **AWS S3** bucket, then terminates all VMs automatically. The instructor can browse and download any student's files directly from the dashboard.

The entire platform — from VM provisioning to file collection — runs on AWS and is managed through a clean, role-based web interface built with React.

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

## 🧰 Built With

![Java](https://img.shields.io/badge/Java_21-ED8B00?style=for-the-badge&logo=openjdk&logoColor=white)
![Spring Boot](https://img.shields.io/badge/Spring_Boot-6DB33F?style=for-the-badge&logo=spring-boot&logoColor=white)
![AWS](https://img.shields.io/badge/AWS-232F3E?style=for-the-badge&logo=amazon-aws&logoColor=white)
![Terraform](https://img.shields.io/badge/Terraform-7B42BC?style=for-the-badge&logo=terraform&logoColor=white)
![Ansible](https://img.shields.io/badge/Ansible-EE0000?style=for-the-badge&logo=ansible&logoColor=white)
![React](https://img.shields.io/badge/React-20232A?style=for-the-badge&logo=react&logoColor=61DAFB)
![postgresql](https://img.shields.io/badge/postgresql-4169e1?style=for-the-badge&logo=postgresql&logoColor=white)
![Docker](https://img.shields.io/badge/Docker-2496ED?style=for-the-badge&logo=docker&logoColor=white)
![JWT](https://img.shields.io/badge/JWT-000000?style=for-the-badge&logo=JSON%20web%20tokens&logoColor=white)
![TailwindCSS](https://img.shields.io/badge/Tailwind_CSS-38B2AC?style=for-the-badge&logo=tailwind-css&logoColor=white)

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
