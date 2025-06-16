package job.scraper.controller;

import job.scraper.dto.JobResponseDto;
import job.scraper.model.Job;
import job.scraper.service.JobScraperService;
import job.scraper.service.JobService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/v1/api/jobs")
@RequiredArgsConstructor
public class JobController {
    private final JobScraperService jobScraperService;
    private final JobService jobService;

    @PostMapping("/scrape")
    public ResponseEntity<String> scrapeJobs(@RequestParam String jobFunction) {
        List<Job> jobs = jobScraperService.scrape(jobFunction);
        return ResponseEntity.ok("Scraping completed. Total jobs saved: " + jobs.size());
    }

    @GetMapping
    public Page<JobResponseDto> getAllJobs(
            @PageableDefault(size = 10, sort = "postedDateUnix", direction = Sort.Direction.DESC) Pageable pageable) {
        return jobService.findAll(pageable);
    }

    @GetMapping("/{id}")
    public ResponseEntity<JobResponseDto> getJobById(@PathVariable Long id) {
        JobResponseDto job = jobService.getById(id);
        return ResponseEntity.ok(job);
    }
}
