# Techstars Job Scraper

A Java-based web scraper that collects job listings from [jobs.techstars.com](https://jobs.techstars.com) using Selenium, processes job data, and saves it into a relational SQL database.

## ✨ Features

- Scrapes job postings filtered by job function
- Extracts job metadata: title, organization, logo, tags, location, posted date, description, etc.
- Saves data into a SQL database using Spring Data JPA
- Includes REST API to fetch jobs and job details
- DTO mapping with MapStruct

## 🚀 Technologies

- Java 17
- Spring Boot
- Spring Data JPA
- Selenium WebDriver
- MapStruct
- MySQL or PostgreSQL (or other SQL DB)
- Maven

## 📦 REST API Endpoints
### 🔎 `POST /api/jobs/scrape`

Scrapes jobs by the specified job function.

**Parameters:**

| Name         | Type    | Description                           |
|--------------|---------|-------------------------------------|
| `jobFunction`| query   | The job function name (e.g., "Software Engineering") |

**Example request:**
`POST /api/jobs/scrape?jobFunction=Software Engineering`
**Response:**
`HTTP 200 OK
Scraping completed. Total jobs saved: 42`
---

### 📋 `GET /api/jobs`

Retrieves a paginated list of jobs as DTOs.

**Query Parameters:**

- `page` — page number (default: 0)
- `size` — page size (default: 10)

**Example request:**
`GET /api/jobs?page=0&size=10`

**Response:**

```json
{
  "content": [
    {
      "id": 1,
      "positionName": "Software Engineer",
      "organizationTitle": "Techstars",
      "organizationUrl": "https://techstars.com",
      "jobPageUrl": "...",
      ...
    },
    ...
  ],
  "pageable": { ... },
  "totalPages": 5,
  "totalElements": 50,
  ...
}
```
---

### 📋 `GET /api/jobs/id`
**Example request:**
`Get api/jobs/1`
**Response:**
```json
{
      "id": 1,
      "positionName": "Software Engineer",
      "organizationTitle": "Techstars",
      "organizationUrl": "https://techstars.com",
      "jobPageUrl": "...",
      ...
    }
```


## ⚙️ How It Works

1. Starts Selenium WebDriver
2. Navigates to Techstars job board
3. Selects a job function
4. Loads all jobs
5. Visits each job page to extract details
6. Saves to database