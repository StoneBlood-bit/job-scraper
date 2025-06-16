package job.scraper.service;

import job.scraper.model.Job;
import java.util.List;

public interface JobService {
    void saveAll(List<Job> jobs);
}
