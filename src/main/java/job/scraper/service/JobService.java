package job.scraper.service;

import job.scraper.dto.JobResponseDto;
import job.scraper.model.Job;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface JobService {

    Page<JobResponseDto> findAll(Pageable pageable);

    JobResponseDto getById(Long id);
}
