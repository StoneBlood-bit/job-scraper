package job.scraper.service;

import job.scraper.model.Job;
import job.scraper.model.JobData;
import job.scraper.model.Tag;
import job.scraper.model.Location;
import job.scraper.repository.JobRepository;
import job.scraper.repository.TagRepository;
import java.time.Duration;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class JobScraperService {
    private static final String JOB_TECHSTARS_URL = "https://jobs.techstars.com/jobs";

    private final JobRepository jobRepository;
    private final TagRepository tagRepository;
    private WebDriver driver;
    private WebDriverWait wait;
    private JavascriptExecutor js;

    public List<Job> scrape(String jobFunction) {

        System.setProperty("webdriver.chrome.driver", "selenium\\chromedriver.exe");
        driver = new ChromeDriver();
        wait = new WebDriverWait(driver, Duration.ofSeconds(10));
        js = (JavascriptExecutor) driver;

        try {
            driver.get(JOB_TECHSTARS_URL);
            closeCookieBanner();
            selectJobFunction(jobFunction);
            loadAllJobs();

            List<JobData> jobDataList = collectJobCards();
            List<Job> jobs = new ArrayList<>();

            for (JobData jobData : jobDataList) {
                Job job = scrapeJobDetails(jobData);
                jobs.add(jobRepository.save(job));
            }

            return jobs;
        } catch (Exception e) {
            throw new RuntimeException("Scraping failed", e);
        } finally {
            driver.quit();
        }
    }

        private void closeCookieBanner() {
            try {
                WebElement closeButton = wait.until(ExpectedConditions.elementToBeClickable(
                        By.cssSelector("button.onetrust-close-btn-handler")
                ));
                closeButton.click();
            } catch (TimeoutException e) {
                System.out.println("Cookie banner not found, skipping.");
            }
        }

        private void selectJobFunction(String jobFunction) throws InterruptedException {
            WebElement jobFunctionDiv = driver.findElement(By.xpath("//div[text()='Job function']"));
            jobFunctionDiv.click();

            wait.until(ExpectedConditions.elementToBeClickable(By.xpath("//div[@data-testid='filter-results']")));
            WebElement function = driver.findElement(By.xpath("//div[@data-testid='job_functions-" + jobFunction + "']"));
            function.click();

            Thread.sleep(2000);
        }

        private void loadAllJobs() throws InterruptedException {
            WebElement container = driver.findElement(By.cssSelector("div.infinite-scroll-component"));
            int previousCount = container.findElements(By.cssSelector("div.job-card")).size();
            int attempts = 0;

            while (attempts < 10) {
                js.executeScript("window.scrollTo(0, document.body.scrollHeight - 700);");
                Thread.sleep(1500);
                int currentCount = container.findElements(By.cssSelector("div.job-card")).size();

                if (currentCount > previousCount) {
                    previousCount = currentCount;
                    attempts = 0;
                } else {
                    attempts++;
                }
            }
        }

        private List<JobData> collectJobCards() {
            List<JobData> data = new ArrayList<>();
            List<WebElement> jobItems = driver.findElements(By.cssSelector("div.sc-beqWaB.sc-gueYoa.diHipZ.MYFxR"));

            for (WebElement item : jobItems) {
                List<WebElement> links = item.findElements(By.cssSelector("a[data-testid='read-more']"));
                if (links.isEmpty()) continue;

                String url = links.get(0).getAttribute("href");
                if (url == null || !url.contains("jobs.techstars.com")) continue;

                List<String> tags = item.findElements(By.cssSelector("div[data-testid='tag']")).stream()
                        .map(WebElement::getText)
                        .map(String::trim)
                        .filter(t -> !t.isEmpty())
                        .collect(Collectors.toList());

                data.add(new JobData(url, tags));
            }

            return data;
        }

        private Job scrapeJobDetails(JobData jobData) {
            try {
                driver.get(jobData.getUrl());
                wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//div[@class='sc-dmqHEX dxKYnR']")));

                Job job = new Job();
                job.setJobPageUrl(jobData.getUrl());
                job.setTags(processTags(jobData.getTags()));
                job.setPositionName(driver.findElement(By.xpath("//h2[@class='sc-beqWaB jqWDOR']")).getText());
                job.setOrganizationUrl(driver.findElement(By.xpath("//a[@data-testid='button']")).getAttribute("href"));
                job.setLogoUrl(driver.findElement(By.xpath("//img[@data-testid='image']")).getAttribute("src"));
                job.setOrganizationTitle(driver.findElement(By.cssSelector("p.sc-beqWaB.bpXRKw")).getText());
                job.setLaborFunction(extractLaborFunction());
                job.setLocations(extractLocations(job));
                job.setPostedDateUnix(extractPostedDateUnix());
                job.setDescriptionHtml(driver.findElement(By.cssSelector("div[data-testid='careerPage']")).getAttribute("innerHTML"));

                return job;
            } catch (Exception e) {
                System.err.println("Error scraping: " + jobData.getUrl());
                throw new RuntimeException(e);
            }
        }

        private List<Tag> processTags(List<String> tagNames) {
            return tagNames.stream()
                    .map(name -> tagRepository.findByName(name).orElseGet(() -> new Tag(name)))
                    .collect(Collectors.toList());
        }

        private String extractLaborFunction() {
            List<WebElement> elements = driver.findElements(By.cssSelector("div.bpXRKw:nth-of-type(1)"));
            return elements.isEmpty() ? "" : elements.get(0).getText().trim();
        }

        private List<Location> extractLocations(Job job) {
            List<WebElement> elements = driver.findElements(By.cssSelector("div.bpXRKw:nth-of-type(2)"));
            if (elements.isEmpty()) return List.of();

            String[] parts = elements.get(0).getText().trim().split(",\\s*");
            return Arrays.stream(parts)
                    .map(part -> new Location(part, job))
                    .collect(Collectors.toList());
        }

        private long extractPostedDateUnix() {
            String raw = driver.findElement(By.cssSelector("div.sc-beqWaB.gRXpLa")).getText().trim();
            if (!raw.startsWith("Posted on ")) return 0;

            String strDate = raw.replace("Posted on ", "");
            try {
                LocalDate date = LocalDate.parse(strDate, DateTimeFormatter.ofPattern("MMM d, yyyy", Locale.ENGLISH));
                return date.atStartOfDay(ZoneId.of("UTC")).toEpochSecond();
            } catch (DateTimeParseException e) {
                return 0;
            }
        }

}
