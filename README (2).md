# 👻 Ghost Share  
**Secure. Temporary. Invisible File Sharing.**

Ghost Share is a modern, secure, and time-bound file-sharing **backend system** built with **Spring Boot**.  
It enables users to upload files, generate temporary access links, and automatically removes expired files — ensuring **privacy, security, and zero digital footprints**.

---

## ✨ Why Ghost Share?

In today’s world, file sharing often means:

- Files living forever on servers  
- No control over expiration  
- Security risks & unauthorized access  

**Ghost Share solves this by design.**

✔ Files auto-expire  
✔ Secure access links  
✔ Multi-file & single-file support  
✔ Clean architecture  
✔ Built for scalability  

> **Upload → Share → Expire → Disappear.**

---

## 🚀 Key Features

### 🔐 Secure File Sharing
- Upload files securely via REST APIs  
- Generate unique access links for downloads  

### ⏳ Time-Bound Expiration
- Files automatically expire after a defined time  
- Background cleanup ensures no stale data remains  

### 📁 Multiple File Support
- Upload single or multiple files  
- Download files individually or as a group  

### 🧹 Automated Cleanup Service
- Scheduled cleanup job removes expired files from:
  - Database  
  - Storage  
- Keeps the system lightweight and efficient  

### ⚙ Production-Ready Backend
- Layered architecture (Controller → Service → Repository)  
- Environment-based configuration  
- Clean separation of concerns  

---

## 🏗 Tech Stack

| Layer        | Technology |
|--------------|------------|
| Language     | Java 21 |
| Framework    | Spring Boot |
| Build Tool   | Maven |
| Database     | PostgreSQL |
| ORM          | Spring Data JPA |
| Scheduling   | Spring Scheduler |
| API Style    | REST |
| Configuration| `.env` based |

---

## 📂 Project Structure

```
ghost-share/
├── controller/
│   ├── StartController
│   ├── ShareOneFileController
│   └── ShareMultipleFilesController
│
├── service/
│   ├── ShareOneFileService
│   ├── ShareMultipleFilesService
│   └── cleanupService/
│       └── ShareMultipleFilesCleanupService
│
├── serviceImplementation/
│   ├── ShareOneFileServiceImplementation
│   └── ShareMultipleFilesServiceImplementation
│
├── model/
│   └── ShareMultipleFiles
│
├── repository/
│   └── JPA Repositories
│
├── resources/
│   ├── application.properties
│   └── .env
│
└── GhostShareApplication.java
```

---

## 🔄 How It Works

1. User uploads file(s)  
2. Backend stores file metadata and content  
3. A secure access link is generated  
4. User downloads within the valid time window  
5. Cleanup service deletes expired files automatically  

---

## ⚙ Setup & Installation

### Prerequisites
- Java 21+  
- Maven  
- PostgreSQL  

### Clone the Repository
```bash
git clone https://github.com/your-username/ghost-share.git
cd ghost-share
```

### Configure Environment

Create a `.env` file:

```env
DB_URL=jdbc:postgresql://localhost:5432/ghostshare
DB_USERNAME=postgres
DB_PASSWORD=yourpassword
```

### Run the Application
```bash
mvn spring-boot:run
```

The backend will start on:

```
http://localhost:8080
```

---

## 📡 API Overview

### Upload Single File
```
POST /share/file
```

### Upload Multiple Files
```
POST /share/files
```

### Download File
```
GET /download/{fileId}
```

### Automatic Cleanup
- Runs in the background  
- Deletes expired files without manual intervention  

---

## 🔐 Security Considerations

- No public directory exposure  
- Temporary access via generated identifiers  
- Expired files are irreversibly removed  
- Controlled error handling to prevent data leaks  

---

## 🌱 Future Enhancements

- 🔑 Password-protected links  
- 📦 ZIP download for multiple files  
- 📊 Admin dashboard  
- 🌍 Frontend integration (React / Next.js)  
- ☁ Cloud storage (AWS S3 / GCP)  

---

## 👨‍💻 Author

**Ashish Kumar**  
Backend Developer | Java | Spring Boot  

Building systems that value **privacy, performance, and simplicity**.

---

## ⭐ Support

If you found this project useful:

- ⭐ Star the repository  
- 🧠 Share feedback  
- 🚀 Contribute ideas  

---

## 👻 Ghost Share

**Because files shouldn’t live longer than they’re needed.**
