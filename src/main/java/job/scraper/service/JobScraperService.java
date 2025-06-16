package job.scraper.service;

import job.scraper.model.Job;
import job.scraper.model.JobData;
import job.scraper.model.Tag;
import job.scraper.model.Location;
import job.scraper.repository.JobRepository;
import job.scraper.repository.TagRepository;
import job.scraper.util.JobScraperLocators;
import job.scraper.util.JsScripts;
import java.time.Duration;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class JobScraperService {
    private static final String JOB_TECHSTARS_URL = "https://jobs.techstars.com/jobs";
    private static final String DOMEN = "jobs.techstars.com";
    private static final String HREF_ATTRIBUTE = "href";
    private static final String SRC_ATTRIBUTE ="src";
    private static final String INNER_HTML_ATTRIBUTE = "innerHTML";
    private static final String CHROME_DRIVER_PATH = "selenium\\chromedriver.exe";

    private final JobRepository jobRepository;
    private final TagRepository tagRepository;
    private WebDriver driver;
    private WebDriverWait wait;
    private JavascriptExecutor js;

    public List<Job> scrape(String jobFunction) {
        setupWebDriver();

        try {
            openMainPage();
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
            if (driver != null) {
                driver.quit();
            }
        }
    }

    private void setupWebDriver() {
        System.setProperty("webdriver.chrome.driver", CHROME_DRIVER_PATH);
        driver = new ChromeDriver();
        wait = new WebDriverWait(driver, Duration.ofSeconds(10));
        js = (JavascriptExecutor) driver;
        driver.manage().window().maximize();
    }

    private void openMainPage() {
        driver.get(JOB_TECHSTARS_URL);
    }

    private void closeCookieBanner() {
        try {
            WebElement closeButton = wait.until(ExpectedConditions.elementToBeClickable(
                    JobScraperLocators.COOKIE_BANNER_CLOSE_BUTTON
            ));
            closeButton.click();
        } catch (TimeoutException e) {
            System.out.println("Cookie banner not found, skipping.");
        }
    }

    private void selectJobFunction(String jobFunction) throws InterruptedException {
        WebElement jobFunctionDiv = wait.until(ExpectedConditions.elementToBeClickable(
                JobScraperLocators.JOB_FUNCTION_DIV_OPEN_BUTTON
        ));
        jobFunctionDiv.click();

        wait.until(ExpectedConditions.elementToBeClickable(
                JobScraperLocators.FILTER_DIV
        ));

        WebElement scrollContainer = driver.findElement(
                JobScraperLocators.SCROLL_CONTAINER
        );

        String encodedText = jobFunction.replace(" ", "%2520");
        By targetLocator = By.cssSelector("div[data-testid='job_functions-" + encodedText + "']");

        int maxScrolls = 10;
        boolean found = false;

        for (int i = 0; i < maxScrolls; i++) {
            try {
                WebElement el = driver.findElement(targetLocator);
                js.executeScript(JsScripts.SCRIPT_SCROLL_INTO_VIEW, el);
                el.click();
                found = true;
                break;
            } catch (NoSuchElementException e) {
                js.executeScript(JsScripts.SCRIPT_SCROLL_TOP, scrollContainer);
                Thread.sleep(500);
            }
        }

        if (!found) {
            throw new RuntimeException("Element '" + jobFunction + "' not found after scrolling");
        }

        Thread.sleep(2000);
    }

    private void loadAllJobs() throws InterruptedException {
        WebElement loadMoreDev = driver.findElement(JobScraperLocators.LOAD_MORE_BUTTON);

        if (loadMoreDev.isDisplayed() && loadMoreDev.isEnabled()) {
            js.executeScript(JsScripts.SCRIPT_SCROLL_INTO_VIEW, loadMoreDev);
            js.executeScript(JsScripts.SCRIPT_CLICK, loadMoreDev);

            Thread.sleep(3000);
        }

        WebElement container = driver.findElement(JobScraperLocators.CONTAINER_ITEMS);
        int previousCount = container.findElements(JobScraperLocators.ITEMS).size();
        int attempts = 0;

        while (attempts < 10) {
            js.executeScript(JsScripts.SCRIPT_SCROLL_WINDOW);
            Thread.sleep(1500);
            int currentCount = container.findElements(JobScraperLocators.ITEMS).size();

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
        List<WebElement> jobItems = driver.findElements(JobScraperLocators.JOB_ITEMS);

        for (WebElement item : jobItems) {
            List<WebElement> links = item.findElements(JobScraperLocators.LINK_READ_MORE);
            if (links.isEmpty()) continue;

            String url = links.get(0).getAttribute(HREF_ATTRIBUTE);
            if (url == null || !url.contains(DOMEN)) continue;

            List<String> tags = item.findElements(JobScraperLocators.TAG_DIV).stream()
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
            wait.until(ExpectedConditions.visibilityOfElementLocated(
                    JobScraperLocators.ITEM_DIV_EXPECTED
            ));

            Job job = new Job();
            job.setJobPageUrl(jobData.getUrl());
            job.setTags(processTags(jobData.getTags()));

            Optional<String> positionName = driver.findElements(
                    JobScraperLocators.POSITION_NAME_DIV
                    )
                    .stream()
                    .findFirst()
                    .map(WebElement::getText);
            job.setPositionName(positionName.orElse(""));


            Optional<String> url = driver.findElements(
                    JobScraperLocators.ORGANIZATION_URL_DIV
                    )
                    .stream()
                    .findFirst()
                    .map(e -> e.getAttribute(HREF_ATTRIBUTE));
            job.setOrganizationUrl(url.orElse(""));

            Optional<String> logoUrl = driver.findElements(
                    JobScraperLocators.LOGO_URL_DIV
                    )
                    .stream()
                    .findFirst()
                    .map(e -> e.getAttribute(SRC_ATTRIBUTE));
            job.setLogoUrl(logoUrl.orElse(""));

            Optional<String> organizationTitle = driver.findElements(
                    JobScraperLocators.ORGANIZATION_TITLE_DIV
                    )
                    .stream()
                    .findFirst()
                    .map(WebElement::getText);
            job.setOrganizationTitle(organizationTitle.orElse(""));

            job.setLaborFunction(extractLaborFunction());
            job.setLocations(extractLocations(job));
            job.setPostedDateUnix(extractPostedDateUnix());

            Optional<String> descriptionHtml = driver.findElements(
                    JobScraperLocators.DESCRIPTION_DIV
                    )
                    .stream()
                    .findFirst()
                    .map(e -> e.getAttribute(INNER_HTML_ATTRIBUTE));
            job.setDescriptionHtml(descriptionHtml.orElse(""));


            return job;
        } catch (Exception e) {
            System.err.println("Error scraping: " + jobData.getUrl());
            throw new RuntimeException(e);
        }
    }

    private List<Tag> processTags(List<String> tagNames) {
        List<Tag> result = new ArrayList<>();
        for (String name : tagNames) {
            Optional<Tag> existingTag = tagRepository.findByName(name);
            if (existingTag.isPresent()) {
                result.add(existingTag.get());
            } else {
                Tag newTag = new Tag(name);
                Tag savedTag = tagRepository.save(newTag);
                result.add(savedTag);
            }
        }
        return result;
    }

    private String extractLaborFunction() {
        List<WebElement> elements = driver.findElements(
                JobScraperLocators.LABOR_FUNCTION_DIV
        );
        return elements.isEmpty() ? "" : elements.get(0).getText().trim();
    }

    private List<Location> extractLocations(Job job) {
        List<WebElement> elements = driver.findElements(
                JobScraperLocators.LOCATION_DIV
        );
        if (elements.isEmpty()) return List.of();

        String[] parts = elements.get(0).getText().trim().split(",\\s*");
        return Arrays.stream(parts)
                .map(part -> new Location(part, job))
                .collect(Collectors.toList());
    }

    private long extractPostedDateUnix() {
        String raw = driver.findElement(
                JobScraperLocators.POSTED_DATE_DIV
        ).getText().trim();
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
