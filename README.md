# CMS + SEO Management System

A complete local CMS and SEO management system built with Java Spring Boot + MySQL.

## Quick Start

### 1. Prerequisites
- Java 17+
- Maven 3.8+
- MySQL 8.0+

### 2. Database Setup
```sql
CREATE DATABASE cms_seo_db;
```
Or run `src/main/resources/schema.sql` to create tables and seed default data.

### 3. Configure Database
Edit `src/main/resources/application.properties`:
```properties
spring.datasource.username=root
spring.datasource.password=your_password
```

### 4. Run the Application
```bash
mvn spring-boot:run
```

### 5. Access
| URL | Purpose |
|-----|---------|
| http://localhost:8080 | Website |
| http://localhost:8080/admin | Admin Panel |

**Default credentials:** `admin` / `admin123`

---

## Features

### Admin Panel (`/admin`)
- **Dashboard** — stats overview + quick actions
- **SEO Management** — per-page SEO title, meta description, keywords, Open Graph, robots, canonical URL
- **Content Management** — heading, subheading, description, button text/link, image selector
- **Banner Management** — banner title, subtitle, image, CTA button, display order
- **Image Management** — upload/replace/delete images, grid & list views, drag & drop upload, copy URL

### REST API Endpoints
| Method | URL | Description |
|--------|-----|-------------|
| GET | `/content/{page}` | Get all active content for a page |
| POST | `/content` | Create content |
| PUT | `/content/{id}` | Update content |
| DELETE | `/content/{id}` | Delete content |
| GET | `/seo/{page}` | Get SEO settings for a page |
| POST | `/seo` | Create SEO settings |
| PUT | `/seo/{id}` | Update SEO settings |
| DELETE | `/seo/{id}` | Delete SEO settings |
| GET | `/images` | List all active images |
| POST | `/upload` | Upload an image |
| DELETE | `/image/{id}` | Delete an image |

### Database Tables
- `admin_users` — admin login credentials
- `seo_settings` — per-page SEO data
- `content_settings` — per-page/section content blocks
- `image_settings` — uploaded image metadata
- `banner_settings` — banner configuration

## How Content Updates Work
1. Admin visits `http://localhost:8080/admin`
2. Updates content (e.g. sets heading to "Best Software Company")
3. Saves → data stored in MySQL immediately
4. Website at `http://localhost:8080` fetches fresh data from DB on every request
5. Changes are reflected instantly — no code changes, no restarts needed

## Project Structure
```
src/
├── main/
│   ├── java/com/cms/
│   │   ├── CmsApplication.java
│   │   ├── config/          # Security, WebConfig, DataInitializer
│   │   ├── controller/      # Admin, Website, Image, REST API controllers
│   │   ├── entity/          # JPA entities
│   │   ├── repository/      # Spring Data repositories
│   │   └── service/         # Business logic services
│   └── resources/
│       ├── application.properties
│       ├── schema.sql
│       ├── static/
│       │   ├── css/         # admin.css, website.css
│       │   └── js/          # admin.js, website.js
│       └── templates/
│           ├── admin/       # Admin Thymeleaf templates
│           └── website/     # Public website templates
└── uploads/                 # Uploaded images stored here
```
