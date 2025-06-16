package job.scraper.service;

import job.scraper.dto.JobResponseDto;
import job.scraper.exception.EntityNotFoundException;
import job.scraper.mapper.JobMapper;
import job.scraper.model.Job;
import job.scraper.repository.JobRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class JobServiceImpl implements JobService {
    private final JobRepository jobRepository;
    private final JobMapper jobMapper;

    @Override
    public Page<JobResponseDto> findAll(Pageable pageable) {
        Page<Job> jobs = jobRepository.findAll(pageable);
        return jobs.map(jobMapper::toDto);
    }

    @Override
    public JobResponseDto getById(Long id) {
        Job job = jobRepository.findById(id).orElseThrow(
                () -> new EntityNotFoundException("Can't fond a job with id: " + id)
        );
        return jobMapper.toDto(job);
    }
}
