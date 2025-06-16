package job.scraper.controller;

import job.scraper.model.Job;
import job.scraper.service.JobScraperService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/v1/api/jobs")
@RequiredArgsConstructor
public class JobController {
    private final JobScraperService jobScraperService;

    @PostMapping("/scrape")
    public ResponseEntity<String> scrapeJobs(@RequestParam String jobFunction) {
        List<Job> jobs = jobScraperService.scrape(jobFunction);
        return ResponseEntity.ok("Scraping completed. Total jobs saved: " + jobs.size());
    }
}
