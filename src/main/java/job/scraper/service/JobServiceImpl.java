package job.scraper.service;

import job.scraper.model.Job;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class JobServiceImpl implements JobService {
    @Override
    public void saveAll(List<Job> jobs) {

    }
}
