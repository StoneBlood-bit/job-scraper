# Installation Guide for Job Scraper Service

## Prerequisites

- Java 17 or higher installed
- Maven installed
- Chrome browser installed
- ChromeDriver executable matching your Chrome version
- Database (e.g., MySQL or PostgreSQL) configured and accessible
- Liquibase installed or integrated with Maven

## Setup Steps

1. **Clone the repository:**

```bash
git clone <your-repo-url>
cd <your-project-folder>
```
2. **Configure database connection:**

   Edit `src/main/resources/application.properties` to set your database URL, username, and password.

3.  **Place ChromeDriver executable:**

   Download ChromeDriver from https://chromedriver.chromium.org/downloads
   Place the executable in the project folder or configure path accordingly.
   Example path in code: `"selenium/chromedriver.exe"`
4. **Build the project:**
`mvn clean install`
5. **Run database migrations:**
`mvn liquibase:update`
6. **Run the application:**
`mvn spring-boot:run`
7. **Use the API:**
   - To scrape jobs, send a POST request to /api/jobs/scrape?jobFunction=Engineering
   - To list jobs, use GET /api/jobs
   - To job by id, use GET /api/jobs/id

*Enjoy scraping! 🚀*