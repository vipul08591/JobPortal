# 🐝 JobHive

[![Java](https://img.shields.io/badge/Java-ED8B00?logo=java&logoColor=white)](https://www.java.com/) 
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-6DB33F?logo=spring&logoColor=white)](https://spring.io/projects/spring-boot) 
[![MySQL](https://img.shields.io/badge/MySQL-4479A1?logo=mysql&logoColor=white)](https://www.mysql.com/) 
[![Maven](https://img.shields.io/badge/Maven-C71A36?logo=apachemaven&logoColor=white)](https://maven.apache.org/) 
[![Thymeleaf](https://img.shields.io/badge/Thymeleaf-005F0F?logo=thymeleaf&logoColor=white)](https://www.thymeleaf.org/)  
[![Tailwind CSS](https://img.shields.io/badge/Tailwind%20CSS-06B6D4?logo=tailwind-css&logoColor=white)](https://tailwindcss.com/)  

**JobHive** is a modern job portal built with **Java, Spring Boot, Hibernate, Thymeleaf, MySQL, Tailwind CSS, HTML & JavaScript**.  
It allows users to register as **Candidates** or **Employers**, providing personalized dashboards to manage jobs, applications, and profiles.  

---

## ✨ Features

### 👨‍🎓 Candidate
- Register and log in as a candidate  
- View all available job listings  
- Apply for jobs with a resume  
- Track application status  
- Manage profile:
  - Languages  
  - Skills  
  - Experience  
  - Education  
  - Profile picture  

### 💼 Employer
- Register and log in as an employer  
- Create and manage job postings  
- View applications with resumes  
- Update application statuses  
- Delete job postings  

---

## 🛠 Tech Stack

| Frontend | Backend | Database | Build Tool |
|----------|--------|---------|------------|
| Thymeleaf, HTML, JavaScript, Tailwind CSS | Java, Spring Boot, Hibernate | MySQL | Maven |

---

## ⚙️ Installation & Setup

1. **Clone the repository**
```bash
git clone <repository-url>
cd JobHive

2.Setup Database

-Create a MySQL database (e.g., jobhive_db)

Update application.properties:

spring.datasource.url=jdbc:mysql://localhost:3306/jobhive_db
spring.datasource.username=<your-db-username>
spring.datasource.password=<your-db-password>
spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=false
spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.MySQLDialect


Build & Run

mvn clean install
mvn spring-boot:run


Access

Open: http://localhost:8080


🌟 Future Enhancements

Email notifications for application updates

Resume parsing to auto-fill candidate profiles

📄 License

This project is licensed under the MIT License.
