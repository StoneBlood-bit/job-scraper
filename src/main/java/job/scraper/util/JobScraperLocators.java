package job.scraper.util;

import org.openqa.selenium.By;

public class JobScraperLocators {
    public static final By COOKIE_BANNER_CLOSE_BUTTON = By
            .cssSelector("button.onetrust-close-btn-handler");
    public static final By JOB_FUNCTION_DIV_OPEN_BUTTON = By
            .xpath("//div[text()='Job function']");
    public static final By FILTER_DIV = By
            .xpath("//div[@data-testid='filter-results']");
    public static final By SCROLL_CONTAINER = By
            .cssSelector("div[data-test-id='virtuoso-scroller']");
    public static final By LOAD_MORE_BUTTON = By
            .xpath("//div[text()='Load more']");
    public static final By CONTAINER_ITEMS = By.
            cssSelector("div.infinite-scroll-component");
    public static final By ITEMS = By.cssSelector("div.job-card");
    public static final By JOB_ITEMS = By.cssSelector("div.sc-beqWaB.sc-gueYoa.diHipZ.MYFxR");
    public static final By LINK_READ_MORE = By.cssSelector("a[data-testid='read-more']");
    public static final By TAG_DIV = By.cssSelector("div[data-testid='tag']");
    public static final By ITEM_DIV_EXPECTED = By
            .xpath("//div[@class='sc-dmqHEX dxKYnR']");
    public static final By POSITION_NAME_DIV = By
            .xpath("//h2[@class='sc-beqWaB jqWDOR']");
    public static final By ORGANIZATION_URL_DIV = By
            .xpath("//a[@data-testid='button']");
    public static final By LOGO_URL_DIV = By.xpath("//img[@data-testid='image']");
    public static final By ORGANIZATION_TITLE_DIV = By.cssSelector("p.sc-beqWaB.bpXRKw");
    public static final By DESCRIPTION_DIV = By.cssSelector("div[data-testid='careerPage']");
    public static final By LABOR_FUNCTION_DIV = By.cssSelector("div.bpXRKw:nth-of-type(1)");
    public static final By LOCATION_DIV = By.cssSelector("div.bpXRKw:nth-of-type(2)");
    public static final By POSTED_DATE_DIV = By.cssSelector("div.sc-beqWaB.gRXpLa");
}
